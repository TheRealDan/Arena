package me.therealdan.battlearena.mechanics.lobby;

import me.therealdan.battlearena.BattleArena;
import me.therealdan.battlearena.mechanics.arena.Arena;
import me.therealdan.battlearena.mechanics.battle.Battle;
import me.therealdan.battlearena.mechanics.battle.BattleType;
import me.therealdan.battlearena.mechanics.battle.FFA;
import me.therealdan.battlearena.mechanics.battle.Team;
import me.therealdan.battlearena.mechanics.killcounter.KillCounter;
import me.therealdan.battlearena.util.Icon;
import me.therealdan.party.Party;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.SimpleDateFormat;
import java.util.*;

public class BattleCreator implements Listener {

    private static BattleCreator battleCreator;
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

    private HashSet<UUID> battleCreatorUIOpen = new HashSet<>();
    private HashSet<UUID> arenaPickerUIOpen = new HashSet<>();

    private HashMap<UUID, Arena> arena = new HashMap<>();
    private HashMap<UUID, BattleType> battleType = new HashMap<>();
    private HashMap<UUID, Long> gracePeriod = new HashMap<>();
    private HashMap<UUID, Long> battleDuration = new HashMap<>();

    private ItemStack gracePeriodIcon, battleDurationIcon, startGameIcon, noFreeArenaIcon;
    private List<Integer> team1Slots, team2Slots;

    private BattleCreator() {
    }

    @EventHandler
    public void onBattleCreatorClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!hasBattleCreatorUIOpen(player)) return;
        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;

        BattleType battleType = getBattleType(player);
        if (battleType.getIcon().isSimilar(event.getCurrentItem())) {
            toggleBattleType(player, event.isLeftClick());
            openBattleCreator(player);
            return;
        } else if (getArenaIcon(player).isSimilar(event.getCurrentItem())) {
            openArenaPicker(player);
            return;
        } else if (getBattleDurationIcon(player).isSimilar(event.getCurrentItem())) {
            toggleBattleDuration(player, event.isLeftClick(), event.isShiftClick());
            openBattleCreator(player);
            return;
        } else if (getGracePeriodIcon(player).isSimilar(event.getCurrentItem())) {
            toggleGracePeriod(player, event.isLeftClick(), event.isShiftClick());
            openBattleCreator(player);
            return;
        } else if (getStartGameIcon().isSimilar(event.getCurrentItem())) {
            if (!canStart(player)) return;
            Arena arena = getArena(player);
            if (arena.inUse()) return;
            Battle battle = null;
            Party party = Party.byPlayer(player);
            switch (battleType.getName()) {
                case "FFA":
                    battle = new FFA(arena, player, party);
                    break;
                case "Team":
                    if (party == null) return;
                    if (party.getPlayers(1).size() == 0) return;
                    if (party.getPlayers(2).size() == 0) return;
                    battle = new Team(arena, player, party);
                    break;
            }
            battle.setGracePeriod(getGracePeriod(player) / 1000);
            battle.setTimeRemaining(getBattleDuration(player) / 1000);
            battle.setOpen(true);
            player.closeInventory();
            return;
        }

        Party party = Party.byPlayer(player);
        if (party == null) return;

        for (Player each : party.getPlayers()) {
            if (getPlayerIcon(each).isSimilar(event.getCurrentItem())) {
                party.setTeam(each.getUniqueId(), party.isTeam(each, 1) ? 2 : 1);
                openBattleCreator(player);
                return;
            }
        }
    }

    @EventHandler
    public void onArenaUIClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!hasArenaPickerUIOpen(player)) return;
        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;

        for (Arena arena : Arena.values()) {
            if (getArenaIcon(arena).isSimilar(event.getCurrentItem())) {
                setArena(player, arena);
                openBattleCreator(player);
                return;
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        battleCreatorUIOpen.remove(player.getUniqueId());
        arenaPickerUIOpen.remove(player.getUniqueId());
    }

    public void openBattleCreator(Player player) {
        BattleType battleType = getBattleType(player);
        Party party = Party.byPlayer(player);

        int size = 9;
        if (party != null) {
            int largerTeam = party.getLargestTeamSize();
            while (size < (largerTeam * 2) + 9) size += 9;
        }
        if (size > 54) size = 54;

        Inventory inventory = Bukkit.createInventory(null, size, "Battle Creator");

        inventory.setItem(0, battleType.getIcon());
        inventory.setItem(1, getArenaIcon(player));
        inventory.setItem(2, getBattleDurationIcon(player));
        inventory.setItem(3, getGracePeriodIcon(player));

        if (canStart(player)) inventory.setItem(8, getStartGameIcon());

        if (party != null) {
            int i = 0;
            for (Player each : party.getPlayers(1))
                inventory.setItem(getSlots(true).get(i++), getPlayerIcon(each));
            i = 0;
            for (Player each : party.getPlayers(2))
                inventory.setItem(getSlots(false).get(i++), getPlayerIcon(each));
        }

        player.openInventory(inventory);
        battleCreatorUIOpen.add(player.getUniqueId());
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

    private void setArena(Player player, Arena arena) {
        this.arena.put(player.getUniqueId(), arena);
    }

    private void toggleBattleType(Player player, boolean next) {
        BattleType battleType = getBattleType(player);
        battleType = battleType.toggle(next);

        this.battleType.put(player.getUniqueId(), battleType);
    }

    private void toggleGracePeriod(Player player, boolean next, boolean shift) {
        long gracePeriod = getGracePeriod(player);

        gracePeriod += (next ? 1000L : -1000L) * (shift ? 1 : 60);

        this.gracePeriod.put(player.getUniqueId(), Math.min(Math.max(gracePeriod, 0L), getBattleDuration(player)));
    }

    private void toggleBattleDuration(Player player, boolean next, boolean shift) {
        long battleDuration = getBattleDuration(player);

        battleDuration += (next ? 1000L : -1000L) * (shift ? 1 : 60);

        long maxDuration = 15 * 60 * 1000;

        this.battleDuration.put(player.getUniqueId(), Math.min(Math.max(battleDuration, 0L), maxDuration));
    }

    private boolean canStart(Player player) {
        Arena arena = getArena(player);

        if (arena == null) return false;
        if (arena.inUse()) return false;

        return true;
    }

    public boolean hasBattleCreatorUIOpen(Player player) {
        return battleCreatorUIOpen.contains(player.getUniqueId());
    }

    public boolean hasArenaPickerUIOpen(Player player) {
        return arenaPickerUIOpen.contains(player.getUniqueId());
    }

    private Arena getArena(Player player) {
        Arena arena = this.arena.getOrDefault(player.getUniqueId(), null);

        if (arena == null)
            arena = Arena.getFree();

        this.arena.put(player.getUniqueId(), arena);
        return arena;
    }

    private BattleType getBattleType(Player player) {
        return this.battleType.getOrDefault(player.getUniqueId(), BattleType.getDefault());
    }

    private long getGracePeriod(Player player) {
        long gracePeriod = this.gracePeriod.getOrDefault(player.getUniqueId(), 0L);
        long battleDuration = getBattleDuration(player);

        if (gracePeriod > battleDuration) gracePeriod = battleDuration;

        return gracePeriod;
    }

    private long getBattleDuration(Player player) {
        return battleDuration.getOrDefault(player.getUniqueId(), 3 * 60 * 1000L);
    }

    private ItemStack getArenaIcon(Player player) {
        Arena arena = getArena(player);
        if (arena == null) {
            if (noFreeArenaIcon == null)
                noFreeArenaIcon = Icon.build(BattleArena.getInstance().getConfig(), "No_Free_Arena.Team", false);
            return noFreeArenaIcon;
        }

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Click to change Arena"));

        ItemStack icon = getArenaIcon(arena);
        ItemMeta itemMeta = icon.getItemMeta();
        itemMeta.setLore(lore);
        icon.setItemMeta(itemMeta);

        return icon;
    }

    private ItemStack getArenaIcon(Arena arena) {
        return Icon.build(arena.getMaterial(), arena.getDurability(), false, arena.getName());
    }

    private ItemStack getGracePeriodIcon(Player player) {
        if (gracePeriodIcon == null) gracePeriodIcon = Icon.build(BattleArena.getInstance().getConfig(), "Battle_Creator.Grace_Period", false);

        Date date = new Date(0);
        date.setTime(getGracePeriod(player));
        String gracePeriod = simpleDateFormat.format(date);

        List<String> lore = new ArrayList<>();
        for (String line : gracePeriodIcon.getItemMeta().getLore()) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line
                    .replace("%graceperiod%", gracePeriod)
            ));
        }

        ItemStack icon = gracePeriodIcon.clone();
        ItemMeta itemMeta = icon.getItemMeta();
        itemMeta.setLore(lore);
        icon.setItemMeta(itemMeta);
        return icon;
    }

    private ItemStack getBattleDurationIcon(Player player) {
        if (battleDurationIcon == null) battleDurationIcon = Icon.build(BattleArena.getInstance().getConfig(), "Battle_Creator.Battle_Duration", false);

        Date date = new Date();
        date.setHours(0);
        date.setTime(getBattleDuration(player));
        String battleDuration = simpleDateFormat.format(date);

        List<String> lore = new ArrayList<>();
        for (String line : battleDurationIcon.getItemMeta().getLore()) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line
                    .replace("%battleduration%", battleDuration)
            ));
        }

        ItemStack icon = battleDurationIcon.clone();
        ItemMeta itemMeta = icon.getItemMeta();
        itemMeta.setLore(lore);
        icon.setItemMeta(itemMeta);
        return icon;
    }

    private ItemStack getStartGameIcon() {
        if (startGameIcon == null)
            startGameIcon = Icon.build(BattleArena.getInstance().getConfig(), "Battle_Creator.Start_Game", false);
        return startGameIcon;
    }

    private ItemStack getPlayerIcon(Player player) {
        Party party = Party.byPlayer(player);
        List<String> lore = new ArrayList<>();
        if (party != null)
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Team " + party.getTeam(player)));
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7KDR: &f" + KillCounter.getKDR(player.getUniqueId())));

        ItemStack icon = new ItemStack(Material.SKULL_ITEM);
        SkullMeta itemMeta = (SkullMeta) icon.getItemMeta();
        itemMeta.setOwningPlayer(player);
        itemMeta.setDisplayName(BattleArena.MAIN + player.getName());
        itemMeta.setLore(lore);
        icon.setItemMeta(itemMeta);
        icon.setDurability((short) 3);
        return icon;
    }

    private List<Integer> getSlots(boolean team1) {
        if (team1Slots == null) {
            team1Slots = new ArrayList<>();
            int slot = 9;
            while (slot < 54) {
                for (int i = 0; i < 4; i++)
                    team1Slots.add(slot + i);
                slot += 9;
            }
        }
        if (team2Slots == null) {
            team2Slots = new ArrayList<>();
            int slot = 14;
            while (slot < 54) {
                for (int i = 0; i < 4; i++)
                    team2Slots.add(slot + i);
                slot += 9;
            }
        }
        return team1 ? team1Slots : team2Slots;
    }

    public static BattleCreator getInstance() {
        if (battleCreator == null) battleCreator = new BattleCreator();
        return battleCreator;
    }
}