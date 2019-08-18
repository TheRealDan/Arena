package me.therealdan.battlearena.mechanics.battle;

import me.therealdan.battlearena.BattleArena;
import me.therealdan.battlearena.events.*;
import me.therealdan.battlearena.mechanics.arena.Arena;
import me.therealdan.battlearena.mechanics.lobby.Lobby;
import me.therealdan.battlearena.mechanics.setup.Settings;
import me.therealdan.battlearena.mechanics.statistics.KillCounter;
import me.therealdan.battlearena.util.PlayerHandler;
import me.therealdan.party.Party;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.*;

public interface Battle {

    HashSet<Battle> battles = new HashSet<>();
    HashSet<Battle> open = new HashSet<>();

    HashMap<Battle, UUID> battleID = new HashMap<>();
    HashMap<Battle, Arena> arena = new HashMap<>();
    HashMap<Battle, BattleType> battleType = new HashMap<>();
    HashMap<Battle, Settings> settings = new HashMap<>();
    HashMap<Battle, KillCounter> killCounter = new HashMap<>();
    HashMap<Battle, Boolean> statisticsTracking = new HashMap<>();
    HashMap<Battle, Boolean> saveRestoreInventory = new HashMap<>();
    HashMap<Battle, Long> startTime = new HashMap<>();
    HashMap<Battle, Long> gracePeriod = new HashMap<>();
    HashMap<Battle, Long> battleDuration = new HashMap<>();
    HashMap<Battle, BossBar> timeRemainingBar = new HashMap<>();
    HashMap<Battle, LinkedHashSet<UUID>> players = new HashMap<>();

    default void init(Arena arena, BattleType battleType, Player started, Party party, Settings settings) {
        Battle.battleID.put(this, UUID.randomUUID());
        Battle.arena.put(this, arena);
        Battle.battleType.put(this, battleType);
        Battle.settings.put(this, settings);
        Battle.killCounter.put(this, new KillCounter());
        Battle.statisticsTracking.put(this, true);
        Battle.saveRestoreInventory.put(this, false);
        Battle.startTime.put(this, System.currentTimeMillis());
        Battle.gracePeriod.put(this, 0L);
        Battle.battleDuration.put(this, 0L);
        Battle.timeRemainingBar.put(this, Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID));
        Battle.players.put(this, new LinkedHashSet<>());

        settings.apply(this);

        BattleStartEvent event = new BattleStartEvent(this, started);
        event.setBattleMessage(BattleArena.MAIN + "Your " + BattleArena.SECOND + getBattleType().getName() + BattleArena.MAIN + " on " + BattleArena.SECOND + arena.getName() + BattleArena.MAIN + " has begun.");
        if (isOpen())
            event.setLobbyMessage(BattleArena.SECOND + started.getName() + BattleArena.MAIN + " has started a " + BattleArena.SECOND + getBattleType().getName() + BattleArena.MAIN + " on " + BattleArena.SECOND + arena.getName());
        Bukkit.getPluginManager().callEvent(event);

        if (event.getPlayerMessage() != null)
            started.sendMessage(event.getPlayerMessage());

        if (event.getBattleMessage() != null)
            for (Player player : getPlayers())
                player.sendMessage(event.getBattleMessage());

        if (event.getLobbyMessage() != null)
            for (Player player : Lobby.getInstance().getPlayers())
                player.sendMessage(event.getLobbyMessage());

        if (party != null) setOpen(party.isOpen());

        Battle.battles.add(this);
    }

    default void end(BattleLeaveEvent.Reason reason) {
        OfflinePlayer mostKills = getKillCounter().getMostKills() != null ? Bukkit.getOfflinePlayer(getKillCounter().getMostKills()) : null;

        String battleMessage = null;
        if (mostKills != null)
            battleMessage = BattleArena.SECOND + mostKills.getName() + BattleArena.MAIN + " got the most kills, with " + BattleArena.SECOND + getKillCounter().getKills(mostKills.getUniqueId()) + BattleArena.MAIN + " kills.";
        end(reason, battleMessage);
    }

    default void end(BattleLeaveEvent.Reason reason, String battleMessage) {
        if (!Battle.battles.contains(this)) return;

        BattleFinishEvent event = new BattleFinishEvent(this, reason);
        event.setBattleMessage(battleMessage);
        Bukkit.getPluginManager().callEvent(event);

        if (event.getBattleMessage() != null)
            for (Player player : getPlayers())
                player.sendMessage(event.getBattleMessage());

        if (event.getLobbyMessage() != null)
            for (Player player : Lobby.getInstance().getPlayers())
                player.sendMessage(event.getLobbyMessage());

        for (Player player : getPlayers())
            remove(player, reason);

        Battle.battles.remove(this);
    }

    default void add(Player player) {
        add(player, BattleArena.SECOND + player.getName() + BattleArena.MAIN + " has joined the " + BattleArena.SECOND + getBattleType().getName());
    }

    default void add(Player player, String joinMessage) {
        if (contains(player)) return;

        Battle battle = Battle.get(player);
        if (battle != null) battle.remove(player, BattleLeaveEvent.Reason.LEAVE);

        BattleJoinEvent event = new BattleJoinEvent(this, player);
        event.setBattleMessage(joinMessage.replace("%player%", player.getName()));
        Bukkit.getPluginManager().callEvent(event);

        if (event.getBattleMessage() != null)
            for (Player each : getPlayers())
                each.sendMessage(event.getBattleMessage());

        if (!Battle.players.containsKey(this)) Battle.players.put(this, new LinkedHashSet<>());
        Battle.players.get(this).add(player.getUniqueId());

        if (isSaveRestoreInventoryEnabled()) {
            PlayerHandler.saveInventory(player, getBattleID() + "_" + player.getUniqueId().toString());
            PlayerHandler.clearInventory(player);
        }

        getTimeRemainingBar().addPlayer(player);

        respawn(player);
    }

    default void remove(Player player, BattleLeaveEvent.Reason reason) {
        BattleLeaveEvent event = new BattleLeaveEvent(this, player, reason, Lobby.getInstance().getSpawnpoint());
        switch (reason) {
            case LEAVE:
            case LOGOUT:
                event.setBattleMessage(BattleArena.SECOND + player.getName() + BattleArena.MAIN + " has left the " + BattleArena.SECOND + getBattleType().getName());
                break;
            case KICK:
                event.setBattleMessage(BattleArena.SECOND + player.getName() + BattleArena.MAIN + " was kicked from " + BattleArena.SECOND + getBattleType().getName());
                break;
            case ADMIN_END:
                event.setBattleMessage(BattleArena.MAIN + "Battle Ended by Admin");
                break;
        }
        Bukkit.getPluginManager().callEvent(event);

        if (event.getBattleMessage() != null)
            for (Player each : getPlayers())
                each.sendMessage(event.getBattleMessage());

        player.teleport(event.getSpawn());
        PlayerHandler.refresh(player);

        if (isSaveRestoreInventoryEnabled()) {
            PlayerHandler.clearInventory(player);
            PlayerHandler.restoreInventory(player, getBattleID() + "_" + player.getUniqueId().toString());
        }

        Battle.players.get(this).remove(player.getUniqueId());

        getTimeRemainingBar().removePlayer(player);
    }

    default void kill(Player player, Player killer) {
        kill(player, killer, killer != null ?
                BattleArena.SECOND + player.getName() + BattleArena.MAIN + " (" + BattleArena.SECOND + "%playerdeaths%" + BattleArena.MAIN + " deaths) was killed by " + BattleArena.SECOND + killer.getName() + BattleArena.MAIN + " (" + BattleArena.SECOND + "%killerkills%" + BattleArena.MAIN + " kills)" :
                BattleArena.SECOND + player.getName() + BattleArena.MAIN + " killed themselves. (" + BattleArena.SECOND + "%playerdeaths%" + BattleArena.MAIN + " deaths)");
    }

    default void kill(Player player, Player killer, String battleMessage) {
        getKillCounter().addDeath(player.getUniqueId());
        if (killer != null) getKillCounter().addKill(killer.getUniqueId());

        BattleDeathEvent event = new BattleDeathEvent(this, player, killer);
        event.setBattleMessage(battleMessage
                .replace("%playerkills%", Long.toString(getKillCounter().getKills(player.getUniqueId())))
                .replace("%playerdeaths%", Long.toString(getKillCounter().getDeaths(player.getUniqueId())))
                .replace("%killerkills%", killer != null ? Long.toString(getKillCounter().getKills(killer.getUniqueId())) : "0")
                .replace("%killerdeaths%", killer != null ? Long.toString(getKillCounter().getDeaths(killer.getUniqueId())) : "0")
        );
        Bukkit.getPluginManager().callEvent(event);

        if (event.getBattleMessage() != null)
            for (Player each : getPlayers())
                each.sendMessage(event.getBattleMessage());

        respawn(player);
    }

    default void respawn(Player player) {
        respawn(player, getRandomSpawnpoint());
    }

    default void respawn(Player player, Location spawnpoint) {
        if (!contains(player)) return;

        BattleRespawnEvent battleRespawnEvent = new BattleRespawnEvent(this, player, spawnpoint);
        Bukkit.getPluginManager().callEvent(battleRespawnEvent);

        for (Player target : getPlayers())
            target.hidePlayer(BattleArena.getInstance(), player);

        player.teleport(battleRespawnEvent.getRespawnLocation());

        for (Player target : getPlayers())
            target.showPlayer(BattleArena.getInstance(), player);

        PlayerHandler.refresh(player);
    }

    default void setGracePeriod(long secondsStartingNow) {
        gracePeriod.put(this, System.currentTimeMillis() + (secondsStartingNow * 1000));
    }

    default void setTimeRemaining(long secondsStartingNow) {
        battleDuration.put(this, System.currentTimeMillis() + (secondsStartingNow * 1000));
    }

    default void setOpen(boolean open) {
        if (open) {
            Battle.open.add(this);
        } else {
            Battle.open.remove(this);
        }
    }

    default boolean isOpen() {
        return Battle.open.contains(this);
    }

    default boolean contains(Player player) {
        return Battle.players.get(this).contains(player.getUniqueId());
    }

    default boolean sameTeam(Player player, Player player1) {
        return false;
    }

    default boolean canPvP() {
        return getGraceTimeRemaining() <= 0;
    }

    default void setSaveRestoreInventory(boolean enabled) {
        saveRestoreInventory.put(this, enabled);
    }

    default boolean isSaveRestoreInventoryEnabled() {
        return saveRestoreInventory.get(this);
    }

    default long getTimePassed() {
        return System.currentTimeMillis() - getStartTime();
    }

    default long getGraceTimeRemaining() {
        return Math.max(gracePeriod.get(this) - System.currentTimeMillis(), 0L);
    }

    default long getTimeRemaining() {
        return Math.max(battleDuration.get(this) - System.currentTimeMillis(), 0L);
    }

    default void setStatisticsTracking(boolean enabled) {
        statisticsTracking.put(this, enabled);
    }

    default boolean statisticsTrackingEnabled() {
        return statisticsTracking.get(this);
    }

    default long getStartTime() {
        return startTime.get(this);
    }

    default long getGraceDuration() {
        return gracePeriod.get(this) - getStartTime();
    }

    default long getBattleDuration() {
        return battleDuration.get(this) - getStartTime() - getGraceDuration();
    }

    default double getProgress() {
        if (getGraceTimeRemaining() > 0) {
            return (double) getTimePassed() / (double) getGraceDuration();
        } else {
            return ((double) getTimePassed() - (double) getGraceDuration()) / (double) getBattleDuration();
        }
    }

    default Location getRandomSpawnpoint() {
        return getRandomSpawnpoint(getArena().getLocations(1));
    }

    default Location getRandomSpawnpoint(List<Location> spawnpoints) {
        if (spawnpoints.size() == 1) return spawnpoints.get(0);

        Location location = null;
        double lastDistance = -1;
        for (Location each : spawnpoints) {
            if (location == null) {
                location = each;
            } else {
                double playerDistance = Double.MAX_VALUE;
                for (Player player : getPlayers()) {
                    if (!player.getWorld().getName().equals(each.getWorld().getName())) continue;
                    double distance = player.getLocation().distance(each);
                    if (distance < playerDistance)
                        playerDistance = distance;
                }

                if (playerDistance > lastDistance) {
                    location = each;
                    lastDistance = playerDistance;
                }
            }
        }

        return location;
    }

    default Arena getArena() {
        return arena.get(this);
    }

    default BattleType getBattleType() {
        return battleType.get(this);
    }

    default Settings getSettings() {
        return settings.get(this);
    }

    default KillCounter getKillCounter() {
        return killCounter.get(this);
    }

    default BossBar getTimeRemainingBar() {
        return timeRemainingBar.get(this);
    }

    default List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();
        for (UUID uuid : Battle.players.get(this))
            players.add(Bukkit.getPlayer(uuid));
        return players;
    }

    default UUID getBattleID() {
        return battleID.get(this);
    }

    static Battle get(Player player) {
        for (Battle battle : values())
            if (battle.contains(player))
                return battle;
        return null;
    }

    static List<Battle> values() {
        return new ArrayList<>(battles);
    }

    static List<Battle> open() {
        return new ArrayList<>(open);
    }
}