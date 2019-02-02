package me.therealdan.battlearena.commands;

import me.therealdan.battlearena.BattleArena;
import me.therealdan.battlearena.events.BattleLeaveEvent;
import me.therealdan.battlearena.mechanics.arena.Arena;
import me.therealdan.battlearena.mechanics.arena.editors.BoundsEditor;
import me.therealdan.battlearena.mechanics.arena.editors.ConsequenceEditor;
import me.therealdan.battlearena.mechanics.arena.editors.LocationsEditor;
import me.therealdan.battlearena.mechanics.battle.Battle;
import me.therealdan.battlearena.mechanics.lobby.BattleCreator;
import me.therealdan.battlearena.mechanics.lobby.Lobby;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BattleArenaCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {
        Player target = sender instanceof Player ? (Player) sender : null;

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("Join")) {
                join(sender, target);
                return true;

            } else if (args[0].equalsIgnoreCase("Create")) {
                create(sender, target);
                return true;

            } else if (args[0].equalsIgnoreCase("Leave")) {
                leave(sender, target);
                return true;

            } else if (args[0].equalsIgnoreCase("Lobby") && lobbySetup(sender)) {
                lobby(sender, target, args);
                return true;

            } else if (args[0].equalsIgnoreCase("Arena") && arenaSetup(sender)) {
                arena(sender, target, args);
                return true;
            }
        }

        sender.sendMessage(BattleArena.MAIN + ChatColor.STRIKETHROUGH + "-----" + BattleArena.MAIN + " BattleArena " + ChatColor.STRIKETHROUGH + "-----");
        sender.sendMessage(BattleArena.MAIN + "/BA Join " + BattleArena.SECOND + "Join a Game");
        sender.sendMessage(BattleArena.MAIN + "/BA Create " + BattleArena.SECOND + "Create a game");
        sender.sendMessage(BattleArena.MAIN + "/BA Leave " + BattleArena.SECOND + "Leave current game");
        if (lobbySetup(sender)) sender.sendMessage(BattleArena.MAIN + "/BA Lobby " + BattleArena.SECOND + "Setup BattleArena Lobby");
        if (arenaSetup(sender)) sender.sendMessage(BattleArena.MAIN + "/BA Arena " + BattleArena.SECOND + "Setup BattleArena Arenas");

        return true;
    }

    private void join(CommandSender sender, Player target) {
        if (target == null) {
            sender.sendMessage(BattleArena.MAIN + "Only players can use this command.");
            return;
        }

        Battle battle = Battle.get(target);
        if (battle == null) {
            if (!Lobby.getInstance().contains(target))
                Lobby.getInstance().join(target);
            Lobby.getInstance().open(target);
        } else {
            sender.sendMessage(BattleArena.MAIN + "Please leave the Battle you are in first.");
        }
    }

    private void create(CommandSender sender, Player target) {
        if (target == null) {
            sender.sendMessage(BattleArena.MAIN + "Only players can use this command.");
            return;
        }

        if (Battle.get(target) == null) {
            BattleCreator.getInstance().openBattleCreator(target);
        } else {
            sender.sendMessage(BattleArena.MAIN + "Please leave the Battle you are in first.");
        }
    }

    private void leave(CommandSender sender, Player target) {
        if (target == null) {
            sender.sendMessage(BattleArena.MAIN + "Only players can use this command.");
            return;
        }

        Battle battle = Battle.get(target);
        if (battle == null) {
            sender.sendMessage(BattleArena.MAIN + "Your not in a Battle.");
            return;
        }

        battle.remove(target, BattleLeaveEvent.Reason.LEAVE);
        Lobby.getInstance().join(target);
    }

    private void lobby(CommandSender sender, Player target, String[] args) {
        if (target == null) {
            sender.sendMessage(BattleArena.MAIN + "Only players can use this command.");
            return;
        }

        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("Spawnpoint")) {
                Lobby.getInstance().setSpawnpoint(target.getLocation());
                sender.sendMessage(BattleArena.MAIN + "Set spawnpoint for lobby to your location");
                return;
            }
        }
        sender.sendMessage(BattleArena.MAIN + "/BA Lobby SpawnPoint " + BattleArena.SECOND + "Set Lobby Spawnpoint");
    }

    private void arena(CommandSender sender, Player target, String[] args) {
        if (args.length > 1) {
            String id = args.length > 2 ? args[2] : null;
            Arena arena = Arena.get(id);

            if (args[1].equalsIgnoreCase("List")) {
                arenaList(sender);
                return;

            } else if (args[1].equalsIgnoreCase("Create")) {
                arenaCreate(sender, args, arena, id);
                return;

            } else if (args[1].equalsIgnoreCase("Delete")) {
                arenaDelete(sender, arena);
                return;

            } else if (args[1].equalsIgnoreCase("Rename")) {
                arenaRename(sender, args, arena);
                return;

            } else if (args[1].equalsIgnoreCase("Icon")) {
                arenaIcon(sender, target, arena);
                return;

            } else if (args[1].equalsIgnoreCase("Bounds")) {
                arenaBounds(sender, target, arena);
                return;

            } else if (args[1].equalsIgnoreCase("Consequence")) {
                arenaConsequence(sender, target, arena);
                return;

            } else if (args[1].equalsIgnoreCase("Locations")) {
                arenaLocations(sender, target, arena);
                return;
            }
        }
        sender.sendMessage(BattleArena.MAIN + ChatColor.STRIKETHROUGH + "-----" + BattleArena.MAIN + " BattleArena Arena " + ChatColor.STRIKETHROUGH + "-----");
        sender.sendMessage(BattleArena.MAIN + "/BA Arena List " + BattleArena.SECOND + "List existing Arenas");
        sender.sendMessage(BattleArena.MAIN + "/BA Arena Create [ID] [Name] " + BattleArena.SECOND + "Create a new Arena");
        sender.sendMessage(BattleArena.MAIN + "/BA Arena Delete [ID] " + BattleArena.SECOND + "Permanently delete an Arena");
        sender.sendMessage(BattleArena.MAIN + "/BA Arena Rename [ID] [Name] " + BattleArena.SECOND + "Rename an existing Arena");
        sender.sendMessage(BattleArena.MAIN + "/BA Arena Icon [ID] " + BattleArena.SECOND + "Change Arena icon");
        sender.sendMessage(BattleArena.MAIN + "/BA Arena Bounds [ID] " + BattleArena.SECOND + "Edit Arena Bounds");
        sender.sendMessage(BattleArena.MAIN + "/BA Arena Consequence [ID] " + BattleArena.SECOND + "Edit Bounds Consequences");
        sender.sendMessage(BattleArena.MAIN + "/BA Arena Locations [ID] " + BattleArena.SECOND + "Edit Arena Locations");
    }

    private void arenaList(CommandSender sender) {
        if (Arena.values().size() == 0) {
            sender.sendMessage(BattleArena.MAIN + "There are no Arenas.");
            return;
        }
        StringBuilder arenas = new StringBuilder();
        for (Arena each : Arena.values())
            arenas.append(BattleArena.MAIN).append(", ").append(BattleArena.SECOND).append(each.getID());
        sender.sendMessage(BattleArena.MAIN + "Arenas: " + arenas.toString().replaceFirst(", ", ""));
    }

    private void arenaCreate(CommandSender sender, String[] args, Arena arena, String id) {
        if (id == null) {
            sender.sendMessage(BattleArena.MAIN + "/BA Arena Create [ID] [Name]");
            return;
        }
        if (arena != null) {
            sender.sendMessage(BattleArena.MAIN + "An Arena with that ID already exists.");
            return;
        }
        String name = id;
        if (args.length > 3) {
            name = "";
            for (int i = 3; i < args.length; i++)
                name += " " + args[i];
            name = name.replaceFirst(" ", "");
        }
        arena = new Arena(id, name);
        sender.sendMessage(BattleArena.MAIN + "Created new Arena with ID: " + BattleArena.SECOND + arena.getID());
    }

    private void arenaDelete(CommandSender sender, Arena arena) {
        if (arena == null) {
            sender.sendMessage(BattleArena.MAIN + "No Arena with that ID exists.");
            return;
        }
        arena.delete();
        sender.sendMessage(BattleArena.MAIN + "Permanently deleted Arena " + BattleArena.SECOND + arena.getID());
    }

    private void arenaRename(CommandSender sender, String[] args, Arena arena) {
        String name = "";
        for (int i = 3; i < args.length; i++)
            name += ", " + args[i];
        name = name.replaceFirst(", ", "");
        arena.setName(name);
        sender.sendMessage(BattleArena.MAIN + "Renamed Arena " + BattleArena.SECOND + arena.getID() + BattleArena.MAIN + " to: " + BattleArena.SECOND + arena.getName());
    }

    private void arenaIcon(CommandSender sender, Player target, Arena arena) {
        if (target == null) {
            sender.sendMessage(BattleArena.MAIN + "Only players can use this command.");
            return;
        }

        ItemStack itemStack = target.getItemInHand();
        if (itemStack == null || itemStack.getType().equals(Material.AIR)) {
            sender.sendMessage(BattleArena.MAIN + "Please hold the item you want to use as an icon.");
            return;
        }
        arena.setIcon(itemStack.getType(), itemStack.getDurability());
        sender.sendMessage(BattleArena.MAIN + "Set Arena " + BattleArena.SECOND + arena.getID() + BattleArena.MAIN + " icon to: " + BattleArena.SECOND + arena.getMaterial().toString());
    }

    private void arenaBounds(CommandSender sender, Player target, Arena arena) {
        if (target == null) {
            sender.sendMessage(BattleArena.MAIN + "Only players can use this command.");
            return;
        }

        if (BoundsEditor.getInstance().isEditing(target)) {
            BoundsEditor.getInstance().stopEditing(target);
        } else {
            BoundsEditor.getInstance().edit(target, arena);
        }
    }

    private void arenaConsequence(CommandSender sender, Player target, Arena arena) {
        if (target == null) {
            sender.sendMessage(BattleArena.MAIN + "Only players can use this command.");
            return;
        }

        ConsequenceEditor.getInstance().open(target, arena);
    }

    private void arenaLocations(CommandSender sender, Player target, Arena arena) {
        if (target == null) {
            sender.sendMessage(BattleArena.MAIN + "Only players can use this command.");
            return;
        }

        if (LocationsEditor.getInstance().isEditing(target)) {
            LocationsEditor.getInstance().stopEditing(target);
        } else {
            LocationsEditor.getInstance().edit(target, arena);
        }
    }

    private boolean lobbySetup(CommandSender player) {
        return player.hasPermission("battlearena.commands.battlearena.lobby");
    }

    private boolean arenaSetup(CommandSender player) {
        return player.hasPermission("battlearena.commands.battlearena.arena");
    }
}