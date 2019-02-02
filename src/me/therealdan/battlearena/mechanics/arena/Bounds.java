package me.therealdan.battlearena.mechanics.arena;

import org.bukkit.Location;
import org.bukkit.World;

public class Bounds {

    private Location pos1;
    private Location pos2;

    public Bounds(Location pos1, Location pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    public void setPos1(Location pos1) {
        this.pos1 = pos1;
    }

    public void setPos2(Location pos2) {
        this.pos2 = pos2;
    }

    public boolean contains(Location location) {
        if (!getWorld().equals(location.getWorld())) return false;

        if (location.getX() > getHighX()) return false;
        if (location.getX() < getLowX()) return false;
        if (location.getY() > getHighY()) return false;
        if (location.getY() < getLowY()) return false;
        if (location.getZ() > getHighZ()) return false;
        if (location.getZ() < getLowZ()) return false;

        return true;
    }

    public boolean isAbove(Location location) {
        return location.getY() > getHighY();
    }

    public boolean isBelow(Location location) {
        return location.getY() < getLowY();
    }

    public boolean isThroughSides(Location location) {
        if (location.getX() > getHighX()) return true;
        if (location.getX() < getLowX()) return true;
        if (location.getZ() > getHighZ()) return true;
        if (location.getZ() < getLowZ()) return true;

        return false;
    }

    public int getHighX() {
        return getPos1().getBlockX() > getPos2().getBlockX() ? getPos1().getBlockX() : getPos2().getBlockX();
    }

    public int getLowX() {
        return getPos1().getBlockX() < getPos2().getBlockX() ? getPos1().getBlockX() : getPos2().getBlockX();
    }

    public int getHighY() {
        return getPos1().getBlockY() > getPos2().getBlockY() ? getPos1().getBlockY() : getPos2().getBlockY();
    }

    public int getLowY() {
        return getPos1().getBlockY() < getPos2().getBlockY() ? getPos1().getBlockY() : getPos2().getBlockY();
    }

    public int getHighZ() {
        return getPos1().getBlockZ() > getPos2().getBlockZ() ? getPos1().getBlockZ() : getPos2().getBlockZ();
    }

    public int getLowZ() {
        return getPos1().getBlockZ() < getPos2().getBlockZ() ? getPos1().getBlockZ() : getPos2().getBlockZ();
    }

    @Override
    public String toString() {
        return getPos1().getBlockX() + "x, " + getPos1().getBlockY() + "y, " + getPos1().getBlockZ() + "z / " + getPos2().getBlockX() + "x, " + getPos2().getBlockY() + "y, " + getPos2().getBlockZ() + "z";
    }

    public Location getCenter() {
        return new Location(
                getWorld(),
                (getHighX() + getLowX()) / 2.0,
                (getHighY() + getLowY()) / 2.0,
                (getHighZ() + getLowZ()) / 2.0
        );
    }

    public Location getPos1() {
        return pos1;
    }

    public Location getPos2() {
        return pos2;
    }

    public World getWorld() {
        return getPos1().getWorld();
    }
}