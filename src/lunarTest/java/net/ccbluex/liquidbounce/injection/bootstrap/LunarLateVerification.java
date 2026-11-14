/*
 * FDPClient Hacked Client
 * Lunar runtime attachment verification by opZywl | https://lucas-lima.xyz
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.bootstrap;

import org.objectweb.asm.*;
import org.objectweb.asm.Type;
import java.io.*;
import java.lang.instrument.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

/** Performs real Java 17 HotSwap after objects and their static initializers already ran. */
public final class LunarLateVerification implements Opcodes {
    private static Instrumentation instrumentation;
    private static int checks;
    private static final String HOST = "net/minecraft/latecheck/LateHost";
    private static final String BASE = "net/minecraft/latecheck/LateBase";
    private static final String CHILD = "net/minecraft/latecheck/LateChild";
    private static final String HIDDEN = "net/minecraft/latecheck/HiddenHost";
    private static final String GENERATED = "net/minecraft/latecheck/GeneratedHost";
    private static final String TOP = "net/minecraft/latecheck/SuperTop";
    private static final String MIDDLE = "net/minecraft/latecheck/SuperMiddle";
    private static final String LEAF = "net/minecraft/latecheck/SuperLeaf";
    private static final String VBASE = "net/minecraft/latecheck/VirtualBase";
    private static final String VSUB = "net/minecraft/latecheck/VirtualSub";
    private static final String SCREEN = "net/minecraft/client/gui/GuiScreen";
    private static final String SCREEN_HOST = "net/minecraft/latecheck/ScreenHost";
    private static final String VIEW = Type.getInternalName(View.class);

    public interface View {
        long getExtra();
        long add(long value);
    }

    public static void premain(String argument, Instrumentation agent) { instrumentation = agent; }

    public static void main(String[] args) throws Exception {
        if (instrumentation == null || !instrumentation.isRedefineClassesSupported())
            throw new IllegalStateException("Run with the late verification agent and Java 17.");
        if (!System.getProperty("java.specification.version").equals("17"))
            throw new IllegalStateException("Late attachment verification requires Java 17.");
        ByteLoader game = new ByteLoader();
        Map<String, byte[]> originals = new LinkedHashMap<>();
        originals.put(BASE, base()); originals.put(CHILD, child(false)); originals.put(HOST, host(false)); originals.put(HIDDEN, hidden());
        originals.put(TOP, chain(TOP, "java/lang/Object", 1, false)); originals.put(MIDDLE, chain(MIDDLE, TOP, 2, false));
        originals.put(LEAF, chain(LEAF, MIDDLE, 3, false));
        originals.put(VBASE, virtualBase(false)); originals.put(VSUB, virtualSub(false));
        originals.put(SCREEN, screen()); originals.put(SCREEN_HOST, screenHost(false));
        ClassReader layoutReader = new ClassReader(originals.get(BASE));
        ClassWriter layoutWriter = new ClassWriter(layoutReader, 0);
        layoutWriter.newConst("unused JVM constant pool entry");
        layoutReader.accept(layoutWriter, 0);
        layoutWriter.visitSource("RetransformedBase.java", null);
        LateSchemaTransformer.Result unchanged = LateSchemaTransformer.prepare(
            Collections.singletonMap(BASE, layoutWriter.toByteArray()), Collections.singletonMap(BASE, originals.get(BASE)),
            (first, second) -> "java/lang/Object");
        check(unchanged.definitions.isEmpty() && unchanged.helpers.isEmpty(),
            "constant pool and debug layout changes do not create runtime hooks");
        Map<String, Class<?>> classes = new LinkedHashMap<>();
        for (Map.Entry<String, byte[]> entry : originals.entrySet()) classes.put(entry.getKey(), game.define(entry.getValue()));
        Class<?> host = classes.get(HOST), base = classes.get(BASE);
        Object before = host.getConstructor(int.class).newInstance(5);
        Object child = classes.get(CHILD).getConstructor().newInstance();
        check((Integer) host.getMethod("tick").invoke(before) == 5, "original object is live before attachment");
        check(host.getField("originalInitRuns").getInt(null) == 1, "original static initializer ran once");
        int fields = host.getDeclaredFields().length, methods = host.getDeclaredMethods().length;

        Map<String, byte[]> patched = new LinkedHashMap<>();
        patched.put(BASE, originals.get(BASE)); patched.put(CHILD, child(true)); patched.put(HOST, host(true));
        patched.put(TOP, chain(TOP, "java/lang/Object", 1, true)); patched.put(MIDDLE, originals.get(MIDDLE));
        patched.put(LEAF, chain(LEAF, MIDDLE, 3, true));
        patched.put(VBASE, virtualBase(true)); patched.put(VSUB, virtualSub(true));
        patched.put(SCREEN, originals.get(SCREEN)); patched.put(SCREEN_HOST, screenHost(true));
        LateSchemaTransformer.Result converted = LateSchemaTransformer.prepare(originals, patched, (first, second) -> {
            try {
                Class<?> a = Class.forName(first.replace('/', '.'), false, game);
                Class<?> b = Class.forName(second.replace('/', '.'), false, game);
                if (a.isAssignableFrom(b)) return first;
                if (b.isAssignableFrom(a)) return second;
                if (a.isInterface() || b.isInterface()) return "java/lang/Object";
                do { a = a.getSuperclass(); } while (a != null && !a.isAssignableFrom(b));
                return a == null ? "java/lang/Object" : Type.getInternalName(a);
            } catch (ClassNotFoundException missing) { throw new IllegalStateException(missing); }
        });
        for (byte[] bytes : converted.helpers.values()) game.define(bytes);
        LateRuntime.install(converted.plans, game);
        List<ClassDefinition> definitions = new ArrayList<>();
        for (Map.Entry<String, byte[]> entry : converted.definitions.entrySet())
            definitions.add(new ClassDefinition(classes.get(entry.getKey()), entry.getValue()));
        instrumentation.redefineClasses(definitions.toArray(new ClassDefinition[0]));
        check(new ClassReader(converted.definitions.get(HOST)).readUnsignedShort(6) >= V1_7,
            "legacy Minecraft class versions support the new invokedynamic bridges");
        check(host.getDeclaredFields().length == fields && host.getDeclaredMethods().length == methods,
            "real HotSwap preserves all fields and methods");
        check(host.getInterfaces().length == 0, "real HotSwap preserves interfaces");
        check((Integer) host.getMethod("tick").invoke(before) == 5, "inactive hooks preserve the running game");

        LateRuntime.activate(true);
        View view = (View) LateRuntime.cast(before, VIEW);
        check(view.getExtra() == 9, "existing object receives its mixin field initializer lazily");
        check((Integer) host.getMethod("tick").invoke(before) == 16, "existing object calls an added private handler");
        check(view.getExtra() == 11, "instance fields persist across hooks and interface adapters");
        check(host.getField("originalInitRuns").getInt(null) == 1, "attachment never reruns the original static initializer");
        Class<?> companion = game.loadClass(HOST.replace('/', '.') + "$FdpLate");
        @SuppressWarnings("unchecked")
        Map<String, String[]> convertedMethods = (Map<String, String[]>) converted.plans.get(HOST).get("methods");
        check(companion.getMethod(convertedMethods.get("add(J)J")[0], host, long.class).getParameters().length == 2,
            "companion parameter metadata matches its explicit receiver");
        String callbacksField = fields(converted.plans.get(HOST)).get("callbacksI")[0];
        check(companion.getField(callbacksField).getInt(null) == 11, "new static initialization and existing constructor callbacks run once");

        Object after = host.getConstructor(int.class).newInstance(7);
        check((Integer) host.getMethod("tick").invoke(after) == 18, "new objects keep their constructor and original fields");
        check(Modifier.isFinal(host.getDeclaredField("value").getModifiers()),
            "pre-super constructor stores preserve original final fields");
        check(companion.getField(callbacksField).getInt(null) == 12, "new constructor callbacks are not duplicated");
        check((Integer) base.getMethod("compute").invoke(child) == 3, "added inherited overrides dispatch and super calls do not recurse");
        Object leaf = classes.get(LEAF).getConstructor().newInstance();
        check((Integer) classes.get(LEAF).getMethod("value").invoke(leaf) == 123,
            "super calls stop at an unpatched intermediate override before a patched ancestor");
        check((Integer) classes.get(LEAF).getMethod("skip").invoke(leaf) == 12,
            "ACC_SUPER invokespecial to a distant ancestor selects from the caller's direct superclass");
        check(!converted.plans.containsKey(MIDDLE) && converted.plans.containsKey(TOP) && converted.plans.containsKey(LEAF),
            "only mixin targets receive runtime plans");
        Object virtualSub = classes.get(VSUB).getConstructor().newInstance(), virtualBase = classes.get(VBASE).getConstructor().newInstance();
        check((Integer) classes.get(VBASE).getMethod("run").invoke(virtualSub) == 2 && (Integer) classes.get(VBASE).getMethod("run").invoke(virtualBase) == 1,
            "mixin-added methods dispatch to mixin overrides of subclasses");
        check(closedScreenIsCollected(classes.get(SCREEN_HOST)), "closed screens release companion state that references them");

        byte[] adapted = LateSchemaTransformer.adaptPayload(converted, payload());
        for (byte[] helper : converted.helpers.values()) game.defineIfAbsent(helper);
        Class<?> probe = game.define(adapted);
        check((Long) probe.getMethod("read", Object.class).invoke(null, before) == 11, "payload CHECKCAST reaches the mixin interface");
        check((Boolean) probe.getMethod("matches", Object.class).invoke(null, before), "payload INSTANCEOF sees the mixin interface");
        check(!(Boolean) probe.getMethod("matches", Object.class).invoke(null, new Object()), "unrelated objects do not acquire mixin interfaces");
        check((Integer) probe.getMethod("privateValue", host).invoke(null, before) == 5, "private original field access works without widening it");
        check(LateRuntime.cast(before, VIEW) == view, "interface adapters preserve identity while referenced");
        check(probe.getMethod("roundTrip", Object.class).invoke(null, before) == before, "interface casts can return to the original Minecraft object");
        check((Boolean) probe.getMethod("same", Object.class).invoke(null, before), "reference comparisons preserve Minecraft object identity");
        check(probe.getMethod("hostClass", Object.class).invoke(null, before) == host, "interface adapters expose the original Minecraft class");
        Constructor<?> hiddenConstructor = classes.get(HIDDEN).getDeclaredConstructor();
        hiddenConstructor.setAccessible(true);
        Object hidden = hiddenConstructor.newInstance();
        check((Integer) probe.getMethod("hidden", Object.class).invoke(null, hidden) == 42, "non-public Minecraft casts need no access widening");
        Object createdHidden = probe.getMethod("createHidden").invoke(null);
        check(createdHidden.getClass() == classes.get(HIDDEN), "private Minecraft constructors work from payload code");
        Class<?> generated = game.define(LateSchemaTransformer.adaptPayload(converted, generated()));
        Object generatedInstance = generated.getConstructor(int.class).newInstance(43);
        check((Integer) generated.getMethod("value").invoke(generatedInstance) == 43,
            "new legacy classes keep physical pre-super fields while supporting dynamic access bridges");
        @SuppressWarnings("unchecked")
        Map<Object, Object> cache = (Map<Object, Object>) game.define(LateSchemaTransformer.adaptPayload(converted, mapCache()))
            .getConstructor().newInstance();
        check(cache.put("key", 41) == null && Integer.valueOf(41).equals(cache.put("key", 42)) &&
            Integer.valueOf(42).equals(cache.get("key")), "payload collection overrides keep inherited JDK super calls");
        check(MemberAccess.method(LinkedHashMap.class, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
            .getDeclaringClass() == HashMap.class, "concrete superclass methods resolve before abstract interface declarations");

        long initial = view.getExtra();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            List<Future<?>> tasks = new ArrayList<>();
            for (int thread = 0; thread < 2; thread++) tasks.add(executor.submit(() -> {
                for (int index = 0; index < 250; index++) view.add(1);
            }));
            for (Future<?> task : tasks) task.get();
        } finally { executor.shutdownNow(); }
        check(view.getExtra() == initial + 500, "added synchronized methods lock the original receiver");

        LateRuntime.activate(false);
        check((Integer) host.getMethod("tick").invoke(before) == 5, "hooks can be disabled without replacing live objects");
        definitions.clear();
        for (Map.Entry<String, byte[]> entry : originals.entrySet()) definitions.add(new ClassDefinition(classes.get(entry.getKey()), entry.getValue()));
        instrumentation.redefineClasses(definitions.toArray(new ClassDefinition[0]));
        check((Integer) host.getMethod("tick").invoke(before) == 5, "original definitions can be restored atomically");
        check((Integer) base.getMethod("compute").invoke(child) == 1, "rollback restores inherited vanilla behavior");
        check((Integer) classes.get(LEAF).getMethod("value").invoke(leaf) == 123, "rollback keeps the vanilla super chain");
        check((Integer) classes.get(VBASE).getMethod("run").invoke(virtualSub) == 0, "rollback removes mixin-added virtual hooks");
        System.out.println("Lunar late attachment: " + checks + " checks passed using real Java 17 class redefinition.");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String[]> fields(Map<String, Object> plan) { return (Map<String, String[]>) plan.get("fields"); }
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }

    private static ClassWriter writer(String name, String parent, String[] interfaces) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        writer.visit(name.equals(HOST) ? V1_6 : V1_8, ACC_PUBLIC | ACC_SUPER, name, null, parent, interfaces);
        return writer;
    }
    private static MethodVisitor method(ClassWriter writer, int access, String name, String descriptor) {
        MethodVisitor method = writer.visitMethod(access, name, descriptor, null, null);
        method.visitCode(); return method;
    }
    private static void end(MethodVisitor method) { method.visitMaxs(0, 0); method.visitEnd(); }

    private static byte[] host(boolean patched) {
        ClassWriter writer = writer(HOST, "java/lang/Object", patched ? new String[]{VIEW} : null);
        writer.visitField(ACC_PRIVATE | ACC_FINAL, "value", "I", null, null).visitEnd();
        writer.visitField(ACC_PUBLIC | ACC_STATIC, "originalInitRuns", "I", null, null).visitEnd();
        if (patched) {
            writer.visitField(ACC_PRIVATE, "extra", "J", null, null).visitEnd();
            writer.visitField(ACC_PRIVATE | ACC_STATIC, "callbacks", "I", null, null).visitEnd();
        }
        MethodVisitor code = method(writer, ACC_STATIC, "<clinit>", "()V");
        code.visitFieldInsn(GETSTATIC, HOST, "originalInitRuns", "I"); code.visitInsn(ICONST_1); code.visitInsn(IADD);
        code.visitFieldInsn(PUTSTATIC, HOST, "originalInitRuns", "I");
        if (patched) { code.visitIntInsn(BIPUSH, 10); code.visitFieldInsn(PUTSTATIC, HOST, "callbacks", "I"); }
        code.visitInsn(RETURN); end(code);
        code = method(writer, ACC_PUBLIC, "<init>", "(I)V");
        code.visitVarInsn(ALOAD, 0); code.visitVarInsn(ILOAD, 1); code.visitFieldInsn(PUTFIELD, HOST, "value", "I");
        code.visitVarInsn(ALOAD, 0); code.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        if (patched) {
            code.visitVarInsn(ALOAD, 0); code.visitLdcInsn(9L); code.visitFieldInsn(PUTFIELD, HOST, "extra", "J");
            code.visitVarInsn(ALOAD, 0); code.visitMethodInsn(INVOKESPECIAL, HOST, "initialized", "()V", false);
        }
        code.visitInsn(RETURN); end(code);
        code = method(writer, ACC_PUBLIC, "tick", "()I");
        code.visitVarInsn(ALOAD, 0); code.visitFieldInsn(GETFIELD, HOST, "value", "I");
        if (patched) {
            code.visitVarInsn(ALOAD, 0); code.visitLdcInsn(2L);
            code.visitMethodInsn(INVOKEVIRTUAL, HOST, "add", "(J)J", false); code.visitInsn(L2I); code.visitInsn(IADD);
        }
        code.visitInsn(IRETURN); end(code);
        if (patched) {
            code = method(writer, ACC_PRIVATE, "initialized", "()V");
            code.visitFieldInsn(GETSTATIC, HOST, "callbacks", "I"); code.visitInsn(ICONST_1); code.visitInsn(IADD);
            code.visitFieldInsn(PUTSTATIC, HOST, "callbacks", "I"); code.visitInsn(RETURN); end(code);
            code = method(writer, ACC_PUBLIC, "getExtra", "()J");
            code.visitVarInsn(ALOAD, 0); code.visitFieldInsn(GETFIELD, HOST, "extra", "J"); code.visitInsn(LRETURN); end(code);
            code = method(writer, ACC_PUBLIC | ACC_SYNCHRONIZED, "add", "(J)J");
            code.visitParameter("increment", 0);
            code.visitVarInsn(ALOAD, 0); code.visitInsn(DUP); code.visitFieldInsn(GETFIELD, HOST, "extra", "J");
            code.visitVarInsn(LLOAD, 1); code.visitInsn(LADD); code.visitFieldInsn(PUTFIELD, HOST, "extra", "J");
            code.visitVarInsn(ALOAD, 0); code.visitFieldInsn(GETFIELD, HOST, "extra", "J"); code.visitInsn(LRETURN); end(code);
        }
        writer.visitEnd(); return writer.toByteArray();
    }

    private static byte[] base() {
        ClassWriter writer = writer(BASE, "java/lang/Object", null);
        MethodVisitor code = method(writer, ACC_PUBLIC, "<init>", "()V");
        code.visitVarInsn(ALOAD, 0); code.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false); code.visitInsn(RETURN); end(code);
        code = method(writer, ACC_PUBLIC, "compute", "()I"); code.visitInsn(ICONST_1); code.visitInsn(IRETURN); end(code);
        writer.visitEnd(); return writer.toByteArray();
    }

    private static byte[] child(boolean patched) {
        ClassWriter writer = writer(CHILD, BASE, null);
        MethodVisitor code = method(writer, ACC_PUBLIC, "<init>", "()V");
        code.visitVarInsn(ALOAD, 0); code.visitMethodInsn(INVOKESPECIAL, BASE, "<init>", "()V", false); code.visitInsn(RETURN); end(code);
        if (patched) {
            code = method(writer, ACC_PUBLIC, "compute", "()I");
            code.visitVarInsn(ALOAD, 0); code.visitMethodInsn(INVOKESPECIAL, BASE, "compute", "()I", false);
            code.visitInsn(ICONST_2); code.visitInsn(IADD); code.visitInsn(IRETURN); end(code);
        }
        writer.visitEnd(); return writer.toByteArray();
    }

    private static byte[] chain(String name, String parent, int digit, boolean patched) {
        ClassWriter writer = writer(name, parent, null);
        MethodVisitor code = method(writer, ACC_PUBLIC, "<init>", "()V");
        code.visitVarInsn(ALOAD, 0); code.visitMethodInsn(INVOKESPECIAL, parent, "<init>", "()V", false); code.visitInsn(RETURN); end(code);
        code = method(writer, ACC_PUBLIC, "value", "()I");
        if (parent.equals("java/lang/Object")) code.visitInsn(ICONST_0);
        else { code.visitVarInsn(ALOAD, 0); code.visitMethodInsn(INVOKESPECIAL, parent, "value", "()I", false); }
        code.visitIntInsn(BIPUSH, 10); code.visitInsn(IMUL); code.visitIntInsn(BIPUSH, digit); code.visitInsn(IADD);
        if (patched) { code.visitVarInsn(ALOAD, 0); code.visitMethodInsn(INVOKESPECIAL, name, "fdp$bonus", "()I", false); code.visitInsn(IADD); }
        code.visitInsn(IRETURN); end(code);
        if (patched) { code = method(writer, ACC_PRIVATE, "fdp$bonus", "()I"); code.visitInsn(ICONST_0); code.visitInsn(IRETURN); end(code); }
        if (name.equals(LEAF)) {
            code = method(writer, ACC_PUBLIC, "skip", "()I");
            code.visitVarInsn(ALOAD, 0); code.visitMethodInsn(INVOKESPECIAL, TOP, "value", "()I", false);
            if (patched) { code.visitVarInsn(ALOAD, 0); code.visitMethodInsn(INVOKESPECIAL, name, "fdp$bonus", "()I", false); code.visitInsn(IADD); }
            code.visitInsn(IRETURN); end(code);
        }
        writer.visitEnd(); return writer.toByteArray();
    }

    private static boolean closedScreenIsCollected(Class<?> host) throws Exception {
        java.lang.ref.WeakReference<Object> released = open(host);
        LateRuntime.releaseUiState(null);
        for (int attempt = 0; attempt < 40 && released.get() != null; attempt++) { System.gc(); Thread.sleep(25); }
        return released.get() == null;
    }

    private static java.lang.ref.WeakReference<Object> open(Class<?> host) throws Exception {
        Object screen = host.getConstructor().newInstance();
        host.getMethod("open").invoke(screen);
        return new java.lang.ref.WeakReference<>(screen);
    }

    private static byte[] screen() {
        ClassWriter writer = writer(SCREEN, "java/lang/Object", null);
        MethodVisitor code = method(writer, ACC_PUBLIC, "<init>", "()V");
        code.visitVarInsn(ALOAD, 0); code.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false); code.visitInsn(RETURN); end(code);
        writer.visitEnd(); return writer.toByteArray();
    }

    private static byte[] screenHost(boolean patched) {
        ClassWriter writer = writer(SCREEN_HOST, SCREEN, null);
        if (patched) writer.visitField(ACC_PRIVATE, "fdp$callback", "Ljava/lang/Object;", null, null).visitEnd();
        MethodVisitor code = method(writer, ACC_PUBLIC, "<init>", "()V");
        code.visitVarInsn(ALOAD, 0); code.visitMethodInsn(INVOKESPECIAL, SCREEN, "<init>", "()V", false); code.visitInsn(RETURN); end(code);
        code = method(writer, ACC_PUBLIC, "open", "()V");
        if (patched) {
            code.visitVarInsn(ALOAD, 0); code.visitInsn(ICONST_1); code.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            code.visitInsn(DUP); code.visitInsn(ICONST_0); code.visitVarInsn(ALOAD, 0); code.visitInsn(AASTORE);
            code.visitFieldInsn(PUTFIELD, SCREEN_HOST, "fdp$callback", "Ljava/lang/Object;");
        }
        code.visitInsn(RETURN); end(code);
        writer.visitEnd(); return writer.toByteArray();
    }

    private static byte[] virtualBase(boolean patched) {
        ClassWriter writer = writer(VBASE, "java/lang/Object", null);
        MethodVisitor code = method(writer, ACC_PUBLIC, "<init>", "()V");
        code.visitVarInsn(ALOAD, 0); code.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false); code.visitInsn(RETURN); end(code);
        code = method(writer, ACC_PUBLIC, "run", "()I");
        if (patched) { code.visitVarInsn(ALOAD, 0); code.visitMethodInsn(INVOKEVIRTUAL, VBASE, "hook", "()I", false); }
        else code.visitInsn(ICONST_0);
        code.visitInsn(IRETURN); end(code);
        if (patched) { code = method(writer, ACC_PROTECTED, "hook", "()I"); code.visitInsn(ICONST_1); code.visitInsn(IRETURN); end(code); }
        writer.visitEnd(); return writer.toByteArray();
    }

    private static byte[] virtualSub(boolean patched) {
        ClassWriter writer = writer(VSUB, VBASE, null);
        MethodVisitor code = method(writer, ACC_PUBLIC, "<init>", "()V");
        code.visitVarInsn(ALOAD, 0); code.visitMethodInsn(INVOKESPECIAL, VBASE, "<init>", "()V", false); code.visitInsn(RETURN); end(code);
        if (patched) { code = method(writer, ACC_PROTECTED, "hook", "()I"); code.visitInsn(ICONST_2); code.visitInsn(IRETURN); end(code); }
        writer.visitEnd(); return writer.toByteArray();
    }

    private static byte[] payload() {
        ClassWriter writer = writer("net/ccbluex/liquidbounce/latecheck/Probe", "java/lang/Object", null);
        MethodVisitor code = method(writer, ACC_PUBLIC | ACC_STATIC, "read", "(Ljava/lang/Object;)J");
        code.visitVarInsn(ALOAD, 0); code.visitTypeInsn(CHECKCAST, VIEW);
        code.visitMethodInsn(INVOKEINTERFACE, VIEW, "getExtra", "()J", true); code.visitInsn(LRETURN); end(code);
        code = method(writer, ACC_PUBLIC | ACC_STATIC, "matches", "(Ljava/lang/Object;)Z");
        code.visitVarInsn(ALOAD, 0); code.visitTypeInsn(INSTANCEOF, VIEW); code.visitInsn(IRETURN); end(code);
        code = method(writer, ACC_PUBLIC | ACC_STATIC, "privateValue", "(L" + HOST + ";)I");
        code.visitVarInsn(ALOAD, 0); code.visitFieldInsn(GETFIELD, HOST, "value", "I"); code.visitInsn(IRETURN); end(code);
        code = method(writer, ACC_PUBLIC | ACC_STATIC, "roundTrip", "(Ljava/lang/Object;)Ljava/lang/Object;");
        code.visitVarInsn(ALOAD, 0); code.visitTypeInsn(CHECKCAST, VIEW); code.visitTypeInsn(CHECKCAST, HOST); code.visitInsn(ARETURN); end(code);
        code = method(writer, ACC_PUBLIC | ACC_STATIC, "hostClass", "(Ljava/lang/Object;)Ljava/lang/Class;");
        code.visitVarInsn(ALOAD, 0); code.visitTypeInsn(CHECKCAST, VIEW); code.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false); code.visitInsn(ARETURN); end(code);
        code = method(writer, ACC_PUBLIC | ACC_STATIC, "same", "(Ljava/lang/Object;)Z");
        Label different = new Label();
        code.visitVarInsn(ALOAD, 0); code.visitTypeInsn(CHECKCAST, VIEW); code.visitVarInsn(ALOAD, 0);
        code.visitJumpInsn(IF_ACMPNE, different); code.visitInsn(ICONST_1); code.visitInsn(IRETURN);
        code.visitLabel(different); code.visitFrame(F_SAME, 0, null, 0, null); code.visitInsn(ICONST_0); code.visitInsn(IRETURN); end(code);
        code = method(writer, ACC_PUBLIC | ACC_STATIC, "hidden", "(Ljava/lang/Object;)I");
        code.visitVarInsn(ALOAD, 0); code.visitTypeInsn(CHECKCAST, HIDDEN); code.visitFieldInsn(GETFIELD, HIDDEN, "value", "I"); code.visitInsn(IRETURN); end(code);
        code = method(writer, ACC_PUBLIC | ACC_STATIC, "createHidden", "()Ljava/lang/Object;");
        code.visitTypeInsn(NEW, HIDDEN); code.visitInsn(DUP); code.visitMethodInsn(INVOKESPECIAL, HIDDEN, "<init>", "()V", false); code.visitInsn(ARETURN); end(code);
        writer.visitEnd(); return writer.toByteArray();
    }

    private static byte[] hidden() {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        writer.visit(V1_8, ACC_SUPER, HIDDEN, null, "java/lang/Object", null);
        writer.visitField(ACC_PUBLIC, "value", "I", null, null).visitEnd();
        MethodVisitor code = method(writer, ACC_PRIVATE, "<init>", "()V");
        code.visitVarInsn(ALOAD, 0); code.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        code.visitVarInsn(ALOAD, 0); code.visitIntInsn(BIPUSH, 42); code.visitFieldInsn(PUTFIELD, HIDDEN, "value", "I"); code.visitInsn(RETURN); end(code);
        writer.visitEnd(); return writer.toByteArray();
    }

    private static byte[] generated() {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        writer.visit(V1_6, ACC_PUBLIC | ACC_SUPER, GENERATED, null, "java/lang/Object", null);
        writer.visitField(ACC_PRIVATE | ACC_FINAL, "value", "I", null, null).visitEnd();
        MethodVisitor code = method(writer, ACC_PUBLIC, "<init>", "(I)V");
        code.visitVarInsn(ALOAD, 0); code.visitVarInsn(ILOAD, 1); code.visitFieldInsn(PUTFIELD, GENERATED, "value", "I");
        code.visitVarInsn(ALOAD, 0); code.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        code.visitInsn(RETURN); end(code);
        code = method(writer, ACC_PUBLIC, "value", "()I");
        code.visitVarInsn(ALOAD, 0); code.visitFieldInsn(GETFIELD, GENERATED, "value", "I"); code.visitInsn(IRETURN); end(code);
        code = method(writer, ACC_PUBLIC | ACC_STATIC, "readHost", "(L" + HOST + ";)I");
        code.visitVarInsn(ALOAD, 0); code.visitMethodInsn(INVOKEVIRTUAL, HOST, "tick", "()I", false); code.visitInsn(IRETURN); end(code);
        writer.visitEnd(); return writer.toByteArray();
    }

    private static byte[] mapCache() {
        String name = "net/ccbluex/liquidbounce/latecheck/MapCache";
        ClassWriter writer = writer(name, "java/util/LinkedHashMap", null);
        MethodVisitor code = method(writer, ACC_PUBLIC, "<init>", "()V");
        code.visitVarInsn(ALOAD, 0); code.visitMethodInsn(INVOKESPECIAL, "java/util/LinkedHashMap", "<init>", "()V", false);
        code.visitInsn(RETURN); end(code);
        code = method(writer, ACC_PUBLIC, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
        code.visitVarInsn(ALOAD, 0); code.visitVarInsn(ALOAD, 1); code.visitVarInsn(ALOAD, 2);
        code.visitMethodInsn(INVOKESPECIAL, "java/util/LinkedHashMap", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
        code.visitInsn(ARETURN); end(code);
        code = method(writer, ACC_PUBLIC, "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
        code.visitVarInsn(ALOAD, 0); code.visitVarInsn(ALOAD, 1);
        code.visitMethodInsn(INVOKESPECIAL, "java/util/LinkedHashMap", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
        code.visitInsn(ARETURN); end(code);
        writer.visitEnd(); return writer.toByteArray();
    }

    private static final class ByteLoader extends ClassLoader {
        ByteLoader() { super(LunarLateVerification.class.getClassLoader()); }
        Class<?> define(byte[] code) { return defineClass(null, code, 0, code.length); }
        void defineIfAbsent(byte[] code) {
            String name = new ClassReader(code).getClassName().replace('/', '.');
            if (findLoadedClass(name) == null) define(code);
        }
        protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (!name.startsWith(LateRuntime.class.getName()) && !name.equals(MemberAccess.class.getName())) return super.loadClass(name, resolve);
            Class<?> defined = findLoadedClass(name);
            if (defined == null) {
                try (InputStream in = getParent().getResourceAsStream(name.replace('.', '/') + ".class")) {
                    if (in == null) throw new ClassNotFoundException(name);
                    ByteArrayOutputStream data = new ByteArrayOutputStream(); byte[] buffer = new byte[8192]; int count;
                    while ((count = in.read(buffer)) != -1) data.write(buffer, 0, count);
                    byte[] bytes = data.toByteArray(); defined = defineClass(name, bytes, 0, bytes.length);
                } catch (IOException failure) { throw new ClassNotFoundException(name, failure); }
            }
            if (resolve) resolveClass(defined);
            return defined;
        }
    }
}
