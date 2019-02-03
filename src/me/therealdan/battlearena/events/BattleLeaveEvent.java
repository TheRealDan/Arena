package me.therealdan.battlearena.events;

import me.therealdan.battlearena.mechanics.battle.Battle;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BattleLeaveEvent extends Event {

    private static HandlerList handlerList = new HandlerList();

    private Battle battle;
    private Player player;
    private Reason reason;
    private Location spawn;

    private String battleMessage;

    public BattleLeaveEvent(Battle battle, Player player, Reason reason, Location spawn) {
        this.battle = battle;
        this.player = player;
        this.reason = reason;
        this.spawn = spawn;
    }

    public void setSpawn(Location spawn) {
        this.spawn = spawn;
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

    public Reason getReason() {
        return reason;
    }

    public Location getSpawn() {
        return spawn;
    }

    public String getBattleMessage() {
        return battleMessage;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public enum Reason {
        BATTLE_FINISHED,
        NOT_ENOUGH_PLAYERS,
        LEAVE, LOGOUT,
        SERVER_SHUTDOWN,
        KICK, ADMIN_END,
        CUSTOM_PLUGIN
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
