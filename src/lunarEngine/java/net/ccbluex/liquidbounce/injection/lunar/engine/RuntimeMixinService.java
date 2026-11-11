/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft.
 * https://github.com/SkidderMC/FDPClient/
 * Lunar compatibility by opZywl
 * Reference: https://lucas-lima.xyz
 */
package net.ccbluex.liquidbounce.injection.lunar.engine;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.platform.container.*;
import org.spongepowered.asm.logging.*;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.service.*;
import org.spongepowered.asm.util.ReEntranceLock;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;

public final class RuntimeMixinService implements IMixinService, IClassProvider, IClassBytecodeProvider {
    static final ThreadLocal<Map<String, byte[]>> CURRENT = ThreadLocal.withInitial(HashMap::new);
    private final ReEntranceLock lock = new ReEntranceLock(1);
    private final IContainerHandle container = new ContainerHandleVirtual("FDP Lunar agent");
    private final Map<String, ILogger> loggers = new HashMap<>();
    private final JarFile baked;
    private final Map<String, byte[]> live;

    @SuppressWarnings("unchecked")
    public RuntimeMixinService() {
        live = (Map<String, byte[]>) System.getProperties().get("fdpclient.lunar.bakedClasses");
        try {
            String path = System.getProperty("fdpclient.lunar.bakeCache");
            baked = path == null ? null : new JarFile(path);
            if (baked == null && live == null) throw new IOException("No matched Lunar bytecode cache");
            if (baked != null) Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try { baked.close(); } catch (IOException ignored) { }
                }, "FDP Lunar metadata cleanup"));
        } catch (IOException failure) { throw new IllegalStateException("Cannot open Lunar's matched bytecode cache", failure); }
    }

    private static ClassLoader game() { return (ClassLoader) System.getProperties().get("fdpclient.lunar.gameLoader"); }
    public String getName() { return "FDP Lunar agent"; }
    public boolean isValid() { return game() != null; }
    public void prepare() { }
    public MixinEnvironment.Phase getInitialPhase() { return MixinEnvironment.Phase.DEFAULT; }
    public void offer(IMixinInternal internal) { }
    public void init() { }
    public void beginPhase() { }
    public void checkEnv(Object bootstrap) { }
    public ReEntranceLock getReEntranceLock() { return lock; }
    public IClassProvider getClassProvider() { return this; }
    public IClassBytecodeProvider getBytecodeProvider() { return this; }
    public ITransformerProvider getTransformerProvider() { return null; }
    public IClassTracker getClassTracker() { return null; }
    public IMixinAuditTrail getAuditTrail() { return null; }
    public Collection<String> getPlatformAgents() { return Collections.emptyList(); }
    public IContainerHandle getPrimaryContainer() { return container; }
    public Collection<IContainerHandle> getMixinContainers() { return Collections.emptyList(); }
    public String getSideName() { return "CLIENT"; }
    public MixinEnvironment.CompatibilityLevel getMinCompatibilityLevel() { return MixinEnvironment.CompatibilityLevel.JAVA_8; }
    public MixinEnvironment.CompatibilityLevel getMaxCompatibilityLevel() { return MixinEnvironment.CompatibilityLevel.JAVA_17; }
    public synchronized ILogger getLogger(String name) { return loggers.computeIfAbsent(name, LoggerAdapterDefault::new); }
    public URL[] getClassPath() { return game() instanceof URLClassLoader ? ((URLClassLoader) game()).getURLs() : new URL[0]; }
    public Class<?> findClass(String name) throws ClassNotFoundException { return findClass(name, true); }
    public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException { return Class.forName(name, initialize, game()); }
    public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException { return Class.forName(name, initialize, getClass().getClassLoader()); }

    public InputStream getResourceAsStream(String name) {
        if (name.endsWith(".class")) {
            String binary = name.substring(0, name.length() - 6).replace('/', '.');
            if (live != null) {
                byte[] bytes = live.get(binary);
                if (bytes == null && binary.startsWith("net.minecraft.") && !binary.startsWith("net.minecraft.launchwrapper.")) {
                    // Other baker workers can finish while Main is held by this transformer.
                    long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(30);
                    do {
                        try { Thread.sleep(10); }
                        catch (InterruptedException interrupted) {
                            Thread.currentThread().interrupt();
                            throw new IllegalStateException("Interrupted while awaiting Lunar metadata: " + binary, interrupted);
                        }
                        bytes = live.get(binary);
                    } while (bytes == null && System.nanoTime() < deadline);
                    if (bytes == null) throw new IllegalStateException("Lunar did not bake class metadata: " + binary);
                }
                if (bytes != null) return new ByteArrayInputStream(bytes);
            }
            JarEntry cached = baked == null ? null : baked.getJarEntry(binary);
            if (cached != null) {
                try { return baked.getInputStream(cached); }
                catch (IOException failure) { throw new IllegalStateException("Cannot read Lunar class metadata: " + name, failure); }
            }
        }
        String path = System.getProperty("fdpclient.lunar.runtimeJar");
        if (path != null) {
            try {
                final JarFile jar = new JarFile(path);
                JarEntry entry = jar.getJarEntry(name);
                if (entry != null) return new FilterInputStream(jar.getInputStream(entry)) {
                    public void close() throws IOException { try { super.close(); } finally { jar.close(); } }
                };
                jar.close();
            } catch (IOException failure) { throw new IllegalStateException("Cannot read FDP runtime JAR", failure); }
        }
        InputStream resource = game().getResourceAsStream(name);
        return resource != null ? resource : ClassLoader.getSystemResourceAsStream(name);
    }

    public ClassNode getClassNode(String name) throws ClassNotFoundException, IOException { return getClassNode(name, true, 0); }
    public ClassNode getClassNode(String name, boolean transform) throws ClassNotFoundException, IOException { return getClassNode(name, transform, 0); }
    public ClassNode getClassNode(String name, boolean transform, int flags) throws ClassNotFoundException, IOException {
        String binary = name.replace('/', '.');
        byte[] current = CURRENT.get().get(binary);
        ClassReader reader;
        if (current != null) reader = new ClassReader(current);
        else {
            try (InputStream in = getResourceAsStream(binary.replace('.', '/') + ".class")) {
                if (in == null) throw new ClassNotFoundException("No class bytes for " + binary);
                reader = new ClassReader(in);
            }
        }
        ClassNode node = new ClassNode();
        reader.accept(node, flags);
        AccessRules.apply(node);
        return node;
    }
}
