package me.therealdan.battlearena.mechanics.arena.editors;

import me.therealdan.battlearena.BattleArena;
import me.therealdan.battlearena.mechanics.arena.Arena;
import me.therealdan.battlearena.util.Icon;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class ConsequenceEditor implements Listener {

    private static ConsequenceEditor consequenceEditor;

    private HashMap<UUID, Arena> uiOpen = new HashMap<>();

    private ConsequenceEditor() {

    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!uiOpen.containsKey(player.getUniqueId())) return;
        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().getType().equals(Material.AIR)) return;

        Arena arena = uiOpen.get(player.getUniqueId());
        if (getTopIcon(arena).isSimilar(event.getCurrentItem())) {
            arena.setTopConsequence(arena.getTopConsequence().next());
        } else if (getSidesIcon(arena).isSimilar(event.getCurrentItem())) {
            arena.setSidesConsequence(arena.getSidesConsequence().next());
        } else if (getFloorIcon(arena).isSimilar(event.getCurrentItem())) {
            arena.setFloorConsequence(arena.getFloorConsequence().next());
        } else {
            return;
        }

        open(player, arena);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        uiOpen.remove(event.getPlayer().getUniqueId());
    }

    public void open(Player player, Arena arena) {
        Inventory inventory = Bukkit.createInventory(null, 9, BattleArena.MAIN + "Consequences for " + BattleArena.SECOND + arena.getID());

        inventory.setItem(0, getTopIcon(arena));
        inventory.setItem(1, getSidesIcon(arena));
        inventory.setItem(2, getFloorIcon(arena));

        player.openInventory(inventory);
        uiOpen.put(player.getUniqueId(), arena);
    }

    private ItemStack getTopIcon(Arena arena) {
        return Icon.build(Material.STONE, false, BattleArena.MAIN + "Top",
                "&7Consequence: " + arena.getTopConsequence().getName(),
                "&7" + arena.getTopConsequence().getDescription()
        );
    }

    private ItemStack getSidesIcon(Arena arena) {
        return Icon.build(Material.STONE, false, BattleArena.MAIN + "Sides",
                "&7Consequence: " + arena.getSidesConsequence().getName(),
                "&7" + arena.getSidesConsequence().getDescription()
        );
    }

    private ItemStack getFloorIcon(Arena arena) {
        return Icon.build(Material.STONE, false, BattleArena.MAIN + "Floor",
                "&7Consequence: " + arena.getFloorConsequence().getName(),
                "&7" + arena.getFloorConsequence().getDescription()
        );
    }

    public static ConsequenceEditor getInstance() {
        if (consequenceEditor == null) consequenceEditor = new ConsequenceEditor();
        return consequenceEditor;
    }
}