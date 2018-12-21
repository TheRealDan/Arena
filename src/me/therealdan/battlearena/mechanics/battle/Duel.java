package me.therealdan.battlearena.mechanics.battle;

import me.therealdan.battlearena.BattleArena;
import me.therealdan.battlearena.events.*;
import me.therealdan.battlearena.mechanics.arena.Arena;
import me.therealdan.battlearena.mechanics.killcounter.KillCounter;
import me.therealdan.battlearena.mechanics.lobby.Lobby;
import me.therealdan.battlearena.util.PlayerHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class Duel implements Battle {

    private static HashSet<Duel> duels = new HashSet<>();

    private Arena arena;
    private KillCounter killCounter;
    private UUID player1, player2;
    private long gracePeriod = 0;
    private long battleDuration = 0;

    private HashSet<UUID> players = new HashSet<>();
    private long startTime = System.currentTimeMillis();

    private BossBar timeRemainingBar;

    public Duel(Arena arena, Player player1, Player player2) {
        this.arena = arena;
        this.player1 = player1.getUniqueId();
        this.player2 = player2.getUniqueId();

        add(player1);
        add(player2);

        BattleStartEvent event = new BattleStartEvent(this, player1);
        event.setBattleMessage(BattleArena.MAIN + "Your " + BattleArena.SECOND + "Duel" + BattleArena.MAIN + " on " + BattleArena.SECOND + arena.getName() + BattleArena.MAIN + " has begun.");
        Bukkit.getPluginManager().callEvent(event);

        if (event.getPlayerMessage() != null)
            player1.sendMessage(event.getPlayerMessage());

        if (event.getBattleMessage() != null)
            for (Player player : getPlayers())
                player.sendMessage(event.getBattleMessage());

        if (event.getLobbyMessage() != null)
            for (Player player : Lobby.getInstance().getPlayers())
                player.sendMessage(event.getLobbyMessage());

        duels.add(this);
    }

    @Override
    public void end(BattleLeaveEvent.Reason reason) {
        if (!duels.contains(this)) return;

        OfflinePlayer mostKills = getKillCounter().getMostKills() != null ? Bukkit.getOfflinePlayer(getKillCounter().getMostKills()) : null;

        BattleFinishEvent event = new BattleFinishEvent(this);
        if (mostKills != null) event.setBattleMessage(BattleArena.SECOND + mostKills.getName() + BattleArena.MAIN + " got the most kills, with " + getKillCounter().getKills(mostKills.getUniqueId()) + BattleArena.MAIN + " kills.");
        Bukkit.getPluginManager().callEvent(event);

        if (event.getBattleMessage() != null)
            for (Player player : getPlayers())
                player.sendMessage(event.getBattleMessage());

        if (event.getLobbyMessage() != null)
            for (Player player : Lobby.getInstance().getPlayers())
                player.sendMessage(event.getLobbyMessage());

        for (Player player : getPlayers())
            remove(player, BattleLeaveEvent.Reason.BATTLE_FINISHED);

        duels.remove(this);
    }

    @Override
    public void add(Player player) {
        if (contains(player)) return;

        Battle battle = Battle.get(player);
        if (battle != null) battle.remove(player, BattleLeaveEvent.Reason.LEAVE);

        BattleJoinEvent event = new BattleJoinEvent(this, player);
        event.setBattleMessage(BattleArena.SECOND + player.getName() + BattleArena.MAIN + " has joined the " + BattleArena.SECOND + getType().name());
        Bukkit.getPluginManager().callEvent(event);

        if (event.getBattleMessage() != null)
            for (Player each : getPlayers())
                each.sendMessage(event.getBattleMessage());

        this.players.add(player.getUniqueId());

        if (getTimeRemainingBar() != null) getTimeRemainingBar().addPlayer(player);

        respawn(player);
    }

    @Override
    public void remove(Player player, BattleLeaveEvent.Reason reason) {
        BattleLeaveEvent event = new BattleLeaveEvent(this, player, reason, Lobby.getInstance().getSpawnpoint());
        switch (reason) {
            case LEAVE:
            case LOGOUT:
                event.setBattleMessage(BattleArena.SECOND + player.getName() + BattleArena.MAIN + " has left the " + BattleArena.SECOND + getType().name());
                break;
        }
        Bukkit.getPluginManager().callEvent(event);

        if (event.getBattleMessage() != null)
            for (Player each : getPlayers())
                each.sendMessage(event.getBattleMessage());

        player.teleport(event.getSpawn());
        PlayerHandler.refresh(player);

        this.players.remove(player.getUniqueId());

        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        if (getTimeRemainingBar() != null) getTimeRemainingBar().removePlayer(player);
    }

    @Override
    public void kill(Player player, Player killer) {
        getKillCounter().addDeath(player.getUniqueId());
        if (killer != null) getKillCounter().addKill(killer.getUniqueId());

        BattleDeathEvent event = new BattleDeathEvent(this, player, killer);
        event.setBattleMessage(killer != null ?
                BattleArena.SECOND + player.getName() + BattleArena.MAIN + " was killed by " + BattleArena.SECOND + killer.getName() :
                BattleArena.SECOND + player.getName() + BattleArena.MAIN + " killed themselves."
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
            target.hidePlayer(BattleArena.getInstance(), player);
        player.teleport(battleRespawnEvent.getRespawnLocation());
        for (Player target : getPlayers())
            target.showPlayer(BattleArena.getInstance(), player);

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
        // Duel's only support two players, and will never be open - See isOpen()
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean contains(Player player) {
        return this.players.contains(player.getUniqueId());
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

    public Player getPlayer1() {
        return Bukkit.getPlayer(player1);
    }

    public Player getPlayer2() {
        return Bukkit.getPlayer(player2);
    }

    @Override
    public Type getType() {
        return Type.Duel;
    }

    public Location getRandomSpawnpoint(Player player) {
        List<Location> spawnpoints = getPlayer1() == player ? getArena().getTeam1Spawnpoints() : getArena().getTeam2Spawnpoints();
        if (spawnpoints.size() == 0) spawnpoints = getArena().getSpawnpoints();

        return getRandomSpawnpoint(spawnpoints);
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
    public BossBar getTimeRemainingBar() {
        if (timeRemainingBar == null) timeRemainingBar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);
        return timeRemainingBar;
    }

    @Override
    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();
        for (UUID uuid : this.players)
            players.add(Bukkit.getPlayer(uuid));
        return players;
    }

    public static Duel get(Player player) {
        for (Duel duel : values())
            if (duel.contains(player))
                return duel;
        return null;
    }

    public static List<Duel> values() {
        return new ArrayList<>(duels);
    }
}