package me.therealdan.battlearena.mechanics.killcounter;

import me.therealdan.battlearena.BattleArena;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;

public class KillCounter {

    private static DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private static File file;
    private static FileConfiguration data;
    private static String path = "data/killcounter.yml";

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

    public String getKDRString(UUID uuid) {
        return decimalFormat.format(getCurrentKDR(uuid));
    }

    public UUID getMostKills() {
        UUID mostKills = null;

        for (UUID uuid : kills.keySet())
            if (mostKills == null ||
                    getKills(uuid) > getKills(mostKills) ||
                    (getKills(uuid) == getKills(mostKills) && getKDR(uuid) > getKDR(mostKills)))
                mostKills = uuid;

        return mostKills;
    }

    public static void unload() {
        for (UUID uuid : totalKills.keySet())
            getData().set("Kills." + uuid.toString(), getTotalKills(uuid));

        for (UUID uuid : totalDeaths.keySet())
            getData().set("Deaths." + uuid.toString(), getTotalDeaths(uuid));

        saveData();
    }

    public static void addCount(KillCounter killCounter) {
        for (UUID uuid : killCounter.kills.keySet())
            totalKills.put(uuid, getTotalKills(uuid) + killCounter.getKills(uuid));

        for (UUID uuid : killCounter.deaths.keySet())
            totalDeaths.put(uuid, getTotalDeaths(uuid) + killCounter.getDeaths(uuid));
    }

    public static long getTotalKills(UUID uuid) {
        return totalKills.getOrDefault(uuid, getData().getLong("Kills." + uuid.toString()));
    }

    public static long getTotalDeaths(UUID uuid) {
        return totalDeaths.getOrDefault(uuid, getData().getLong("Deaths." + uuid.toString()));
    }

    public static double getKDR(UUID uuid) {
        double kills = getTotalKills(uuid);
        double deaths = getTotalDeaths(uuid);

        if (deaths == 0) return kills;

        return kills / deaths;
    }

    private static void saveData() {
        try {
            getData().save(getFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static FileConfiguration getData() {
        if (data == null) data = YamlConfiguration.loadConfiguration(getFile());
        return data;
    }

    private static File getFile() {
        if (file == null) {
            file = new File(BattleArena.getInstance().getDataFolder(), path);
        }
        return file;
    }
}