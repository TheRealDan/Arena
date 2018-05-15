package me.therealdan.galacticwarfront.mechanics.battle.battle;

import me.therealdan.galacticwarfront.events.BattleLeaveEvent;
import me.therealdan.galacticwarfront.mechanics.battle.Arena;
import me.therealdan.galacticwarfront.mechanics.killcounter.KillCounter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public interface Battle {

    double SPAWN_RANGE = 15;

    void end(BattleLeaveEvent.Reason reason);

    void add(Player player);

    void remove(Player player, BattleLeaveEvent.Reason reason);

    void kill(Player player, Player killer);

    void respawn(Player player);

    void setGracePeriod(long secondsStartingNow);

    boolean isOpen();

    boolean contains(Player player);

    boolean sameTeam(Player player, Player player1);

    boolean canPvP();

    long getGracePeriod();

    long getStartTime();

    String getType();

    Location getRandomSpawnpoint(Player player);

    KillCounter getKillCounter();

    Arena getArena();

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
        battles.addAll(TeamBattle.values());
        battles.addAll(FFA.values());
        return battles;
    }
}