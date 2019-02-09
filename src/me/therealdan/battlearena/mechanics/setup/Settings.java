package me.therealdan.battlearena.mechanics.setup;

import me.therealdan.battlearena.mechanics.battle.Battle;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Settings {

    private LinkedList<Setting> settings = new LinkedList<>();

    public Settings(Setting... settings) {
        for (Setting setting : settings)
            add(setting);
    }

    public void add(Setting setting) {
        for (Setting each : values())
            if (setting.getName().equals(each.getName()))
                settings.remove(each);

        settings.add(setting);
    }

    public void apply(Battle battle) {
        for (Setting setting : values())
            setting.apply(battle);
    }

    public void copy(Settings settings) {
        for (Setting setting : values()) {
            for (Setting each : settings.values()) {
                if (setting.getName().equals(each.getName())) {
                    setting.set(each.getValue());
                }
            }
        }
    }

    public List<Setting> values() {
        return new ArrayList<>(settings);
    }

    @Override
    public Settings clone() {
        Settings settings = new Settings();
        for (Setting setting : values())
            settings.add(setting);
        return settings;
    }
}