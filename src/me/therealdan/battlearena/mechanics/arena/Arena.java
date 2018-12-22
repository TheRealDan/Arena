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
import java.util.*;

public class Arena {

    private static File file;
    private static FileConfiguration data;
    private static String path = "data/arenas.yml";

    private static HashSet<Arena> arenas = new HashSet<>();

    private String id;
    private String name;
    private Material material = Material.BOOK;
    private short durability = 0;

    private LinkedHashMap<Integer, LinkedHashSet<WXYZ>> locations;

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

        this.locations = new LinkedHashMap<>();

        if (getData().contains("Arenas." + getID() + ".Location_Groups"))
            for (String group : getData().getConfigurationSection("Arenas." + getID() + ".Location_Groups").getKeys(false))
                for (String wxyz : getData().getConfigurationSection("Arenas." + getID() + ".Location_Groups." + group).getKeys(false))
                    addLocation(Integer.parseInt(group), new WXYZ(wxyz));

        arenas.add(this);
    }

    private void save() {
        getData().set("Arenas." + getID() + ".Name", name);
        getData().set("Arenas." + getID() + ".Material", material.toString());
        getData().set("Arenas." + getID() + ".Durability", durability);

        for (int group : locations.keySet())
            for (WXYZ wxyz : locations.get(group))
                getData().set("Arenas." + getID() + ".Location_Groups." + group + "." + wxyz.getWxyz(), group);
    }

    public void delete() {
        arenas.remove(this);
    }

    public void clearSpawnpoints(int group) {
        this.locations.get(group).clear();
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

    public void addLocation(int group, WXYZ wxyz) {
        if (!this.locations.containsKey(group)) this.locations.put(group, new LinkedHashSet<>());
        this.locations.get(group).add(wxyz);
    }

    public boolean inUse() {
        for (Battle battle : Battle.values())
            if (battle.getArena().getID().equalsIgnoreCase(getID()))
                return true;
        return false;
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

    public List<Location> getLocations(int group) {
        List<Location> locations = new ArrayList<>();
        for (WXYZ wxyz : getWXYZs(group))
            locations.add(wxyz.getLocation());
        return locations;
    }

    public List<WXYZ> getWXYZs(int group) {
        if (!this.locations.containsKey(group)) this.locations.put(group, new LinkedHashSet<>());
        return new ArrayList<>(this.locations.get(group));
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