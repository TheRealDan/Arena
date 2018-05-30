package me.therealdan.galacticwarfront.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public interface BattleListener extends Listener {

    @EventHandler
    void onStart(BattleStartEvent event);

    @EventHandler
    void onFinish(BattleFinishEvent event);

    @EventHandler
    void onJoin(BattleJoinEvent event);

    @EventHandler
    void onLeave(BattleLeaveEvent event);

    @EventHandler
    void onDamage(BattleDamageEvent event);

    @EventHandler
    void onDeath(BattleDeathEvent event);

    @EventHandler
    void onRespawn(BattleRespawnEvent event);
}