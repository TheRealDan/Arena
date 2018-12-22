package me.therealdan.battlearena.mechanics.battle;

import me.therealdan.battlearena.mechanics.arena.Arena;
import me.therealdan.party.Party;
import org.bukkit.entity.Player;

public class FFA implements Battle {

    public FFA(Arena arena, Player started, Party party) {
        init(arena, BattleType.byName("FFA"), started, party);

        add(started);
        if (party != null)
            for (Player player : party.getPlayers())
                add(player);
    }
}