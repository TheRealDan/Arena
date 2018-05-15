package me.therealdan.galacticwarfront.mechanics.lobby;

import me.therealdan.galacticwarfront.GalacticWarFront;
import me.therealdan.galacticwarfront.mechanics.battle.Arena;
import me.therealdan.galacticwarfront.mechanics.battle.battle.Duel;
import me.therealdan.galacticwarfront.mechanics.battle.battle.FFA;
import me.therealdan.galacticwarfront.mechanics.battle.battle.TeamBattle;
import me.therealdan.galacticwarfront.mechanics.party.Party;
import me.therealdan.galacticwarfront.util.Icon;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class BattleCreator implements Listener {

    private HashSet<UUID> arenaPickerUIOpen = new HashSet<>();
    private HashSet<UUID> battlePickerUIOpen = new HashSet<>();

    private HashMap<UUID, Arena> arenas = new HashMap<>();

    private ItemStack duelIcon, ffaIcon, teamBattleIcon;

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (event.getCurrentItem() == null) return;

        if (hasArenaPickerUIOpen(player)) {
            for (Arena arena : Arena.available()) {
                if (getArenaIcon(arena).isSimilar(event.getCurrentItem())) {
                    this.arenas.put(player.getUniqueId(), arena);
                    openBattlePicker(player);
                    return;
                }
            }
        } else if (hasBattlePickerUIOpen(player)) {
            if (getDuelIcon().isSimilar(event.getCurrentItem())) {
                Arena arena = arenas.get(player.getUniqueId());
                if (arena.inUse()) {
                    player.closeInventory();
                    return;
                }
                Party party = Party.get(player);
                if (party == null) {
                    player.closeInventory();
                    return;
                }
                Player target = null;
                for (Player each : party.getPlayers()) {
                    if (each != player) {
                        target = each;
                        break;
                    }
                }
                if (target == null) {
                    player.closeInventory();
                    return;
                }
                new Duel(arena, player, target);
            } else if (getFFAIcon().isSimilar(event.getCurrentItem())) {
                Arena arena = arenas.get(player.getUniqueId());
                if (arena.inUse()) {
                    player.closeInventory();
                    return;
                }
                FFA ffa = new FFA(arena, player);
                Party party = Party.get(player);
                if (party != null) {
                    for (Player each : party.getPlayers())
                        ffa.add(each);
                } else {
                    ffa.add(player);
                }
            } else if (getTeamBattleICon().isSimilar(event.getCurrentItem())) {
                Arena arena = arenas.get(player.getUniqueId());
                if (arena.inUse()) {
                    player.closeInventory();
                    return;
                }
                TeamBattle teamBattle = new TeamBattle(arena, player);
                Party party = Party.get(player);
                if (party != null) {
                    for (Player each : party.getPlayers())
                        teamBattle.add(each, party.isTeam1(each));
                } else {
                    teamBattle.add(player);
                }
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        arenaPickerUIOpen.remove(player.getUniqueId());
        battlePickerUIOpen.remove(player.getUniqueId());
    }

    public void openArenaPicker(Player player) {
        int size = 9;
        while (size < Arena.available().size()) size += 9;
        if (size > 54) size = 54;
        Inventory inventory = Bukkit.createInventory(null, size, "Pick an Arena");

        for (Arena arena : Arena.available())
            inventory.addItem(getArenaIcon(arena));

        player.openInventory(inventory);
        arenaPickerUIOpen.add(player.getUniqueId());
    }

    public void openBattlePicker(Player player) {
        Arena arena = arenas.get(player.getUniqueId());

        int size = 9;
        while (size < Arena.available().size()) size += 9;
        if (size > 54) size = 54;
        Inventory inventory = Bukkit.createInventory(null, size, "Pick a Battle Type");

        if (arena.getTeam1Spawnpoints().size() > 0 && arena.getTeam2Spawnpoints().size() > 0) inventory.setItem(3, getDuelIcon());
        if (arena.getSpawnpoints().size() > 0) inventory.setItem(4, getFFAIcon());
        if (arena.getTeam1Spawnpoints().size() > 0 && arena.getTeam2Spawnpoints().size() > 0) inventory.setItem(5, getTeamBattleICon());

        player.openInventory(inventory);
        battlePickerUIOpen.add(player.getUniqueId());
    }

    public boolean hasArenaPickerUIOpen(Player player) {
        return arenaPickerUIOpen.contains(player.getUniqueId());
    }

    public boolean hasBattlePickerUIOpen(Player player) {
        return battlePickerUIOpen.contains(player.getUniqueId());
    }

    private ItemStack getDuelIcon() {
        if (duelIcon == null)
            duelIcon = Icon.build(GalacticWarFront.getInstance().getConfig(), "Battle_Picker.Duel.Icon", false);
        return duelIcon;
    }

    private ItemStack getFFAIcon() {
        if (ffaIcon == null)
            ffaIcon = Icon.build(GalacticWarFront.getInstance().getConfig(), "Battle_Picker.FFA.Icon", false);
        return ffaIcon;
    }

    private ItemStack getTeamBattleICon() {
        if (teamBattleIcon == null)
            teamBattleIcon = Icon.build(GalacticWarFront.getInstance().getConfig(), "Battle_Picker.TeamBattle.Icon", false);
        return teamBattleIcon;
    }

    private ItemStack getArenaIcon(Arena arena) {
        ItemStack icon = new ItemStack(arena.getMaterial());
        ItemMeta itemMeta = icon.getItemMeta();
        itemMeta.setDisplayName(arena.getName());
        icon.setItemMeta(itemMeta);
        return icon;
    }
}