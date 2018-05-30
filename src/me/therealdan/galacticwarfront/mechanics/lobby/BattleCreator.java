package me.therealdan.galacticwarfront.mechanics.lobby;

import me.therealdan.galacticwarfront.GalacticWarFront;
import me.therealdan.galacticwarfront.mechanics.battle.Arena;
import me.therealdan.galacticwarfront.mechanics.battle.battle.Battle;
import me.therealdan.galacticwarfront.mechanics.battle.battle.Duel;
import me.therealdan.galacticwarfront.mechanics.battle.battle.FFA;
import me.therealdan.galacticwarfront.mechanics.battle.battle.Team;
import me.therealdan.galacticwarfront.mechanics.killcounter.KillCounter;
import me.therealdan.galacticwarfront.util.Icon;
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
    private HashMap<UUID, Battle.Type> battleType = new HashMap<>();
    private HashMap<UUID, Long> gracePeriod = new HashMap<>();
    private HashMap<UUID, Long> battleDuration = new HashMap<>();

    private ItemStack duelIcon, ffaIcon, teamBattleIcon, gracePeriodIcon, battleDurationIcon, startGameIcon, noFreeArenaIcon;
    private List<Integer> team1Slots, team2Slots;

    private int battleTypeSlot = 0;
    private int arenaSlot = 1;
    private int battleDurationSlot = 2;
    private int gracePeriodSlot = 3;
    private int startGameSlot = 8;

    private BattleCreator() {
    }

    @EventHandler
    public void onBattleCreatorClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!hasBattleCreatorUIOpen(player)) return;
        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;

        if (event.getSlot() == battleTypeSlot) {
            toggleBattleType(player, event.isLeftClick());
            openBattleCreator(player);
            return;
        } else if (event.getSlot() == arenaSlot) {
            openArenaPicker(player);
            return;
        } else if (event.getSlot() == battleDurationSlot) {
            toggleBattleDuration(player, event.isLeftClick(), event.isShiftClick());
            openBattleCreator(player);
            return;
        } else if (event.getSlot() == gracePeriodSlot) {
            toggleGracePeriod(player, event.isLeftClick(), event.isShiftClick());
            openBattleCreator(player);
            return;
        } else if (getStartGameIcon().isSimilar(event.getCurrentItem())) {
            Arena arena = getArena(player);
            if (arena.inUse()) return;
            Battle battle = null;
            Party party = Party.byPlayer(player);
            switch (getBattleType(player)) {
                case FFA:
                    battle = new FFA(arena, player);
                    if (party != null)
                        for (Player each : party.getPlayers())
                            battle.add(each);
                    break;
                case Duel:
                    if (party == null) return;
                    if (party.getPlayers().size() < 2) return;
                    battle = new Duel(arena, player, party.getPlayers().get(1));
                    for (Player each : party.getPlayers())
                        battle.add(each);
                    break;
                case Team:
                    if (party == null) return;
                    if (party.getPlayers(1).size() == 0) return;
                    if (party.getPlayers(2).size() == 0) return;
                    Team team = new Team(arena, player);
                    for (Player each : party.getPlayers())
                        team.add(each, party.isTeam(each, 1));
                    battle = team;
                    break;
            }
            battle.setGracePeriod(getGracePeriod(player) / 1000);
            battle.setTimeRemaining(getBattleDuration(player) / 1000);
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
        Battle.Type battleType = getBattleType(player);
        Party party = Party.byPlayer(player);

        int size = 9;
        if (party != null) {
            if (battleType.hasTeams()) {
                int largerTeam = party.getLargestTeamSize();
                while (size < (largerTeam * 2) + 9) size += 9;
            } else {
                while (size < party.getPlayers().size() + 9) size += 9;
            }
        }
        if (size > 54) size = 54;

        Inventory inventory = Bukkit.createInventory(null, size, "Battle Creator");

        inventory.setItem(battleTypeSlot, battleType.equals(Battle.Type.FFA) ? getFFAIcon() : battleType.equals(Battle.Type.Team) ? getTeamBattleIcon() : getDuelIcon());
        inventory.setItem(arenaSlot, getArenaIcon(player));
        inventory.setItem(battleDurationSlot, getBattleDurationIcon(player));
        inventory.setItem(gracePeriodSlot, getGracePeriodIcon(player));

        boolean canStart = true;
        if (getArena(player) == null || getArena(player).inUse()) canStart = false;
        if (battleType.hasTeams()) {
            if (getArena(player) == null || !getArena(player).hasTeamSpawnpoints())
                canStart = false;
            if (party == null || party.size(1) == 0 || party.size(2) == 0)
                canStart = false;
        } else {
            if (getArena(player) == null || !getArena(player).hasSpawnpoints())
                canStart = false;
        }
        if (canStart) inventory.setItem(startGameSlot, getStartGameIcon());

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
        Battle.Type battleType = getBattleType(player);
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

    private Battle.Type getBattleType(Player player) {
        Battle.Type battleType = this.battleType.getOrDefault(player.getUniqueId(), Battle.Type.FFA);

        // TODO - Better conditions

        switch (battleType) {
            case FFA:

                break;
            case Duel:

                break;
            case Team:

                break;
        }

        if (battleType.equals(Battle.Type.Team) || battleType.equals(Battle.Type.Duel)) {
            Party party = Party.byPlayer(player);
            if (party == null || party.size(1) == 0 || party.size(2) == 0)
                battleType = Battle.Type.FFA;
        }

        return battleType;
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

    private ItemStack getDuelIcon() {
        if (duelIcon == null)
            duelIcon = Icon.build(GalacticWarFront.getInstance().getConfig(), "Battle_Creator.Duel", false);
        return duelIcon;
    }

    private ItemStack getFFAIcon() {
        if (ffaIcon == null)
            ffaIcon = Icon.build(GalacticWarFront.getInstance().getConfig(), "Battle_Creator.FFA", false);
        return ffaIcon;
    }

    private ItemStack getTeamBattleIcon() {
        if (teamBattleIcon == null)
            teamBattleIcon = Icon.build(GalacticWarFront.getInstance().getConfig(), "Battle_Creator.Team", false);
        return teamBattleIcon;
    }

    private ItemStack getArenaIcon(Player player) {
        Arena arena = getArena(player);
        if (arena == null) {
            if (noFreeArenaIcon == null)
                noFreeArenaIcon = Icon.build(GalacticWarFront.getInstance().getConfig(), "No_Free_Arena.Team", false);
            return noFreeArenaIcon;
        }
        return getArenaIcon(arena);
    }

    private ItemStack getArenaIcon(Arena arena) {
        ItemStack icon = new ItemStack(arena.getMaterial());
        ItemMeta itemMeta = icon.getItemMeta();
        itemMeta.setDisplayName(arena.getName());
        icon.setItemMeta(itemMeta);
        icon.setDurability(arena.getDurability());
        return icon;
    }

    private ItemStack getGracePeriodIcon(Player player) {
        if (gracePeriodIcon == null) gracePeriodIcon = Icon.build(GalacticWarFront.getInstance().getConfig(), "Battle_Creator.Grace_Period", false);

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
        if (battleDurationIcon == null) battleDurationIcon = Icon.build(GalacticWarFront.getInstance().getConfig(), "Battle_Creator.Battle_Duration", false);

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
            startGameIcon = Icon.build(GalacticWarFront.getInstance().getConfig(), "Battle_Creator.Start_Game", false);
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
        itemMeta.setDisplayName(GalacticWarFront.MAIN + player.getName());
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