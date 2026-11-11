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

/** Registers FDP assets before the normal Minecraft resource reload. Forge registers its mod pack. */
public final class LunarRuntime {
    private LunarRuntime() { }

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
}
