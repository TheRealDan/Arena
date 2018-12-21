package me.therealdan.battlearena.events;

import me.therealdan.battlearena.mechanics.battle.Battle;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BattleStartEvent extends Event {

    private static HandlerList handlerList = new HandlerList();

    private Battle battle;
    private Player started;
    private String playerMessage, battleMessage, lobbyMessage;

    public BattleStartEvent(Battle battle, Player started) {
        this.battle = battle;
        this.started = started;
    }

    public void setPlayerMessage(String playerMessage) {
        this.playerMessage = playerMessage;
    }

    public void setBattleMessage(String battleMessage) {
        this.battleMessage = battleMessage;
    }

    public void setLobbyMessage(String lobbyMessage) {
        this.lobbyMessage = lobbyMessage;
    }

    public Battle getBattle() {
        return battle;
    }

    public Player getStarted() {
        return started;
    }

    public String getPlayerMessage() {
        return playerMessage;
    }

    public String getBattleMessage() {
        return battleMessage;
    }

    public String getLobbyMessage() {
        return lobbyMessage;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
