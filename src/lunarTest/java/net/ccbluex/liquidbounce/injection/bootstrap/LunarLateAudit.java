/*
 * FDPClient Hacked Client
 * Installed Lunar bytecode audit by opZywl | https://lucas-lima.xyz
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.bootstrap;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import java.io.*;
import java.lang.instrument.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.jar.*;

/** Audits user-provided Lunar caches in an isolated JVM, without opening a game or using an account. */
public final class LunarLateAudit {
    private LunarLateAudit() { }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        if (args.length < 5) throw new IllegalArgumentException("Expected payload, agent, bake cache, mappings and output directory");
        Path root = Paths.get(args[4]).toAbsolutePath().normalize();
        Files.createDirectories(root);
        Path output = Files.createTempDirectory(root, "profile-");
        boolean hotSwap = Boolean.getBoolean("fdpclient.audit.hotswap");
        System.setProperty("user.home", output.resolve("home").toString());
        System.setProperty("fdpclient.lunar.generatedClasses", output.resolve("generated").toString());
        Map<String, byte[]> baked = new ConcurrentHashMap<>();
        try (JarFile archive = new JarFile(args[2])) {
            Enumeration<JarEntry> entries = archive.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory()) continue;
                byte[] bytes;
                try (InputStream in = archive.getInputStream(entry)) { bytes = read(in); }
                if (bytes.length < 8 || bytes[0] != (byte) 0xca || bytes[1] != (byte) 0xfe) continue;
                baked.put(new ClassReader(bytes).getClassName().replace('/', '.'), bytes);
            }
        }
        URL agent = new File(args[1]).toURI().toURL();
        ClassLoader platform = ClassLoader.getSystemClassLoader().getParent();
        try (URLClassLoader engine = new URLClassLoader(new URL[]{agent}, platform)) {
            Thread.currentThread().setContextClassLoader(engine);
            Map<String, String> names;
            try (JarFile mappings = new JarFile(args[3]); InputStream in = mappings.getInputStream(mappings.getJarEntry("lunar/searge2lunar_1.8.9.kin"))) {
                names = (Map<String, String>) engine.loadClass("net.ccbluex.liquidbounce.injection.bootstrap.LunarNames")
                    .getMethod("read", InputStream.class).invoke(null, in);
            }
            Class<?> agentType = engine.loadClass("net.ccbluex.liquidbounce.injection.bootstrap.LunarAgent");
            Constructor<?> constructor = agentType.getDeclaredConstructor(Instrumentation.class, File.class);
            constructor.setAccessible(true);
            Object instance = constructor.newInstance(null, new File(args[0]));
            Method remap = agentType.getDeclaredMethod("runtimeJar", Map.class);
            remap.setAccessible(true);
            File runtime = (File) remap.invoke(instance, names);
            List<URL> urls = new ArrayList<>();
            urls.add(runtime.toURI().toURL()); urls.add(agent); urls.add(new File(args[3]).toURI().toURL());
            File[] neighbors = new File(args[3]).getAbsoluteFile().getParentFile().listFiles((directory, name) -> name.endsWith(".jar"));
            if (neighbors != null) for (File jar : neighbors) urls.add(jar.toURI().toURL());
            if (args.length > 5 && !args[5].isEmpty()) for (String path : args[5].split(java.util.regex.Pattern.quote(File.pathSeparator)))
                urls.add(new File(path).toURI().toURL());
            try (GameLoader game = new GameLoader(urls.toArray(new URL[0]), engine, baked)) {
                System.setProperty("fdpclient.lunar.runtimeJar", runtime.getAbsolutePath());
                System.setProperty("fdpclient.lunar.early", "true");
                System.setProperty("fdpclient.lunar.late", "true");
                System.getProperties().put("fdpclient.lunar.gameLoader", game);
                System.getProperties().put("fdpclient.lunar.bakedClasses", baked);
                Map<String, byte[]> originals = new LinkedHashMap<>();
                for (Map.Entry<String, byte[]> entry : new TreeMap<>(baked).entrySet())
                    if (entry.getKey().startsWith("net.minecraft.") && !entry.getKey().startsWith("net.minecraft.launchwrapper."))
                        originals.put(entry.getKey().replace('.', '/'), entry.getValue());
                Instrumentation instrumentation = null;
                Map<String, Class<?>> loaded = new LinkedHashMap<>();
                if (hotSwap) {
                    Field access = LunarLateVerification.class.getDeclaredField("instrumentation");
                    access.setAccessible(true);
                    instrumentation = (Instrumentation) access.get(null);
                    if (instrumentation == null) throw new IllegalStateException("The HotSwap audit requires the verification agent");
                    for (String name : new ArrayList<>(originals.keySet())) {
                        try { loaded.put(name, Class.forName(name.replace('/', '.'), false, game)); }
                        catch (LinkageError | ClassNotFoundException failure) {
                            throw new IllegalStateException("Could not load installed Lunar class " + name, failure);
                        }
                    }
                    Map<String, byte[]> captured = new ConcurrentHashMap<>();
                    ClassFileTransformer capture = new ClassFileTransformer() {
                        public byte[] transform(ClassLoader loader, String name, Class<?> type, java.security.ProtectionDomain domain, byte[] bytes) {
                            if (loader == game && type != null) captured.put(name, bytes.clone());
                            return null;
                        }
                    };
                    instrumentation.addTransformer(capture, true);
                    try { instrumentation.retransformClasses(loaded.values().toArray(new Class<?>[0])); }
                    finally { instrumentation.removeTransformer(capture); }
                    originals.putAll(captured);
                    for (Map.Entry<String, byte[]> entry : captured.entrySet()) baked.put(entry.getKey().replace('/', '.'), entry.getValue());
                    System.out.println("Captured " + captured.size() + " real JVM class definitions before lowering.");
                }
                Class<?> bootstrap = engine.loadClass("net.ccbluex.liquidbounce.injection.lunar.engine.EngineBootstrap");
                bootstrap.getMethod("initialize").invoke(null);
                Map<String, byte[]> definitions;
                try { definitions = (Map<String, byte[]>) bootstrap.getMethod("prepareLate", Map.class).invoke(null, originals); }
                catch (InvocationTargetException failure) { throw new IllegalStateException("Installed Lunar cache could not be lowered: " + args[2], failure.getCause()); }
                Method transformLate = bootstrap.getMethod("transformLate", String.class, byte[].class);
                int untouched = 0;
                for (Map.Entry<String, byte[]> entry : originals.entrySet()) {
                    if (definitions.containsKey(entry.getKey())) continue;
                    byte[] transformed = (byte[]) transformLate.invoke(null, entry.getKey(), entry.getValue());
                    if (!Arrays.equals(entry.getValue(), transformed)) throw new AssertionError("Unmodified Lunar class was rewritten: " + entry.getKey());
                    untouched++;
                }
                System.out.println("Preserved " + untouched + " unmodified game classes for future loading.");
                int methods = 0;
                for (Map.Entry<String, byte[]> entry : definitions.entrySet()) {
                    ClassNode before = node(originals.get(entry.getKey())), after = node(entry.getValue());
                    LateSchemaTransformer.requireSameSchema(before, after);
                    for (MethodNode method : after.methods) {
                        if (method.instructions.size() == 0) continue;
                        try { new Analyzer<>(new BasicVerifier()).analyze(after.name, method); }
                        catch (AnalyzerException failure) { throw new IllegalStateException("Invalid runtime method " + after.name + "." + method.name + method.desc, failure); }
                        methods++;
                    }
                }
                Field prepared = bootstrap.getDeclaredField("late");
                prepared.setAccessible(true);
                Object result = prepared.get(null);
                Map<String, byte[]> helpers = (Map<String, byte[]>) result.getClass().getField("helpers").get(result);
                Map<String, Integer> bootstraps = new TreeMap<>();
                int helperMethods = 0;
                for (Map.Entry<String, byte[]> entry : helpers.entrySet()) {
                    ClassNode helper = node(entry.getValue());
                    for (MethodNode method : helper.methods) {
                        if (method.instructions.size() == 0) continue;
                        try { new Analyzer<>(new BasicVerifier()).analyze(helper.name, method); }
                        catch (AnalyzerException failure) { throw new IllegalStateException("Invalid runtime helper " + helper.name + "." + method.name + method.desc, failure); }
                        helperMethods++;
                        for (AbstractInsnNode instruction : method.instructions.toArray()) {
                            if (instruction instanceof InvokeDynamicInsnNode) {
                                InvokeDynamicInsnNode dynamic = (InvokeDynamicInsnNode) instruction;
                                if (!dynamic.bsm.getOwner().endsWith("/LateRuntime")) bootstraps.merge(dynamic.bsm.getOwner() + "." + dynamic.bsm.getName(), 1, Integer::sum);
                            }
                        }
                    }
                }
                Map<String, Map<String, Object>> plans = (Map<String, Map<String, Object>>) result.getClass().getField("plans").get(result);
                int addedMethods = 0, addedFields = 0;
                for (Map<String, Object> plan : plans.values()) {
                    addedFields += ((Map<?, ?>) plan.get("fields")).size();
                    for (String[] method : ((Map<String, String[]>) plan.get("methods")).values()) if (method[2].equals("added")) addedMethods++;
                }
                if (definitions.size() < 40 || addedMethods < 100) throw new AssertionError("Too few runtime mixins were applied: " + definitions.size() + " classes, " + addedMethods + " added methods");
                System.out.println("Installed Lunar runtime audit: " + definitions.size() + " schemas preserved, " + methods +
                    " method bodies and " + helperMethods + " helpers verified, " + addedMethods + " methods and " + addedFields + " fields externalized.");
                System.out.println("Companion bootstrap methods: " + bootstraps);
                if (hotSwap) {
                    Map<String, byte[]> verification = new ConcurrentHashMap<>(baked);
                    for (Map.Entry<String, byte[]> entry : definitions.entrySet()) verification.put(entry.getKey().replace('/', '.'), entry.getValue());
                    String cacheName = "net.ccbluex.liquidbounce.utils.kotlin.LruCache";
                    try (JarFile payload = new JarFile(runtime); InputStream in = payload.getInputStream(payload.getJarEntry(cacheName.replace('.', '/') + ".class"))) {
                        verification.put(cacheName, (byte[]) bootstrap.getMethod("adaptLatePayload", byte[].class).invoke(null, read(in)));
                    }
                    Method adaptLate = bootstrap.getMethod("adaptLatePayload", byte[].class);
                    Method memberFlags = bootstrap.getMethod("memberAccess", String.class, String.class, String.class, boolean.class);
                    Class<?> accessType = engine.loadClass("net.ccbluex.liquidbounce.injection.bootstrap.LunarAccessTransformer");
                    Class<?> accessLookup = engine.loadClass("net.ccbluex.liquidbounce.injection.bootstrap.LunarAccessTransformer$Lookup");
                    Object flags = java.lang.reflect.Proxy.newProxyInstance(engine, new Class<?>[]{accessLookup},
                        (proxy, method, values) -> memberFlags.invoke(null, values));
                    Method access = accessType.getMethod("transform", byte[].class, ClassLoader.class, accessLookup);
                    Map<String, byte[]> adaptedPayload = new ConcurrentHashMap<>();
                    try (GameLoader probe = new GameLoader(urls.toArray(new URL[0]), engine, verification)) {
                        probe.fallback = baked;
                        probe.adapted = adaptedPayload;
                        probe.adapter = bytes -> {
                            try { return (byte[]) access.invoke(null, (byte[]) adaptLate.invoke(null, (Object) bytes), probe, flags); }
                            catch (InvocationTargetException failure) { throw new IllegalStateException(failure.getCause()); }
                            catch (IllegalAccessException failure) { throw new IllegalStateException(failure); }
                        };
                        for (String name : definitions.keySet()) {
                            try { Class.forName(name.replace('/', '.'), false, probe).getDeclaredMethods(); }
                            catch (LinkageError failure) { throw new IllegalStateException("JVM rejected lowered Lunar class " + name, failure); }
                        }
                        Method installRuntime = engine.loadClass("net.ccbluex.liquidbounce.injection.bootstrap.LateRuntime")
                            .getMethod("install", Map.class, ClassLoader.class);
                        installRuntime.invoke(null, plans, probe);
                        try {
                            Map<Object, Object> fontCache = (Map<Object, Object>) Class.forName(cacheName, true, probe).getConstructor(int.class).newInstance(2);
                            fontCache.put("one", 1); fontCache.put("two", 2);
                            if (!Integer.valueOf(1).equals(fontCache.get("one"))) throw new AssertionError("Font cache read failed");
                            fontCache.put("three", 3);
                            if (fontCache.size() != 2 || fontCache.containsKey("two")) throw new AssertionError("Font cache LRU eviction failed");
                            Class<?> supplierType = Class.forName("kotlin.jvm.functions.Function0", true, probe);
                            Object supplier = java.lang.reflect.Proxy.newProxyInstance(probe, new Class<?>[]{supplierType},
                                (proxy, method, values) -> { throw new AssertionError("Cached font value was recomputed"); });
                            Method getOrPut = fontCache.getClass().getMethod("getOrPut", Object.class, supplierType);
                            if (!Integer.valueOf(1).equals(getOrPut.invoke(fontCache, "one", supplier))) throw new AssertionError("Font cache hit failed");
                            Object compute = java.lang.reflect.Proxy.newProxyInstance(probe, new Class<?>[]{supplierType},
                                (proxy, method, values) -> 37);
                            if (!Integer.valueOf(37).equals(getOrPut.invoke(fontCache, "width", compute))) throw new AssertionError("Font cache miss failed");
                            System.out.println("Production font cache: superclass calls, LRU eviction and getOrPut hits/misses verified.");
                            dispatchGaps(originals, plans);
                            linkEverything(instrumentation, probe, runtime, definitions, helpers, adaptedPayload, originals);
                        } finally { installRuntime.invoke(null, plans, game); }
                    }
                    List<ClassDefinition> replacements = new ArrayList<>();
                    for (Map.Entry<String, byte[]> entry : definitions.entrySet())
                        replacements.add(new ClassDefinition(loaded.get(entry.getKey()), entry.getValue()));
                    instrumentation.redefineClasses(replacements.toArray(new ClassDefinition[0]));
                    System.out.println("Installed Lunar HotSwap: " + replacements.size() + " real class definitions replaced successfully.");
                }
                System.out.println("Audit evidence: " + output);
            }
        }
    }

    private static ClassNode node(byte[] bytes) {
        ClassNode node = new ClassNode(); new ClassReader(bytes).accept(node, ClassReader.EXPAND_FRAMES); return node;
    }
    private static byte[] read(InputStream in) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(); byte[] buffer = new byte[8192]; int count;
        while ((count = in.read(buffer)) != -1) bytes.write(buffer, 0, count);
        return bytes.toByteArray();
    }

    private static void linkEverything(Instrumentation instrumentation, GameLoader probe, File runtime, Map<String, byte[]> definitions,
                                       Map<String, byte[]> helpers, Map<String, byte[]> adaptedPayload, Map<String, byte[]> originals) throws Exception {
        java.lang.invoke.MethodHandles.Lookup trusted = trustedLookup(instrumentation);
        List<String> payload = new ArrayList<>();
        try (JarFile jar = new JarFile(runtime)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (!name.endsWith(".class") || name.contains("-")) continue;
                String binary = name.substring(0, name.length() - 6).replace('/', '.');
                if (GameLoader.adapts(binary)) payload.add(binary);
            }
        }
        Map<String, List<String>> failures = new TreeMap<>(), expected = new TreeMap<>();
        int loaded = 0;
        for (String name : payload) {
            try { Class.forName(name, false, probe).getDeclaredMethods(); loaded++; }
            catch (Throwable failure) {
                String reason = describe(failure);
                (reason.contains("net/minecraftforge/") ? expected : failures)
                    .computeIfAbsent("load " + reason + (reason.contains("net/minecraftforge/") ? " [Forge-only entry, never loaded on Lunar]" : ""),
                        key -> new ArrayList<>()).add(name);
            }
        }
        Map<String, byte[]> targets = new TreeMap<>();
        targets.putAll(adaptedPayload);
        for (Map.Entry<String, byte[]> entry : definitions.entrySet()) targets.put(entry.getKey().replace('/', '.'), entry.getValue());
        for (Map.Entry<String, byte[]> entry : helpers.entrySet()) targets.put(entry.getKey().replace('/', '.'), entry.getValue());
        int sites = 0, linked = 0;
        for (Map.Entry<String, byte[]> entry : targets.entrySet()) {
            Class<?> owner;
            try { owner = Class.forName(entry.getKey(), false, probe); owner.getDeclaredMethods(); }
            catch (Throwable failure) {
                if (!describe(failure).contains("net/minecraftforge/")) failures.computeIfAbsent("load " + describe(failure), key -> new ArrayList<>()).add(entry.getKey());
                continue;
            }
            java.lang.invoke.MethodHandles.Lookup caller = trusted.in(owner);
            for (MethodNode method : node(entry.getValue()).methods) {
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (!(instruction instanceof InvokeDynamicInsnNode)) continue;
                    InvokeDynamicInsnNode dynamic = (InvokeDynamicInsnNode) instruction;
                    sites++;
                    try { link(caller, owner.getClassLoader(), dynamic); linked++; }
                    catch (Throwable failure) {
                        String reason = describe(failure), known = preexisting(dynamic, reason, originals);
                        (known == null ? failures : expected).computeIfAbsent("link " + reason + (known == null ? "" : " [" + known + "]"), key -> new ArrayList<>())
                            .add(entry.getKey() + "." + method.name + method.desc + " -> " + dynamic.bsm.getName() + " " + Arrays.toString(dynamic.bsmArgs));
                    }
                }
            }
        }
        System.out.println("Runtime linkage audit: " + loaded + "/" + payload.size() + " FDP classes defined and verified, " +
            linked + "/" + sites + " invokedynamic call sites linked across " + targets.size() + " classes.");
        for (Map.Entry<String, List<String>> known : expected.entrySet())
            System.out.println("Expected without FDP too x" + known.getValue().size() + ": " + known.getKey());
        int shown = 0;
        for (Map.Entry<String, List<String>> failure : failures.entrySet()) {
            System.out.println("LINK FAILURE x" + failure.getValue().size() + ": " + failure.getKey());
            for (String where : failure.getValue()) if (shown++ < 400) System.out.println("    " + where);
        }
        if (!failures.isEmpty()) throw new AssertionError("Runtime linkage audit found " + failures.size() + " failure kinds");
    }

    private static String preexisting(InvokeDynamicInsnNode dynamic, String reason, Map<String, byte[]> originals) {
        if (reason.contains("tv/twitch/") || reason.contains("tv.twitch.")) return "Twitch library is absent from Lunar";
        if (!dynamic.bsm.getOwner().endsWith("/LateRuntime") || !dynamic.bsm.getName().equals("bootstrap") ||
            dynamic.bsmArgs.length < 4 || !Integer.valueOf(187).equals(dynamic.bsmArgs[3])) return null;
        byte[] target = originals.get((String) dynamic.bsmArgs[0]);
        if (target == null) return null;
        ClassNode node = new ClassNode();
        new ClassReader(target).accept(node, ClassReader.SKIP_CODE);
        for (MethodNode method : node.methods) if (method.name.equals("<init>") && method.desc.equals(dynamic.bsmArgs[2])) return null;
        return "Lunar factory for a constructor that Lunar itself does not declare";
    }

    @SuppressWarnings("unchecked")
    private static void dispatchGaps(Map<String, byte[]> originals, Map<String, Map<String, Object>> plans) {
        Map<String, ClassNode> nodes = new HashMap<>();
        for (Map.Entry<String, byte[]> entry : originals.entrySet()) {
            ClassNode node = new ClassNode(); new ClassReader(entry.getValue()).accept(node, ClassReader.SKIP_CODE); nodes.put(node.name, node);
        }
        List<String> gaps = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> plan : plans.entrySet()) {
            for (Map.Entry<String, String[]> method : ((Map<String, String[]>) plan.getValue().get("methods")).entrySet()) {
                if (!"added".equals(method.getValue()[2]) || (Integer.parseInt(method.getValue()[3]) & 10) != 0) continue;
                for (ClassNode candidate : nodes.values()) {
                    if (candidate.name.equals(plan.getKey()) || !inherits(nodes, candidate.name, plan.getKey())) continue;
                    boolean physical = false;
                    for (MethodNode declared : candidate.methods) physical |= (declared.name + declared.desc).equals(method.getKey());
                    Map<String, Object> sub = plans.get(candidate.name);
                    String[] added = sub == null ? null : ((Map<String, String[]>) sub.get("methods")).get(method.getKey());
                    if (physical || (added != null && "added".equals(added[2])))
                        gaps.add(plan.getKey() + "." + method.getKey() + " overridden by " + candidate.name + (physical ? " (physical)" : " (mixin)"));
                }
            }
        }
        System.out.println("Added-method dispatch audit: " + gaps.size() + " subclass overrides of mixin-added methods" + (gaps.isEmpty() ? "." : ":"));
        for (String gap : gaps) System.out.println("    " + gap);
    }

    private static boolean inherits(Map<String, ClassNode> nodes, String name, String ancestor) {
        for (ClassNode current = nodes.get(name); current != null; current = nodes.get(current.superName))
            if (ancestor.equals(current.superName)) return true;
        return false;
    }

    private static String describe(Throwable failure) {
        while (failure.getCause() != null && (failure instanceof InvocationTargetException || failure instanceof IllegalStateException ||
            failure instanceof java.lang.invoke.WrongMethodTypeException || failure.getClass().getName().endsWith("BootstrapMethodError")))
            failure = failure.getCause();
        String message = String.valueOf(failure.getMessage());
        return failure.getClass().getName() + ": " + (message.length() > 220 ? message.substring(0, 220) : message);
    }

    private static java.lang.invoke.MethodHandles.Lookup trustedLookup(Instrumentation instrumentation) throws Exception {
        Method getModule = Class.class.getMethod("getModule");
        Class<?> module = Class.forName("java.lang.Module");
        Method redefine = Instrumentation.class.getMethod("redefineModule", module, Set.class, Map.class, Map.class, Set.class, Map.class);
        redefine.invoke(instrumentation, getModule.invoke(Object.class), Collections.emptySet(), Collections.emptyMap(),
            Collections.singletonMap("java.lang.invoke", Collections.singleton(getModule.invoke(LunarLateAudit.class))), Collections.emptySet(), Collections.emptyMap());
        Field field = java.lang.invoke.MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
        field.setAccessible(true);
        return (java.lang.invoke.MethodHandles.Lookup) field.get(null);
    }

    private static void link(java.lang.invoke.MethodHandles.Lookup caller, ClassLoader loader, InvokeDynamicInsnNode dynamic) throws Throwable {
        org.objectweb.asm.Handle bsm = dynamic.bsm;
        Class<?> owner = Class.forName(bsm.getOwner().replace('/', '.'), false, loader);
        java.lang.invoke.MethodHandle bootstrap = caller.findStatic(owner, bsm.getName(),
            java.lang.invoke.MethodType.fromMethodDescriptorString(bsm.getDesc(), loader));
        java.lang.invoke.MethodType type = java.lang.invoke.MethodType.fromMethodDescriptorString(dynamic.desc, loader);
        List<Object> arguments = new ArrayList<>();
        arguments.add(caller); arguments.add(dynamic.name); arguments.add(type);
        for (Object constant : dynamic.bsmArgs) arguments.add(constant(caller, loader, constant));
        java.lang.invoke.CallSite site = (java.lang.invoke.CallSite) bootstrap.invokeWithArguments(arguments);
        if (!site.type().equals(type)) throw new IllegalStateException("Call site type " + site.type() + " differs from " + type);
    }

    private static Object constant(java.lang.invoke.MethodHandles.Lookup caller, ClassLoader loader, Object constant) throws Exception {
        if (constant instanceof org.objectweb.asm.Type) {
            org.objectweb.asm.Type type = (org.objectweb.asm.Type) constant;
            return type.getSort() == org.objectweb.asm.Type.METHOD
                ? java.lang.invoke.MethodType.fromMethodDescriptorString(type.getDescriptor(), loader) : classOf(type, loader);
        }
        if (!(constant instanceof org.objectweb.asm.Handle)) return constant;
        org.objectweb.asm.Handle handle = (org.objectweb.asm.Handle) constant;
        Class<?> owner = Class.forName(handle.getOwner().replace('/', '.'), false, loader);
        switch (handle.getTag()) {
            case org.objectweb.asm.Opcodes.H_GETFIELD: return caller.findGetter(owner, handle.getName(), classOf(org.objectweb.asm.Type.getType(handle.getDesc()), loader));
            case org.objectweb.asm.Opcodes.H_GETSTATIC: return caller.findStaticGetter(owner, handle.getName(), classOf(org.objectweb.asm.Type.getType(handle.getDesc()), loader));
            case org.objectweb.asm.Opcodes.H_PUTFIELD: return caller.findSetter(owner, handle.getName(), classOf(org.objectweb.asm.Type.getType(handle.getDesc()), loader));
            case org.objectweb.asm.Opcodes.H_PUTSTATIC: return caller.findStaticSetter(owner, handle.getName(), classOf(org.objectweb.asm.Type.getType(handle.getDesc()), loader));
            default:
        }
        java.lang.invoke.MethodType type = java.lang.invoke.MethodType.fromMethodDescriptorString(handle.getDesc(), loader);
        switch (handle.getTag()) {
            case org.objectweb.asm.Opcodes.H_INVOKESTATIC: return caller.findStatic(owner, handle.getName(), type);
            case org.objectweb.asm.Opcodes.H_INVOKESPECIAL: return caller.findSpecial(owner, handle.getName(), type, caller.lookupClass());
            case org.objectweb.asm.Opcodes.H_NEWINVOKESPECIAL: return caller.findConstructor(owner, type);
            default: return caller.findVirtual(owner, handle.getName(), type);
        }
    }

    private static Class<?> classOf(org.objectweb.asm.Type type, ClassLoader loader) throws ClassNotFoundException {
        switch (type.getSort()) {
            case org.objectweb.asm.Type.VOID: return void.class;
            case org.objectweb.asm.Type.BOOLEAN: return boolean.class;
            case org.objectweb.asm.Type.CHAR: return char.class;
            case org.objectweb.asm.Type.BYTE: return byte.class;
            case org.objectweb.asm.Type.SHORT: return short.class;
            case org.objectweb.asm.Type.INT: return int.class;
            case org.objectweb.asm.Type.FLOAT: return float.class;
            case org.objectweb.asm.Type.LONG: return long.class;
            case org.objectweb.asm.Type.DOUBLE: return double.class;
            case org.objectweb.asm.Type.ARRAY: return Class.forName(type.getDescriptor().replace('/', '.'), false, loader);
            default: return Class.forName(type.getClassName(), false, loader);
        }
    }

    public static final class GameLoader extends URLClassLoader {
        private final ConcurrentMap<String, byte[]> cache;
        Map<String, byte[]> fallback;
        Map<String, byte[]> adapted;
        java.util.function.UnaryOperator<byte[]> adapter;
        GameLoader(URL[] urls, ClassLoader parent, Map<String, byte[]> cache) { super(urls, parent); this.cache = (ConcurrentMap<String, byte[]>) cache; }
        public ConcurrentMap<String, byte[]> bytecodeCache() { return cache; }
        static boolean adapts(String name) {
            return name.startsWith("net.ccbluex.liquidbounce.") && !name.startsWith("net.ccbluex.liquidbounce.injection.bootstrap.") &&
                !name.startsWith("net.ccbluex.liquidbounce.libraries.") && !name.startsWith("net.ccbluex.liquidbounce.injection.forge.mixins.");
        }
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (!Boolean.getBoolean("fdpclient.audit.hotswap") && name.startsWith("net.minecraft.") && !name.startsWith("net.minecraft.launchwrapper."))
                throw new AssertionError("The offline audit tried to define Minecraft: " + name);
            return super.loadClass(name, resolve);
        }
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] bytes = cache.get(name);
            if (bytes == null && fallback != null) bytes = fallback.get(name);
            if (bytes == null && adapter != null && adapts(name)) {
                try (InputStream in = getResourceAsStream(name.replace('.', '/') + ".class")) {
                    if (in != null) { bytes = adapter.apply(read(in)); adapted.put(name, bytes); }
                } catch (IOException failure) { throw new ClassNotFoundException(name, failure); }
            }
            return bytes == null ? super.findClass(name) : defineClass(name, bytes, 0, bytes.length);
        }
    }
}
