/*
 * FDPClient Hacked Client
 * Lunar runtime attachment by opZywl | https://lucas-lima.xyz
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.bootstrap;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import java.util.*;

/** Lowers mixin additions into companion classes while preserving the loaded class schema. */
public final class LateSchemaTransformer implements Opcodes {
    private static final String RUNTIME = "net/ccbluex/liquidbounce/injection/bootstrap/LateRuntime";
    private static final Handle LINK = new Handle(H_INVOKESTATIC, RUNTIME, "bootstrap",
        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;)Ljava/lang/invoke/CallSite;", false);
    private static final Handle CAST = new Handle(H_INVOKESTATIC, RUNTIME, "castBootstrap",
        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;)Ljava/lang/invoke/CallSite;", false);
    public interface Hierarchy { String commonSuperClass(String first, String second); }

    public static final class Result {
        public final Map<String, byte[]> definitions = new LinkedHashMap<>();
        public final Map<String, byte[]> helpers = new LinkedHashMap<>();
        public final Map<String, Map<String, Object>> plans = new LinkedHashMap<>();
        public final Set<String> interfaces = new HashSet<>();
        private final Map<String, ClassNode> originals = new LinkedHashMap<>();
        private final Map<String, ClassNode> patched = new LinkedHashMap<>();
        private final Set<String> dispatch = new HashSet<>();
        private final Hierarchy hierarchy;
        Result(Hierarchy hierarchy) { this.hierarchy = hierarchy; }
        public boolean containsOriginal(String name) { return originals.containsKey(name); }
    }

    private LateSchemaTransformer() { }

    public static Result prepare(Map<String, byte[]> originals, Map<String, byte[]> patched, Hierarchy hierarchy) throws Exception {
        Result result = new Result(hierarchy);
        for (Map.Entry<String, byte[]> entry : originals.entrySet()) {
            ClassNode original = read(entry.getValue());
            result.originals.put(original.name, original);
            byte[] changed = patched.get(entry.getKey());
            if (changed != null && !sameDefinition(entry.getValue(), changed)) {
                ClassNode altered = read(changed);
                result.patched.put(original.name, altered);
                result.plans.put(original.name, plan(original, altered));
                result.interfaces.addAll(addedInterfaces(original, altered));
            }
        }
        Set<String> dispatchOwners = new LinkedHashSet<>();
        for (Map.Entry<String, ClassNode> entry : result.patched.entrySet()) {
            ClassNode original = result.originals.get(entry.getKey());
            Set<String> existing = methodKeys(original);
            for (MethodNode method : entry.getValue().methods) {
                if (method.name.startsWith("<") || existing.contains(method.name + method.desc) ||
                    (method.access & (ACC_PRIVATE | ACC_STATIC)) != 0) continue;
                for (ClassNode parent = result.originals.get(original.superName); parent != null;
                     parent = result.originals.get(parent.superName)) {
                    if (methodKeys(parent).contains(method.name + method.desc)) {
                        result.dispatch.add(parent.name + "." + method.name + method.desc);
                        dispatchOwners.add(parent.name);
                    }
                }
            }
        }
        for (String owner : dispatchOwners) {
            if (!result.patched.containsKey(owner)) {
                ClassNode original = result.originals.get(owner);
                result.patched.put(owner, original);
                result.plans.put(owner, plan(original, original));
            }
        }
        for (Map.Entry<String, ClassNode> entry : result.patched.entrySet()) {
            String owner = entry.getKey();
            ClassNode original = result.originals.get(owner), altered = entry.getValue();
            ClassNode companion = companion(result, original, altered);
            result.helpers.put(companion.name, write(companion, hierarchy));
            ClassNode preserved = read(originals.get(owner));
            // Vanilla 1.8.9 still contains pre-Java-7 classes. Our invokedynamic
            // access bridges require a modern constant pool and computed frames.
            preserved.version = Math.max(V1_8, preserved.version);
            Map<String, MethodNode> replacements = methodMap(altered);
            for (int index = 0; index < preserved.methods.size(); index++) {
                MethodNode method = preserved.methods.get(index);
                MethodNode replacement = replacements.get(method.name + method.desc);
                if (replacement == null || method.name.equals("<clinit>") || (method.access & (ACC_ABSTRACT | ACC_NATIVE)) != 0) continue;
                preserved.methods.set(index, gated(result, original, method, replacement));
            }
            byte[] definition = write(preserved, hierarchy);
            requireSameSchema(original, read(definition));
            result.definitions.put(owner, definition);
        }
        return result;
    }

    private static boolean sameDefinition(byte[] original, byte[] changed) {
        return Arrays.equals(original, changed) || Arrays.equals(comparable(original), comparable(changed));
    }

    /** JVM retransformation can reorder the constant pool and regenerate debug/frame data. */
    private static byte[] comparable(byte[] bytes) {
        ClassWriter writer = new ClassWriter(0);
        new ClassReader(bytes).accept(new ClassVisitor(ASM9, writer) {
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                return new MethodVisitor(ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                    public void visitMaxs(int stack, int locals) {
                        // Comparison only: these bytes are never used as a class definition.
                        super.visitMaxs(0, 0);
                    }
                };
            }
        }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return writer.toByteArray();
    }

    private static ClassNode read(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, ClassReader.EXPAND_FRAMES);
        return node;
    }

    private static byte[] write(ClassNode node, Hierarchy hierarchy) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {
            protected String getCommonSuperClass(String first, String second) { return hierarchy.commonSuperClass(first, second); }
        };
        node.accept(writer);
        return writer.toByteArray();
    }

    private static List<String> addedInterfaces(ClassNode original, ClassNode patched) {
        List<String> added = new ArrayList<>(patched.interfaces);
        added.removeAll(original.interfaces);
        return added;
    }

    private static Set<String> methodKeys(ClassNode node) { return methodMap(node).keySet(); }
    private static Map<String, MethodNode> methodMap(ClassNode node) {
        Map<String, MethodNode> methods = new LinkedHashMap<>();
        for (MethodNode method : node.methods) methods.put(method.name + method.desc, method);
        return methods;
    }

    private static Map<String, Object> plan(ClassNode original, ClassNode patched) {
        Map<String, Object> plan = new HashMap<>();
        plan.put("owner", original.name);
        plan.put("helper", original.name + "$FdpLate");
        plan.put("interfaces", addedInterfaces(original, patched));
        Map<String, String[]> fields = new LinkedHashMap<>(), methods = new LinkedHashMap<>();
        Set<String> existingFields = new HashSet<>();
        for (FieldNode field : original.fields) existingFields.add(field.name + field.desc);
        for (FieldNode field : patched.fields) {
            if (!existingFields.contains(field.name + field.desc))
                fields.put(field.name + field.desc, new String[]{"f" + fields.size(), field.desc, Integer.toString(field.access)});
        }
        Set<String> existingMethods = methodKeys(original);
        for (MethodNode method : patched.methods) {
            if (method.name.startsWith("<") || (method.access & (ACC_ABSTRACT | ACC_NATIVE)) != 0) continue;
            String descriptor = (method.access & ACC_STATIC) != 0 ? method.desc : "(L" + original.name + ";" + method.desc.substring(1);
            methods.put(method.name + method.desc, new String[]{"m" + methods.size(), descriptor,
                existingMethods.contains(method.name + method.desc) ? "existing" : "added", Integer.toString(method.access)});
        }
        plan.put("fields", fields);
        plan.put("methods", methods);
        return plan;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String[]> members(Map<String, Object> plan, String kind) {
        return (Map<String, String[]>) plan.get(kind);
    }

    private static ClassNode companion(Result result, ClassNode original, ClassNode patched) throws Exception {
        Map<String, Object> plan = result.plans.get(original.name);
        ClassNode helper = new ClassNode();
        helper.version = Math.max(V1_8, original.version);
        helper.access = ACC_PUBLIC | ACC_FINAL | ACC_SUPER | ACC_SYNTHETIC;
        helper.name = (String) plan.get("helper");
        helper.superName = "java/lang/Object";
        for (FieldNode field : patched.fields) {
            String[] metadata = members(plan, "fields").get(field.name + field.desc);
            if (metadata != null) helper.fields.add(new FieldNode(ACC_PUBLIC | (field.access & (ACC_STATIC | ACC_VOLATILE | ACC_TRANSIENT)),
                metadata[0], field.desc, null, field.value));
        }
        MethodNode constructor = new MethodNode(ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.instructions.add(new VarInsnNode(ALOAD, 0));
        constructor.instructions.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false));
        constructor.instructions.add(new InsnNode(RETURN));
        helper.methods.add(constructor);
        for (MethodNode method : patched.methods) {
            String[] metadata = members(plan, "methods").get(method.name + method.desc);
            if (metadata == null) continue;
            MethodNode copy = copy(method);
            copy.name = metadata[0];
            copy.desc = metadata[1];
            copy.access = ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC | (method.access & ACC_VARARGS);
            copy.signature = null;
            copy.parameters = null;
            copy.visibleParameterAnnotations = null;
            copy.invisibleParameterAnnotations = null;
            copy.visibleAnnotableParameterCount = 0;
            copy.invisibleAnnotableParameterCount = 0;
            copy.visibleTypeAnnotations = null;
            copy.invisibleTypeAnnotations = null;
            if ((method.access & ACC_SYNCHRONIZED) != 0) monitor(copy, original.name, (method.access & ACC_STATIC) != 0);
            rewrite(result, original.name, copy, true, helper);
            helper.methods.add(copy);
        }
        boolean instanceFields = false;
        for (String[] field : members(plan, "fields").values()) instanceFields |= (Integer.parseInt(field[2]) & ACC_STATIC) == 0;
        MethodNode initialize = new MethodNode(ACC_PUBLIC | ACC_STATIC, "initialize", "(L" + original.name + ";)V", null, null);
        MethodNode initializeStatic = new MethodNode(ACC_STATIC, "<clinit>", "()V", null, null);
        Set<String> initialized = new HashSet<>(), initializedStatic = new HashSet<>();
        for (MethodNode method : patched.methods) {
            if (method.name.equals("<init>") && instanceFields) appendInitializers(result, patched, method, initialize, initialized, false);
            if (method.name.equals("<clinit>")) appendInitializers(result, patched, method, initializeStatic, initializedStatic, true);
        }
        initialize.instructions.add(new InsnNode(RETURN));
        rewrite(result, original.name, initialize, true, helper);
        helper.methods.add(initialize);
        initializeStatic.instructions.add(new InsnNode(RETURN));
        rewrite(result, original.name, initializeStatic, true, helper);
        helper.methods.add(initializeStatic);
        return helper;
    }

    private static void appendInitializers(Result result, ClassNode owner, MethodNode source, MethodNode destination,
                                           Set<String> initialized, boolean staticFields) throws AnalyzerException {
        Map<String, Object> plan = result.plans.get(owner.name);
        Frame<SourceValue>[] frames = new Analyzer<>(new SourceInterpreter()).analyze(owner.name, source);
        AbstractInsnNode[] instructions = source.instructions.toArray();
        for (int end = 0; end < instructions.length; end++) {
            AbstractInsnNode instruction = instructions[end];
            String key = null;
            if (instruction instanceof FieldInsnNode) {
                FieldInsnNode field = (FieldInsnNode) instruction;
                if (field.owner.equals(owner.name) && field.getOpcode() == (staticFields ? PUTSTATIC : PUTFIELD) &&
                    members(plan, "fields").containsKey(field.name + field.desc)) key = "field:" + field.name + field.desc;
            } else if (instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                String[] metadata = members(plan, "methods").get(call.name + call.desc);
                if (call.owner.equals(owner.name) && metadata != null && "added".equals(metadata[2]) &&
                    Type.getReturnType(call.desc).getSort() == Type.VOID) key = "call:" + call.name + call.desc;
            }
            if (key == null || initialized.contains(key)) continue;
            int start = end;
            while (start > 0 && (frames[start] == null || frames[start].getStackSize() != 0 || instructions[start].getOpcode() < 0)) start--;
            Set<LabelNode> labels = new HashSet<>();
            for (int index = start; index <= end; index++) if (instructions[index] instanceof LabelNode) labels.add((LabelNode) instructions[index]);
            for (int index = start; index <= end; index++) {
                AbstractInsnNode candidate = instructions[index];
                if ((candidate instanceof VarInsnNode && (staticFields || ((VarInsnNode) candidate).var != 0)) ||
                    candidate instanceof IincInsnNode || candidate instanceof TableSwitchInsnNode || candidate instanceof LookupSwitchInsnNode ||
                    (candidate instanceof JumpInsnNode && !labels.contains(((JumpInsnNode) candidate).label)))
                    throw new IllegalStateException("Initializer needs constructor arguments: " + owner.name + "." + source.name + source.desc + " / " + key);
            }
            Map<LabelNode, LabelNode> mapping = new HashMap<>();
            for (LabelNode label : labels) mapping.put(label, new LabelNode());
            for (int index = start; index <= end; index++) {
                AbstractInsnNode candidate = instructions[index];
                if (candidate.getOpcode() >= 0 || candidate instanceof LabelNode) destination.instructions.add(candidate.clone(mapping));
            }
            initialized.add(key);
        }
    }

    private static MethodNode copy(MethodNode source) {
        MethodNode copied = new MethodNode(source.access, source.name, source.desc, source.signature,
            source.exceptions == null ? null : source.exceptions.toArray(new String[0]));
        source.accept(copied);
        copied.localVariables = null;
        for (AbstractInsnNode instruction : copied.instructions.toArray()) if (instruction instanceof FrameNode) copied.instructions.remove(instruction);
        return copied;
    }

    private static MethodNode gated(Result result, ClassNode owner, MethodNode original, MethodNode patched) {
        MethodNode unchanged = copy(original), active = copy(patched);
        rewrite(result, owner.name, active, false, null);
        boolean instanceFields = false;
        for (String[] field : members(result.plans.get(owner.name), "fields").values())
            instanceFields |= (Integer.parseInt(field[2]) & ACC_STATIC) == 0;
        if (original.name.equals("<init>") && instanceFields) {
            for (AbstractInsnNode instruction : active.instructions.toArray()) {
                if (instruction instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if (call.getOpcode() == INVOKESPECIAL && call.name.equals("<init>") &&
                        (call.owner.equals(owner.name) || call.owner.equals(owner.superName))) {
                        InsnList begin = construction(owner.name, "beginConstruction");
                        active.instructions.insert(call, begin);
                        break;
                    }
                }
            }
            for (AbstractInsnNode instruction : active.instructions.toArray()) if (instruction.getOpcode() == RETURN)
                active.instructions.insertBefore(instruction, construction(owner.name, "endConstruction"));
        }
        InsnList body = new InsnList();
        LabelNode enabled = new LabelNode();
        body.add(new MethodInsnNode(INVOKESTATIC, RUNTIME, "active", "()Z", false));
        body.add(new JumpInsnNode(IFNE, enabled));
        body.add(unchanged.instructions);
        body.add(enabled);
        if (result.dispatch.contains(owner.name + "." + original.name + original.desc)) body.add(dispatch(owner.name, original));
        body.add(active.instructions);
        unchanged.instructions = body;
        unchanged.tryCatchBlocks.addAll(active.tryCatchBlocks);
        unchanged.maxLocals = Math.max(original.maxLocals, patched.maxLocals);
        return unchanged;
    }

    private static void monitor(MethodNode method, String owner, boolean staticMethod) {
        int lock = method.maxLocals, failure = lock + 1;
        LabelNode start = new LabelNode(), end = new LabelNode(), handler = new LabelNode();
        InsnList prefix = new InsnList();
        prefix.add(staticMethod ? new LdcInsnNode(Type.getObjectType(owner)) : new VarInsnNode(ALOAD, 0));
        prefix.add(new InsnNode(DUP)); prefix.add(new VarInsnNode(ASTORE, lock)); prefix.add(new InsnNode(MONITORENTER)); prefix.add(start);
        method.instructions.insert(prefix);
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (instruction.getOpcode() >= IRETURN && instruction.getOpcode() <= RETURN) {
                InsnList unlock = new InsnList();
                unlock.add(new VarInsnNode(ALOAD, lock)); unlock.add(new InsnNode(MONITOREXIT));
                method.instructions.insertBefore(instruction, unlock);
            }
        }
        method.instructions.add(end); method.instructions.add(handler);
        method.instructions.add(new VarInsnNode(ASTORE, failure));
        method.instructions.add(new VarInsnNode(ALOAD, lock)); method.instructions.add(new InsnNode(MONITOREXIT));
        method.instructions.add(new VarInsnNode(ALOAD, failure)); method.instructions.add(new InsnNode(ATHROW));
        method.tryCatchBlocks.add(new TryCatchBlockNode(start, end, handler, null));
        method.maxLocals = failure + 1;
    }

    private static InsnList construction(String owner, String method) {
        InsnList body = new InsnList();
        body.add(new VarInsnNode(ALOAD, 0));
        body.add(new LdcInsnNode(owner));
        body.add(new MethodInsnNode(INVOKESTATIC, RUNTIME, method, "(Ljava/lang/Object;Ljava/lang/String;)V", false));
        return body;
    }

    private static InsnList dispatch(String owner, MethodNode method) {
        InsnList body = new InsnList();
        LabelNode unchanged = new LabelNode();
        body.add(new VarInsnNode(ALOAD, 0));
        body.add(new LdcInsnNode(owner)); body.add(new LdcInsnNode(method.name)); body.add(new LdcInsnNode(method.desc));
        body.add(new MethodInsnNode(INVOKESTATIC, RUNTIME, "override", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/invoke/MethodHandle;", false));
        body.add(new InsnNode(DUP)); body.add(new JumpInsnNode(IFNULL, unchanged));
        body.add(new VarInsnNode(ALOAD, 0));
        int variable = 1;
        for (Type argument : Type.getArgumentTypes(method.desc)) {
            body.add(new VarInsnNode(argument.getOpcode(ILOAD), variable)); variable += argument.getSize();
        }
        body.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "invokeExact", "(Ljava/lang/Object;" + method.desc.substring(1), false));
        body.add(new InsnNode(Type.getReturnType(method.desc).getOpcode(IRETURN)));
        body.add(unchanged); body.add(new InsnNode(POP));
        return body;
    }

    public static byte[] adaptPayload(Result result, byte[] bytes) {
        ClassNode node = read(bytes);
        node.version = Math.max(V1_8, node.version);
        for (MethodNode method : node.methods) rewrite(result, node.name, method, true, node);
        return write(node, result.hierarchy);
    }

    private static void rewrite(Result result, String origin, MethodNode method, boolean companion, ClassNode context) {
        boolean moved = companion && context != null && !context.name.equals(origin);
        lowerAllocations(result, origin, method);
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (instruction instanceof FieldInsnNode) {
                FieldInsnNode field = (FieldInsnNode) instruction;
                if (!field.owner.startsWith("net/minecraft/")) continue;
                if (field.owner.equals(origin) && ((!companion && declaresField(result.originals.get(origin), field.name, field.desc)) ||
                    (context != null && context.name.equals(origin) && declaresField(context, field.name, field.desc))))
                    continue; // Physical fields may be initialized before super() initializes this.
                boolean get = field.getOpcode() == GETFIELD || field.getOpcode() == GETSTATIC;
                boolean instance = field.getOpcode() == GETFIELD || field.getOpcode() == PUTFIELD;
                String call = "(" + (instance ? "L" + field.owner + ";" : "") + (get ? "" : field.desc) + ")" + (get ? field.desc : "V");
                linked(result, origin, method, field, field.name, call, field.owner, field.name, field.desc, field.getOpcode());
            } else if (instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (call.owner.equals("java/lang/Object") && call.name.equals("getClass") && call.desc.equals("()Ljava/lang/Class;"))
                    method.instructions.insertBefore(call, new MethodInsnNode(INVOKESTATIC, RUNTIME, "unwrap", "(Ljava/lang/Object;)Ljava/lang/Object;", false));
                if (call.name.equals("<init>") || (!call.owner.startsWith("net/minecraft/") && !(moved && call.getOpcode() == INVOKESPECIAL))) continue;
                String descriptor = call.getOpcode() == INVOKESTATIC ? call.desc : "(L" + call.owner + ";" + call.desc.substring(1);
                linked(result, origin, method, call, call.name, descriptor, call.owner, call.name, call.desc, call.getOpcode());
            } else if (instruction instanceof TypeInsnNode) {
                TypeInsnNode cast = (TypeInsnNode) instruction;
                if ((cast.desc.startsWith("net/minecraft/") || (cast.desc.startsWith("[") && cast.desc.contains("net/minecraft/"))) &&
                    (cast.getOpcode() == CHECKCAST || cast.getOpcode() == INSTANCEOF)) {
                    if (cast.getOpcode() == CHECKCAST) {
                        String descriptor = cast.desc.startsWith("[") ? cast.desc : "L" + cast.desc + ";";
                        Type target = Type.getType(descriptor);
                        if (!visible(result, origin, target)) method.instructions.set(cast, castBridge(result, target));
                        else method.instructions.set(cast, new InvokeDynamicInsnNode("cast", "(Ljava/lang/Object;)" + descriptor, CAST, cast.desc));
                    } else {
                        method.instructions.insertBefore(cast, new LdcInsnNode(cast.desc));
                        method.instructions.set(cast, new MethodInsnNode(INVOKESTATIC, RUNTIME, "isInstance", "(Ljava/lang/Object;Ljava/lang/String;)Z", false));
                    }
                    continue;
                }
                if (!result.interfaces.contains(cast.desc) || (cast.getOpcode() != CHECKCAST && cast.getOpcode() != INSTANCEOF)) continue;
                InsnList bridge = new InsnList();
                bridge.add(new LdcInsnNode(cast.desc));
                bridge.add(new MethodInsnNode(INVOKESTATIC, RUNTIME, cast.getOpcode() == CHECKCAST ? "cast" : "isInstance",
                    cast.getOpcode() == CHECKCAST ? "(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;" : "(Ljava/lang/Object;Ljava/lang/String;)Z", false));
                if (cast.getOpcode() == CHECKCAST) bridge.add(new TypeInsnNode(CHECKCAST, cast.desc));
                method.instructions.insertBefore(cast, bridge); method.instructions.remove(cast);
            } else if (instruction instanceof JumpInsnNode && (instruction.getOpcode() == IF_ACMPEQ || instruction.getOpcode() == IF_ACMPNE)) {
                JumpInsnNode jump = (JumpInsnNode) instruction;
                method.instructions.insertBefore(jump, new MethodInsnNode(INVOKESTATIC, RUNTIME, "same", "(Ljava/lang/Object;Ljava/lang/Object;)Z", false));
                method.instructions.set(jump, new JumpInsnNode(jump.getOpcode() == IF_ACMPEQ ? IFNE : IFEQ, jump.label));
            } else if (instruction instanceof InvokeDynamicInsnNode) {
                InvokeDynamicInsnNode dynamic = (InvokeDynamicInsnNode) instruction;
                for (int index = 0; index < dynamic.bsmArgs.length; index++) {
                    if (!(dynamic.bsmArgs[index] instanceof Handle)) continue;
                    Handle handle = (Handle) dynamic.bsmArgs[index];
                    Map<String, Object> plan = result.plans.get(handle.getOwner());
                    String[] metadata = plan == null ? null : members(plan, "methods").get(handle.getName() + handle.getDesc());
                    if (metadata != null && ("added".equals(metadata[2]) || (companion && (handle.getTag() == H_INVOKESPECIAL ||
                        (Integer.parseInt(metadata[3]) & ACC_PRIVATE) != 0))))
                        dynamic.bsmArgs[index] = new Handle(H_INVOKESTATIC, (String) plan.get("helper"), metadata[0], metadata[1], false);
                }
            }
        }
    }

    private static boolean declaresField(ClassNode owner, String name, String descriptor) {
        if (owner != null) for (FieldNode field : owner.fields)
            if (field.name.equals(name) && field.desc.equals(descriptor)) return true;
        return false;
    }

    private static void lowerAllocations(Result result, String origin, MethodNode method) {
        Deque<TypeInsnNode> allocations = new ArrayDeque<>();
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (instruction.getOpcode() == NEW) allocations.push((TypeInsnNode) instruction);
            if (!(instruction instanceof MethodInsnNode)) continue;
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (call.getOpcode() != INVOKESPECIAL || !call.name.equals("<init>") || allocations.isEmpty() ||
                !allocations.peek().desc.equals(call.owner)) continue;
            TypeInsnNode allocation = allocations.pop();
            if (!call.owner.startsWith("net/minecraft/")) continue;
            AbstractInsnNode duplicate = allocation.getNext();
            while (duplicate != null && duplicate.getOpcode() < 0) duplicate = duplicate.getNext();
            if (duplicate == null || duplicate.getOpcode() != DUP)
                throw new IllegalStateException("Unsupported constructor allocation in " + origin + "." + method.name + method.desc);
            method.instructions.remove(allocation);
            method.instructions.remove(duplicate);
            String signature = call.desc.substring(0, call.desc.length() - 1) + "L" + call.owner + ";";
            linked(result, origin, method, call, "newInstance", signature, call.owner, "<init>", call.desc, NEW);
        }
    }

    private static boolean visible(Result result, String origin, Type type) {
        Type element = type.getSort() == Type.ARRAY ? type.getElementType() : type;
        if (element.getSort() != Type.OBJECT) return true;
        ClassNode node = result.originals.get(element.getInternalName());
        return node == null || (node.access & ACC_PUBLIC) != 0 ||
            element.getInternalName().substring(0, element.getInternalName().lastIndexOf('/') + 1)
                .equals(origin.substring(0, origin.lastIndexOf('/') + 1));
    }

    private static void linked(Result result, String origin, MethodNode method, AbstractInsnNode previous,
                               String callName, String signature, String owner, String name, String descriptor, int opcode) {
        Type[] arguments = Type.getArgumentTypes(signature);
        for (int index = 0; index < arguments.length; index++)
            if (!visible(result, origin, arguments[index])) arguments[index] = Type.getType(Object.class);
        Type returns = Type.getReturnType(signature);
        Type exposedReturn = visible(result, origin, returns) ? returns : Type.getType(Object.class);
        InvokeDynamicInsnNode linked = new InvokeDynamicInsnNode(callName, Type.getMethodDescriptor(exposedReturn, arguments), LINK,
            owner, name, descriptor, opcode, origin);
        method.instructions.set(previous, linked);
        if (!returns.equals(exposedReturn)) method.instructions.insert(linked, castBridge(result, returns));
    }

    private static MethodInsnNode castBridge(Result result, Type target) {
        Type element = target.getSort() == Type.ARRAY ? target.getElementType() : target;
        String owner = element.getInternalName() + "$FdpAccess" + (target.getSort() == Type.ARRAY ? "$" + target.getDimensions() : "");
        String name = "cast";
        ClassNode access = result.helpers.containsKey(owner) ? read(result.helpers.get(owner)) : new ClassNode();
        if (access.name == null) {
            access.version = V1_8; access.access = ACC_PUBLIC | ACC_SUPER | ACC_SYNTHETIC;
            access.name = owner; access.superName = "java/lang/Object";
        }
        String descriptor = "(Ljava/lang/Object;)" + target.getDescriptor();
        if (!methodKeys(access).contains(name + descriptor)) {
            MethodNode cast = new MethodNode(ACC_PUBLIC | ACC_STATIC, name, descriptor, null, null);
            cast.instructions.add(new VarInsnNode(ALOAD, 0));
            cast.instructions.add(new MethodInsnNode(INVOKESTATIC, RUNTIME, "unwrap", "(Ljava/lang/Object;)Ljava/lang/Object;", false));
            cast.instructions.add(new TypeInsnNode(CHECKCAST, target.getInternalName()));
            cast.instructions.add(new InsnNode(ARETURN));
            access.methods.add(cast);
            result.helpers.put(owner, write(access, result.hierarchy));
        }
        return new MethodInsnNode(INVOKESTATIC, owner, name, descriptor, false);
    }

    public static void requireSameSchema(ClassNode original, ClassNode changed) {
        if (!original.name.equals(changed.name) || !Objects.equals(original.superName, changed.superName) ||
            original.access != changed.access || !original.interfaces.equals(changed.interfaces) ||
            !Objects.equals(original.nestHostClass, changed.nestHostClass) || !Objects.equals(original.nestMembers, changed.nestMembers))
            throw new IllegalStateException("Late attachment changed the class hierarchy: " + original.name);
        Map<String, Integer> fields = new HashMap<>(), updatedFields = new HashMap<>(), methods = new HashMap<>(), updatedMethods = new HashMap<>();
        for (FieldNode field : original.fields) fields.put(field.name + field.desc, field.access);
        for (FieldNode field : changed.fields) updatedFields.put(field.name + field.desc, field.access);
        for (MethodNode method : original.methods) methods.put(method.name + method.desc, method.access);
        for (MethodNode method : changed.methods) updatedMethods.put(method.name + method.desc, method.access);
        if (!fields.equals(updatedFields) || !methods.equals(updatedMethods)) throw new IllegalStateException("Late attachment changed the member schema: " + original.name);
    }
}
