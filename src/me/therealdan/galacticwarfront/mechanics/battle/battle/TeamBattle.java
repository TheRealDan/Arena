package me.therealdan.galacticwarfront.mechanics.battle.battle;

import me.therealdan.galacticwarfront.GalacticWarFront;
import me.therealdan.galacticwarfront.events.*;
import me.therealdan.galacticwarfront.mechanics.battle.Arena;
import me.therealdan.galacticwarfront.mechanics.killcounter.KillCounter;
import me.therealdan.galacticwarfront.util.PlayerHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

public class TeamBattle implements Battle {

    private static HashSet<TeamBattle> teamBattles = new HashSet<>();

    private Arena arena;
    private KillCounter killCounter;
    private long gracePeriod = 0;
    private boolean open = false;

    private HashSet<UUID> team1 = new HashSet<>();
    private HashSet<UUID> team2 = new HashSet<>();
    private long startTime = System.currentTimeMillis();

    public TeamBattle(Arena arena, Player started) {
        this.arena = arena;

        BattleStartEvent battleStartEvent = new BattleStartEvent(this, started);
        Bukkit.getPluginManager().callEvent(battleStartEvent);

        teamBattles.add(this);
    }

    @Override
    public void end(BattleLeaveEvent.Reason reason) {
        if (!teamBattles.contains(this)) return;

        BattleFinishEvent battleFinishEvent = new BattleFinishEvent(this);
        Bukkit.getPluginManager().callEvent(battleFinishEvent);

        for (Player player : getPlayers())
            remove(player, BattleLeaveEvent.Reason.BATTLE_FINISHED);

        teamBattles.remove(this);
    }

    @Override
    public void add(Player player) {
        add(player, !(team1.size() > team2.size()));
    }

    public void add(Player player, boolean team1) {
        Battle battle = Battle.get(player);
        if (battle != null) battle.remove(player, BattleLeaveEvent.Reason.LEAVE);

        if (team1) {
            this.team1.add(player.getUniqueId());
        } else {
            this.team2.add(player.getUniqueId());
        }

        BattleJoinEvent battleJoinEvent = new BattleJoinEvent(this, player);
        Bukkit.getPluginManager().callEvent(battleJoinEvent);

        respawn(player);
    }

    @Override
    public void remove(Player player, BattleLeaveEvent.Reason reason) {
        BattleLeaveEvent battleLeaveEvent = new BattleLeaveEvent(this, player, reason, GalacticWarFront.getInstance().getLobby().getSpawnpoint());
        Bukkit.getPluginManager().callEvent(battleLeaveEvent);

        player.teleport(battleLeaveEvent.getSpawn());

        this.team1.remove(player.getUniqueId());
        this.team2.remove(player.getUniqueId());
    }

    @Override
    public void kill(Player player, Player killer) {
        BattleDeathEvent battleDeathEvent = new BattleDeathEvent(this, player, killer);
        Bukkit.getPluginManager().callEvent(battleDeathEvent);

        respawn(player);
    }

    @Override
    public void respawn(Player player) {
        if (!contains(player)) return;

        BattleRespawnEvent battleRespawnEvent = new BattleRespawnEvent(this, player, getRandomSpawnpoint(player));
        Bukkit.getPluginManager().callEvent(battleRespawnEvent);

        for (Player target : getPlayers())
            target.hidePlayer(GalacticWarFront.getInstance(), player);
        player.teleport(battleRespawnEvent.getRespawnLocation());
        for (Player target : getPlayers())
            target.showPlayer(GalacticWarFront.getInstance(), player);

        PlayerHandler.refresh(player);
    }

    @Override
    public void setGracePeriod(long secondsStartingNow) {
        long secondsPassed = System.currentTimeMillis() - getStartTime() / 1000;
        this.gracePeriod = secondsPassed + secondsStartingNow;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public boolean contains(Player player) {
        return team1.contains(player.getUniqueId()) || team2.contains(player.getUniqueId());
    }

    public boolean isTeam1(Player player) {
        return team1.contains(player.getUniqueId());
    }

    @Override
    public boolean sameTeam(Player player, Player player1) {
        return isTeam1(player) == isTeam1(player1);
    }

    @Override
    public boolean canPvP() {
        if (System.currentTimeMillis() - getStartTime() < getGracePeriod() * 1000) return false;

        return true;
    }

    @Override
    public long getGracePeriod() {
        return gracePeriod;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public String getType() {
        return "Team Battle";
    }

    @Override
    public Location getRandomSpawnpoint(Player player) {
        List<Location> spawnpoints = isTeam1(player) ? getArena().getTeam1Spawnpoints() : getArena().getTeam2Spawnpoints();

        int checks = 0;
        Location location = null;
        while (checks < 10) {
            boolean safe = true;
            location = spawnpoints.get(new Random().nextInt(spawnpoints.size()));
            for (Entity entity : location.getWorld().getNearbyEntities(location, SPAWN_RANGE, SPAWN_RANGE, SPAWN_RANGE))
                if (entity instanceof Player)
                    if (!sameTeam(player, (Player) entity))
                        safe = false;
            if (safe) return location;
            checks++;
        }

        return location;
    }

    @Override
    public KillCounter getKillCounter() {
        if (killCounter == null) killCounter = new KillCounter();
        return killCounter;
    }

    @Override
    public Arena getArena() {
        return arena;
    }

    public List<Player> getTeam1Players() {
        List<Player> players = new ArrayList<>();
        for (UUID uuid : team1)
            players.add(Bukkit.getPlayer(uuid));
        return players;
    }

    public List<Player> getTeam2Players() {
        List<Player> players = new ArrayList<>();
        for (UUID uuid : team2)
            players.add(Bukkit.getPlayer(uuid));
        return players;
    }

    @Override
    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();
        players.addAll(getTeam1Players());
        players.addAll(getTeam2Players());
        return players;
    }

    public static TeamBattle get(Player player) {
        for (TeamBattle teamBattle : values())
            if (teamBattle.contains(player))
                return teamBattle;
        return null;
    }

    public static List<TeamBattle> values() {
        return new ArrayList<>(teamBattles);
    }
}
