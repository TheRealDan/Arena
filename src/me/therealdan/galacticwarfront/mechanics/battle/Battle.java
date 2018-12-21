package me.therealdan.galacticwarfront.mechanics.battle;

import me.therealdan.galacticwarfront.events.BattleLeaveEvent;
import me.therealdan.galacticwarfront.mechanics.arena.Arena;
import me.therealdan.galacticwarfront.mechanics.killcounter.KillCounter;
import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public interface Battle {

    double SPAWN_RANGE = 15;

    void end(BattleLeaveEvent.Reason reason);

    void add(Player player);

    void remove(Player player, BattleLeaveEvent.Reason reason);

    void kill(Player player, Player killer);

    void respawn(Player player);

    void setGracePeriod(long secondsStartingNow);

    void setTimeRemaining(long secondsStartingNow);

    void setOpen(boolean open);

    boolean isOpen();

    boolean contains(Player player);

    default boolean sameTeam(Player player, Player player1) {
        return false;
    }

    boolean canPvP();

    long getTimePassed();

    long getGraceTimeRemaining();

    long getTimeRemaining();

    long getStartTime();

    long getGraceDuration();

    long getBattleDuration();

    double getProgress();

    Type getType();

    default Location getRandomSpawnpoint() {
        return getRandomSpawnpoint(getArena().getSpawnpoints());
    }

    default Location getRandomSpawnpoint(List<Location> spawnpoints) {
        boolean safe;
        int checks = 0;
        Location location = null;
        while (checks < 10) {
            safe = true;
            location = spawnpoints.get(new Random().nextInt(spawnpoints.size()));
            for (Entity entity : location.getWorld().getNearbyEntities(location, SPAWN_RANGE, SPAWN_RANGE, SPAWN_RANGE))
                if (entity instanceof Player)
                    safe = false;
            if (safe) break;
            checks++;
        }

        return location;
    }

    KillCounter getKillCounter();

    Arena getArena();

    BossBar getTimeRemainingBar();

    List<Player> getPlayers();

    static Battle get(Player player) {
        for (Battle battle : values())
            if (battle.contains(player))
                return battle;
        return null;
    }

    static List<Battle> values() {
        List<Battle> battles = new ArrayList<>();
        battles.addAll(Duel.values());
        battles.addAll(Team.values());
        battles.addAll(FFA.values());
        return battles;
    }

    enum Type {
        FFA, Duel, Team;

        public boolean hasTeams() {
            switch (this) {
                case FFA:
                    return false;
                case Duel:
                case Team:
                    return true;
            }
            return false;
        }

        public Type toggle(boolean next) {
            return next ? next() : previous();
        }

        public Type next() {
            switch (this) {
                case FFA:
                    return Duel;
                case Duel:
                    return Team;
                case Team:
                    return FFA;
                default:
                    return FFA;
            }
        }

        public Type previous() {
            switch (this) {
                case FFA:
                    return Team;
                case Duel:
                    return FFA;
                case Team:
                    return Duel;
                default:
                    return FFA;
            }
        }
    }
}