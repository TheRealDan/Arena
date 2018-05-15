package me.therealdan.galacticwarfront.mechanics.party;

import me.therealdan.galacticwarfront.GalacticWarFront;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class Party {

    private static File file;
    private static FileConfiguration data;
    private static String path = "data/party.yml";

    private static HashSet<Party> parties = new HashSet<>();

    private HashSet<UUID> invites = new HashSet<>();
    private HashSet<UUID> members = new HashSet<>();
    private HashSet<UUID> team2 = new HashSet<>();
    private boolean open = false;

    public Party(Player player) {
        player.sendMessage(GalacticWarFront.MAIN + "You have created a Party.");
        join(player);

        parties.add(this);
    }

    private Party(String id) {
        this.open = getData().getBoolean("Party." + id + ".Open");

        HashSet<UUID> online = new HashSet<>();
        for (Player each : Bukkit.getOnlinePlayers())
            online.add(each.getUniqueId());

        if (getData().contains("Party." + id + ".Invites"))
            for (String uuid : getData().getConfigurationSection("Party." + id + ".Invites").getKeys(false))
                if (online.contains(UUID.fromString(uuid)))
                    invites.add(UUID.fromString(uuid));
        if (getData().contains("Party." + id + ".Members"))
            for (String uuid : getData().getConfigurationSection("Party." + id + ".Members").getKeys(false))
                if (online.contains(UUID.fromString(uuid)))
                    members.add(UUID.fromString(uuid));
        if (getData().contains("Party." + id + ".Team2"))
            for (String uuid : getData().getConfigurationSection("Party." + id + ".Team2").getKeys(false))
                if (online.contains(UUID.fromString(uuid)))
                    team2.add(UUID.fromString(uuid));

        parties.add(this);
    }

    private void save() {
        getData().set("Party." + UUID.randomUUID().toString() + ".Open", open);

        for (UUID uuid : invites)
            getData().set("Party." + UUID.randomUUID().toString() + ".Invites." + uuid, Bukkit.getPlayer(uuid).getName());
        for (UUID uuid : members)
            getData().set("Party." + UUID.randomUUID().toString() + ".Members." + uuid, Bukkit.getPlayer(uuid).getName());
        for (UUID uuid : team2)
            getData().set("Party." + UUID.randomUUID().toString() + ".Team2." + uuid, Bukkit.getPlayer(uuid).getName());
    }

    public void setOpen(boolean open) {
        open = open;
    }

    public void setTeam2(Player player, boolean team2) {
        if (team2) {
            this.team2.add(player.getUniqueId());
        } else {
            this.team2.remove(player.getUniqueId());
        }

        for (Player each : getPlayers())
            each.sendMessage(GalacticWarFront.SECOND + player.getName() + GalacticWarFront.MAIN + " is now on Team " + (isTeam1(player) ? "1" : "2"));
    }

    public void changeTeam(Player player) {
        setTeam2(player, isTeam1(player));
    }

    public void info(Player player) {
        player.sendMessage(GalacticWarFront.MAIN + "-- Your Party --");
        for (Player each : getPlayers())
            player.sendMessage(GalacticWarFront.MAIN + each.getName() + ": " + GalacticWarFront.SECOND + "Team " + (isTeam1(each) ? "1" : "2"));
    }

    public void invite(Player target, Player sender) {
        invites.add(target.getUniqueId());

        for (Player each : getPlayers())
            each.sendMessage(GalacticWarFront.SECOND + sender.getName() + GalacticWarFront.MAIN + " has invited " + GalacticWarFront.SECOND + target.getName() + GalacticWarFront.MAIN + " to the Party.");

        target.sendMessage(GalacticWarFront.SECOND + sender.getName() + GalacticWarFront.MAIN + " has invited you to join their Party.");
    }

    public void kick(Player target, Player sender) {
        leave(target);
        target.sendMessage(GalacticWarFront.SECOND + sender.getName() + GalacticWarFront.MAIN + " has kicked you from the Party.");
    }

    public void join(Player player) {
        members.add(player.getUniqueId());

        for (Player each : getPlayers())
            each.sendMessage(GalacticWarFront.SECOND + player.getName() + GalacticWarFront.MAIN + " has joined the Party.");
    }

    public void leave(Player player) {
        player.sendMessage(GalacticWarFront.MAIN + "You left the Party.");

        members.remove(player.getUniqueId());

        for (Player each : getPlayers())
            each.sendMessage(GalacticWarFront.SECOND + player.getName() + GalacticWarFront.MAIN + " has left the Party.");
    }

    public void disband() {
        for (Player player : getPlayers())
            leave(player);
        parties.remove(this);
    }

    public boolean isOpen() {
        return open;
    }

    public boolean isTeam1(Player player) {
        return !team2.contains(player.getUniqueId());
    }

    public boolean contains(Player player) {
        return members.contains(player.getUniqueId());
    }

    public boolean hasInvite(Player player) {
        return invites.contains(player.getUniqueId());
    }

    public List<Player> getTeam1() {
        List<Player> team1 = new ArrayList<>();
        for (Player player : getPlayers())
            if (isTeam1(player))
                team1.add(player);
        return team1;
    }

    public List<Player> getTeam2() {
        List<Player> team1 = new ArrayList<>();
        for (Player player : getPlayers())
            if (!isTeam1(player))
                team1.add(player);
        return team1;
    }

    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();
        for (UUID uuid : this.members)
            players.add(Bukkit.getPlayer(uuid));
        return players;
    }

    public static Party get(Player player) {
        for (Party party : values())
            if (party.contains(player))
                return party;
        return null;
    }

    public static List<Party> values() {
        return new ArrayList<>(parties);
    }

    public static void load() {
        if (getData().contains("Party")) {
            for (String id : getData().getConfigurationSection("Party").getKeys(false)) {
                try {
                    new Party(id);
                } catch (Exception e) {
                    //
                }
            }
        }
    }

    public static void unload() {
        getData().set("Party", null);

        for (Party party : values())
            party.save();

        saveData();
    }

    private static void saveData() {
        try {
            getData().save(getFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static FileConfiguration getData() {
        if (data == null) data = YamlConfiguration.loadConfiguration(getFile());
        return data;
    }

    private static File getFile() {
        if (file == null) {
            file = new File(GalacticWarFront.getInstance().getDataFolder(), path);
        }
        return file;
    }
}