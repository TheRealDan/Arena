package me.therealdan.galacticwarfront.events;

import me.therealdan.galacticwarfront.mechanics.battle.battle.Battle;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BattleJoinEvent extends Event {

    private static HandlerList handlerList = new HandlerList();

    private Battle battle;
    private Player player;

    public BattleJoinEvent(Battle battle, Player player) {
        this.battle = battle;
        this.player = player;
    }

    public Battle getBattle() {
        return battle;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
