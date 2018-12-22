package me.therealdan.battlearena.events;

import me.therealdan.battlearena.mechanics.battle.Battle;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BattleFinishEvent extends Event {

    private static HandlerList handlerList = new HandlerList();

    private Battle battle;
    private BattleLeaveEvent.Reason reason;
    private String battleMessage, lobbyMessage;

    public BattleFinishEvent(Battle battle, BattleLeaveEvent.Reason reason) {
        this.battle = battle;
        this.reason = reason;
    }

    public Battle getBattle() {
        return battle;
    }

    public BattleLeaveEvent.Reason getReason() {
        return reason;
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
