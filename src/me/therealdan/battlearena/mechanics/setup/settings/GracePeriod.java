package me.therealdan.battlearena.mechanics.setup.settings;

import me.therealdan.battlearena.mechanics.battle.Battle;
import me.therealdan.battlearena.mechanics.setup.Setting;
import me.therealdan.battlearena.mechanics.setup.Setup;
import me.therealdan.battlearena.mechanics.setup.SetupHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GracePeriod extends Setting {

    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("mm:ss");

    public GracePeriod(long seconds) {
        super("Grace Period", seconds * 1000);
    }

    @Override
    public void apply(Battle battle) {
        battle.setGracePeriod(getGracePeriodInSeconds() - (battle.getTimePassed() / 1000));
    }

    @Override
    public boolean click(Player player, boolean shift, boolean left) {
        long MAX = 0;
        Setup setup = SetupHandler.getInstance().getSetup(player);
        for (Setting setting : setup.getSettings().values())
            if (setting instanceof BattleDuration)
                MAX = ((BattleDuration) setting).getBattleDurationInSeconds();

        long gracePeriod = getGracePeriodInSeconds();
        gracePeriod += (left ? 1 : -1) * (shift ? 1 : 60);
        set(Math.min(Math.max(gracePeriod, 0), MAX) * 1000);
        return false;
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_CHESTPLATE;
    }

    @Override
    public List<String> getDescription() {
        Date date = new Date(0);
        date.setTime(getGracePeriodInSeconds() * 1000);
        List<String> description = new ArrayList<>();
        description.add("&7Duration: &f" + DATE_FORMAT.format(date));
        return description;
    }

    public long getGracePeriodInSeconds() {
        return ((long) getValue()) / 1000;
    }

    @Override
    public Object clone() {
        return new GracePeriod(getGracePeriodInSeconds());
    }
}