package me.therealdan.galacticwarfront.mechanics.battle;

import me.therealdan.galacticwarfront.GalacticWarFront;
import me.therealdan.galacticwarfront.events.*;
import me.therealdan.galacticwarfront.mechanics.arena.Arena;
import me.therealdan.galacticwarfront.mechanics.killcounter.KillCounter;
import me.therealdan.galacticwarfront.mechanics.lobby.Lobby;
import me.therealdan.galacticwarfront.util.PlayerHandler;
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
        event.setBattleMessage(GalacticWarFront.MAIN + "Your " + GalacticWarFront.SECOND + "FFA" + GalacticWarFront.MAIN + " on " + GalacticWarFront.SECOND + arena.getName() + GalacticWarFront.MAIN + " has begun.");
        if (isOpen()) event.setLobbyMessage(GalacticWarFront.SECOND + started.getName() + GalacticWarFront.MAIN + " has started an " + GalacticWarFront.SECOND + "FFA" + GalacticWarFront.MAIN + " on " + GalacticWarFront.SECOND + arena.getName());
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
        if (mostKills != null) event.setBattleMessage(GalacticWarFront.SECOND + mostKills.getName() + GalacticWarFront.MAIN + " got the most kills, with " + getKillCounter().getKills(mostKills.getUniqueId()) + GalacticWarFront.MAIN + " kills.");
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
        event.setBattleMessage(GalacticWarFront.SECOND + player.getName() + GalacticWarFront.MAIN + " has joined the " + GalacticWarFront.SECOND + getType().name());
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
                event.setBattleMessage(GalacticWarFront.SECOND + player.getName() + GalacticWarFront.MAIN + " has left the " + GalacticWarFront.SECOND + getType().name());
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
                GalacticWarFront.SECOND + player.getName() + GalacticWarFront.MAIN + " (" + GalacticWarFront.SECOND + getKillCounter().getDeaths(player.getUniqueId()) + GalacticWarFront.MAIN + " deaths) was killed by " + GalacticWarFront.SECOND + killer.getName() + GalacticWarFront.MAIN + " (" + GalacticWarFront.SECOND + getKillCounter().getKills(killer.getUniqueId()) + GalacticWarFront.MAIN + " kills)" :
                GalacticWarFront.SECOND + player.getName() + GalacticWarFront.MAIN + " killed themselves. (" + GalacticWarFront.SECOND + getKillCounter().getDeaths(player.getUniqueId()) + GalacticWarFront.MAIN + " deaths)"
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
