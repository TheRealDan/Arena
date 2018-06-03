package me.therealdan.galacticwarfront.mechanics.battle;

import me.therealdan.galacticwarfront.GalacticWarFront;
import me.therealdan.galacticwarfront.events.BattleDamageEvent;
import me.therealdan.galacticwarfront.events.BattleLeaveEvent;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
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
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BattleHandler implements Listener {

    private static BattleHandler battleHandler;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

    private BattleHandler() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(GalacticWarFront.getInstance(), () -> {
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
                            GalacticWarFront.MAIN + "PvP Starts in: " + GalacticWarFront.SECOND + simpleDateFormat.format(date)
                            : GalacticWarFront.MAIN + "Time Remaining: " + GalacticWarFront.SECOND + simpleDateFormat.format(date)
                    );
                    double progress = battle.getProgress();
                    if (progress > 1.0) progress = 1.0;
                    if (progress < 0.0) progress = 0.0;
                    bar.setProgress(progress);
                }

                if (battle.getScoreboard() != null) {
                    Scoreboard scoreboard = battle.getScoreboard();
                    if (battle instanceof FFA) {
                        Objective objective = scoreboard.registerNewObjective("dummy", "title");
                        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                        objective.setDisplayName(battle.getArena().getName() + GalacticWarFront.SECOND + " - " + battle.getType().name());

                        int i = 0;
                        for (Player player : battle.getPlayers())
                            objective.getScore(
                                    GalacticWarFront.MAIN + player.getName()
                                            + GalacticWarFront.MAIN + " - KDR: " + GalacticWarFront.SECOND + battle.getKillCounter().getKDRString(player.getUniqueId())
                                            + GalacticWarFront.MAIN + " - Kills: " + GalacticWarFront.SECOND + battle.getKillCounter().getKills(player.getUniqueId())
                                            + GalacticWarFront.MAIN + " - Deaths: " + GalacticWarFront.SECOND + battle.getKillCounter().getDeaths(player.getUniqueId())
                            ).setScore(i++);

                        for (Player player : battle.getPlayers())
                            player.setScoreboard(scoreboard);
                    }
                }
            }
        }, 20, 20);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Battle battle = Battle.get(player);
        if (battle == null) return;

        player.setNoDamageTicks(0);
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

    public static BattleHandler getInstance() {
        if (battleHandler == null) battleHandler = new BattleHandler();
        return battleHandler;
    }
}