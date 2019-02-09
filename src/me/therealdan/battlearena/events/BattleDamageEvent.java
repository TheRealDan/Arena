package me.therealdan.battlearena.events;

import me.therealdan.battlearena.mechanics.battle.Battle;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;

public class BattleDamageEvent extends Event implements Cancellable {

    private static HandlerList handlerList = new HandlerList();

    private Battle battle;
    private Player attacker, victim;
    private boolean cancelled = false;
    private double damage;
    private EntityDamageEvent.DamageCause damageCause;

    public BattleDamageEvent(Battle battle, Player attacker, Player victim, double damage, EntityDamageEvent.DamageCause damageCause) {
        this.battle = battle;
        this.attacker = attacker;
        this.victim = victim;
        this.damage = damage;
        this.damageCause = damageCause;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public Battle getBattle() {
        return battle;
    }

    public void setAttacker(Player attacker) {
        this.attacker = attacker;
    }

    public Player getAttacker() {
        return attacker;
    }

    public Player getVictim() {
        return victim;
    }

    public EntityDamageEvent.DamageCause getDamageCause() {
        return damageCause;
    }

    public double getDamage() {
        return damage;
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
