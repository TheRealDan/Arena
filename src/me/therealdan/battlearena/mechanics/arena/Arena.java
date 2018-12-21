package me.therealdan.battlearena.mechanics.arena;

import me.therealdan.battlearena.BattleArena;
import me.therealdan.battlearena.mechanics.battle.Battle;
import me.therealdan.battlearena.util.WXYZ;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class Arena {

    private static File file;
    private static FileConfiguration data;
    private static String path = "data/arenas.yml";

    private static HashSet<Arena> arenas = new HashSet<>();

    private String id;
    private String name;
    private Material material = Material.BOOK;
    private short durability = 0;
    private HashSet<WXYZ> spawnpoints = new HashSet<>();
    private HashSet<WXYZ> team1Spawnpoints = new HashSet<>();
    private HashSet<WXYZ> team2Spawnpoints = new HashSet<>();

    public Arena(String id, String name) {
        this.id = id;
        this.name = name;

        arenas.add(this);
    }

    private Arena(String id) {
        this.id = id;
        this.name = getData().getString("Arenas." + getID() + ".Name");
        this.material = Material.valueOf(getData().getString("Arenas." + getID() + ".Material"));
        this.durability = (short) getData().getInt("Arenas." + getID() + ".Durability");

        if (getData().contains("Arenas." + getID() + ".Spawnpoints"))
            for (String wxyz : getData().getConfigurationSection("Arenas." + getID() + ".Spawnpoints").getKeys(false))
                spawnpoints.add(new WXYZ(wxyz));
        if (getData().contains("Arenas." + getID() + ".Team_1_SpawnPoints"))
            for (String wxyz : getData().getConfigurationSection("Arenas." + getID() + ".Team_1_SpawnPoints").getKeys(false))
                team1Spawnpoints.add(new WXYZ(wxyz));
        if (getData().contains("Arenas." + getID() + ".Team_2_SpawnPoints"))
            for (String wxyz : getData().getConfigurationSection("Arenas." + getID() + ".Team_2_SpawnPoints").getKeys(false))
                team2Spawnpoints.add(new WXYZ(wxyz));

        arenas.add(this);
    }

    private void save() {
        getData().set("Arenas." + getID() + ".Name", name);
        getData().set("Arenas." + getID() + ".Material", material.toString());
        getData().set("Arenas." + getID() + ".Durability", durability);

        for (WXYZ wxyz : spawnpoints)
            getData().set("Arenas." + getID() + ".Spawnpoints." + wxyz.getWxyz(), 0);
        for (WXYZ wxyz : team1Spawnpoints)
            getData().set("Arenas." + getID() + ".Team_1_SpawnPoints." + wxyz.getWxyz(), 0);
        for (WXYZ wxyz : team2Spawnpoints)
            getData().set("Arenas." + getID() + ".Team_2_SpawnPoints." + wxyz.getWxyz(), 0);
    }

    public void delete() {
        arenas.remove(this);
    }

    public void clearSpawnpoints(String type) {
        switch (type.toLowerCase()) {
            case "general":
                spawnpoints.clear();
                break;
            case "team1":
                team1Spawnpoints.clear();
                break;
            case "team2":
                team2Spawnpoints.clear();
                break;
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIcon(Material material, short durability) {
        setMaterial(material);
        setDurability(durability);
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public void setDurability(short durability) {
        this.durability = durability;
    }

    public void addSpawnpoint(Location location) {
        spawnpoints.add(new WXYZ(location));
    }

    public void addTeam1Spawnpoint(Location location) {
        team1Spawnpoints.add(new WXYZ(location));
    }

    public void addTeam2Spawnpoint(Location location) {
        team2Spawnpoints.add(new WXYZ(location));
    }

    public boolean inUse() {
        for (Battle battle : Battle.values())
            if (battle.getArena().getID().equalsIgnoreCase(getID()))
                return true;
        return false;
    }

    public boolean hasSpawnpoints() {
        return getSpawnpoints().size() > 0;
    }

    public boolean hasTeamSpawnpoints() {
        return getTeam1Spawnpoints().size() > 0 && getTeam2Spawnpoints().size() > 0;
    }

    public String getID() {
        return id;
    }

    public String getName() {
        return ChatColor.translateAlternateColorCodes('&', name);
    }

    public Material getMaterial() {
        return material;
    }

    public short getDurability() {
        return durability;
    }

    public List<Location> getSpawnpoints() {
        List<Location> spawnpoints = new ArrayList<>();
        for (WXYZ wxyz : this.spawnpoints)
            spawnpoints.add(wxyz.getLocation());
        return spawnpoints;
    }

    public List<Location> getTeam1Spawnpoints() {
        List<Location> spawnpoints = new ArrayList<>();
        for (WXYZ wxyz : this.team1Spawnpoints)
            spawnpoints.add(wxyz.getLocation());
        return spawnpoints;
    }

    public List<Location> getTeam2Spawnpoints() {
        List<Location> spawnpoints = new ArrayList<>();
        for (WXYZ wxyz : this.team2Spawnpoints)
            spawnpoints.add(wxyz.getLocation());
        return spawnpoints;
    }

    public List<WXYZ> getSpawnpoints(String type) {
        switch (type.toLowerCase()) {
            case "general":
                return new ArrayList<>(spawnpoints);
            case "team1":
                return new ArrayList<>(team1Spawnpoints);
            case "team2":
                return new ArrayList<>(team2Spawnpoints);
        }
        return new ArrayList<>();
    }

    public static Arena getFree() {
        if (available().size() == 0) return null;

        if (available().size() > 1)
            return available().get(new Random().nextInt(available().size()));

        return available().get(0);
    }

    public static Arena get(String id) {
        for (Arena arena : values())
            if (arena.getID().equalsIgnoreCase(id))
                return arena;
        return null;
    }

    public static List<Arena> available() {
        List<Arena> arenas = new ArrayList<>();
        for (Arena arena : values())
            if (!arena.inUse())
                arenas.add(arena);
        return arenas;
    }

    public static List<Arena> values() {
        return new ArrayList<>(arenas);
    }

    public static void load() {
        if (getData().contains("Arenas")) {
            for (String id : getData().getConfigurationSection("Arenas").getKeys(false)) {
                try {
                    new Arena(id);
                } catch (Exception e) {
                    e.printStackTrace();
                    BattleArena.getInstance().getLogger().info("Error loading arena: " + id);
                }
            }
        }
    }

    public static void unload() {
        getData().set("Arenas", null);

        for (Arena arena : values())
            arena.save();

        saveData();
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