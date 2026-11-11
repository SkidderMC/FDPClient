/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft.
 * https://github.com/SkidderMC/FDPClient/
 * Lunar compatibility by opZywl
 * Reference: https://lucas-lima.xyz
 */
package net.ccbluex.liquidbounce.injection.bootstrap;

import net.minecraft.lunarcheck.Host;
import org.objectweb.asm.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

/** Runs without Minecraft, Forge, a game account or a native injector. */
public final class LunarBridgeVerification {
    private static int checks;

    public static void main(String[] args) throws Exception {
        Map<String, String> names = LunarNames.read(new ByteArrayInputStream(map(false)));
        check("getMinecraft".equals(names.get("func_71410_x")), "mapping getMinecraft");
        check("hidden".equals(names.get("field_12345_a")), "nested class mapping");
        rejected(Arrays.copyOf(map(false), 24), "truncated map");
        byte[] badHeader = map(false);
        badHeader[0] = 0;
        rejected(badHeader, "wrong header");
        byte[] negativeCount = map(false);
        Arrays.fill(negativeCount, 5, 13, (byte) -1);
        rejected(negativeCount, "negative class count");
        rejected(map(true), "ambiguous member name");

        byte[] code = probe("net/ccbluex/liquidbounce/probe/Payload", "func_71410_x", "ç 日本語 \u0000 \uD83D\uDC1F");
        byte[] renamed = ClassFileNames.remap(code, names);
        Class<?> probe = new ByteLoader().define(renamed);
        check("ç 日本語 \u0000 \uD83D\uDC1F".equals(probe.getMethod("getMinecraft").invoke(null)), "modified UTF-8 preservation");
        check(probe.getField("LARGE").getLong(null) == Long.MAX_VALUE, "two-slot long constant");
        Class<?> privateMixin = new ByteLoader().define(ClassFileNames.remap(
            probe("net/ccbluex/liquidbounce/probe/Annotation", "check", "Lorg/spongepowered/asm/mixin/Mixin;"), names, true));
        check("Lnet/ccbluex/liquidbounce/libraries/mixin/mixin/Mixin;".equals(privateMixin.getMethod("check").invoke(null)),
            "Mixin descriptor relocation survives shading");
        Class<?> privateHttp = new ByteLoader().define(ClassFileNames.remap(
            probe("net/ccbluex/liquidbounce/probe/Http", "check", "Lokio/Buffer;"), names, true));
        check("Lnet/ccbluex/liquidbounce/libraries/lunar/okio/Buffer;".equals(privateHttp.getMethod("check").invoke(null)),
            "Lunar HTTP library isolation");
        Class<?> privateScript = new ByteLoader().define(ClassFileNames.remap(
            probe("net/ccbluex/liquidbounce/probe/Script", "check", "Ljdk/nashorn/api/scripting/JSObject;"), names, true));
        check("Lorg/openjdk/nashorn/api/scripting/JSObject;".equals(privateScript.getMethod("check").invoke(null)),
            "Lunar script API relocation");
        Class<?> privateAsm = new ByteLoader().define(ClassFileNames.remap(
            probe("net/ccbluex/liquidbounce/probe/Asm", "check", "Lorg/objectweb/asm/ClassReader;"), names, true));
        check("Lnet/ccbluex/liquidbounce/libraries/asm/ClassReader;".equals(privateAsm.getMethod("check").invoke(null)),
            "runtime ASM isolation survives shading without changing Forge");
        Class<?> browser = new ByteLoader().define(LunarMcefTransformer.transform(browserProbe()));
        Object browserInstance = browser.newInstance();
        browser.getMethod("fdp$renderTick").invoke(browserInstance);
        check(browser.getField("frames").getInt(browserInstance) == 1, "browser frames work without Forge event classes");
        ClassWriter pump = new ClassWriter(0);
        pump.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "org/cef/handler/CefAppHandlerAdapter", null, "java/lang/Object", null);
        MethodVisitor callback = pump.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "onScheduleMessagePumpWork", "(J)V", null, null);
        callback.visitCode();
        callback.visitInsn(Opcodes.ACONST_NULL);
        callback.visitInsn(Opcodes.ATHROW);
        callback.visitMaxs(1, 2);
        callback.visitEnd();
        pump.visitEnd();
        Class<?> manualPump = new ByteLoader().define(LunarMcefTransformer.transform(pump.toByteArray()));
        manualPump.getMethod("onScheduleMessagePumpWork", long.class).invoke(null, 0L);
        check(true, "manual CEF pump never schedules a competing Swing loop");

        byte[] adapted = LunarAccessTransformer.transform(accessProbe(), Host.class.getClassLoader());
        Class<?> access = new ByteLoader().define(adapted);
        Host host = new Host();
        check(((Integer) access.getMethod("run", Host.class).invoke(null, host)) == 42, "private field and method access");
        check(host.value() == 14, "private field write reaches host object");
        access.getMethod("setFinal", Host.class, long.class).invoke(null, host, 31L);
        check(host.fixed() == 31L, "instance final field write without changing the host class");
        ClassLoader noHostLoading = new ClassLoader(Host.class.getClassLoader()) {
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.startsWith("net.minecraft.")) throw new AssertionError("Host class loaded while adapting bytecode: " + name);
                return super.loadClass(name, resolve);
            }
        };
        byte[] structural = LunarAccessTransformer.transform(accessProbe(), noHostLoading,
            (owner, name, descriptor, method) -> new int[]{Modifier.PUBLIC, Modifier.PRIVATE | (name.equals("fixed") ? Modifier.FINAL : 0)});
        Class<?> structuralAccess = new ByteLoader().define(structural);
        check(((Integer) structuralAccess.getMethod("run", Host.class).invoke(null, new Host())) == 42,
            "visibility adaptation does not define host classes recursively");

        Path jar = Files.createTempFile("fdp-lunar-test-", ".jar");
        try {
            try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar))) {
                entry(out, "net/ccbluex/liquidbounce/probe/Payload.class", renamed);
                entry(out, "fdp-lunar-resource.txt", "payload resource".getBytes("UTF-8"));
            }
            try (URLClassLoader loader = new URLClassLoader(new URL[]{jar.toUri().toURL()}, LunarBridgeVerification.class.getClassLoader())) {
                Class<?> payload = loader.loadClass("net.ccbluex.liquidbounce.probe.Payload");
                check(payload.getClassLoader() == loader, "payload class ownership");
                check(ClassInventory.names(loader, "net.ccbluex.liquidbounce.probe").contains(payload.getName()), "class discovery in JARs without directory entries");
                check(payload == loader.loadClass(payload.getName()), "one class identity per loader");
                check(loader.loadClass(Host.class.getName()) == Host.class, "Minecraft host class identity");
                check(payload.getMethod("getMinecraft").invoke(null).equals("ç 日本語 \u0000 \uD83D\uDC1F"), "mapped runtime archive bytecode");
                try (InputStream in = loader.getResourceAsStream("fdp-lunar-resource.txt")) {
                    check(in != null && in.read() == 'p', "payload resources are visible");
                }
            }
        } finally { Files.delete(jar); }

        verifyFirstLaunchCache();

        if (args.length != 0) {
            try (JarFile installed = new JarFile(args[0]); InputStream in = installed.getInputStream(installed.getJarEntry(LunarNames.RESOURCE))) {
                Map<String, String> actual = LunarNames.read(in);
                check(actual.containsKey("field_71428_T"), "installed Lunar timer map");
                check(actual.containsKey("func_76320_a"), "installed Lunar profiler map");
                System.out.println("Installed Lunar map: " + actual.size() + " SRG members");
            }
        }
        System.out.println("Lunar bridge: " + checks + " checks passed");
    }

    private static void verifyFirstLaunchCache() throws Exception {
        Path root = Files.createTempDirectory("fdp-lunar-cold-cache-");
        Path mappings = Files.createFile(root.resolve("lunar-platform-mappings-v1_8.jar"));
        Path cache = Files.createDirectories(root.resolve("cache/fixture"));
        Path zip = Files.createFile(cache.resolve("bake.zip"));
        byte[] bytes = probe("net/minecraft/client/main/Main", "main", "cold-cache");
        String original = System.getProperty("java.class.path");
        try {
            System.setProperty("java.class.path", mappings.toString());
            Constructor<LunarAgent> constructor = LunarAgent.class.getDeclaredConstructor(java.lang.instrument.Instrumentation.class, File.class);
            constructor.setAccessible(true);
            LunarAgent agent = constructor.newInstance(null, null);
            Method locate = LunarAgent.class.getDeclaredMethod("matchingCaches", String.class, byte[].class);
            locate.setAccessible(true);
            check(((List<?>) locate.invoke(agent, "net/minecraft/client/main/Main", bytes)).isEmpty(),
                "first-launch metadata ignores an incomplete Lunar ZIP");
            try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(zip))) {
                entry(out, "net.minecraft.client.main.Main", bytes);
            }
            check(((List<?>) locate.invoke(agent, "net/minecraft/client/main/Main", bytes)).contains(zip.toFile()),
                "completed Lunar metadata matches the actual defining bytes");
            ByteCacheLoader live = new ByteCacheLoader();
            live.bytes.put("net.minecraft.client.main.Main", bytes);
            Method readLive = LunarAgent.class.getDeclaredMethod("liveCache", ClassLoader.class, String.class, byte[].class);
            readLive.setAccessible(true);
            check(readLive.invoke(agent, live, "net/minecraft/client/main/Main", bytes) == live.bytes,
                "first-launch metadata uses the verified live class cache without defining Minecraft classes");
            try {
                readLive.invoke(agent, live, "net/minecraft/client/main/Main", new byte[]{0});
                throw new AssertionError("Accepted a live cache with different defining bytes");
            } catch (InvocationTargetException expected) {
                check(expected.getCause() instanceof IOException, "mismatched live metadata is rejected");
            }
        } finally {
            System.setProperty("java.class.path", original);
            Files.deleteIfExists(zip);
            Files.delete(cache);
            Files.delete(cache.getParent());
            Files.delete(mappings);
            Files.delete(root);
        }
    }

    private static byte[] map(boolean conflicting) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeInt(0x05E8F046);
        out.writeByte(1);
        out.writeLong(1);
        out.writeUTF("net/minecraft/client/Minecraft");
        out.writeUTF("net/minecraft/client/Minecraft");
        out.writeInt(1);
        out.writeUTF("Nested"); out.writeUTF("Nested"); out.writeInt(0);
        out.writeInt(1);
        member(out, "field_12345_a", "I", "hidden");
        out.writeInt(0);
        out.writeInt(0);
        out.writeInt(conflicting ? 2 : 1);
        member(out, "func_71410_x", "()Lnet/minecraft/client/Minecraft;", "getMinecraft");
        if (conflicting) member(out, "func_71410_x", "()Lnet/minecraft/client/Minecraft;", "different");
        return bytes.toByteArray();
    }

    private static byte[] browserProbe() {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        String owner = "net/montoyo/mcef/client/ClientProxy";
        writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, owner, null, "java/lang/Object", null);
        writer.visitField(Opcodes.ACC_PUBLIC, "frames", "I", null, null).visitEnd();
        MethodVisitor ctor = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        ctor.visitCode(); ctor.visitVarInsn(Opcodes.ALOAD, 0);
        ctor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        ctor.visitInsn(Opcodes.RETURN); ctor.visitMaxs(0, 0); ctor.visitEnd();
        String phase = "net/minecraftforge/fml/common/gameevent/TickEvent$Phase";
        MethodVisitor tick = writer.visitMethod(Opcodes.ACC_PUBLIC, "onTick", "(Lnet/minecraftforge/fml/common/gameevent/TickEvent$RenderTickEvent;)V", null, null);
        tick.visitCode(); tick.visitVarInsn(Opcodes.ALOAD, 1);
        tick.visitFieldInsn(Opcodes.GETFIELD, "net/minecraftforge/fml/common/gameevent/TickEvent$RenderTickEvent", "phase", "L" + phase + ";");
        tick.visitFieldInsn(Opcodes.GETSTATIC, phase, "START", "L" + phase + ";");
        Label done = new Label(); tick.visitJumpInsn(Opcodes.IF_ACMPNE, done);
        tick.visitVarInsn(Opcodes.ALOAD, 0); tick.visitInsn(Opcodes.DUP);
        tick.visitFieldInsn(Opcodes.GETFIELD, owner, "frames", "I");
        tick.visitInsn(Opcodes.ICONST_1); tick.visitInsn(Opcodes.IADD);
        tick.visitFieldInsn(Opcodes.PUTFIELD, owner, "frames", "I");
        tick.visitLabel(done); tick.visitInsn(Opcodes.RETURN); tick.visitMaxs(0, 0); tick.visitEnd();
        writer.visitEnd(); return writer.toByteArray();
    }

    private static void member(DataOutputStream out, String name, String descriptor, String target) throws IOException {
        out.writeUTF(name); out.writeUTF(descriptor); out.writeUTF(target);
    }

    private static void rejected(byte[] data, String description) throws IOException {
        try {
            LunarNames.read(new ByteArrayInputStream(data));
            throw new AssertionError("Accepted " + description);
        } catch (IOException expected) { checks++; }
    }

    private static byte[] probe(String owner, String method, String value) {
        ClassWriter out = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        out.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, owner, null, "java/lang/Object", null);
        out.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "LARGE", "J", null, Long.MAX_VALUE).visitEnd();
        MethodVisitor mv = out.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, method, "()Ljava/lang/String;", null, null);
        mv.visitCode(); mv.visitLdcInsn(value); mv.visitInsn(Opcodes.ARETURN); mv.visitMaxs(0, 0); mv.visitEnd();
        out.visitEnd();
        return out.toByteArray();
    }

    private static byte[] accessProbe() {
        String host = "net/minecraft/lunarcheck/Host";
        ClassWriter out = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        out.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "net/ccbluex/liquidbounce/probe/Access", null, "java/lang/Object", null);
        MethodVisitor mv = out.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "run", "(L" + host + ";)I", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0); mv.visitIntInsn(Opcodes.BIPUSH, 14);
        mv.visitFieldInsn(Opcodes.PUTFIELD, host, "value", "I");
        mv.visitVarInsn(Opcodes.ALOAD, 0); mv.visitInsn(Opcodes.ICONST_3);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, host, "multiply", "(I)I", false);
        mv.visitInsn(Opcodes.IRETURN); mv.visitMaxs(0, 0); mv.visitEnd();
        mv = out.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "setFinal", "(L" + host + ";J)V", null, null);
        mv.visitCode(); mv.visitVarInsn(Opcodes.ALOAD, 0); mv.visitVarInsn(Opcodes.LLOAD, 1);
        mv.visitFieldInsn(Opcodes.PUTFIELD, host, "fixed", "J");
        mv.visitInsn(Opcodes.RETURN); mv.visitMaxs(0, 0); mv.visitEnd();
        out.visitEnd(); return out.toByteArray();
    }

    private static void entry(JarOutputStream out, String name, byte[] data) throws IOException {
        out.putNextEntry(new JarEntry(name)); out.write(data); out.closeEntry();
    }

    private static void check(boolean okay, String description) {
        if (!okay) throw new AssertionError(description);
        checks++;
    }

    private static final class ByteLoader extends ClassLoader {
        Class<?> define(byte[] bytes) { return defineClass(null, bytes, 0, bytes.length); }
    }

    private static final class ByteCacheLoader extends ClassLoader {
        final java.util.concurrent.ConcurrentMap<String, byte[]> bytes = new java.util.concurrent.ConcurrentHashMap<>();
        public java.util.concurrent.ConcurrentMap<String, byte[]> entries() { return bytes; }
    }
}
