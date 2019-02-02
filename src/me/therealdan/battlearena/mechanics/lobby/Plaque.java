package me.therealdan.battlearena.mechanics.lobby;

import me.therealdan.battlearena.BattleArena;
import me.therealdan.battlearena.mechanics.killcounter.KillCounter;
import me.therealdan.battlearena.util.Icon;
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
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class Plaque implements Listener {

    private static Plaque plaque;

    private HashSet<UUID> uiOpen = new HashSet<>();

    private Plaque() {

    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!isViewing(player)) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        uiOpen.remove(player.getUniqueId());
    }

    public void view(Player player) {
        int size = 9;
        while (size < Bukkit.getOnlinePlayers().size()) size += 9;
        if (size > 54) size = 54;

        Inventory inventory = Bukkit.createInventory(null, size, ChatColor.translateAlternateColorCodes('&', "&1Plaque"));

        for (Player online : getPlayers())
            inventory.addItem(getIcon(online));

        player.openInventory(inventory);
        uiOpen.add(player.getUniqueId());
    }

    public boolean isViewing(Player player) {
        return uiOpen.contains(player.getUniqueId());
    }

    public ItemStack getIcon(Player player) {
        ItemStack icon = Icon.build(Material.SKULL_ITEM, 3, false, BattleArena.MAIN + player.getName(),
                BattleArena.MAIN + "KDR: " + BattleArena.SECOND + KillCounter.getKDRString(player.getUniqueId()),
                BattleArena.MAIN + "Total Kills: " + BattleArena.SECOND + KillCounter.getTotalKills(player.getUniqueId()),
                BattleArena.MAIN + "Total Deaths: " + BattleArena.SECOND + KillCounter.getTotalDeaths(player.getUniqueId())
        );

        SkullMeta skullMeta = (SkullMeta) icon.getItemMeta();
        skullMeta.setOwningPlayer(player);
        icon.setItemMeta(skullMeta);

        return icon;
    }

    public List<Player> getPlayers() {
        List<Player> orderedList = new ArrayList<>();
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());

        while (players.size() > 0) {
            Player top = null;
            for (Player each : players) {
                if (top == null ||
                        KillCounter.getTotalKills(each.getUniqueId()) > KillCounter.getTotalKills(top.getUniqueId()) ||
                        (KillCounter.getTotalKills(each.getUniqueId()) == KillCounter.getTotalKills(top.getUniqueId()) && KillCounter.getTotalDeaths(each.getUniqueId()) < KillCounter.getTotalDeaths(top.getUniqueId()))
                ) {
                    top = each;
                }
            }
            orderedList.add(top);
            players.remove(top);
        }

        return orderedList;
    }

    public static Plaque getInstance() {
        if (plaque == null) plaque = new Plaque();
        return plaque;
    }
}