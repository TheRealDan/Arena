package me.therealdan.galacticwarfront.mechanics.party;

import me.therealdan.galacticwarfront.GalacticWarFront;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class PartyHandler implements Listener {

    public PartyHandler() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(GalacticWarFront.getInstance(), new Runnable() {
            @Override
            public void run() {
                task();
            }
        }, 20, 20);
    }

    private void task() {
        for (Party party : Party.values())
            if (party.getPlayers().size() == 0)
                party.disband();
    }
}