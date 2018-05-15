package me.therealdan.galacticwarfront.commands;

import me.therealdan.galacticwarfront.GalacticWarFront;
import me.therealdan.galacticwarfront.mechanics.battle.Arena;
import me.therealdan.galacticwarfront.events.BattleLeaveEvent;
import me.therealdan.galacticwarfront.mechanics.battle.battle.Battle;
import me.therealdan.galacticwarfront.mechanics.party.Party;
import me.therealdan.galacticwarfront.util.WXYZ;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GalacticWarFrontCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("Join")) {
                Battle battle = Battle.get(player);
                if (battle == null) {
                    if (GalacticWarFront.getInstance().getLobby().contains(player)) {
                        GalacticWarFront.getInstance().getLobby().open(player);
                    } else {
                        GalacticWarFront.getInstance().getLobby().join(player);
                    }
                } else {
                    player.sendMessage(GalacticWarFront.MAIN + "Please leave the Battle you are in first.");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("Leave")) {
                Battle battle = Battle.get(player);
                if (battle == null) {
                    player.sendMessage(GalacticWarFront.MAIN + "Your not in a Battle.");
                    return true;
                }
                battle.remove(player, BattleLeaveEvent.Reason.LEAVE);
                GalacticWarFront.getInstance().getLobby().join(player);
            } else if (args[0].equalsIgnoreCase("Party")) {
                party(player, args);
                return true;
            } else if (args[0].equalsIgnoreCase("Lobby") && lobbySetup(player)) {
                if (args.length > 1) {
                    if (args[1].equalsIgnoreCase("Spawnpoint")) {
                        GalacticWarFront.getInstance().getLobby().setSpawnpoint(player.getLocation());
                        player.sendMessage(GalacticWarFront.MAIN + "Set spawnpoint for lobby to your location");
                        return true;
                    }
                }
                player.sendMessage(GalacticWarFront.MAIN + "/GWF Lobby SpawnPoint " + GalacticWarFront.SECOND + "Set Lobby Spawnpoint");
                return true;
            } else if (args[0].equalsIgnoreCase("Arena") && arenaSetup(player)) {
                arena(player, args);
                return true;
            }
        }

        player.sendMessage(GalacticWarFront.MAIN + "/GWF Join " + GalacticWarFront.SECOND + "Join GalacticWarFront Lobby");
        player.sendMessage(GalacticWarFront.MAIN + "/GWF Leave " + GalacticWarFront.SECOND + "Leave current game");
        player.sendMessage(GalacticWarFront.MAIN + "/GWF Party " + GalacticWarFront.SECOND + "Create or Join a Party");
        if (lobbySetup(player)) player.sendMessage(GalacticWarFront.MAIN + "/GWF Lobby " + GalacticWarFront.SECOND + "Setup GalacticWarFront Lobby");
        if (arenaSetup(player)) player.sendMessage(GalacticWarFront.MAIN + "/GWF Arena " + GalacticWarFront.SECOND + "Setup GalacticWarFront Arenas");

        return true;
    }

    private void party(Player player, String[] args) {
        Party party = Party.get(player);
        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("Create") && party == null) {
                new Party(player);
                return;
            } else if (args[1].equalsIgnoreCase("Join") && party == null) {
                try {
                    Player target = Bukkit.getPlayer(args[2]);
                    party = Party.get(target);
                    if (party == null) {
                        player.sendMessage(GalacticWarFront.SECOND + target.getName() + GalacticWarFront.MAIN + " is not in a Party.");
                        return;
                    }
                    if (party.hasInvite(player)) {
                        party.join(player);
                    } else {
                        player.sendMessage(GalacticWarFront.SECOND + target.getName() + GalacticWarFront.MAIN + " hasn't invited you to their Party.");
                    }
                } catch (Exception e) {
                    player.sendMessage(GalacticWarFront.MAIN + "/GWF Party Join [Player]");
                }
                return;
            } else if (args[1].equalsIgnoreCase("Info") && party != null) {
                party.info(player);
                return;
            } else if (args[1].equalsIgnoreCase("ChangeTeam") && party != null) {
                party.changeTeam(player);
                return;
            } else if (args[1].equalsIgnoreCase("Leave") && party != null) {
                party.leave(player);
                return;
            } else if (args[1].equalsIgnoreCase("Invite") && party != null) {
                try {
                    Player target = Bukkit.getPlayer(args[2]);
                    if (Party.get(target) != null) {
                        player.sendMessage(GalacticWarFront.SECOND + target.getName() + GalacticWarFront.MAIN + " already has a Party.");
                        return;
                    }
                    party.invite(target, player);
                } catch (Exception e) {
                    player.sendMessage(GalacticWarFront.MAIN + "/GWF Invite [Player]");
                }
                return;
            } else if (args[1].equalsIgnoreCase("Kick") && party != null) {
                try {
                    Player target = Bukkit.getPlayer(args[2]);
                    if (!party.contains(target)) {
                        player.sendMessage(GalacticWarFront.SECOND + target.getName() + GalacticWarFront.MAIN + " isn't in your Party.");
                        return;
                    }
                    party.kick(target, player);
                } catch (Exception e) {
                    player.sendMessage(GalacticWarFront.MAIN + "/GWF Kick [Player]");
                }
                return;
            }
        }
        if (party == null) player.sendMessage(GalacticWarFront.MAIN + "/GWF Party Create " + GalacticWarFront.SECOND + "Create a Party.");
        if (party == null) player.sendMessage(GalacticWarFront.MAIN + "/GWF Party Join [Player] " + GalacticWarFront.SECOND + "Join a players Party.");
        if (party != null) player.sendMessage(GalacticWarFront.MAIN + "/GWF Party Info " + GalacticWarFront.SECOND + "Check your Party.");
        if (party != null) player.sendMessage(GalacticWarFront.MAIN + "/GWF Party ChangeTeam " + GalacticWarFront.SECOND + "Change Team (Only for Team Battles)");
        if (party != null) player.sendMessage(GalacticWarFront.MAIN + "/GWF Party Leave " + GalacticWarFront.SECOND + "Leave the Party.");
        if (party != null) player.sendMessage(GalacticWarFront.MAIN + "/GWF Party Invite [Player] " + GalacticWarFront.SECOND + "Invite someone to the Party.");
        if (party != null) player.sendMessage(GalacticWarFront.MAIN + "/GWF Party Kick [Player] " + GalacticWarFront.SECOND + "Kick someone from the Party.");
    }

    private void arena(Player player, String[] args) {
        if (args.length > 1) {
            String id = args.length > 2 ? args[2] : null;
            if (args[1].equalsIgnoreCase("List")) {
                if (Arena.values().size() == 0) {
                    player.sendMessage(GalacticWarFront.MAIN + "There are no Arenas.");
                    return;
                }
                StringBuilder arenas = new StringBuilder();
                for (Arena arena : Arena.values())
                    arenas.append(GalacticWarFront.MAIN).append(", ").append(GalacticWarFront.SECOND).append(arena.getID());
                player.sendMessage(GalacticWarFront.MAIN + "Arenas: " + arenas.toString().replaceFirst(", ", ""));
                return;
            } else if (args[1].equalsIgnoreCase("Create")) {
                if (id == null) {
                    player.sendMessage(GalacticWarFront.MAIN + "/GWF Arena Create [ID] [Name]");
                    return;
                }
                if (Arena.get(id) != null) {
                    player.sendMessage(GalacticWarFront.MAIN + "An Arena with that ID already exists.");
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
                player.sendMessage(GalacticWarFront.MAIN + "Created new Arena with ID: " + GalacticWarFront.SECOND + arena.getID());
                return;
            } else if (args[1].equalsIgnoreCase("Delete")) {
                if (id == null) {
                    player.sendMessage(GalacticWarFront.MAIN + "/GWF Arena Delete [ID]");
                    return;
                }
                Arena arena = Arena.get(id);
                if (arena == null) {
                    player.sendMessage(GalacticWarFront.MAIN + "No Arena with that ID exists.");
                    return;
                }
                arena.delete();
                player.sendMessage(GalacticWarFront.MAIN + "Permanently deleted Arena " + GalacticWarFront.SECOND + arena.getID());
                return;
            } else if (args[1].equalsIgnoreCase("Edit")) {
                if (id == null) {
                    player.sendMessage(GalacticWarFront.MAIN + "/GWF Arena Edit [ID]");
                    return;
                }
                Arena arena = Arena.get(id);
                if (arena == null) {
                    player.sendMessage(GalacticWarFront.MAIN + "No Arena with that ID exists.");
                    return;
                }
                if (args.length > 3) {
                    if (args[3].equalsIgnoreCase("Name") && args.length > 4) {
                        String name = "";
                        for (int i = 4; i < args.length; i++)
                            name += ", " + args[i];
                        name = name.replaceFirst(", ", "");
                        arena.setName(name);
                        player.sendMessage(GalacticWarFront.MAIN + "Set Arena " + GalacticWarFront.SECOND + arena.getID() + GalacticWarFront.MAIN + " name to: " + GalacticWarFront.SECOND + arena.getName());
                        return;
                    } else if (args[3].equalsIgnoreCase("Icon")) {
                        ItemStack itemStack = player.getItemInHand();
                        if (itemStack == null || itemStack.getType().equals(Material.AIR)) {
                            player.sendMessage(GalacticWarFront.MAIN + "Please hold the item you want to use as an icon.");
                            return;
                        }
                        arena.setIcon(itemStack.getType(), itemStack.getDurability());
                        player.sendMessage(GalacticWarFront.MAIN + "Set Arena " + GalacticWarFront.SECOND + arena.getID() + GalacticWarFront.MAIN + " icon to: " + GalacticWarFront.SECOND + arena.getMaterial().toString());
                        return;
                    } else if (args[3].equalsIgnoreCase("SpawnPoints")) {
                        if (args.length > 5) {
                            String type = args[5].toLowerCase();
                            if (type.equalsIgnoreCase("General") || type.equalsIgnoreCase("Team1") || type.equalsIgnoreCase("Team2")) {
                                List<WXYZ> spawnpoints = arena.getSpawnpoints(type);
                                if (args[4].equalsIgnoreCase("List")) {
                                    if (spawnpoints.size() == 0) {
                                        player.sendMessage(GalacticWarFront.MAIN + "There are no Spawnpoints for " + GalacticWarFront.SECOND + type);
                                        return;
                                    }
                                    player.sendMessage(GalacticWarFront.MAIN + "SpawnPoints for " + type + ":");
                                    for (WXYZ wxyz : spawnpoints)
                                        player.sendMessage(GalacticWarFront.SECOND + wxyz.getFormat());
                                    return;
                                } else if (args[4].equalsIgnoreCase("Add")) {
                                    switch (type) {
                                        case "general":
                                            arena.addSpawnpoint(player.getLocation());
                                            player.sendMessage(GalacticWarFront.MAIN + "Added your location to " + GalacticWarFront.SECOND + type + GalacticWarFront.MAIN + " spawnpoints.");
                                            break;
                                        case "team1":
                                            arena.addTeam1Spawnpoint(player.getLocation());
                                            player.sendMessage(GalacticWarFront.MAIN + "Added your location to " + GalacticWarFront.SECOND + type + GalacticWarFront.MAIN + " spawnpoints.");
                                            break;
                                        case "team2":
                                            arena.addTeam2Spawnpoint(player.getLocation());
                                            player.sendMessage(GalacticWarFront.MAIN + "Added your location to " + GalacticWarFront.SECOND + type + GalacticWarFront.MAIN + " spawnpoints.");
                                            break;
                                        default:
                                            player.sendMessage(GalacticWarFront.MAIN + "Invalid Type: " + GalacticWarFront.SECOND + type);
                                            break;
                                    }
                                    return;
                                } else if (args[4].equalsIgnoreCase("Clear")) {
                                    if (spawnpoints.size() == 0) {
                                        player.sendMessage(GalacticWarFront.MAIN + "There are no Spawnpoints for " + GalacticWarFront.SECOND + type);
                                        return;
                                    }
                                    arena.clearSpawnpoints(type);
                                    player.sendMessage(GalacticWarFront.MAIN + "Removed all spawnpoints for " + GalacticWarFront.SECOND + type);
                                    return;
                                }
                            }
                        }
                        player.sendMessage(GalacticWarFront.MAIN + "/GWF Arena Edit [ID] SpawnPoints List General:TeamBattle:Team2");
                        player.sendMessage(GalacticWarFront.MAIN + "/GWF Arena Edit [ID] SpawnPoints Add General:Team1:Team2");
                        player.sendMessage(GalacticWarFront.MAIN + "/GWF Arena Edit [ID] SpawnPoints Clear General:Team1:Team2");
                        return;
                    }
                }
                player.sendMessage(GalacticWarFront.MAIN + "/GWF Arena Edit [ID] Name [Name] " + GalacticWarFront.SECOND + "Change the Arenas name");
                player.sendMessage(GalacticWarFront.MAIN + "/GWF Arena Edit [ID] Icon " + GalacticWarFront.SECOND + "Change the Arenas Icon");
                player.sendMessage(GalacticWarFront.MAIN + "/GWF Arena Edit [ID] SpawnPoints " + GalacticWarFront.SECOND + "Manage Spawnpoints");
                return;
            }
        }
        player.sendMessage(GalacticWarFront.MAIN + "/GWF Arena List " + GalacticWarFront.SECOND + "List existing Arenas");
        player.sendMessage(GalacticWarFront.MAIN + "/GWF Arena Create [ID] [Name] " + GalacticWarFront.SECOND + "Create a new Arena");
        player.sendMessage(GalacticWarFront.MAIN + "/GWF Arena Delete [ID] " + GalacticWarFront.SECOND + "Permanently delete an Arena");
        player.sendMessage(GalacticWarFront.MAIN + "/GWF Arena Edit [ID] " + GalacticWarFront.SECOND + "Edit an existing Arena");
    }

    private boolean lobbySetup(Player player) {
        return player.hasPermission("galacticwarfront.commands.galacticwarfront.lobby");
    }

    private boolean arenaSetup(Player player) {
        return player.hasPermission("galacticwarfront.commands.galacticwarfront.arena");
    }
}