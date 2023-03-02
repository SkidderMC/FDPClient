package net.ccbluex.liquidbounce.utils;

import net.ccbluex.liquidbounce.event.*;
import net.ccbluex.liquidbounce.utils.timer.MSTimer;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;

public class SessionUtils extends MinecraftInstance implements Listenable {

    private static final MSTimer sessionTimer = new MSTimer();
    private static final MSTimer worldTimer = new MSTimer();

    public static long lastSessionTime = 0L;
    public static long backupSessionTime = 0L;
    public static long lastWorldTime = 0L;

    private static boolean requireDelay = false;

    private static GuiScreen lastScreen = null;

    @EventTarget
    public void onWorld(WorldEvent event) {
        lastWorldTime = System.currentTimeMillis() - worldTimer.getTime();
        worldTimer.reset();

        if (event.getWorldClient() == null) {
            backupSessionTime = System.currentTimeMillis() - sessionTimer.getTime();
            requireDelay = true;
        } else {
            requireDelay = false;
        }
    }

    @EventTarget
    public void onSession(SessionEvent event) {
        handleConnection();
    }

    @EventTarget
    public void onScreen(ScreenEvent event) {
        if (event.getGuiScreen() == null && lastScreen != null && (lastScreen instanceof GuiDownloadTerrain || lastScreen instanceof GuiConnecting))
            handleReconnection();

        lastScreen = event.getGuiScreen();
    }

    public static void handleConnection() {
        backupSessionTime = 0L;
        requireDelay = true;
        lastSessionTime = System.currentTimeMillis() - sessionTimer.getTime();
        if (lastSessionTime < 0L) lastSessionTime = 0L;
        sessionTimer.reset();
    }

    public static void handleReconnection() {
        if (requireDelay) sessionTimer.setTime(System.currentTimeMillis() - backupSessionTime);
    }

    public static String getFormatSessionTime() {
        if (System.currentTimeMillis() - sessionTimer.getTime() < 0L) sessionTimer.reset();

        int realTime = (int) (System.currentTimeMillis() - sessionTimer.getTime()) / 1000;
        int hours = realTime / 3600;
        int seconds = (realTime % 3600) % 60;
        int minutes = (realTime % 3600) / 60;

        return hours + "h " + minutes + "m " + seconds + "s";
    }

    @Override
    public boolean handleEvents() {
        return true;
    }

}