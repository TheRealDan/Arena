package me.therealdan.galacticwarfront.mechanics.party;

import me.therealdan.galacticwarfront.GalacticWarFront;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PartyHandler implements Listener {

    private static PartyHandler partyHandler;

    private PartyHandler() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(GalacticWarFront.getInstance(), () -> {
            for (Party party : Party.values()) {
                if (party.getPlayers().size() == 0) {
                    party.disband();
                }
            }
        }, 20, 20);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Party party = Party.byPlayer(player);
        if (party == null) return;

        party.leave(player);
    }

    public static PartyHandler getInstance() {
        if (partyHandler == null) partyHandler = new PartyHandler();
        return partyHandler;
    }
}