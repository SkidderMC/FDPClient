/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft.
 * https://github.com/SkidderMC/FDPClient/
 * Lunar compatibility by opZywl
 * Reference: https://lucas-lima.xyz
 */
package net.ccbluex.liquidbounce.injection.lunar.engine;

import org.objectweb.asm.ClassReader;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/** Publishes Args and inner-class helpers for the game's class loader, without defining them early. */
final class GeneratedClasses {
    private static final Set<String> REGISTERED = new HashSet<>();
    private static ConcurrentMap<String, byte[]> classCache;
    private GeneratedClasses() { }

    static void publish(IMixinTransformer transformer, byte[] classFile) {
        ClassReader reader = new ClassReader(classFile);
        char[] buffer = new char[reader.getMaxStringLength()];
        Set<String> references = new HashSet<>();
        for (int i = 1; i < reader.getItemCount(); i++) {
            int item = reader.getItem(i);
            if (item != 0 && reader.readByte(item - 1) == 7) {
                String name = reader.readUTF8(item, buffer);
                if (name != null && !name.startsWith("[") && !name.startsWith("java/")) references.add(name.replace('/', '.'));
            }
        }
        for (String name : references) {
            if (REGISTERED.contains(name)) continue;
            byte[] generated = transformer.generateClass(MixinEnvironment.getCurrentEnvironment(), name);
            if (generated == null) continue;
            try {
                publish(name, generated);
            } catch (ReflectiveOperationException | IOException failure) {
                throw new IllegalStateException("Cannot publish Mixin helper " + name, failure);
            }
        }
    }

    @SuppressWarnings("unchecked")
    static void publish(String name, byte[] generated) throws ReflectiveOperationException, IOException {
        if (REGISTERED.contains(name)) return;
        Path root = Paths.get(System.getProperty("fdpclient.lunar.generatedClasses")).toAbsolutePath().normalize();
        Path file = root.resolve(name.replace('.', '/') + ".class").normalize();
        if (!file.startsWith(root)) throw new IOException("Invalid generated class name: " + name);
        Files.createDirectories(file.getParent());
        Files.write(file, generated);
        byte[] existing = cache().putIfAbsent(name, generated);
        if (existing != null && !Arrays.equals(existing, generated)) throw new IOException("Generated class collision: " + name);
        Object metadata = System.getProperties().get("fdpclient.lunar.bakedClasses");
        if (metadata instanceof Map) ((Map<String, byte[]>) metadata).put(name, generated);
        REGISTERED.add(name);
    }

    @SuppressWarnings("unchecked")
    private static ConcurrentMap<String, byte[]> cache() throws ReflectiveOperationException {
        if (classCache != null) return classCache;
        ClassLoader game = (ClassLoader) System.getProperties().get("fdpclient.lunar.gameLoader");
        Method found = null;
        for (Method method : game.getClass().getMethods()) {
            if (method.getParameterTypes().length == 0 && ConcurrentMap.class.isAssignableFrom(method.getReturnType())) {
                if (found != null) throw new IllegalStateException("Ambiguous Genesis generated-class API");
                found = method;
            }
        }
        if (found == null) throw new NoSuchMethodException("Genesis has no generated-class cache API");
        classCache = (ConcurrentMap<String, byte[]>) found.invoke(game);
        return classCache;
    }
}
