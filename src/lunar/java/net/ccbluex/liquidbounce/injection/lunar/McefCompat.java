/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft.
 * https://github.com/SkidderMC/FDPClient/
 * Lunar compatibility by opZywl
 * Reference: https://lucas-lima.xyz
 */
package net.ccbluex.liquidbounce.injection.lunar;

import net.montoyo.mcef.MCEF;
import java.lang.reflect.*;

/** The embedded browser has no Forge bus on Lunar; FDP owns its render loop instead. */
public final class McefCompat {
    public static final boolean enabled = false;
    public static String[] nativePaths = new String[0];
    private static Method renderTick;
    private McefCompat() { }

    public static void tick() {
        if (!Boolean.getBoolean("fdpclient.lunar.early") || MCEF.PROXY == null) return;
        try {
            if (renderTick == null) renderTick = MCEF.PROXY.getClass().getMethod("fdp$renderTick");
            renderTick.invoke(MCEF.PROXY);
        } catch (ReflectiveOperationException failure) { throw new IllegalStateException("Lunar browser frame failed", failure); }
    }

    public static void shutdown() {
        if (Boolean.getBoolean("fdpclient.lunar.early") && MCEF.PROXY != null) MCEF.onMinecraftShutdown();
    }
}
