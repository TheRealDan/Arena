package me.therealdan.battlearena.events;

import me.therealdan.battlearena.mechanics.battle.Battle;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BattleDeathEvent extends Event {

    private static HandlerList handlerList = new HandlerList();

    private Battle battle;
    private Player player, killer;
    private Reason reason;

    private String battleMessage;

    public BattleDeathEvent(Battle battle, Player player, Player killer, Reason reason) {
        this.battle = battle;
        this.player = player;
        this.killer = killer;
        this.reason = reason;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setKiller(Player killer) {
        this.killer = killer;
    }

    public void setReason(Reason reason) {
        this.reason = reason;
    }

    public void setBattleMessage(String battleMessage) {
        this.battleMessage = battleMessage;
    }

    public Battle getBattle() {
        return battle;
    }

    public Player getPlayer() {
        return player;
    }

    public Player getKiller() {
        return killer;
    }

    public Reason getReason() {
        return reason;
    }

    public String getBattleMessage() {
        return battleMessage;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public enum Reason {
        PLAYER,
        SUICIDE,
        FLEE,
        CUSTOM,
    }
}
