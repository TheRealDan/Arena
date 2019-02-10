package me.therealdan.battlearena.mechanics.arena;

import me.therealdan.battlearena.BattleArena;
import me.therealdan.battlearena.mechanics.battle.Battle;
import me.therealdan.battlearena.util.WXYZ;
import me.therealdan.battlearena.util.YamlFile;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.*;

public class Arena {

    private static YamlFile yamlFile;
    private static HashSet<Arena> arenas = new HashSet<>();

    private String id;
    private String name;
    private String recommendedPlayers = "?";
    private Material material = Material.BOOK;
    private short durability = 0;

    private Bounds bounds = null;
    private Consequence topConsequence = Consequence.PUSH;
    private Consequence sidesConsequence = Consequence.PUSH;
    private Consequence floorConsequence = Consequence.PUSH;

    private LinkedHashMap<Integer, LinkedHashSet<WXYZ>> locations = new LinkedHashMap<>();

    public Arena(String id, String name) {
        this.id = id;
        this.name = name;

        arenas.add(this);
    }

    private Arena(String id) {
        this.id = id;
        this.name = getYamlFile().getData().getString("Arenas." + getID() + ".Name");
        this.recommendedPlayers = getYamlFile().getData().getString("Arenas." + getID() + ".RecommendedPlayers");
        this.material = Material.valueOf(getYamlFile().getData().getString("Arenas." + getID() + ".Material"));
        this.durability = (short) getYamlFile().getData().getInt("Arenas." + getID() + ".Durability");

        if (getYamlFile().getData().contains("Arenas." + getID() + ".Bounds")) {
            if (getYamlFile().getData().contains("Arenas." + getID() + ".Bounds.Top_Consequence")) this.topConsequence = Consequence.valueOf(getYamlFile().getData().getString("Arenas." + getID() + ".Bounds.Top_Consequence"));
            if (getYamlFile().getData().contains("Arenas." + getID() + ".Bounds.Sides_Consequence")) this.sidesConsequence = Consequence.valueOf(getYamlFile().getData().getString("Arenas." + getID() + ".Bounds.Sides_Consequence"));
            if (getYamlFile().getData().contains("Arenas." + getID() + ".Bounds.Floor_Consequence")) this.floorConsequence = Consequence.valueOf(getYamlFile().getData().getString("Arenas." + getID() + ".Bounds.Floor_Consequence"));
            Location pos1 = new WXYZ(getYamlFile().getData().getString("Arenas." + getID() + ".Bounds.Pos1")).getLocation();
            Location pos2 = new WXYZ(getYamlFile().getData().getString("Arenas." + getID() + ".Bounds.Pos2")).getLocation();
            createBounds(pos1);
            getBounds().setPos1(pos1);
            getBounds().setPos2(pos2);
        }

        if (getYamlFile().getData().contains("Arenas." + getID() + ".Location_Groups"))
            for (String group : getYamlFile().getData().getConfigurationSection("Arenas." + getID() + ".Location_Groups").getKeys(false))
                for (String wxyz : getYamlFile().getData().getConfigurationSection("Arenas." + getID() + ".Location_Groups." + group).getKeys(false))
                    addLocation(Integer.parseInt(group), new WXYZ(wxyz));

        arenas.add(this);
    }

    private void save() {
        getYamlFile().getData().set("Arenas." + getID() + ".Name", name);
        getYamlFile().getData().set("Arenas." + getID() + ".RecommendedPlayers", recommendedPlayers);
        getYamlFile().getData().set("Arenas." + getID() + ".Material", material.toString());
        getYamlFile().getData().set("Arenas." + getID() + ".Durability", durability);

        if (hasBounds()) {
            getYamlFile().getData().set("Arenas." + getID() + ".Bounds.Top_Consequence", getTopConsequence().toString());
            getYamlFile().getData().set("Arenas." + getID() + ".Bounds.Sides_Consequence", getSidesConsequence().toString());
            getYamlFile().getData().set("Arenas." + getID() + ".Bounds.Floor_Consequence", getFloorConsequence().toString());
            getYamlFile().getData().set("Arenas." + getID() + ".Bounds.Pos1", new WXYZ(getBounds().getPos1()).getWxyz());
            getYamlFile().getData().set("Arenas." + getID() + ".Bounds.Pos2", new WXYZ(getBounds().getPos2()).getWxyz());
        }

        for (int group : locations.keySet())
            for (WXYZ wxyz : locations.get(group))
                getYamlFile().getData().set("Arenas." + getID() + ".Location_Groups." + group + "." + wxyz.getWxyz(), group);
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

    public void setRecommendedPlayers(String recommendedPlayers) {
        this.recommendedPlayers = recommendedPlayers;
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

    public void removeLocation(int group, WXYZ wxyz) {
        for (WXYZ existing : locations.get(group)) {
            if (existing.getWxyz().equals(wxyz.getWxyz())) {
                locations.get(group).remove(existing);
                return;
            }
        }
    }

    public void createBounds(Location location) {
        this.bounds = new Bounds(location, location);
    }

    public void clearBounds() {
        this.bounds = null;
    }

    public void setTopConsequence(Consequence topConsequence) {
        this.topConsequence = topConsequence;
    }

    public void setSidesConsequence(Consequence sidesConsequence) {
        this.sidesConsequence = sidesConsequence;
    }

    public void setFloorConsequence(Consequence floorConsequence) {
        this.floorConsequence = floorConsequence;
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

    public String getRecommendedPlayers() {
        return recommendedPlayers;
    }

    public Material getMaterial() {
        return material;
    }

    public short getDurability() {
        return durability;
    }

    public boolean hasBounds() {
        return bounds != null;
    }

    public Bounds getBounds() {
        return bounds;
    }

    public Consequence getTopConsequence() {
        return topConsequence;
    }

    public Consequence getSidesConsequence() {
        return sidesConsequence;
    }

    public Consequence getFloorConsequence() {
        return floorConsequence;
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

    public enum Consequence {
        PUSH,
        DAMAGE,
        KILL,
        RESPAWN;

        public Consequence next() {
            boolean next = false;
            for (Consequence consequence : Consequence.values()) {
                if (next) return consequence;
                if (this.equals(consequence)) next = true;
            }
            return Consequence.values()[0];
        }

        public String getName() {
            return this.toString().toUpperCase().substring(0, 1) + this.toString().toLowerCase().substring(1);
        }

        public String getDescription() {
            switch (this) {
                case PUSH:
                    return "Push towards center of Arena.";
                case DAMAGE:
                    return "Constant damage while outside of Arena bounds.";
                case KILL:
                    return "Instant death.";
                case RESPAWN:
                    return "Instant respawn.";
            }
            return this.toString();
        }
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
        if (getYamlFile().getData().contains("Arenas")) {
            for (String id : getYamlFile().getData().getConfigurationSection("Arenas").getKeys(false)) {
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
        getYamlFile().getData().set("Arenas", null);

        for (Arena arena : values())
            arena.save();

        getYamlFile().save();
    }

    private static YamlFile getYamlFile() {
        if (yamlFile == null) yamlFile = new YamlFile("data/arenas.yml");
        return yamlFile;
    }
}