/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft.
 * https://github.com/SkidderMC/FDPClient/
 * Lunar compatibility by opZywl
 * Reference: https://lucas-lima.xyz
 */
package net.ccbluex.liquidbounce.injection.bootstrap;

import java.io.*;
import java.lang.instrument.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.jar.*;
import java.util.regex.*;

/** Early entry: the small agent JAR deliberately contains no FDP application or Minecraft classes. */
public final class LunarAgent implements ClassFileTransformer {
    private static final Pattern SRG = Pattern.compile("(?:func|field)_[0-9]+_[A-Za-z_]+");
    private static volatile LunarAgent active;
    private static volatile String nativeFailure;
    private final Instrumentation instrumentation;
    private final File payload;
    private volatile ClassLoader game;
    private volatile boolean attempted;
    private volatile boolean configured;
    private volatile Throwable failure;
    private Method mixinTransform;
    private Method memberAccess;

    private LunarAgent(Instrumentation instrumentation, File payload) {
        this.instrumentation = instrumentation;
        this.payload = payload;
    }

    public static void premain(String argument, Instrumentation instrumentation) throws Exception {
        install(argument, instrumentation);
    }

    /** Native delivery still happens before Minecraft: loaded mixin targets cannot change their schema. */
    public static void agentmain(String argument, Instrumentation instrumentation) throws Exception {
        try {
            boolean lunar = false;
            for (Class<?> loaded : instrumentation.getAllLoadedClasses()) {
                String name = loaded.getName();
                if (name.startsWith("com.moonsworth.lunar.genesis.")) lunar = true;
                if (name.startsWith("net.minecraft.") && !name.startsWith("net.minecraft.launchwrapper.")) {
                    throw new IllegalStateException("Minecraft has already initialized. Start the ZAVZ Lunar watcher before launching the game; no late mixin injection was attempted.");
                }
            }
            if (!lunar) throw new IllegalStateException("The native FDP loader supports Lunar Client 1.8.9 only.");
            install(argument, instrumentation);
        } catch (Exception failure) {
            nativeFailure = failure.toString();
            throw failure;
        }
    }

    public static String nativeStatus() {
        if (nativeFailure != null) return "error: " + nativeFailure;
        LunarAgent agent = active;
        if (agent == null) return "error: FDP's Lunar agent was not registered";
        if (agent.failure != null) return "error: " + agent.failure;
        if (Boolean.getBoolean("fdpclient.ready") || Boolean.getBoolean("fdpclient.lunar.ready")) return "ok";
        return "waiting";
    }

    private static synchronized void install(String argument, Instrumentation instrumentation) throws Exception {
        if (active != null || System.getProperties().containsKey("fdpclient.lunar.agentRegistered")) {
            throw new IllegalStateException("FDP's Lunar agent is already loaded. Restart Lunar without the -javaagent argument before using the native loader.");
        }
        File self = new File(LunarAgent.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        File payload;
        if (argument != null && !argument.trim().isEmpty()) {
            payload = new File(argument).getCanonicalFile();
        } else {
            try (JarFile jar = new JarFile(self)) {
                String embedded = jar.getManifest().getMainAttributes().getValue("FDP-Payload-Resource");
                if (embedded != null) {
                    payload = extractPayload(jar, embedded);
                } else {
                    String name = jar.getManifest().getMainAttributes().getValue("FDP-Payload-File");
                    if (name == null) throw new IOException("Missing FDP-Payload-File in agent manifest");
                    payload = new File(self.getParentFile(), name).getCanonicalFile();
                }
            }
        }
        if (!payload.isFile()) throw new FileNotFoundException("Keep the FDPClient JAR beside its Lunar agent: " + payload);
        LunarAgent agent = new LunarAgent(instrumentation, payload);
        instrumentation.addTransformer(agent);
        active = agent;
        System.setProperty("fdpclient.lunar.agentRegistered", "true");
    }

    private static File extractPayload(JarFile bundle, String name) throws Exception {
        JarEntry entry = bundle.getJarEntry(name);
        if (entry == null) throw new IOException("The Lunar bundle has no embedded FDP payload");
        Path directory = Paths.get(System.getProperty("user.home"), ".zywl", "fdpclient", "payloads");
        Files.createDirectories(directory);
        Path temporary = Files.createTempFile(directory, "fdp-payload-", ".jar");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream in = bundle.getInputStream(entry); OutputStream out = Files.newOutputStream(temporary)) {
                byte[] buffer = new byte[65536]; int length;
                while ((length = in.read(buffer)) != -1) { digest.update(buffer, 0, length); out.write(buffer, 0, length); }
            }
            StringBuilder hash = new StringBuilder();
            for (byte value : digest.digest()) hash.append(String.format(Locale.ROOT, "%02x", value & 255));
            Path destination = directory.resolve(hash + ".jar");
            if (!Files.isRegularFile(destination)) Files.move(temporary, destination);
            return destination.toFile();
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String name, Class<?> redefining, ProtectionDomain domain, byte[] bytes) {
        if (loader == null || name == null) return null;
        try {
            if (!configured && name.startsWith("net/minecraft/") && !name.startsWith("net/minecraft/launchwrapper/")) {
                synchronized (this) {
                    if (!attempted) {
                        attempted = true;
                        configure(loader, name, bytes);
                        configured = true;
                    }
                }
            }
            if (failure != null && loader == game && name.startsWith("net/minecraft/")) return new byte[]{0};
            if (loader == game && mixinTransform != null && name.startsWith("net/minecraft/") &&
                !name.startsWith("net/minecraft/launchwrapper/")) {
                return (byte[]) mixinTransform.invoke(null, name.replace('/', '.'), bytes);
            }
            if (loader == game && name.startsWith("net/montoyo/mcef/")) {
                byte[] adapted = LunarMcefTransformer.transform(bytes);
                adapted = (byte[]) mixinTransform.invoke(null, name.replace('/', '.'), adapted);
                return adaptMembers(adapted, loader);
            }
            if (loader == game && name.startsWith("org/cef/")) return adaptMembers(LunarMcefTransformer.transform(bytes), loader);
            if (loader == game && name.startsWith("net/ccbluex/liquidbounce/") &&
                !name.startsWith("net/ccbluex/liquidbounce/injection/bootstrap/") &&
                !name.startsWith("net/ccbluex/liquidbounce/libraries/") &&
                !name.startsWith("net/ccbluex/liquidbounce/injection/forge/mixins/")) {
                return adaptMembers(bytes, loader);
            }
        } catch (Throwable problem) {
            if (failure == null) failure = problem;
            System.err.println("[FDPCLIENT] Lunar agent error while loading " + name);
            problem.printStackTrace();
            // ClassFileTransformer exceptions are swallowed by the JVM. Invalid bytes ensure the
            // client cannot continue with only a fraction of the required patches installed.
            return new byte[]{0};
        }
        return null;
    }

    private byte[] adaptMembers(byte[] bytes, ClassLoader loader) {
        return LunarAccessTransformer.transform(bytes, loader, (owner, member, descriptor, method) -> {
            try { return (int[]) memberAccess.invoke(null, owner, member, descriptor, method); }
            catch (ReflectiveOperationException problem) { throw new IllegalStateException("Cannot inspect Lunar member " + owner + "." + member, problem); }
        });
    }

    private void configure(ClassLoader loader, String first, byte[] firstBytes) throws Exception {
        Map<String, String> names = LunarNames.load(loader);
        File runtime = runtimeJar(names);
        game = loader;
        System.setProperty("fdpclient.lunar.runtimeJar", runtime.getAbsolutePath());
        System.setProperty("fdpclient.lunar.early", "true");
        System.getProperties().put("fdpclient.lunar.gameLoader", loader);
        List<File> caches = matchingCaches(first, firstBytes);
        if (caches.size() == 1) {
            System.setProperty("fdpclient.lunar.bakeCache", caches.get(0).getAbsolutePath());
        } else {
            System.getProperties().put("fdpclient.lunar.bakedClasses", liveCache(loader, first, firstBytes));
            System.out.println("[FDPCLIENT] Using Lunar's live bytecode cache during first-launch baking");
        }
        addURL(loader, runtime.toURI().toURL());
        // Injected callback descriptors must also be resolvable by Genesis' isolated game loader.
        addURL(loader, LunarAgent.class.getProtectionDomain().getCodeSource().getLocation());
        Path generated = runtime.toPath().getParent().resolve(runtime.getName() + "-classes-" + Long.toHexString(System.nanoTime()));
        Files.createDirectories(generated);
        System.setProperty("fdpclient.lunar.generatedClasses", generated.toString());
        addURL(loader, generated.toUri().toURL());
        Class<?> engine = Class.forName("net.ccbluex.liquidbounce.injection.lunar.engine.EngineBootstrap", true, LunarAgent.class.getClassLoader());
        engine.getMethod("initialize").invoke(null);
        mixinTransform = engine.getMethod("transform", String.class, byte[].class);
        memberAccess = engine.getMethod("memberAccess", String.class, String.class, String.class, boolean.class);
        System.out.println("[FDPCLIENT] Registered Lunar mixins before " + first + " using " + loader.getClass().getName());
    }

    @SuppressWarnings("unchecked")
    private Map<String, byte[]> liveCache(ClassLoader loader, String first, byte[] bytes) throws Exception {
        Method found = null;
        for (Method method : loader.getClass().getMethods()) {
            if (method.getParameterTypes().length == 0 && java.util.concurrent.ConcurrentMap.class.isAssignableFrom(method.getReturnType())) {
                if (found != null) throw new IOException("Ambiguous Lunar live bytecode cache API");
                found = method;
            }
        }
        if (found == null) throw new IOException("Lunar has no live bytecode cache API");
        found.setAccessible(true);
        Map<String, byte[]> cache = (Map<String, byte[]>) found.invoke(loader);
        // Metadata must describe exactly the bytecode that Genesis is defining,
        // rather than the unmodified resources exposed by its parent loader.
        if (cache == null || !Arrays.equals(bytes, cache.get(first.replace('/', '.')))) {
            throw new IOException("Lunar live bytecode cache does not match " + first);
        }
        return cache;
    }

    private List<File> matchingCaches(String first, byte[] bytes) throws IOException {
        List<File> matches = new ArrayList<>();
        for (String path : System.getProperty("java.class.path", "").split(File.pathSeparator)) {
            File mappings = new File(path);
            if (!mappings.getName().equals("lunar-platform-mappings-v1_8.jar")) continue;
            Path caches = mappings.toPath().toAbsolutePath().getParent().resolve("cache");
            if (!Files.isDirectory(caches)) continue;
            try (java.util.stream.Stream<Path> files = Files.walk(caches, 4)) {
                Iterator<Path> candidates = files.filter(file -> file.getFileName().toString().equals("bake.zip")).iterator();
                while (candidates.hasNext()) {
                    File candidate = candidates.next().toFile();
                    try (JarFile jar = new JarFile(candidate)) {
                        JarEntry entry = jar.getJarEntry(first.replace('/', '.'));
                        if (entry != null) {
                            try (InputStream in = jar.getInputStream(entry)) {
                                if (Arrays.equals(bytes, read(in))) matches.add(candidate);
                            }
                        }
                    } catch (java.util.zip.ZipException stillBeingWritten) { /* Use the live cache until Genesis finishes the ZIP. */ }
                }
            }
        }
        return matches;
    }

    private void addURL(ClassLoader loader, URL url) throws Exception {
        Method add = null;
        for (Class<?> type = loader.getClass(); type != null; type = type.getSuperclass()) {
            try { add = type.getDeclaredMethod("addURL", URL.class); break; }
            catch (NoSuchMethodException next) { }
        }
        if (add == null) throw new IllegalStateException("Unsupported Lunar class loader: " + loader.getClass().getName());
        if (add.getDeclaringClass() == URLClassLoader.class) openJavaNet();
        add.setAccessible(true);
        add.invoke(loader, url);
    }

    private void openJavaNet() throws Exception {
        // Java 8 has no module boundary; Java 17 exposes this process-local operation via Instrumentation.
        try {
            Method getModule = Class.class.getMethod("getModule");
            Object javaBase = getModule.invoke(URLClassLoader.class);
            Object ours = getModule.invoke(LunarAgent.class);
            Class<?> module = Class.forName("java.lang.Module");
            Method redefine = Instrumentation.class.getMethod("redefineModule", module, Set.class, Map.class, Map.class, Set.class, Map.class);
            redefine.invoke(instrumentation, javaBase, Collections.emptySet(), Collections.emptyMap(),
                Collections.singletonMap("java.net", Collections.singleton(ours)), Collections.emptySet(), Collections.emptyMap());
        } catch (NoSuchMethodException java8) { }
    }

    private File runtimeJar(Map<String, String> names) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream in = new FileInputStream(payload)) {
            byte[] buffer = new byte[65536]; int n;
            while ((n = in.read(buffer)) != -1) digest.update(buffer, 0, n);
        }
        for (Map.Entry<String, String> member : new TreeMap<>(names).entrySet()) {
            digest.update((member.getKey() + "=" + member.getValue() + "\n").getBytes(StandardCharsets.UTF_8));
        }
        StringBuilder hash = new StringBuilder();
        for (byte value : digest.digest()) hash.append(String.format(Locale.ROOT, "%02x", value & 255));
        Path directory = Paths.get(System.getProperty("user.home"), ".zywl", "fdpclient", "lunar-cache");
        Files.createDirectories(directory);
        Path runtime = directory.resolve(hash + ".jar");
        if (Files.isRegularFile(runtime)) return runtime.toFile();
        Path temporary = Files.createTempFile(directory, "fdp-runtime-", ".jar");
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
        manifest.getMainAttributes().putValue("FDP-Lunar-Runtime", hash.toString());
        try (JarFile input = new JarFile(payload); JarOutputStream out = new JarOutputStream(new BufferedOutputStream(Files.newOutputStream(temporary), 65536), manifest)) {
            Enumeration<JarEntry> entries = input.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.equals("META-INF/MANIFEST.MF") || name.startsWith("org/spongepowered/") ||
                    (name.startsWith("net/ccbluex/liquidbounce/injection/bootstrap/") && !name.equals("net/ccbluex/liquidbounce/injection/bootstrap/ClassInventory.class")) ||
                    name.startsWith("jdk/nashorn/") || name.startsWith("jdk/internal/dynalink/") ||
                    name.startsWith("META-INF/services/org.spongepowered.") || name.endsWith(".SF") || name.endsWith(".RSA") || name.endsWith(".DSA")) continue;
                byte[] data;
                try (InputStream stream = input.getInputStream(entry)) { data = read(stream); }
                if (name.endsWith(".class") && !name.startsWith("net/ccbluex/liquidbounce/injection/bootstrap/")) {
                    data = ClassFileNames.remap(data, names, true);
                }
                if (name.equals("fdpclient.mixins.refmap.json")) {
                    name = "fdpclient.lunar.refmap.json";
                    data = remapJson(new String(data, StandardCharsets.UTF_8), names).getBytes(StandardCharsets.UTF_8);
                }
                if (name.endsWith("fdpclient_at.cfg")) data = remapJson(new String(data, StandardCharsets.UTF_8), names).getBytes(StandardCharsets.UTF_8);
                if (name.startsWith("okhttp3/") || name.startsWith("okio/")) name = "net/ccbluex/liquidbounce/libraries/lunar/" + name;
                if (name.startsWith("org/".concat("objectweb/asm/"))) continue; // The agent supplies the private ASM copy.
                out.putNextEntry(new JarEntry(name)); out.write(data); out.closeEntry();
            }
        }
        try { Files.move(temporary, runtime); }
        catch (FileAlreadyExistsException simultaneousLaunch) { Files.delete(temporary); }
        return runtime.toFile();
    }

    private static String remapJson(String json, Map<String, String> names) {
        Matcher matcher = SRG.matcher(json);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String target = names.get(matcher.group());
            matcher.appendReplacement(result, Matcher.quoteReplacement(target == null ? matcher.group() : target));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static byte[] read(InputStream in) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192]; int n;
        while ((n = in.read(buffer)) != -1) bytes.write(buffer, 0, n);
        return bytes.toByteArray();
    }
}
