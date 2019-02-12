package me.therealdan.battlearena.mechanics.setup;

import me.therealdan.battlearena.BattleArena;
import me.therealdan.battlearena.events.BattleCreateEvent;
import me.therealdan.battlearena.mechanics.arena.Arena;
import me.therealdan.battlearena.mechanics.battle.battles.FFA;
import me.therealdan.battlearena.mechanics.battle.battles.Team;
import me.therealdan.battlearena.mechanics.setup.settings.Map;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class SetupHandler implements Listener {

    private static SetupHandler setupHandler;

    private HashSet<UUID> uiOpen = new HashSet<>();
    private HashMap<UUID, Setup> setup = new HashMap<>();
    private HashMap<UUID, Setting> setting = new HashMap<>();

    private Setup defaultSetup = null;

    private SetupHandler() {

    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!isViewing(player)) return;
        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().getType().equals(Material.AIR)) return;

        ItemStack icon = event.getCurrentItem();
        boolean shift = event.isShiftClick();
        boolean left = event.isLeftClick();

        Setting setting = getSetting(player);
        if (setting != null) {
            if (setting.click(player, icon, shift, left)) {
                this.setting.put(player.getUniqueId(), setting);
                uiOpen.add(player.getUniqueId());
            } else {
                this.setting.remove(player.getUniqueId());
                open(player);
            }
            return;
        }

        Setup setup = getSetup(player);
        if (setup != null) {
            for (Setting each : setup.getSettings().values()) {
                if (setup.getSettingIcon(each).isSimilar(icon)) {
                    if (each.click(player, shift, left)) {
                        this.setting.put(player.getUniqueId(), each);
                        uiOpen.add(player.getUniqueId());
                        return;
                    }
                }
            }

            if (setup.getBattleTypeIcon().isSimilar(event.getCurrentItem())) {
                Setup nextSetup = setup.getBattleType().next().getSetup();
                nextSetup.getSettings().copy(setup.getSettings());
                this.setup.put(player.getUniqueId(), nextSetup);
                open(player);
                return;
            }

            if (setup.getStartIcon().isSimilar(event.getCurrentItem())) {
                Party party = Party.byPlayer(player);
                Arena arena = null;
                for (Setting each : setup.getSettings().values())
                    if (each instanceof Map)
                        arena = ((Map) each).getArena();

                BattleCreateEvent battleCreateEvent = new BattleCreateEvent(player, party, arena, setup.getBattleType(), setup.getSettings());
                Bukkit.getPluginManager().callEvent(battleCreateEvent);

                if (!battleCreateEvent.isCreated()) {
                    switch (battleCreateEvent.getBattleType().getName()) {
                        case "FFA":
                            new FFA(arena, player, party, setup.getSettings());
                            break;
                        case "Team":
                            new Team(arena, player, party, setup.getSettings());
                            break;
                    }
                }

                player.closeInventory();
                return;
            }
        }

        open(player);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        uiOpen.remove(player.getUniqueId());
        setting.remove(player.getUniqueId());
    }

    public void open(Player player) {
        Setup setup = getSetup(player);
        if (setup == null) {
            player.sendMessage(BattleArena.MAIN + "There is no default battle mode!");
            return;
        }

        Party party = Party.byPlayer(player);
        int size = 9;
        if (party != null) {
            int largerTeam = party.getLargestTeamSize();
            while (size < (largerTeam * 2) + 9) size += 9;
        }
        if (size > 54) size = 54;

        Inventory inventory = Bukkit.createInventory(null, size, ChatColor.translateAlternateColorCodes('&', "&1Arena Setup"));

        inventory.setItem(0, setup.getBattleTypeIcon());
        inventory.setItem(8, setup.getStartIcon());

        for (Setting setting : setup.getSettings().values())
            inventory.addItem(setup.getSettingIcon(setting));

        player.openInventory(inventory);
        uiOpen.add(player.getUniqueId());
    }

    public boolean isViewing(Player player) {
        return uiOpen.contains(player.getUniqueId());
    }

    public Setup getSetup(Player player) {
        if (defaultSetup == null) return null;
        if (!setup.containsKey(player.getUniqueId())) setup.put(player.getUniqueId(), defaultSetup.clone());
        return setup.get(player.getUniqueId());
    }

    public Setting getSetting(Player player) {
        if (!setting.containsKey(player.getUniqueId())) return null;
        return setting.get(player.getUniqueId());
    }

    public static void setDefault(Setup setup) {
        getInstance().defaultSetup = setup;
    }

    public static SetupHandler getInstance() {
        if (setupHandler == null) setupHandler = new SetupHandler();
        return setupHandler;
    }
}