package me.therealdan.battlearena.mechanics.setup.settings;

import me.therealdan.battlearena.mechanics.battle.Battle;
import me.therealdan.battlearena.mechanics.setup.Setting;
import org.bukkit.Material;
import org.bukkit.entity.Player;

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
        return Material.STAINED_GLASS_PANE;
    }

    @Override
    public short getDurability() {
        return (short) (isOpen() ? 5 : 14);
    }

    @Override
    public String getDescription() {
        return "&7Open: " + (isOpen() ? "&atrue" : "&cfalse");
    }

    public boolean isOpen() {
        return (boolean) getValue();
    }

    @Override
    public Open clone() {
        return new Open(isOpen());
    }
}