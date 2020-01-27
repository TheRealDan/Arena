package me.therealdan.battlearena;

import me.therealdan.battlearena.commands.BattleArenaCommand;
import me.therealdan.battlearena.events.BattleLeaveEvent;
import me.therealdan.battlearena.mechanics.arena.Arena;
import me.therealdan.battlearena.mechanics.arena.editors.BoundsEditor;
import me.therealdan.battlearena.mechanics.arena.editors.ConsequenceEditor;
import me.therealdan.battlearena.mechanics.arena.editors.LocationsEditor;
import me.therealdan.battlearena.mechanics.battle.Battle;
import me.therealdan.battlearena.mechanics.battle.BattleHandler;
import me.therealdan.battlearena.mechanics.battle.BattleType;
import me.therealdan.battlearena.mechanics.lobby.Lobby;
import me.therealdan.battlearena.mechanics.setup.SetupHandler;
import me.therealdan.battlearena.mechanics.setup.setup.FFASetup;
import me.therealdan.battlearena.mechanics.setup.setup.TeamSetup;
import me.therealdan.battlearena.mechanics.statistics.Statistics;
import me.therealdan.battlearena.util.Icon;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class BattleArena extends JavaPlugin {

    private static BattleArena battleArena;
    public static String MAIN, SECOND, ERROR;

    @Override
    public void onEnable() {
        battleArena = this;

        saveDefaultConfig();
        MAIN = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Color.Main"));
        SECOND = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Color.Secondary"));
        ERROR = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Color.Error"));

        Arena.load();

        if (getConfig().getBoolean("Load_Default_Battles")) {
            BattleType.register("FFA", Icon.build(BattleArena.getInstance().getConfig(), "Battle_Creator.FFA", false), new FFASetup());
            BattleType.register("Team", Icon.build(BattleArena.getInstance().getConfig(), "Battle_Creator.Team", false), new TeamSetup());
            SetupHandler.setDefault(BattleType.byName("FFA").getSetup());
        }

        getServer().getPluginManager().registerEvents(Lobby.getInstance(), this);
        getServer().getPluginManager().registerEvents(SetupHandler.getInstance(), this);
        getServer().getPluginManager().registerEvents(BattleHandler.getInstance(), this);
        getServer().getPluginManager().registerEvents(Statistics.getHandler(), this);
        getServer().getPluginManager().registerEvents(ConsequenceEditor.getInstance(), this);
        getServer().getPluginManager().registerEvents(LocationsEditor.getInstance(), this);
        getServer().getPluginManager().registerEvents(BoundsEditor.getInstance(), this);

        BattleArenaCommand battleArenaCommand = new BattleArenaCommand();
        getCommand("BattleArena").setExecutor(battleArenaCommand);
        getCommand("BA").setExecutor(battleArenaCommand);
    }

    @Override
    public void onDisable() {
        for (Battle battle : Battle.values())
            battle.end(BattleLeaveEvent.Reason.SERVER_SHUTDOWN);

        Lobby.getInstance().unload();

        Arena.unload();
        Statistics.unload();
    }

    public static BattleArena getInstance() {
        return battleArena;
    }
}