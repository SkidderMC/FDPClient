package net.skiddermc.fdpclient.utils;

import net.skiddermc.fdpclient.event.EntityKilledEvent;
import net.skiddermc.fdpclient.event.EventTarget;
import net.skiddermc.fdpclient.event.Listenable;
import net.minecraft.entity.player.EntityPlayer;

public class StatisticsUtils implements Listenable {
    private static int kills;
    private static int deaths;

    @EventTarget
    public void onTargetKilled(EntityKilledEvent e) {
        if (!(e.getTargetEntity() instanceof EntityPlayer)) {
            return;
        }

        kills++;
    }

    public static void addDeaths() {
        deaths++;
    }

    public static int getDeaths() {
        return deaths;
    }

    public static int getKills() {
        return kills;
    }

    @Override
    public boolean handleEvents() { return true; }
}
