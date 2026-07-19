/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.client;

import java.io.File;

/**
 * Detects platforms where the bundled in-game browser can never run because no CEF natives
 * exist for them (Android launchers, ARM devices, anything that is not a desktop OS).
 */
public final class McefPlatform {

    private static Boolean unsupported;

    private McefPlatform() {
    }

    public static synchronized boolean isUnsupported() {
        if (unsupported == null) {
            unsupported = detect();
        }
        return unsupported;
    }

    private static boolean detect() {
        String os = System.getProperty("os.name", "").toLowerCase();
        String arch = System.getProperty("os.arch", "").toLowerCase();
        String vendor = System.getProperty("java.vendor", "").toLowerCase();

        boolean arm = arch.contains("aarch64") || arch.startsWith("arm");
        boolean android = vendor.contains("android")
                || System.getenv("POJAV_ENVIRON") != null
                || System.getenv("ANDROID_ROOT") != null
                || new File("/system/build.prop").isFile();
        boolean desktopOs = os.contains("win") || os.contains("mac") || os.contains("linux");

        return android || arm || !desktopOs;
    }
}
