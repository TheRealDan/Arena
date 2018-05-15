package me.therealdan.galacticwarfront.mechanics.battle;

import me.therealdan.galacticwarfront.GalacticWarFront;
import me.therealdan.galacticwarfront.events.BattleDamageEvent;
import me.therealdan.galacticwarfront.events.BattleLeaveEvent;
import me.therealdan.galacticwarfront.mechanics.battle.battle.Battle;
import me.therealdan.galacticwarfront.mechanics.battle.battle.TeamBattle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.*;

public class BattleHandler implements Listener {

    public BattleHandler() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(GalacticWarFront.getInstance(), new Runnable() {
            @Override
            public void run() {
                task();
            }
        }, 20, 20);
    }

    private void task() {
        for (Battle battle : Battle.values()) {
            if (System.currentTimeMillis() - battle.getStartTime() > 60 * 1000) {
                if (battle.getPlayers().size() <= 1)
                    battle.end(BattleLeaveEvent.Reason.BATTLE_FINISHED);

                if (battle instanceof TeamBattle) {
                    TeamBattle teamBattle = (TeamBattle) battle;
                    if (teamBattle.getTeam1Players().size() == 0 || teamBattle.getTeam2Players().size() == 0)
                        teamBattle.end(BattleLeaveEvent.Reason.BATTLE_FINISHED);
                }
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Battle battle = Battle.get(player);
        if (battle == null) return;

        player.setFoodLevel(20);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Battle battle = Battle.get(player);
        if (battle == null) return;

        battle.remove(player, BattleLeaveEvent.Reason.LOGOUT);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Battle battle = Battle.get(player);
        if (battle == null) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Battle battle = Battle.get(player);
        if (battle == null) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        Battle battle = Battle.get(player);
        if (battle == null) return;

        if (event.getMessage().toLowerCase().startsWith("/gwf") || event.getMessage().toLowerCase().startsWith("/galacticwarfront")) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Battle battle = Battle.get(player);
        if (battle == null) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        Battle battle = Battle.get(player);
        if (battle == null) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCombat(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player victim = (Player) event.getEntity();

        Player attacker = null;
        if (event.getDamager() instanceof Player) attacker = (Player) event.getDamager();
        if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) attacker = (Player) projectile.getShooter();
        }

        if (attacker == null) return;

        Battle battle = Battle.get(attacker);
        if (battle == null) return;
        if (!battle.contains(victim)) return;

        BattleDamageEvent battleDamageEvent = new BattleDamageEvent(battle, attacker, victim, event.getDamage(), event.getCause());
        event.setDamage(0);

        if (battle.sameTeam(attacker, victim) || !battle.canPvP())
            battleDamageEvent.setCancelled(true);

        Bukkit.getPluginManager().callEvent(battleDamageEvent);

        if (battleDamageEvent.isCancelled()) {
            event.setCancelled(true);
        } else {
            event.setDamage(battleDamageEvent.getDamage());
            if (victim.getHealth() - battleDamageEvent.getDamage() <= 0.0) {
                battle.kill(victim, attacker);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player victim = (Player) event.getEntity();

        Battle battle = Battle.get(victim);
        if (battle == null) return;

        switch (event.getCause()) {
            case PROJECTILE:
            case ENTITY_ATTACK:
                return;
        }

        BattleDamageEvent battleDamageEvent = new BattleDamageEvent(battle, null, victim, event.getDamage(), event.getCause());
        event.setDamage(0);

        if (!battle.canPvP())
            battleDamageEvent.setCancelled(true);

        Bukkit.getPluginManager().callEvent(battleDamageEvent);

        if (battleDamageEvent.isCancelled()) {
            event.setCancelled(true);
        } else {
            event.setDamage(battleDamageEvent.getDamage());
            if (victim.getHealth() - battleDamageEvent.getDamage() <= 0.0) {
                battle.kill(victim, null);
                event.setCancelled(true);
            }
        }
    }

}