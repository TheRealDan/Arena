package me.therealdan.battlearena.mechanics.battle;

import me.therealdan.battlearena.mechanics.setup.Setup;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class BattleType {

    private static LinkedHashMap<String, BattleType> battleTypes = new LinkedHashMap<>();

    private String name;
    private ItemStack icon;
    private Setup setup;

    private BattleType(String name, ItemStack icon) {
        this.name = name;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public Setup getSetup() {
        return setup.clone();
    }

    public BattleType toggle(boolean next) {
        return next ? next() : previous();
    }

    public BattleType next() {
        boolean next = false;
        for (BattleType battleType : values()) {
            if (next) return battleType;
            if (getName().equals(battleType.getName())) next = true;
        }
        return values().get(0);
    }

    public BattleType previous() {
        int index = -1;
        for (BattleType battleType : values()) {
            if (getName().equals(battleType.getName())) {
                if (index == -1) return values().get(values().size() - 1);
                return values().get(index);
            }
            index++;
        }
        return values().get(values().size() - 1);
    }

    public static void register(String name, ItemStack icon, Setup setup) {
        BattleType battleType = new BattleType(name, icon);
        battleTypes.put(name, battleType);
        battleType.setup = setup;
    }

    public static BattleType getDefault() {
        return values().get(0);
    }

    public static BattleType byName(String name) {
        return battleTypes.get(name);
    }

    public static List<BattleType> values() {
        return new ArrayList<>(battleTypes.values());
    }
}