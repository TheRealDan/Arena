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

public class FFA implements Battle {

    private static HashSet<FFA> ffas = new HashSet<>();

    private Arena arena;
    private KillCounter killCounter;
    private long gracePeriod = 0;
    private long battleDuration = 0;
    private boolean open = true;

    private HashSet<UUID> players = new HashSet<>();
    private long startTime = System.currentTimeMillis();

    public FFA(Arena arena, Player started) {
        this.arena = arena;

        BattleStartEvent battleStartEvent = new BattleStartEvent(this, started);
        Bukkit.getPluginManager().callEvent(battleStartEvent);

        ffas.add(this);
    }

    @Override
    public void end(BattleLeaveEvent.Reason reason) {
        if (!ffas.contains(this)) return;

        BattleFinishEvent battleFinishEvent = new BattleFinishEvent(this);
        Bukkit.getPluginManager().callEvent(battleFinishEvent);

        for (Player player : getPlayers())
            remove(player, BattleLeaveEvent.Reason.BATTLE_FINISHED);

        ffas.remove(this);
    }

    @Override
    public void add(Player player) {
        if (contains(player)) return;

        Battle battle = Battle.get(player);
        if (battle != null) battle.remove(player, BattleLeaveEvent.Reason.LEAVE);

        this.players.add(player.getUniqueId());

        BattleJoinEvent battleJoinEvent = new BattleJoinEvent(this, player);
        Bukkit.getPluginManager().callEvent(battleJoinEvent);

        respawn(player);
    }

    @Override
    public void remove(Player player, BattleLeaveEvent.Reason reason) {
        BattleLeaveEvent battleLeaveEvent = new BattleLeaveEvent(this, player, reason, Lobby.getInstance().getSpawnpoint());
        Bukkit.getPluginManager().callEvent(battleLeaveEvent);

        player.teleport(battleLeaveEvent.getSpawn());

        this.players.remove(player.getUniqueId());
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
    public boolean isOpen() {
        return open;
    }

    @Override
    public boolean contains(Player player) {
        return this.players.contains(player.getUniqueId());
    }

    @Override
    public boolean sameTeam(Player player, Player player1) {
        return false;
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
        return Type.FFA;
    }

    @Override
    public Location getRandomSpawnpoint(Player player) {
        List<Location> spawnpoints = getArena().getSpawnpoints();

        int checks = 0;
        Location location = null;
        while (checks < 10) {
            boolean safe = true;
            location = spawnpoints.get(new Random().nextInt(spawnpoints.size()));
            for (Entity entity : location.getWorld().getNearbyEntities(location, SPAWN_RANGE, SPAWN_RANGE, SPAWN_RANGE))
                if (entity instanceof Player)
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
    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();
        for (UUID uuid : this.players)
            players.add(Bukkit.getPlayer(uuid));
        return players;
    }

    public static FFA get(Player player) {
        for (FFA ffa : values())
            if (ffa.contains(player))
                return ffa;
        return null;
    }

    public static List<FFA> values() {
        return new ArrayList<>(ffas);
    }
}
