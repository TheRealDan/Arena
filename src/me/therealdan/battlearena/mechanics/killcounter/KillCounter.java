package me.therealdan.battlearena.mechanics.killcounter;

import me.therealdan.battlearena.util.YamlFile;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;

public class KillCounter {

    private static DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private static YamlFile yamlFile;

    private static HashMap<UUID, Long> totalKills = new HashMap<>();
    private static HashMap<UUID, Long> totalDeaths = new HashMap<>();

    private HashMap<UUID, Long> kills = new HashMap<>();
    private HashMap<UUID, Long> deaths = new HashMap<>();

    public void remove(UUID uuid) {
        kills.remove(uuid);
        deaths.remove(uuid);
    }

    public void addKill(UUID uuid) {
        kills.put(uuid, getKills(uuid) + 1);
    }

    public void addDeath(UUID uuid) {
        deaths.put(uuid, getDeaths(uuid) + 1);
    }

    public long getKills(UUID uuid) {
        return kills.getOrDefault(uuid, 0L);
    }

    public long getDeaths(UUID uuid) {
        return deaths.getOrDefault(uuid, 0L);
    }

    public double getCurrentKDR(UUID uuid) {
        double kills = getKills(uuid);
        double deaths = getDeaths(uuid);

        if (deaths == 0) return kills;

        return kills / deaths;
    }

    public String getCurrentKDRString(UUID uuid) {
        return decimalFormat.format(getCurrentKDR(uuid));
    }

    public UUID getMostKills() {
        UUID mostKills = null;

        for (UUID uuid : kills.keySet())
            if (mostKills == null ||
                    getKills(uuid) > getKills(mostKills) ||
                    (getKills(uuid) == getKills(mostKills) && getDeaths(uuid) < getDeaths(mostKills)))
                mostKills = uuid;

        return mostKills;
    }

    public static void unload() {
        for (UUID uuid : totalKills.keySet())
            getYamlFile().getData().set("Kills." + uuid.toString(), getTotalKills(uuid));

        for (UUID uuid : totalDeaths.keySet())
            getYamlFile().getData().set("Deaths." + uuid.toString(), getTotalDeaths(uuid));

        getYamlFile().save();
    }

    public static void addCount(KillCounter killCounter) {
        for (UUID uuid : killCounter.kills.keySet())
            totalKills.put(uuid, getTotalKills(uuid) + killCounter.getKills(uuid));

        for (UUID uuid : killCounter.deaths.keySet())
            totalDeaths.put(uuid, getTotalDeaths(uuid) + killCounter.getDeaths(uuid));
    }

    public static long getTotalKills(UUID uuid) {
        return totalKills.getOrDefault(uuid, getYamlFile().getData().getLong("Kills." + uuid.toString()));
    }

    public static long getTotalDeaths(UUID uuid) {
        return totalDeaths.getOrDefault(uuid, getYamlFile().getData().getLong("Deaths." + uuid.toString()));
    }

    public static double getKDR(UUID uuid) {
        double kills = getTotalKills(uuid);
        double deaths = getTotalDeaths(uuid);

        if (deaths == 0) return kills;

        return kills / deaths;
    }

    public static String getKDRString(UUID uuid) {
        return decimalFormat.format(getKDR(uuid));
    }

    private static YamlFile getYamlFile() {
        if (yamlFile == null) yamlFile = new YamlFile("data/killcounter.yml");
        return yamlFile;
    }
}