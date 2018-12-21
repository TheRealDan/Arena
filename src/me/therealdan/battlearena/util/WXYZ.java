package me.therealdan.battlearena.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class WXYZ {

    private Location location;
    private String wxyz;

    public WXYZ(Location location) {
        this.location = location;
        this.wxyz = location.getWorld().getName() + ";" +
                (location.getX() + ";" +
                        location.getY() + ";" +
                        location.getZ() + ";" +
                        location.getYaw() + ";" +
                        location.getPitch()).replace(".", ",");
    }

    public WXYZ(String wxyz) {
        this.wxyz = wxyz;

        String[] args = wxyz.replace(",", ".").split(";");
        this.location = new Location(Bukkit.getWorld(args[0]),
                Double.parseDouble(args[1]),
                Double.parseDouble(args[2]),
                Double.parseDouble(args[3]),
                Float.parseFloat(args[4]),
                Float.parseFloat(args[5])
        );
    }

    public Location getLocation() {
        return location;
    }

    public String getWxyz() {
        return wxyz;
    }

    public String getFormat() {
        return getLocation().getWorld().getName() + ", " + getLocation().getBlockX() + "x, " + getLocation().getBlockY() + "y, " + getLocation().getBlockZ() + "z";
    }

    @Override
    public String toString() {
        return getWxyz();
    }
}