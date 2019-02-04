package me.therealdan.battlearena.mechanics.setup;

import me.therealdan.battlearena.mechanics.battle.Battle;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Setting {

    private String name;
    private Object value;

    public Setting(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    /**
     * To be overridden by subclass
     */
    public void apply(Battle battle) {
    }

    /**
     * To be overridden by subclass
     * This method is called when this setting is clicked from main UI
     * If this setting has a UI then open it and return true
     * If this setting does not have a UI then update the setting and return false
     *
     * @param player The player that clicked the UI
     * @param shift  Whether or not shift is held
     * @param left   Whether the click was left or right
     */
    public boolean click(Player player, boolean shift, boolean left) {
        return false;
    }

    /**
     * To be overridden by subclass
     * This method is called when this settings UI is clicked
     *
     * @param player The player that clicked the UI
     * @param icon   The icon that was clicked
     * @param shift  Whether or not shift is held
     * @param left   Whether the click was left or right
     * @return true if you want to hold the player in uiOpen
     * false to go back to he main UI
     */
    public boolean click(Player player, ItemStack icon, boolean shift, boolean left) {
        return false;
    }

    public void set(Object value) {
        this.value = value;
    }

    public Material getMaterial() {
        return Material.GRASS;
    }

    public short getDurability() {
        return 0;
    }

    public String getDescription() {
        return null;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getName() + ": " + getValue();
    }
}