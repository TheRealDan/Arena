package me.therealdan.galacticwarfront;

import me.therealdan.galacticwarfront.events.BattleLeaveEvent;
import me.therealdan.galacticwarfront.mechanics.battle.BattleHandler;
import me.therealdan.galacticwarfront.mechanics.battle.Arena;
import me.therealdan.galacticwarfront.commands.GalacticWarFrontCommand;
import me.therealdan.galacticwarfront.mechanics.battle.battle.Battle;
import me.therealdan.galacticwarfront.mechanics.battle.eventhandler.DefaultHandler;
import me.therealdan.galacticwarfront.mechanics.killcounter.KillCounter;
import me.therealdan.galacticwarfront.mechanics.lobby.BattleCreator;
import me.therealdan.galacticwarfront.mechanics.lobby.Lobby;
import me.therealdan.galacticwarfront.mechanics.party.Party;
import me.therealdan.galacticwarfront.mechanics.party.PartyHandler;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class GalacticWarFront extends JavaPlugin {

    private static GalacticWarFront galacticWarFront;
    public static String MAIN, SECOND;

    private Lobby lobby;
    private PartyHandler partyHandler;
    private BattleCreator battleCreator;
    private BattleHandler battleHandler;

    private DefaultHandler defaultHandler;

    @Override
    public void onEnable() {
        galacticWarFront = this;

        saveDefaultConfig();
        MAIN = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Color.Main"));
        SECOND = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Color.Secondary"));

        Arena.load();
        Party.load();

        getServer().getPluginManager().registerEvents(getLobby(), this);
        getServer().getPluginManager().registerEvents(getPartyHandler(), this);
        getServer().getPluginManager().registerEvents(getBattleCreator(), this);
        getServer().getPluginManager().registerEvents(getBattleHandler(), this);

        if (getConfig().getBoolean("Default_Handler_Enabled")) getServer().getPluginManager().registerEvents(getDefaultHandler(), this);

        getCommand("GalacticWarFront").setExecutor(new GalacticWarFrontCommand());
        getCommand("GWF").setExecutor(new GalacticWarFrontCommand());
    }

    @Override
    public void onDisable() {
        for (Battle battle : Battle.values())
            battle.end(BattleLeaveEvent.Reason.SERVER_SHUTDOWN);

        Arena.unload();
        Party.unload();
        KillCounter.unload();

        getLobby().unload();

        getDefaultHandler().unload();
    }

    public Lobby getLobby() {
        if (lobby == null) lobby = new Lobby();
        return lobby;
    }

    public PartyHandler getPartyHandler() {
        if (partyHandler == null) partyHandler = new PartyHandler();
        return partyHandler;
    }

    public BattleCreator getBattleCreator() {
        if (battleCreator == null) battleCreator = new BattleCreator();
        return battleCreator;
    }

    public BattleHandler getBattleHandler() {
        if (battleHandler == null) battleHandler = new BattleHandler();
        return battleHandler;
    }

    public DefaultHandler getDefaultHandler() {
        if (defaultHandler == null) defaultHandler = new DefaultHandler();
        return defaultHandler;
    }

    public static GalacticWarFront getInstance() {
        return galacticWarFront;
    }
}