/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft.
 * https://github.com/SkidderMC/FDPClient/
 * Lunar compatibility by opZywl
 * Reference: https://lucas-lima.xyz
 */
package net.ccbluex.liquidbounce.injection.lunar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.FileResourcePack;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.lang.reflect.InvocationTargetException;

/** Registers FDP assets before the normal Minecraft resource reload. Forge registers its mod pack. */
public final class LunarRuntime {
    private LunarRuntime() { }

    public static void frame() {
        if (Boolean.getBoolean("fdpclient.lunar.late"))
            net.ccbluex.liquidbounce.injection.bootstrap.LateRuntime.releaseUiState(Minecraft.getMinecraft().currentScreen);
    }

    @SuppressWarnings("unchecked")
    public static void prepareEarly() {
        if (!Boolean.getBoolean("fdpclient.lunar.early")) return;
        try {
            Field packs = Minecraft.class.getDeclaredField("field_110449_ao");
            packs.setAccessible(true);
            ((List<Object>) packs.get(Minecraft.getMinecraft())).add(
                new FileResourcePack(new File(System.getProperty("fdpclient.lunar.runtimeJar"))));
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Cannot register FDP's Lunar resources", failure);
        }
    }

    /** Called from Minecraft's task queue after preload, with the current OpenGL context. */
    public static void startLate() throws Exception {
        prepareEarly();
        Minecraft.getMinecraft().refreshResources();
        Class<?> client = Class.forName("net.ccbluex.liquidbounce.FDPClient", true, LunarRuntime.class.getClassLoader());
        Object instance = client.getField("INSTANCE").get(null);
        try { client.getMethod("startClient").invoke(instance); }
        catch (InvocationTargetException failure) { throw new IllegalStateException("FDPClient startup failed", failure.getCause()); }
        if (!Boolean.TRUE.equals(client.getMethod("isStarted").invoke(instance))) {
            Object failure = client.getMethod("getStartupFailure").invoke(instance);
            throw new IllegalStateException("FDPClient did not finish starting: " + failure);
        }
    }

    public static void finishLate() throws Exception {
        if (Minecraft.getMinecraft().theWorld == null) return;
        ClassLoader loader = LunarRuntime.class.getClassLoader();
        Class<?> event = Class.forName("net.ccbluex.liquidbounce.event.Event", true, loader);
        Class<?> worldEvent = Class.forName("net.ccbluex.liquidbounce.event.WorldEvent", true, loader);
        Object world = worldEvent.getConstructor(net.minecraft.client.multiplayer.WorldClient.class)
            .newInstance(Minecraft.getMinecraft().theWorld);
        Class<?> manager = Class.forName("net.ccbluex.liquidbounce.event.EventManager", true, loader);
        manager.getMethod("call", event).invoke(manager.getField("INSTANCE").get(null), world);
    }
}
