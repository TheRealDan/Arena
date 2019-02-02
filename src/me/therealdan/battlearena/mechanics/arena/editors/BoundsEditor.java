package me.therealdan.battlearena.mechanics.arena.editors;

import me.therealdan.battlearena.BattleArena;
import me.therealdan.battlearena.mechanics.arena.Arena;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.UUID;

public class BoundsEditor implements Listener {

    private static BoundsEditor boundsEditor;

    private HashMap<UUID, Arena> editing = new HashMap<>();

    private BoundsEditor() {
    }

    @EventHandler
    public void onQuit(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!isEditing(player)) return;
        stopEditing(player);
    }

    @EventHandler
    public void onSetPos1(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!isEditing(player)) return;
        event.setCancelled(true);

        Arena arena = getEditing(player);
        Location location = event.getBlock().getLocation();
        arena.getBounds().setPos1(location);
        player.sendMessage(BattleArena.MAIN + "Position 1 for " + BattleArena.SECOND + arena.getID() + BattleArena.MAIN + " set");
    }

    @EventHandler
    public void onSetPos2(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isEditing(player)) return;
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        event.setCancelled(true);

        Arena arena = getEditing(player);
        Location location = event.getClickedBlock().getLocation();
        arena.getBounds().setPos2(location);
        player.sendMessage(BattleArena.MAIN + "Position 2 for " + BattleArena.SECOND + arena.getID() + BattleArena.MAIN + " set");
    }

    public void edit(Player player, Arena arena) {
        if (isEditing(player)) stopEditing(player);
        editing.put(player.getUniqueId(), arena);
        player.sendMessage(BattleArena.MAIN + "You are now editing bounds for arena " + BattleArena.SECOND + arena.getID());
        player.sendMessage(BattleArena.MAIN + "Use left and right click to set position 1 and position 2");
    }

    public void stopEditing(Player player) {
        if (isEditing(player)) player.sendMessage(BattleArena.MAIN + "You are no longer editing bounds for arena " + BattleArena.SECOND + getEditing(player).getID());
        editing.remove(player.getUniqueId());
    }

    public boolean isEditing(Player player) {
        return getEditing(player) != null;
    }

    public Arena getEditing(Player player) {
        return editing.getOrDefault(player.getUniqueId(), null);
    }

    public static BoundsEditor getInstance() {
        if (boundsEditor == null) boundsEditor = new BoundsEditor();
        return boundsEditor;
    }
}