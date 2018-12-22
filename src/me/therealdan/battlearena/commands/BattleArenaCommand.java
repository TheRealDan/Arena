package me.therealdan.battlearena.commands;

import me.therealdan.battlearena.BattleArena;
import me.therealdan.battlearena.events.BattleLeaveEvent;
import me.therealdan.battlearena.mechanics.arena.Arena;
import me.therealdan.battlearena.mechanics.battle.Battle;
import me.therealdan.battlearena.mechanics.lobby.BattleCreator;
import me.therealdan.battlearena.mechanics.lobby.Lobby;
import me.therealdan.battlearena.util.WXYZ;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BattleArenaCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("Join")) {
                Battle battle = Battle.get(player);
                if (battle == null) {
                    if (!Lobby.getInstance().contains(player))
                        Lobby.getInstance().join(player);
                    Lobby.getInstance().open(player);
                } else {
                    player.sendMessage(BattleArena.MAIN + "Please leave the Battle you are in first.");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("Create")) {
                Battle battle = Battle.get(player);
                if (battle == null) {
                    BattleCreator.getInstance().openBattleCreator(player);
                } else {
                    player.sendMessage(BattleArena.MAIN + "Please leave the Battle you are in first.");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("Leave")) {
                Battle battle = Battle.get(player);
                if (battle == null) {
                    player.sendMessage(BattleArena.MAIN + "Your not in a Battle.");
                    return true;
                }
                battle.remove(player, BattleLeaveEvent.Reason.LEAVE);
                Lobby.getInstance().join(player);
                return true;
            } else if (args[0].equalsIgnoreCase("Lobby") && lobbySetup(player)) {
                if (args.length > 1) {
                    if (args[1].equalsIgnoreCase("Spawnpoint")) {
                        Lobby.getInstance().setSpawnpoint(player.getLocation());
                        player.sendMessage(BattleArena.MAIN + "Set spawnpoint for lobby to your location");
                        return true;
                    }
                }
                player.sendMessage(BattleArena.MAIN + "/BA Lobby SpawnPoint " + BattleArena.SECOND + "Set Lobby Spawnpoint");
                return true;
            } else if (args[0].equalsIgnoreCase("Arena") && arenaSetup(player)) {
                arena(player, args);
                return true;
            }
        }

        player.sendMessage(BattleArena.MAIN + "/BA Join " + BattleArena.SECOND + "Join a Game");
        player.sendMessage(BattleArena.MAIN + "/BA Create " + BattleArena.SECOND + "Create a game");
        player.sendMessage(BattleArena.MAIN + "/BA Leave " + BattleArena.SECOND + "Leave current game");
        if (lobbySetup(player)) player.sendMessage(BattleArena.MAIN + "/BA Lobby " + BattleArena.SECOND + "Setup BattleArena Lobby");
        if (arenaSetup(player)) player.sendMessage(BattleArena.MAIN + "/BA Arena " + BattleArena.SECOND + "Setup BattleArena Arenas");

        return true;
    }

    private void arena(Player player, String[] args) {
        if (args.length > 1) {
            String id = args.length > 2 ? args[2] : null;
            if (args[1].equalsIgnoreCase("List")) {
                if (Arena.values().size() == 0) {
                    player.sendMessage(BattleArena.MAIN + "There are no Arenas.");
                    return;
                }
                StringBuilder arenas = new StringBuilder();
                for (Arena arena : Arena.values())
                    arenas.append(BattleArena.MAIN).append(", ").append(BattleArena.SECOND).append(arena.getID());
                player.sendMessage(BattleArena.MAIN + "Arenas: " + arenas.toString().replaceFirst(", ", ""));
                return;
            } else if (args[1].equalsIgnoreCase("Create")) {
                if (id == null) {
                    player.sendMessage(BattleArena.MAIN + "/BA Arena Create [ID] [Name]");
                    return;
                }
                if (Arena.get(id) != null) {
                    player.sendMessage(BattleArena.MAIN + "An Arena with that ID already exists.");
                    return;
                }
                String name = id;
                if (args.length > 3) {
                    name = "";
                    for (int i = 3; i < args.length; i++)
                        name += " " + args[i];
                    name = name.replaceFirst(" ", "");
                }
                Arena arena = new Arena(id, name);
                player.sendMessage(BattleArena.MAIN + "Created new Arena with ID: " + BattleArena.SECOND + arena.getID());
                return;
            } else if (args[1].equalsIgnoreCase("Delete")) {
                if (id == null) {
                    player.sendMessage(BattleArena.MAIN + "/BA Arena Delete [ID]");
                    return;
                }
                Arena arena = Arena.get(id);
                if (arena == null) {
                    player.sendMessage(BattleArena.MAIN + "No Arena with that ID exists.");
                    return;
                }
                arena.delete();
                player.sendMessage(BattleArena.MAIN + "Permanently deleted Arena " + BattleArena.SECOND + arena.getID());
                return;
            } else if (args[1].equalsIgnoreCase("Edit")) {
                if (id == null) {
                    player.sendMessage(BattleArena.MAIN + "/BA Arena Edit [ID]");
                    return;
                }
                Arena arena = Arena.get(id);
                if (arena == null) {
                    player.sendMessage(BattleArena.MAIN + "No Arena with that ID exists.");
                    return;
                }
                if (args.length > 3) {
                    if (args[3].equalsIgnoreCase("Name") && args.length > 4) {
                        String name = "";
                        for (int i = 4; i < args.length; i++)
                            name += ", " + args[i];
                        name = name.replaceFirst(", ", "");
                        arena.setName(name);
                        player.sendMessage(BattleArena.MAIN + "Set Arena " + BattleArena.SECOND + arena.getID() + BattleArena.MAIN + " name to: " + BattleArena.SECOND + arena.getName());
                        return;
                    } else if (args[3].equalsIgnoreCase("Icon")) {
                        ItemStack itemStack = player.getItemInHand();
                        if (itemStack == null || itemStack.getType().equals(Material.AIR)) {
                            player.sendMessage(BattleArena.MAIN + "Please hold the item you want to use as an icon.");
                            return;
                        }
                        arena.setIcon(itemStack.getType(), itemStack.getDurability());
                        player.sendMessage(BattleArena.MAIN + "Set Arena " + BattleArena.SECOND + arena.getID() + BattleArena.MAIN + " icon to: " + BattleArena.SECOND + arena.getMaterial().toString());
                        return;
                    } else if (args[3].equalsIgnoreCase("SpawnPoints")) {
                        if (args.length > 5) {
                            String type = args[5].toLowerCase();
                            if (type.equalsIgnoreCase("General") || type.equalsIgnoreCase("Team1") || type.equalsIgnoreCase("Team2")) {
                                List<WXYZ> spawnpoints = arena.getSpawnpoints(type);
                                if (args[4].equalsIgnoreCase("List")) {
                                    if (spawnpoints.size() == 0) {
                                        player.sendMessage(BattleArena.MAIN + "There are no Spawnpoints for " + BattleArena.SECOND + type);
                                        return;
                                    }
                                    player.sendMessage(BattleArena.MAIN + "SpawnPoints for " + type + ":");
                                    for (WXYZ wxyz : spawnpoints)
                                        player.sendMessage(BattleArena.SECOND + wxyz.getFormat());
                                    return;
                                } else if (args[4].equalsIgnoreCase("Add")) {
                                    switch (type) {
                                        case "general":
                                            arena.addSpawnpoint(player.getLocation());
                                            player.sendMessage(BattleArena.MAIN + "Added your location to " + BattleArena.SECOND + type + BattleArena.MAIN + " spawnpoints.");
                                            break;
                                        case "team1":
                                            arena.addTeam1Spawnpoint(player.getLocation());
                                            player.sendMessage(BattleArena.MAIN + "Added your location to " + BattleArena.SECOND + type + BattleArena.MAIN + " spawnpoints.");
                                            break;
                                        case "team2":
                                            arena.addTeam2Spawnpoint(player.getLocation());
                                            player.sendMessage(BattleArena.MAIN + "Added your location to " + BattleArena.SECOND + type + BattleArena.MAIN + " spawnpoints.");
                                            break;
                                        default:
                                            player.sendMessage(BattleArena.MAIN + "Invalid Type: " + BattleArena.SECOND + type);
                                            break;
                                    }
                                    return;
                                } else if (args[4].equalsIgnoreCase("Clear")) {
                                    if (spawnpoints.size() == 0) {
                                        player.sendMessage(BattleArena.MAIN + "There are no Spawnpoints for " + BattleArena.SECOND + type);
                                        return;
                                    }
                                    arena.clearSpawnpoints(type);
                                    player.sendMessage(BattleArena.MAIN + "Removed all spawnpoints for " + BattleArena.SECOND + type);
                                    return;
                                }
                            }
                        }
                        player.sendMessage(BattleArena.MAIN + "/BA Arena Edit [ID] SpawnPoints List General:Team:Team2");
                        player.sendMessage(BattleArena.MAIN + "/BA Arena Edit [ID] SpawnPoints Add General:Team1:Team2");
                        player.sendMessage(BattleArena.MAIN + "/BA Arena Edit [ID] SpawnPoints Clear General:Team1:Team2");
                        return;
                    }
                }
                player.sendMessage(BattleArena.MAIN + "/BA Arena Edit [ID] Name [Name] " + BattleArena.SECOND + "Change the Arenas name");
                player.sendMessage(BattleArena.MAIN + "/BA Arena Edit [ID] Icon " + BattleArena.SECOND + "Change the Arenas Icon");
                player.sendMessage(BattleArena.MAIN + "/BA Arena Edit [ID] SpawnPoints " + BattleArena.SECOND + "Manage Spawnpoints");
                return;
            }
        }
        player.sendMessage(BattleArena.MAIN + "/BA Arena List " + BattleArena.SECOND + "List existing Arenas");
        player.sendMessage(BattleArena.MAIN + "/BA Arena Create [ID] [Name] " + BattleArena.SECOND + "Create a new Arena");
        player.sendMessage(BattleArena.MAIN + "/BA Arena Delete [ID] " + BattleArena.SECOND + "Permanently delete an Arena");
        player.sendMessage(BattleArena.MAIN + "/BA Arena Edit [ID] " + BattleArena.SECOND + "Edit an existing Arena");
    }

    private boolean lobbySetup(Player player) {
        return player.hasPermission("battlearena.commands.battlearena.lobby");
    }

    private boolean arenaSetup(Player player) {
        return player.hasPermission("battlearena.commands.battlearena.arena");
    }
}