package me.therealdan.galacticwarfront.events;

import me.therealdan.galacticwarfront.mechanics.battle.battle.Battle;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BattleStartEvent extends Event {

    private static HandlerList handlerList = new HandlerList();

    private Battle battle;
    private Player started;

    public BattleStartEvent(Battle battle, Player started) {
        this.battle = battle;
        this.started = started;
    }

    public Battle getBattle() {
        return battle;
    }

    public Player getStarted() {
        return started;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
