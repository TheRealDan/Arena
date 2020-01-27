package me.therealdan.battlearena.mechanics.arena.editors;

import me.therealdan.battlearena.BattleArena;
import me.therealdan.battlearena.mechanics.arena.Arena;
import me.therealdan.battlearena.util.Icon;
import me.therealdan.battlearena.util.WXYZ;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class LocationsEditor implements Listener {

    private static LocationsEditor locationsEditor;

    private HashMap<UUID, Arena> editing = new HashMap<>();
    private boolean alternate = true;

    private HashMap<UUID, HashMap<Integer, ItemStack>> inventory = new HashMap<>();
    private HashMap<UUID, HashMap<Integer, ItemStack>> armor = new HashMap<>();

    private LocationsEditor() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(BattleArena.getInstance(), this::tick, 100, 20);
    }

    private void tick() {
        for (UUID uuid : editing.keySet()) {
            if (alternate) {
                show(Bukkit.getPlayer(uuid), editing.get(uuid));
            } else {
                hide(Bukkit.getPlayer(uuid), editing.get(uuid));
            }
        }
        alternate = !alternate;
    }

    private void show(Player player, Arena arena) {
        for (int group = 1; group <= 4; group++) {
            for (Location location : arena.getLocations(group)) {
                player.sendBlockChange(location, getMaterialForGroup(group).createBlockData());
            }
        }
    }

    private void hide(Player player, Arena arena) {
        for (int group = 1; group <= 4; group++) {
            for (Location location : arena.getLocations(group)) {
                player.sendBlockChange(location, location.getBlock().getType(), location.getBlock().getData());
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!isEditing(player)) return;
        stopEditing(player);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!isEditing(player)) return;
        event.setCancelled(true);

        int group = getGroupFromMaterial(player.getEquipment().getItemInMainHand().getType());

        if (group < 1 || group > 9) {
            player.sendMessage(BattleArena.MAIN + "Invalid block.");
            return;
        }

        Location location = event.getBlock().getLocation();
        Arena arena = getEditing(player);
        arena.addLocation(group, new WXYZ(location));

        player.sendMessage(BattleArena.MAIN + "Added a location to group " + BattleArena.SECOND + group + BattleArena.MAIN + " (" + BattleArena.SECOND + arena.getLocations(group).size() + BattleArena.MAIN + " total)");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!isEditing(player)) return;
        event.setCancelled(true);

        Location location = event.getBlock().getLocation();
        Arena arena = getEditing(player);

        for (int group = 1; group <= 9; group++) {
            for (Location each : arena.getLocations(group)) {
                if (location.equals(each)) {
                    arena.removeLocation(group, new WXYZ(location));
                    player.sendMessage(BattleArena.MAIN + "Removed a location from group " + BattleArena.SECOND + group + BattleArena.MAIN + " (" + BattleArena.SECOND + arena.getLocations(group).size() + BattleArena.MAIN + " remain)");
                }
            }
        }
    }

    public void edit(Player player, Arena arena) {
        stopEditing(player);
        editing.put(player.getUniqueId(), arena);
        saveItems(player);
        clearInventory(player);
        loadSpecialBlocks(player);
        player.sendMessage(BattleArena.MAIN + "You are now editing locations for arena " + BattleArena.SECOND + arena.getID());
    }

    public void stopEditing(Player player) {
        if (isEditing(player)) {
            clearInventory(player);
            restoreItems(player);
            hide(player, getEditing(player));
            player.sendMessage(BattleArena.MAIN + "You are no longer editing locations for arenas");
        }
        editing.remove(player.getUniqueId());
    }

    private void saveItems(Player player) {
        HashMap<Integer, ItemStack> armor = new HashMap<>();
        if (player.getEquipment().getHelmet() != null) armor.put(0, player.getEquipment().getHelmet());
        if (player.getEquipment().getChestplate() != null) armor.put(1, player.getEquipment().getChestplate());
        if (player.getEquipment().getLeggings() != null) armor.put(2, player.getEquipment().getLeggings());
        if (player.getEquipment().getBoots() != null) armor.put(3, player.getEquipment().getBoots());
        this.armor.put(player.getUniqueId(), armor);

        HashMap<Integer, ItemStack> inventory = new HashMap<>();
        int slot = 0;
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack != null && !itemStack.getType().equals(Material.AIR))
                inventory.put(slot, itemStack);
            slot++;
        }
        this.inventory.put(player.getUniqueId(), inventory);
    }

    private void clearInventory(Player player) {
        player.getInventory().clear();
    }

    private void restoreItems(Player player) {
        HashMap<Integer, ItemStack> armor = this.armor.get(player.getUniqueId());
        if (armor.containsKey(0)) player.getEquipment().setHelmet(armor.get(0));
        if (armor.containsKey(1)) player.getEquipment().setHelmet(armor.get(1));
        if (armor.containsKey(2)) player.getEquipment().setHelmet(armor.get(2));
        if (armor.containsKey(3)) player.getEquipment().setHelmet(armor.get(3));

        HashMap<Integer, ItemStack> inventory = this.inventory.get(player.getUniqueId());
        for (int slot : inventory.keySet())
            player.getInventory().setItem(slot, inventory.get(slot));
    }

    private void loadSpecialBlocks(Player player) {
        player.getInventory().setItem(0, Icon.build(getMaterialForGroup(1), false, BattleArena.MAIN + "General Spawnpoints"));
        player.getInventory().setItem(1, Icon.build(getMaterialForGroup(2), false, BattleArena.MAIN + "Team 1 Spawnpoints"));
        player.getInventory().setItem(2, Icon.build(getMaterialForGroup(3), false, BattleArena.MAIN + "Team 2 Spawnpoints"));
        player.getInventory().setItem(3, Icon.build(getMaterialForGroup(4), false, BattleArena.MAIN + "Location 4"));
        player.getInventory().setItem(4, Icon.build(getMaterialForGroup(5), false, BattleArena.MAIN + "Location 5"));
        player.getInventory().setItem(5, Icon.build(getMaterialForGroup(6), false, BattleArena.MAIN + "Location 6"));
        player.getInventory().setItem(6, Icon.build(getMaterialForGroup(7), false, BattleArena.MAIN + "Location 7"));
        player.getInventory().setItem(7, Icon.build(getMaterialForGroup(8), false, BattleArena.MAIN + "Location 8"));
        player.getInventory().setItem(8, Icon.build(getMaterialForGroup(9), false, BattleArena.MAIN + "Location 9"));
    }

    private int getGroupFromMaterial(Material material) {
        for (int group = 1; group <= 9; group++)
            if (getMaterialForGroup(group).equals(material))
                return group;
        return -1;
    }

    private Material getMaterialForGroup(int group) {
        switch (group) {
            case 1:
                return Material.RED_STAINED_GLASS;
            case 2:
                return Material.ORANGE_STAINED_GLASS;
            case 3:
                return Material.YELLOW_STAINED_GLASS;
            case 4:
                return Material.GREEN_STAINED_GLASS;
            case 5:
                return Material.BLUE_STAINED_GLASS;
            case 6:
                return Material.PURPLE_STAINED_GLASS;
            case 7:
                return Material.MAGENTA_STAINED_GLASS;
            case 8:
                return Material.WHITE_STAINED_GLASS;
            case 9:
                return Material.BLACK_STAINED_GLASS;
        }
        return null;
    }

    public boolean isEditing(Player player) {
        return getEditing(player) != null;
    }

    public Arena getEditing(Player player) {
        return editing.getOrDefault(player.getUniqueId(), null);
    }

    public static LocationsEditor getInstance() {
        if (locationsEditor == null) locationsEditor = new LocationsEditor();
        return locationsEditor;
    }
}