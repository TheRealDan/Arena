package me.therealdan.battlearena.events;

import me.therealdan.battlearena.mechanics.arena.Arena;
import me.therealdan.battlearena.mechanics.battle.BattleType;
import me.therealdan.battlearena.mechanics.setup.Settings;
import me.therealdan.party.Party;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BattleCreateEvent extends Event {

    private static HandlerList handlerList = new HandlerList();

    private Player player;
    private Party party;
    private Arena arena;
    private BattleType battleType;
    private Settings settings;

    private boolean created = false;

    public BattleCreateEvent(Player player, Party party, Arena arena, BattleType battleType, Settings settings) {
        this.player = player;
        this.party = party;
        this.arena = arena;
        this.battleType = battleType;
        this.settings = settings;
    }

    public Player getPlayer() {
        return player;
    }

    public Party getParty() {
        return party;
    }

    public Arena getArena() {
        return arena;
    }

    public BattleType getBattleType() {
        return battleType;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setCreated(boolean created) {
        this.created = created;
    }

    public boolean isCreated() {
        return created;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
