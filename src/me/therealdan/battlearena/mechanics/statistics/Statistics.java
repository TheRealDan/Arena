package me.therealdan.battlearena.mechanics.statistics;

import me.therealdan.battlearena.util.YamlFile;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Statistics {

    private static DecimalFormat decimalFormat = new DecimalFormat("#.##");
    private static StatisticsHandler statisticsHandler;
    private static YamlFile yamlFile;
    private static HashMap<UUID, Statistics> statistics = new HashMap<>();

    private UUID uuid;
    private long kills, deaths, gamesPlayed, gamesWon;

    private Statistics(UUID uuid) {
        this.uuid = uuid;

        this.kills = getYamlFile().getData().getLong("Kills." + getUUID().toString());
        this.deaths = getYamlFile().getData().getLong("Deaths." + getUUID().toString());
        this.gamesPlayed = getYamlFile().getData().getLong("Games_Played." + getUUID().toString());
        this.gamesWon = getYamlFile().getData().getLong("Games_Won." + getUUID().toString());
    }

    public void addKill() {
        kills++;
    }

    public void addDeath() {
        deaths++;
    }

    public void addGamePlayed() {
        gamesPlayed++;
    }

    public void addGameWon() {
        gamesWon++;
    }

    public long getKills() {
        return kills;
    }

    public long getDeaths() {
        return deaths;
    }

    public long getGamesPlayed() {
        return gamesPlayed;
    }

    public long getGamesWon() {
        return gamesWon;
    }

    public double getKDR() {
        double kills = getKills();
        double deaths = getDeaths();

        if (getDeaths() == 0.0) return kills;

        return kills / deaths;
    }

    public String getKDRString() {
        return decimalFormat.format(getKDR());
    }

    public UUID getUUID() {
        return uuid;
    }

    public static void unload() {
        boolean save = false;

        for (Statistics statistics : values()) {
            getYamlFile().getData().set("Kills." + statistics.getUUID().toString(), statistics.getKills());
            getYamlFile().getData().set("Deaths." + statistics.getUUID().toString(), statistics.getDeaths());
            getYamlFile().getData().set("Games_Played." + statistics.getUUID().toString(), statistics.getGamesPlayed());
            getYamlFile().getData().set("Games_Won." + statistics.getUUID().toString(), statistics.getGamesWon());
            save = true;
        }

        if (save) getYamlFile().save();
    }

    public static Statistics byPlayer(Player player) {
        return byUUID(player.getUniqueId());
    }

    public static Statistics byUUID(UUID uuid) {
        if (!statistics.containsKey(uuid)) statistics.put(uuid, new Statistics(uuid));
        return statistics.get(uuid);
    }

    public static List<Statistics> values() {
        return new ArrayList<>(statistics.values());
    }

    public static StatisticsHandler getHandler() {
        if (statisticsHandler == null) statisticsHandler = new StatisticsHandler();
        return statisticsHandler;
    }

    private static YamlFile getYamlFile() {
        if (yamlFile == null) yamlFile = new YamlFile("data/statistics.yml");
        return yamlFile;
    }
}