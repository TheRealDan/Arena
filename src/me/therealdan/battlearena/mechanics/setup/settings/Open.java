package me.therealdan.battlearena.mechanics.setup.settings;

import me.therealdan.battlearena.mechanics.battle.Battle;
import me.therealdan.battlearena.mechanics.setup.Setting;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Open extends Setting {

    public Open(boolean open) {
        super("Open", open);
    }

    @Override
    public void apply(Battle battle) {
        battle.setOpen(isOpen());
    }

    @Override
    public boolean click(Player player, boolean shift, boolean left) {
        set(!isOpen());
        return false;
    }

    @Override
    public Material getMaterial() {
        return isOpen() ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
    }

    @Override
    public List<String> getDescription() {
        List<String> description = new ArrayList<>();
        description.add("&7Open: " + (isOpen() ? "&atrue" : "&cfalse"));
        return description;
    }

    public boolean isOpen() {
        return (boolean) getValue();
    }

    @Override
    public Open clone() {
        return new Open(isOpen());
    }
}