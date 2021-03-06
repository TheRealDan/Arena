package me.therealdan.battlearena.mechanics.statistics;

import me.therealdan.battlearena.events.BattleDeathEvent;
import me.therealdan.battlearena.events.BattleFinishEvent;
import me.therealdan.battlearena.events.BattleJoinEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class StatisticsHandler implements Listener {

    @EventHandler
    public void onJoin(BattleJoinEvent event) {
        if (event.getBattle().statisticsTrackingEnabled())
            Statistics.byPlayer(event.getPlayer()).addGamePlayed();
    }

    @EventHandler
    public void onFinish(BattleFinishEvent event) {
        if (event.getBattle().getKillCounter().getMostKills() == null) return;

        if (event.getBattle().statisticsTrackingEnabled())
            Statistics.byUUID(event.getBattle().getKillCounter().getMostKills()).addGameWon();
    }

    @EventHandler
    public void onDeath(BattleDeathEvent event) {
        if (event.getBattle().statisticsTrackingEnabled())
            if (event.getKiller() != null) Statistics.byPlayer(event.getKiller()).addKill();
        if (event.getBattle().statisticsTrackingEnabled())
            Statistics.byPlayer(event.getPlayer()).addDeath();
    }
}