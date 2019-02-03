package me.therealdan.battlearena.mechanics.statistics;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;

public class KillCounter {

    private static DecimalFormat decimalFormat = new DecimalFormat("#.##");

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

    public double getKDR(UUID uuid) {
        double kills = getKills(uuid);
        double deaths = getDeaths(uuid);

        if (deaths == 0) return kills;

        return kills / deaths;
    }

    public String getKDRString(UUID uuid) {
        return decimalFormat.format(getKDR(uuid));
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
}