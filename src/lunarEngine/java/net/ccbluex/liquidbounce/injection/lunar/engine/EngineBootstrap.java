/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft.
 * https://github.com/SkidderMC/FDPClient/
 * Lunar compatibility by opZywl
 * Reference: https://lucas-lima.xyz
 */
package net.ccbluex.liquidbounce.injection.lunar.engine;

import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigSource;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import java.lang.reflect.*;
import net.ccbluex.liquidbounce.injection.bootstrap.LunarAccessTransformer;

/** A private engine applies FDP's mixins to Lunar's already baked class bytes at initial definition. */
public final class EngineBootstrap {
    private static IMixinTransformer transformer;
    private EngineBootstrap() { }

    public static int[] memberAccess(String owner, String name, String descriptor, boolean method) throws Exception {
        return MemberMetadata.flags(owner, name, descriptor, method);
    }

    public static void initialize() throws Exception {
        MixinBootstrap.init();
        Method phase = MixinEnvironment.class.getDeclaredMethod("gotoPhase", MixinEnvironment.Phase.class);
        phase.setAccessible(true);
        phase.invoke(null, MixinEnvironment.Phase.DEFAULT);
        MixinEnvironment.getCurrentEnvironment().setSide(MixinEnvironment.Side.CLIENT);
        Mixins.addConfiguration("fdpclient.lunar.mixins.json", (IMixinConfigSource) null);
        Class<?> type = Class.forName("org.spongepowered.asm.mixin.transformer.MixinTransformer");
        Constructor<?> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        transformer = (IMixinTransformer) constructor.newInstance();
    }

    public static synchronized byte[] transform(String name, byte[] code) {
        byte[] accessible = AccessRules.transform(code);
        RuntimeMixinService.CURRENT.get().put(name, accessible);
        try {
            byte[] transformed = transformer.transformClassBytes(name, name, accessible);
            transformed = LunarAccessTransformer.compatibility(transformed);
            GeneratedClasses.publish(transformer, transformed);
            return transformed;
        }
        finally { RuntimeMixinService.CURRENT.get().remove(name); }
    }
}
