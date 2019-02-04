package me.therealdan.battlearena.mechanics.setup.settings;

import me.therealdan.battlearena.mechanics.battle.Battle;
import me.therealdan.battlearena.mechanics.setup.Setting;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BattleDuration extends Setting {

    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("mm:ss");
    private final static long MAX_BATTLE_DURATION = 15 * 60;

    public BattleDuration(long seconds) {
        super("Battle Duration", seconds * 1000);
    }

    @Override
    public void apply(Battle battle) {
        battle.setTimeRemaining(getBattleDurationInSeconds() - (battle.getTimePassed() / 1000));
    }

    @Override
    public boolean click(Player player, boolean shift, boolean left) {
        long battleDuration = getBattleDurationInSeconds();
        battleDuration += (left ? 1 : -1) * (shift ? 1 : 60);
        set(Math.min(Math.max(battleDuration, 0), MAX_BATTLE_DURATION) * 1000);
        return false;
    }

    @Override
    public Material getMaterial() {
        return Material.WATCH;
    }

    @Override
    public String getDescription() {
        Date date = new Date();
        date.setHours(0);
        date.setTime(getBattleDurationInSeconds() * 1000);
        return "&7Time Limit: &f" + DATE_FORMAT.format(date);
    }

    public long getBattleDurationInSeconds() {
        return ((long) getValue()) / 1000;
    }

    @Override
    public Object clone() {
        return new BattleDuration(getBattleDurationInSeconds());
    }
}