package me.therealdan.battlearena.events;

import me.therealdan.battlearena.mechanics.battle.Battle;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BattleCommandEvent extends Event implements Cancellable {

    private static HandlerList handlerList = new HandlerList();

    private Battle battle;
    private Player player;
    private boolean cancelled = false;
    private String command;
    private String[] args;

    public BattleCommandEvent(Battle battle, Player player, String message) {
        this.battle = battle;
        this.player = player;

        this.command = message.split(" ")[0].substring(1);
        this.args = message.substring(1).replaceFirst(command + " ", "").split(" ");
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public Battle getBattle() {
        return battle;
    }

    public Player getPlayer() {
        return player;
    }

    public String getCommand() {
        return command;
    }

    public String[] getArgs() {
        return args;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
