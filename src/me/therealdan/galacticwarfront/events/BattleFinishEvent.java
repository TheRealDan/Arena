package me.therealdan.galacticwarfront.events;

import me.therealdan.galacticwarfront.mechanics.battle.battle.Battle;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BattleFinishEvent extends Event {

    private static HandlerList handlerList = new HandlerList();

    private Battle battle;

    public BattleFinishEvent(Battle battle) {
        this.battle = battle;
    }

    public Battle getBattle() {
        return battle;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
