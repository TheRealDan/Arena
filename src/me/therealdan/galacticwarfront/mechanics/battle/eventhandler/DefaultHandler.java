package me.therealdan.galacticwarfront.mechanics.battle.eventhandler;

import me.therealdan.galacticwarfront.GalacticWarFront;
import me.therealdan.galacticwarfront.events.*;
import me.therealdan.galacticwarfront.mechanics.battle.battle.TeamBattle;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Random;

public class DefaultHandler implements BattleListener {

    private HashSet<Item> blood = new HashSet<>();

    private Scoreboard scoreboard;

    private int up = 0;
    private int hold = 40;
    private int down = 20;

    public DefaultHandler() {
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(GalacticWarFront.getInstance(), new Runnable() {
            @Override
            public void run() {
                task();
            }
        }, 20, 1);
    }

    private void task() {
        for (Item blood : this.blood)
            if (blood.getTicksLived() > 20)
                blood.remove();
    }

    public void unload() {
        for (Item blood : this.blood)
            blood.remove();
    }

    @EventHandler
    public void onStart(BattleStartEvent event) {
        if (!event.getBattle().isOpen()) return;

        String title = GalacticWarFront.SECOND + event.getBattle().getArena().getName();
        String sub = GalacticWarFront.SECOND + event.getStarted().getName() + GalacticWarFront.MAIN + " has started a " + GalacticWarFront.SECOND + event.getBattle().getType();

        for (Player player : Bukkit.getOnlinePlayers())
            if (GalacticWarFront.getInstance().getLobby().contains(player))
                sendTitle(player, title, sub);
    }

    @EventHandler
    public void onFinish(BattleFinishEvent event) {
        Player mostKills = Bukkit.getPlayer(event.getBattle().getKillCounter().getMostKills());
        if (mostKills == null) return;

        String title = GalacticWarFront.SECOND + mostKills.getName();
        String sub = GalacticWarFront.MAIN + "Killed the most players, with " + GalacticWarFront.SECOND + event.getBattle().getKillCounter().getKills(mostKills.getUniqueId()) + GalacticWarFront.MAIN + " kills";

        for (Player player : event.getBattle().getPlayers())
            sendTitle(player, title, sub);
    }

    @EventHandler
    public void onJoin(BattleJoinEvent event) {
        String title = GalacticWarFront.SECOND + event.getPlayer().getName();
        String sub = GalacticWarFront.MAIN + "Has joined the " + GalacticWarFront.SECOND + event.getBattle().getType();

        if (event.getBattle() instanceof TeamBattle) {
            TeamBattle teamBattle = (TeamBattle) event.getBattle();
            sub = GalacticWarFront.MAIN + "Has joined " + GalacticWarFront.SECOND + "Team " + (teamBattle.isTeam1(event.getPlayer()) ? "1" : "2");
        }

        for (Player player : event.getBattle().getPlayers())
            sendTitle(player, title, sub);

        update(event.getPlayer());
    }

    @EventHandler
    public void onLeave(BattleLeaveEvent event) {
        switch (event.getReason()) {
            case LEAVE:
            case LOGOUT:
                event.getBattle().getKillCounter().remove(event.getPlayer().getUniqueId());
                break;

            case BATTLE_FINISHED:
                String title = GalacticWarFront.SECOND + event.getPlayer().getName();
                String sub = GalacticWarFront.MAIN + "Has left the " + GalacticWarFront.SECOND + event.getBattle().getType();

                for (Player player : event.getBattle().getPlayers())
                    sendTitle(player, title, sub);
                break;
        }
    }

    @EventHandler
    public void onDamage(BattleDamageEvent event) {
        Player attacker = event.getAttacker();

        if (attacker != null)
            attacker.playSound(attacker.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
    }

    @EventHandler
    public void onDeath(BattleDeathEvent event) {
        Player victim = event.getPlayer();
        Player killer = event.getKiller();

        if (killer != null && victim != killer) event.getBattle().getKillCounter().addKill(killer.getUniqueId());
        event.getBattle().getKillCounter().addDeath(victim.getUniqueId());

        Location origin = victim.getLocation();

        double spread = 0.2;
        double height = 0.5;
        for (int i = 0; i < new Random().nextInt(20) + 10; i++) {
            Item blood = origin.getWorld().dropItem(origin, new ItemStack(Material.INK_SACK, 1, (short) 1));
            blood.setPickupDelay(Integer.MAX_VALUE);
            blood.setVelocity(new Vector(
                    (new Random().nextDouble() + -new Random().nextDouble()) * spread,
                    (new Random().nextDouble() * height) + 0.2,
                    (new Random().nextDouble() + -new Random().nextDouble()) * spread));
            this.blood.add(blood);
        }

        String title = GalacticWarFront.SECOND + victim.getName();
        String sub = killer != null ?
                GalacticWarFront.MAIN + "Was killed by " + GalacticWarFront.SECOND + killer.getName() + GalacticWarFront.MAIN + " (" + GalacticWarFront.SECOND + event.getBattle().getKillCounter().getKills(killer.getUniqueId()) + GalacticWarFront.MAIN + " kills)" :
                GalacticWarFront.MAIN + "Killed themselves.";

        for (Player player : event.getBattle().getPlayers())
            sendTitle(player, title, sub);
    }

    @EventHandler
    public void onRespawn(BattleRespawnEvent event) {

        event.getPlayer().playSound(event.getRespawnLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1);
    }

    private void sendTitle(Player player, String title, String sub) {
        player.sendTitle(title, sub, up, hold, down);
    }

    private void update(Player player) {
        String name = player.getName();

        Team team = getScoreboard().getTeam(name) == null ? getScoreboard().registerNewTeam(name) : getScoreboard().getTeam(name);

        team.setPrefix("");
        team.setSuffix("");
        team.setDisplayName("A");

        if (!team.hasEntry(name))
            team.addEntry(name);

        player.setScoreboard(getScoreboard());
    }

    private Scoreboard getScoreboard() {
        return scoreboard;
    }
}