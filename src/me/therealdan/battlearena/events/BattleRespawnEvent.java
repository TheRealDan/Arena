package me.therealdan.battlearena.events;

import me.therealdan.battlearena.mechanics.battle.Battle;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BattleRespawnEvent extends Event {

    private static HandlerList handlerList = new HandlerList();

    private Battle battle;
    private Player player;
    private Location respawnLocation;

    public BattleRespawnEvent(Battle battle, Player player, Location respawnLocation) {
        this.battle = battle;
        this.player = player;
        this.respawnLocation = respawnLocation;
    }

    public void setRespawnLocation(Location respawnLocation) {
        this.respawnLocation = respawnLocation;
    }

    public Battle getBattle() {
        return battle;
    }

    public Player getPlayer() {
        return player;
    }

    public Location getRespawnLocation() {
        return respawnLocation;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
