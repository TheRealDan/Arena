package me.therealdan.battlearena.util;

import me.therealdan.battlearena.BattleArena;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class YamlFile {

    private File file;
    private FileConfiguration data;
    private String path;

    public YamlFile(String path) {
        this(path, false);
    }

    public YamlFile(String path, boolean load) {
        this.path = path;
        if (load) loadFile();
    }

    public void save() {
        try {
            getData().save(getFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadFile() {
        BattleArena.getInstance().saveResource(path, false);
    }

    public FileConfiguration getData() {
        if (data == null) data = YamlConfiguration.loadConfiguration(getFile());
        return data;
    }

    private File getFile() {
        if (file == null) file = new File(BattleArena.getInstance().getDataFolder(), path);
        return file;
    }
}