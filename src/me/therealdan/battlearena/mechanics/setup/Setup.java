package me.therealdan.battlearena.mechanics.setup;

import me.therealdan.battlearena.BattleArena;
import me.therealdan.battlearena.mechanics.battle.BattleType;
import me.therealdan.battlearena.util.Icon;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Setup {

    private String battleTypeString;
    private BattleType battleType;
    private Settings settings;

    public Setup(String battleType, Settings settings) {
        this.battleTypeString = battleType;
        this.settings = settings;
    }

    public BattleType getBattleType() {
        if (battleType == null) battleType = BattleType.byName(battleTypeString);
        return battleType;
    }

    public ItemStack getBattleTypeIcon() {
        return getBattleType().getIcon();
    }

    public ItemStack getSettingIcon(Setting setting) {
        return Icon.build(setting.getMaterial(), setting.getDurability(), false, BattleArena.MAIN + setting.getName(), setting.getDescription());
    }

    public ItemStack getStartIcon() {
        return Icon.build(Material.DIAMOND_SWORD, 0, false, BattleArena.MAIN + "Start Game");
    }

    public Settings getSettings() {
        return settings;
    }

    @Override
    public Setup clone() {
        return new Setup(getBattleType().getName(), settings.clone());
    }
}