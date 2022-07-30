/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.skiddermc.fdpclient.utils;

import net.skiddermc.fdpclient.event.EventTarget;
import net.skiddermc.fdpclient.event.Listenable;
import net.skiddermc.fdpclient.event.ScreenEvent;
import net.skiddermc.fdpclient.event.SessionEvent;
import net.skiddermc.fdpclient.event.WorldEvent;
import net.skiddermc.fdpclient.utils.timer.MSTimer;

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
        int hours = (int) realTime / 3600;
        int seconds = (realTime % 3600) % 60;
        int minutes = (int) (realTime % 3600) / 60;

        return hours + "h " + minutes + "m " + seconds + "s";
    }

    public static String getFormatLastSessionTime() {
        if (lastSessionTime < 0L) lastSessionTime = 0L;

        int realTime = (int) lastSessionTime / 1000;
        int hours = (int) realTime / 3600;
        int seconds = (realTime % 3600) % 60;
        int minutes = (int) (realTime % 3600) / 60;

        return hours + "h " + minutes + "m " + seconds + "s";
    }

    public static String getFormatWorldTime() {
        if (System.currentTimeMillis() - worldTimer.getTime() < 0L) worldTimer.reset();

        int realTime = (int) (System.currentTimeMillis() - worldTimer.getTime()) / 1000;
        int hours = (int) realTime / 3600;
        int seconds = (realTime % 3600) % 60;
        int minutes = (int) (realTime % 3600) / 60;

        return hours + "h " + minutes + "m " + seconds + "s";
    }

    @Override
    public boolean handleEvents() {
        return true;
    }

}