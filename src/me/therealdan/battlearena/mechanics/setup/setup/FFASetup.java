package me.therealdan.battlearena.mechanics.setup.setup;

import me.therealdan.battlearena.mechanics.arena.Arena;
import me.therealdan.battlearena.mechanics.battle.Battle;
import me.therealdan.battlearena.mechanics.battle.battles.FFA;
import me.therealdan.battlearena.mechanics.setup.Settings;
import me.therealdan.battlearena.mechanics.setup.Setup;
import me.therealdan.battlearena.mechanics.setup.settings.BattleDuration;
import me.therealdan.battlearena.mechanics.setup.settings.GracePeriod;
import me.therealdan.battlearena.mechanics.setup.settings.Map;
import me.therealdan.battlearena.mechanics.setup.settings.Open;
import me.therealdan.party.Party;
import org.bukkit.entity.Player;

public class FFASetup extends Setup {

    public FFASetup() {
        super("FFA", new Settings(
                new Map(Arena.getFree()),
                new BattleDuration(180),
                new GracePeriod(0),
                new Open(true)
        ));
    }

    @Override
    public Battle startBattle(Player player, Party party, Arena arena) {
        return new FFA(arena, player, party);
    }
}