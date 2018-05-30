package me.therealdan.galacticwarfront;

import me.therealdan.galacticwarfront.commands.GalacticWarFrontCommand;
import me.therealdan.galacticwarfront.events.BattleLeaveEvent;
import me.therealdan.galacticwarfront.mechanics.arena.Arena;
import me.therealdan.galacticwarfront.mechanics.battle.BattleHandler;
import me.therealdan.galacticwarfront.mechanics.battle.Battle;
import me.therealdan.galacticwarfront.mechanics.killcounter.KillCounter;
import me.therealdan.galacticwarfront.mechanics.lobby.BattleCreator;
import me.therealdan.galacticwarfront.mechanics.lobby.Lobby;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class GalacticWarFront extends JavaPlugin {

    private static GalacticWarFront galacticWarFront;
    public static String MAIN, SECOND;

    @Override
    public void onEnable() {
        galacticWarFront = this;

        saveDefaultConfig();
        MAIN = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Color.Main"));
        SECOND = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Color.Secondary"));

        Arena.load();

        getServer().getPluginManager().registerEvents(Lobby.getInstance(), this);
        getServer().getPluginManager().registerEvents(BattleCreator.getInstance(), this);
        getServer().getPluginManager().registerEvents(BattleHandler.getInstance(), this);

        getCommand("GalacticWarFront").setExecutor(new GalacticWarFrontCommand());
        getCommand("GWF").setExecutor(new GalacticWarFrontCommand());
    }

    @Override
    public void onDisable() {
        for (Battle battle : Battle.values())
            battle.end(BattleLeaveEvent.Reason.SERVER_SHUTDOWN);

        Lobby.getInstance().unload();

        Arena.unload();
        KillCounter.unload();
    }

    public static GalacticWarFront getInstance() {
        return galacticWarFront;
    }
}