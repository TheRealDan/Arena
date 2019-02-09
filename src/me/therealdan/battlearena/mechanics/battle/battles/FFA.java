package me.therealdan.battlearena.mechanics.battle.battles;

import me.therealdan.battlearena.mechanics.arena.Arena;
import me.therealdan.battlearena.mechanics.battle.Battle;
import me.therealdan.battlearena.mechanics.battle.BattleType;
import me.therealdan.battlearena.mechanics.setup.Settings;
import me.therealdan.party.Party;
import org.bukkit.entity.Player;

public class FFA implements Battle {

    public FFA(Arena arena, Player started, Party party, Settings settings) {
        init(arena, BattleType.byName("FFA"), started, party, settings);

        add(started);
        if (party != null)
            for (Player player : party.getPlayers())
                add(player);
    }
}