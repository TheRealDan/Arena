package me.therealdan.battlearena.mechanics.setup.settings;

import me.therealdan.battlearena.mechanics.arena.Arena;
import me.therealdan.battlearena.mechanics.setup.Setting;
import me.therealdan.battlearena.util.Icon;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Map extends Setting {

    public Map(Arena arena) {
        super("Map", arena.getID());
    }

    @Override
    public boolean click(Player player, boolean shift, boolean left) {
        List<Arena> arenas = new ArrayList<>(Arena.values());

        int size = 9;
        while (size < arenas.size()) size += 9;
        if (size > 54) size = 54;

        Inventory inventory = Bukkit.createInventory(null, size, ChatColor.translateAlternateColorCodes('&', "&1Maps"));

        while (arenas.size() > 0) {
            Arena arena = null;
            for (Arena each : arenas) {
                if (arena == null) {
                    arena = each;
                } else if (arena.getName().compareTo(each.getName()) > 0) {
                    arena = each;
                }
            }

            inventory.addItem(getIcon(arena));
            arenas.remove(arena);
        }

        player.openInventory(inventory);
        return true;
    }

    @Override
    public boolean click(Player player, ItemStack icon, boolean shift, boolean left) {
        for (Arena arena : Arena.available()) {
            if (getIcon(arena).isSimilar(icon)) {
                set(arena.getID());
                return false;
            }
        }

        return true;
    }

    @Override
    public Material getMaterial() {
        return getArena().getMaterial();
    }

    @Override
    public short getDurability() {
        return getArena().getDurability();
    }

    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("&7Map: " + getArena().getName());
        return description;
    }

    public Arena getArena() {
        return Arena.get((String) getValue());
    }

    private ItemStack getIcon(Arena arena) {
        return Icon.build(arena.getMaterial(), arena.getDurability(), false, arena.getName());
    }

    @Override
    public Map clone() {
        return new Map(getArena());
    }
}