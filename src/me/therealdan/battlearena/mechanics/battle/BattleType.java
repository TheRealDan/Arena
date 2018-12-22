package me.therealdan.battlearena.mechanics.battle;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class BattleType {

    private static LinkedHashSet<BattleType> battleTypes = new LinkedHashSet<>();

    private String name;
    private ItemStack icon;

    private BattleType(String name, ItemStack icon) {
        this.name = name;
        this.icon = icon;

        battleTypes.add(this);
    }

    public String getName() {
        return name;
    }

    public ItemStack getIcon() {
        return icon;
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

    public static void register(String name, ItemStack icon) {
        new BattleType(name, icon);
    }

    public static BattleType getDefault() {
        return values().get(0);
    }

    public static BattleType byName(String name) {
        for (BattleType battleType : values())
            if (battleType.getName().equalsIgnoreCase(name))
                return battleType;
        return null;
    }

    public static List<BattleType> values() {
        return new ArrayList<>(battleTypes);
    }
}