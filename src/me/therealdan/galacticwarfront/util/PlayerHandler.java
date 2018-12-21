package me.therealdan.galacticwarfront.util;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class PlayerHandler {

    public static void refresh(Player player) {
        for (PotionEffect potionEffect : player.getActivePotionEffects())
            player.removePotionEffect(potionEffect.getType());
        player.setFoodLevel(20);
        player.setHealth(player.getMaxHealth());
        player.setGameMode(GameMode.SURVIVAL);
        player.setFireTicks(0);
    }
}