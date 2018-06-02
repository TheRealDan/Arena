package me.therealdan.galacticwarfront.listeners;

import me.therealdan.galacticwarfront.events.BattleLeaveEvent;
import me.therealdan.galacticwarfront.mechanics.battle.Battle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Battle battle = Battle.get(player);
        if (battle == null) return;

        battle.remove(player, BattleLeaveEvent.Reason.LOGOUT);
    }
}