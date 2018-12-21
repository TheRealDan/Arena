package me.therealdan.battlearena.events;

import me.therealdan.battlearena.mechanics.battle.Battle;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BattleFinishEvent extends Event {

    private static HandlerList handlerList = new HandlerList();

    private Battle battle;
    private String battleMessage, lobbyMessage;

    public BattleFinishEvent(Battle battle) {
        this.battle = battle;
    }

    public Battle getBattle() {
        return battle;
    }

    public void setBattleMessage(String battleMessage) {
        this.battleMessage = battleMessage;
    }

    public void setLobbyMessage(String lobbyMessage) {
        this.lobbyMessage = lobbyMessage;
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
