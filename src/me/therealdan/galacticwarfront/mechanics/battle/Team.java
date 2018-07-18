package me.therealdan.galacticwarfront.mechanics.battle;

import me.therealdan.galacticwarfront.GalacticWarFront;
import me.therealdan.galacticwarfront.events.*;
import me.therealdan.galacticwarfront.mechanics.arena.Arena;
import me.therealdan.galacticwarfront.mechanics.killcounter.KillCounter;
import me.therealdan.galacticwarfront.mechanics.lobby.Lobby;
import me.therealdan.galacticwarfront.util.PlayerHandler;
import me.therealdan.party.Party;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class Team implements Battle {

    private static HashSet<Team> teams = new HashSet<>();

    private Arena arena;
    private KillCounter killCounter;
    private long gracePeriod = 0;
    private long battleDuration = 0;
    private boolean open;

    private HashSet<UUID> team1 = new HashSet<>();
    private HashSet<UUID> team2 = new HashSet<>();
    private long startTime = System.currentTimeMillis();

    private Scoreboard scoreboard;
    private BossBar timeRemainingBar;

    public Team(Arena arena, Player started, Party party) {
        this.arena = arena;
        if (party != null) this.open = party.isOpen();

        if (party != null)
            for (Player player : party.getPlayers())
                add(player, party.isTeam(player, 1));

        BattleStartEvent event = new BattleStartEvent(this, started);
        event.setBattleMessage(GalacticWarFront.MAIN + "Your " + GalacticWarFront.SECOND + "Team Battle" + GalacticWarFront.MAIN + " on " + GalacticWarFront.SECOND + arena.getName() + GalacticWarFront.MAIN + " has begun.");
        if (isOpen()) event.setLobbyMessage(GalacticWarFront.SECOND + started.getName() + GalacticWarFront.MAIN + " has started a " + GalacticWarFront.SECOND + "Team Battle" + GalacticWarFront.MAIN + " on " + GalacticWarFront.SECOND + arena.getName());
        Bukkit.getPluginManager().callEvent(event);

        if (event.getPlayerMessage() != null)
            started.sendMessage(event.getPlayerMessage());

        if (event.getBattleMessage() != null)
            for (Player player : getPlayers())
                player.sendMessage(event.getBattleMessage());

        if (event.getLobbyMessage() != null)
            for (Player player : Lobby.getInstance().getPlayers())
                player.sendMessage(event.getLobbyMessage());

        teams.add(this);
    }

    @Override
    public void end(BattleLeaveEvent.Reason reason) {
        if (!teams.contains(this)) return;

        int team1Kills = getTotalKills(true);
        int team2Kills = getTotalKills(false);
        int mostKills = Math.max(team1Kills, team2Kills);
        String mostKillsTeam = team1Kills >= team2Kills ? "Team 1" : "Team 2";

        BattleFinishEvent event = new BattleFinishEvent(this);
        if (mostKills > 0) event.setBattleMessage(GalacticWarFront.SECOND + mostKillsTeam + GalacticWarFront.MAIN + " got the most kills, with " + GalacticWarFront.SECOND + mostKills + GalacticWarFront.MAIN + " kills.");
        Bukkit.getPluginManager().callEvent(event);

        if (event.getBattleMessage() != null)
            for (Player player : getPlayers())
                player.sendMessage(event.getBattleMessage());

        if (event.getLobbyMessage() != null)
            for (Player player : Lobby.getInstance().getPlayers())
                player.sendMessage(event.getLobbyMessage());

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

        BattleJoinEvent event = new BattleJoinEvent(this, player);
        event.setBattleMessage(GalacticWarFront.SECOND + player.getName() + GalacticWarFront.MAIN + " has joined " + GalacticWarFront.SECOND + "Team " + (isTeam1(player) ? "1" : "2"));
        Bukkit.getPluginManager().callEvent(event);

        if (event.getBattleMessage() != null)
            for (Player each : getPlayers())
                each.sendMessage(event.getBattleMessage());

        if (team1) {
            this.team1.add(player.getUniqueId());
        } else {
            this.team2.add(player.getUniqueId());
        }

        player.setScoreboard(getScoreboard() != null ? getScoreboard() : Bukkit.getScoreboardManager().getNewScoreboard());
        if (getTimeRemainingBar() != null) getTimeRemainingBar().addPlayer(player);

        respawn(player);
    }

    @Override
    public void remove(Player player, BattleLeaveEvent.Reason reason) {
        BattleLeaveEvent event = new BattleLeaveEvent(this, player, reason, Lobby.getInstance().getSpawnpoint());
        switch (reason) {
            case LEAVE:
            case LOGOUT:
                event.setBattleMessage(GalacticWarFront.SECOND + player.getName() + GalacticWarFront.MAIN + " has left the " + GalacticWarFront.SECOND + getType().name());
                break;
        }
        Bukkit.getPluginManager().callEvent(event);

        if (event.getBattleMessage() != null)
            for (Player each : getPlayers())
                each.sendMessage(event.getBattleMessage());

        player.teleport(event.getSpawn());
        PlayerHandler.refresh(player);

        this.team1.remove(player.getUniqueId());
        this.team2.remove(player.getUniqueId());

        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        if (getTimeRemainingBar() != null) getTimeRemainingBar().removePlayer(player);
    }

    @Override
    public void kill(Player player, Player killer) {
        getKillCounter().addDeath(player.getUniqueId());
        if (killer != null) getKillCounter().addKill(killer.getUniqueId());

        BattleDeathEvent event = new BattleDeathEvent(this, player, killer);
        event.setBattleMessage(killer != null ?
                GalacticWarFront.SECOND + player.getName() + GalacticWarFront.MAIN + " was killed by " + GalacticWarFront.SECOND + killer.getName() :
                GalacticWarFront.SECOND + player.getName() + GalacticWarFront.MAIN + " killed themselves."
        );
        Bukkit.getPluginManager().callEvent(event);

        if (event.getBattleMessage() != null)
            for (Player each : getPlayers())
                each.sendMessage(event.getBattleMessage());

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
    public long getGraceDuration() {
        return gracePeriod - getStartTime();
    }

    @Override
    public long getBattleDuration() {
        return battleDuration - getStartTime() - getGraceDuration();
    }

    @Override
    public double getProgress() {
        if (getGraceTimeRemaining() > 0) {
            return (double) getTimePassed() / (double) getGraceDuration();
        } else {
            return ((double) getTimePassed() - (double) getGraceDuration()) / (double) getBattleDuration();
        }
    }

    public int getTotalKills(boolean team1) {
        int totalKills = 0;
        for (Player player : team1 ? getTeam1Players() : getTeam2Players())
            totalKills += getKillCounter().getKills(player.getUniqueId());
        return totalKills;
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

    @Override
    public Scoreboard getScoreboard() {
        if (scoreboard == null) scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        return scoreboard;
    }

    @Override
    public BossBar getTimeRemainingBar() {
        if (timeRemainingBar == null) timeRemainingBar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);
        return timeRemainingBar;
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
