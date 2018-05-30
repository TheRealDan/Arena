package me.therealdan.galacticwarfront.mechanics.battle.battle;

import me.therealdan.galacticwarfront.GalacticWarFront;
import me.therealdan.galacticwarfront.events.*;
import me.therealdan.galacticwarfront.mechanics.battle.Arena;
import me.therealdan.galacticwarfront.mechanics.killcounter.KillCounter;
import me.therealdan.galacticwarfront.mechanics.lobby.Lobby;
import me.therealdan.galacticwarfront.util.PlayerHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

public class Team implements Battle {

    private static HashSet<Team> teams = new HashSet<>();

    private Arena arena;
    private KillCounter killCounter;
    private long gracePeriod = 0;
    private long battleDuration = 0;
    private boolean open = false;

    private HashSet<UUID> team1 = new HashSet<>();
    private HashSet<UUID> team2 = new HashSet<>();
    private long startTime = System.currentTimeMillis();

    public Team(Arena arena, Player started) {
        this.arena = arena;

        BattleStartEvent battleStartEvent = new BattleStartEvent(this, started);
        Bukkit.getPluginManager().callEvent(battleStartEvent);

        add(started);

        teams.add(this);
    }

    @Override
    public void end(BattleLeaveEvent.Reason reason) {
        if (!teams.contains(this)) return;

        BattleFinishEvent battleFinishEvent = new BattleFinishEvent(this);
        Bukkit.getPluginManager().callEvent(battleFinishEvent);

        for (Player player : getPlayers())
            remove(player, BattleLeaveEvent.Reason.BATTLE_FINISHED);

        teams.remove(this);
    }

    @Override
    public void add(Player player) {
        add(player, !(team1.size() > team2.size()));
    }

    public void add(Player player, boolean team1) {
        if (contains(player)) return;

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
        BattleLeaveEvent battleLeaveEvent = new BattleLeaveEvent(this, player, reason, Lobby.getInstance().getSpawnpoint());
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
        this.gracePeriod = System.currentTimeMillis() + (secondsStartingNow * 1000);
    }

    @Override
    public void setTimeRemaining(long secondsStartingNow) {
        this.battleDuration = System.currentTimeMillis() + (secondsStartingNow * 1000);
    }

    @Override
    public void setOpen(boolean open) {
        this.open = open;
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
        return getGraceTimeRemaining() <= 0;
    }

    @Override
    public long getTimePassed() {
        return System.currentTimeMillis() - getStartTime();
    }

    @Override
    public long getGraceTimeRemaining() {
        return Math.max(gracePeriod - System.currentTimeMillis(), 0);
    }

    @Override
    public long getTimeRemaining() {
        return Math.max(battleDuration - System.currentTimeMillis(), 0);
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public Type getType() {
        return Type.Team;
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

    public static Team get(Player player) {
        for (Team team : values())
            if (team.contains(player))
                return team;
        return null;
    }

    public static List<Team> values() {
        return new ArrayList<>(teams);
    }
}
