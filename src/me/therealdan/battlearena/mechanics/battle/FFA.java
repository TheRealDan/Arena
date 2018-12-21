package me.therealdan.battlearena.mechanics.battle;

import me.therealdan.battlearena.BattleArena;
import me.therealdan.battlearena.events.*;
import me.therealdan.battlearena.mechanics.arena.Arena;
import me.therealdan.battlearena.mechanics.killcounter.KillCounter;
import me.therealdan.battlearena.mechanics.lobby.Lobby;
import me.therealdan.battlearena.util.PlayerHandler;
import me.therealdan.party.Party;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class FFA implements Battle {

    private static HashSet<FFA> ffas = new HashSet<>();

    private Arena arena;
    private KillCounter killCounter;
    private long gracePeriod = 0;
    private long battleDuration = 0;
    private boolean open = false;

    private HashSet<UUID> players = new HashSet<>();
    private long startTime = System.currentTimeMillis();

    private BossBar timeRemainingBar;

    public FFA(Arena arena, Player started, Party party) {
        this.arena = arena;
        if (party != null) this.open = party.isOpen();

        add(started);
        if (party != null)
            for (Player player : party.getPlayers())
                add(player);

        BattleStartEvent event = new BattleStartEvent(this, started);
        event.setBattleMessage(BattleArena.MAIN + "Your " + BattleArena.SECOND + "FFA" + BattleArena.MAIN + " on " + BattleArena.SECOND + arena.getName() + BattleArena.MAIN + " has begun.");
        if (isOpen()) event.setLobbyMessage(BattleArena.SECOND + started.getName() + BattleArena.MAIN + " has started an " + BattleArena.SECOND + "FFA" + BattleArena.MAIN + " on " + BattleArena.SECOND + arena.getName());
        Bukkit.getPluginManager().callEvent(event);

        if (event.getPlayerMessage() != null)
            started.sendMessage(event.getPlayerMessage());

        if (event.getBattleMessage() != null)
            for (Player player : getPlayers())
                player.sendMessage(event.getBattleMessage());

        if (event.getLobbyMessage() != null)
            for (Player player : Lobby.getInstance().getPlayers())
                player.sendMessage(event.getLobbyMessage());

        ffas.add(this);
    }

    @Override
    public void end(BattleLeaveEvent.Reason reason) {
        if (!ffas.contains(this)) return;

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

        ffas.remove(this);
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
                BattleArena.SECOND + player.getName() + BattleArena.MAIN + " (" + BattleArena.SECOND + getKillCounter().getDeaths(player.getUniqueId()) + BattleArena.MAIN + " deaths) was killed by " + BattleArena.SECOND + killer.getName() + BattleArena.MAIN + " (" + BattleArena.SECOND + getKillCounter().getKills(killer.getUniqueId()) + BattleArena.MAIN + " kills)" :
                BattleArena.SECOND + player.getName() + BattleArena.MAIN + " killed themselves. (" + BattleArena.SECOND + getKillCounter().getDeaths(player.getUniqueId()) + BattleArena.MAIN + " deaths)"
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

        BattleRespawnEvent battleRespawnEvent = new BattleRespawnEvent(this, player, getRandomSpawnpoint());
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
        this.open = open;
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

    @Override
    public Type getType() {
        return Type.FFA;
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
