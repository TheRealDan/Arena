package me.therealdan.battlearena.util;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.HashMap;

public class PlayerHandler {

    private static HashMap<String, HashMap<String, ItemStack[]>> inventoryContents = new HashMap<>();

    public static void refresh(Player player) {
        for (PotionEffect potionEffect : player.getActivePotionEffects())
            player.removePotionEffect(potionEffect.getType());
        player.setFoodLevel(20);
        player.setHealth(player.getMaxHealth());
        player.setGameMode(GameMode.SURVIVAL);
        player.setFireTicks(0);
        player.setFallDistance(0);
    }

    public static void saveInventory(Player player, String key) {
        HashMap<String, ItemStack[]> contents = new HashMap<>();
        contents.put("contents", player.getInventory().getContents());
        contents.put("armor", player.getInventory().getArmorContents());
        inventoryContents.put(key, contents);
    }

    public static void restoreInventory(Player player, String key) {
        if (!inventoryContents.containsKey(key)) return;
        HashMap<String, ItemStack[]> contents = inventoryContents.get(key);
        player.getInventory().setContents(contents.get("contents"));
        player.getInventory().setArmorContents(contents.get("armor"));
    }

    public static void clearInventory(Player player) {
        player.getInventory().clear();
        player.getEquipment().clear();
    }
}