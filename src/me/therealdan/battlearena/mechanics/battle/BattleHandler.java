package me.therealdan.battlearena.mechanics.battle;

import me.therealdan.battlearena.BattleArena;
import me.therealdan.battlearena.events.BattleDamageEvent;
import me.therealdan.battlearena.events.BattleLeaveEvent;
import me.therealdan.battlearena.mechanics.arena.Arena;
import me.therealdan.battlearena.mechanics.battle.battles.Team;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.*;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BattleHandler implements Listener {

    private static BattleHandler battleHandler;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

    private BattleHandler() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(BattleArena.getInstance(), () -> {

            for (Battle battle : Battle.values()) {
                for (Player player : battle.getPlayers()) {
                    if (battle.getArena().hasBounds()) {
                        if (!battle.getArena().getBounds().contains(player.getLocation())) {
                            if (!battle.getArena().getBounds().getWorld().getName().equals(player.getWorld().getName())) {
                                battle.respawn(player);
                            } else {
                                Arena.Consequence consequence = Arena.Consequence.RESPAWN;
                                if (battle.getArena().getBounds().isAbove(player.getLocation())) consequence = battle.getArena().getTopConsequence();
                                if (battle.getArena().getBounds().isThroughSides(player.getLocation())) consequence = battle.getArena().getSidesConsequence();
                                if (battle.getArena().getBounds().isBelow(player.getLocation())) consequence = battle.getArena().getFloorConsequence();
                                switch (consequence) {
                                    case PUSH:
                                        player.setVelocity(battle.getArena().getBounds().getCenter().toVector().subtract(player.getLocation().toVector()).multiply(0.025));
                                        break;
                                    case DAMAGE:
                                        player.damage(0.2);
                                        break;
                                    case KILL:
                                        battle.kill(player, null, BattleArena.SECOND + player.getName() + BattleArena.MAIN + " tried to flee the battle (" + BattleArena.SECOND + (battle.getKillCounter().getDeaths(player.getUniqueId()) + 1) + BattleArena.MAIN + " deaths)");
                                        break;
                                    case RESPAWN:
                                        battle.respawn(player);
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        }, 1, 1);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(BattleArena.getInstance(), () -> {
            for (Battle battle : Battle.values()) {

                if (battle.getTimeRemaining() <= 0) {
                    battle.end(BattleLeaveEvent.Reason.BATTLE_FINISHED);

                } else if (battle.getPlayers().size() <= 1 && battle.getTimePassed() > 60 * 1000) {
                    battle.end(BattleLeaveEvent.Reason.NOT_ENOUGH_PLAYERS);

                } else if (battle instanceof Team) {
                    Team team = (Team) battle;
                    if (team.getTeam1Players().size() == 0 || team.getTeam2Players().size() == 0)
                        team.end(BattleLeaveEvent.Reason.NOT_ENOUGH_PLAYERS);
                }

                if (battle.getTimeRemainingBar() != null) {
                    Date date = new Date();
                    date.setTime(battle.getGraceTimeRemaining() > 0 ? battle.getGraceTimeRemaining() : battle.getTimeRemaining());

                    BossBar bar = battle.getTimeRemainingBar();
                    bar.setTitle(battle.getGraceTimeRemaining() > 0 ?
                            BattleArena.MAIN + "PvP Starts in: " + BattleArena.SECOND + simpleDateFormat.format(date)
                            : BattleArena.MAIN + "Time Remaining: " + BattleArena.SECOND + simpleDateFormat.format(date)
                    );
                    double progress = battle.getProgress();
                    if (progress > 1.0) progress = 1.0;
                    if (progress < 0.0) progress = 0.0;
                    bar.setProgress(Math.abs(progress - 1.0));
                }
            }
        }, 20, 20);
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
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_AIR)) return;

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

        if (event.getMessage().toLowerCase().startsWith("/ba") || event.getMessage().toLowerCase().startsWith("/battlearena")) return;

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

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Battle battle = Battle.get(player);
        if (battle == null) return;

        if (player.getGameMode().equals(GameMode.SURVIVAL))
            event.setCancelled(true);
    }

    @EventHandler
    public void onEntity(PlayerArmorStandManipulateEvent event) {
        Player player = event.getPlayer();
        Battle battle = Battle.get(player);
        if (battle == null) return;

        if (player.getGameMode().equals(GameMode.SURVIVAL))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCombat(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player victim = (Player) event.getEntity();

        boolean wasProjectile = false;
        Player attacker = null;
        if (event.getDamager() instanceof Player) attacker = (Player) event.getDamager();
        if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) attacker = (Player) projectile.getShooter();
            wasProjectile = true;
        }

        if (attacker == null) return;

        Battle battle = Battle.get(attacker);
        if (battle == null) return;
        if (!battle.contains(victim)) return;

        BattleDamageEvent battleDamageEvent = new BattleDamageEvent(battle, attacker, victim, event.getDamage(), event.getCause());
        event.setDamage(0);

        if (battle.sameTeam(attacker, victim) || !battle.canPvP() || !attacker.getGameMode().equals(GameMode.SURVIVAL) || attacker.equals(victim))
            battleDamageEvent.setCancelled(true);

        Bukkit.getPluginManager().callEvent(battleDamageEvent);

        if (battleDamageEvent.isCancelled()) {
            event.setCancelled(true);
        } else {
            event.setDamage(battleDamageEvent.getDamage());
            if (victim.getHealth() - battleDamageEvent.getDamage() <= 0.0) {
                battle.kill(victim, attacker);
                event.setCancelled(true);
            } else if (wasProjectile) {
                event.setCancelled(true);
                victim.damage(battleDamageEvent.getDamage(), attacker);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player victim = (Player) event.getEntity();

        Battle battle = Battle.get(victim);
        if (battle == null) return;

        Player attacker = null;
        switch (event.getCause()) {
            case ENTITY_ATTACK:
            case PROJECTILE:
                return;
            case FIRE_TICK:
                attacker = victim.getKiller();
                break;
            case FALL:
                attacker = victim.getKiller();
                break;
        }

        BattleDamageEvent battleDamageEvent = new BattleDamageEvent(battle, attacker, victim, event.getDamage(), event.getCause());
        event.setDamage(0);

        if (!battle.canPvP())
            battleDamageEvent.setCancelled(true);

        Bukkit.getPluginManager().callEvent(battleDamageEvent);

        if (battleDamageEvent.isCancelled()) {
            event.setCancelled(true);
        } else {
            event.setDamage(battleDamageEvent.getDamage());
            if (victim.getHealth() - battleDamageEvent.getDamage() <= 0.0) {
                battle.kill(victim, battleDamageEvent.getAttacker());
                event.setCancelled(true);
            }
        }
    }

    public static BattleHandler getInstance() {
        if (battleHandler == null) battleHandler = new BattleHandler();
        return battleHandler;
    }
}