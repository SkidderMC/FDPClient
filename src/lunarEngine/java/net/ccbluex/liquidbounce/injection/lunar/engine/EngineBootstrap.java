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
import java.util.*;
import net.ccbluex.liquidbounce.injection.bootstrap.LunarAccessTransformer;
import net.ccbluex.liquidbounce.injection.bootstrap.LateRuntime;
import net.ccbluex.liquidbounce.injection.bootstrap.LateSchemaTransformer;

/** A private engine applies FDP's mixins to Lunar's already baked class bytes at initial definition. */
public final class EngineBootstrap {
    private static IMixinTransformer transformer;
    private static LateSchemaTransformer.Result late;
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

    public static synchronized Map<String, byte[]> prepareLate(Map<String, byte[]> originals) throws Exception {
        Map<String, byte[]> patched = new LinkedHashMap<>();
        for (Map.Entry<String, byte[]> entry : originals.entrySet()) {
            Object deadline = System.getProperties().get("fdpclient.lunar.late.deadline");
            if (deadline instanceof Long && System.nanoTime() > (Long) deadline)
                throw new IllegalStateException("Runtime mixin preparation exceeded the attachment deadline");
            patched.put(entry.getKey(), transform(entry.getKey().replace('/', '.'), entry.getValue()));
        }
        LateSchemaTransformer.Result prepared = LateSchemaTransformer.prepare(originals, patched, MemberMetadata::commonSuperClass);
        for (Map.Entry<String, byte[]> helper : prepared.helpers.entrySet())
            GeneratedClasses.publish(helper.getKey().replace('/', '.'), helper.getValue());
        LateRuntime.install(prepared.plans, (ClassLoader) System.getProperties().get("fdpclient.lunar.gameLoader"));
        late = prepared;
        System.out.println("[FDPCLIENT] Prepared schema-preserving runtime hooks for " + prepared.definitions.size() + " Minecraft classes");
        return prepared.definitions;
    }

    public static synchronized byte[] transformLate(String name, byte[] original) {
        if (late == null) throw new IllegalStateException("Runtime mixins were not prepared");
        String internal = name.replace('.', '/');
        if (late.helpers.containsKey(internal)) return original;
        byte[] prepared = late.definitions.get(internal);
        if (prepared != null) return prepared;
        // Cached game classes with no FDP hooks keep their original constructor and schema.
        return late.containsOriginal(internal) ? original : adaptLatePayload(original);
    }

    public static synchronized byte[] adaptLatePayload(byte[] code) {
        if (late == null) throw new IllegalStateException("Runtime mixins were not prepared");
        byte[] adapted = LateSchemaTransformer.adaptPayload(late, LunarAccessTransformer.compatibility(code));
        try {
            for (Map.Entry<String, byte[]> helper : late.helpers.entrySet())
                GeneratedClasses.publish(helper.getKey().replace('/', '.'), helper.getValue());
        } catch (ReflectiveOperationException | java.io.IOException failure) {
            throw new IllegalStateException("Could not publish runtime access helpers", failure);
        }
        return adapted;
    }
}
