/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft.
 * https://github.com/SkidderMC/FDPClient/
 * Lunar compatibility by opZywl
 * Reference: https://lucas-lima.xyz
 */
package net.ccbluex.liquidbounce.injection.lunar.engine;

import org.objectweb.asm.tree.*;
import java.util.*;
import java.util.concurrent.*;
import org.spongepowered.asm.service.MixinService;

/** Member visibility is read from bytecode, never by loading a class during its own definition. */
final class MemberMetadata {
    private static final Map<String, Info> TYPES = new ConcurrentHashMap<>();

    private static final class Info {
        int access;
        final Map<String, Integer> fields = new HashMap<>();
        final Map<String, Integer> methods = new HashMap<>();
        final List<String> parents = new ArrayList<>();
    }

    static int[] flags(String owner, String name, String descriptor, boolean method) throws Exception {
        Info type = type(owner);
        Integer access = member(owner, name + descriptor, method, new HashSet<>());
        if (access == null) throw new NoSuchFieldException(owner + "." + name + descriptor);
        return new int[]{type.access, access};
    }

    private static Integer member(String owner, String key, boolean method, Set<String> seen) throws Exception {
        if (!seen.add(owner)) return null;
        Info type = type(owner);
        Integer found = (method ? type.methods : type.fields).get(key);
        if (found != null) return found;
        for (String parent : type.parents) {
            Integer inherited = member(parent, key, method, seen);
            if (inherited != null) return inherited;
        }
        return null;
    }

    private static Info type(String owner) throws Exception {
        Info cached = TYPES.get(owner);
        if (cached != null) return cached;
        ClassNode node = MixinService.getService().getBytecodeProvider().getClassNode(owner);
        Info parsed = new Info();
        parsed.access = node.access;
        for (FieldNode field : node.fields) parsed.fields.put(field.name + field.desc, field.access);
        for (MethodNode method : node.methods) parsed.methods.put(method.name + method.desc, method.access);
        if (node.superName != null) parsed.parents.add(node.superName);
        parsed.parents.addAll(node.interfaces);
        TYPES.put(owner, parsed);
        return parsed;
    }

    static String commonSuperClass(String first, String second) {
        if (first.equals(second)) return first;
        try {
            if (first.startsWith("[") && second.startsWith("[")) {
                String a = first.substring(1), b = second.substring(1);
                if ((a.startsWith("L") || a.startsWith("[")) && (b.startsWith("L") || b.startsWith("["))) {
                    String aa = a.startsWith("L") ? a.substring(1, a.length() - 1) : a;
                    String bb = b.startsWith("L") ? b.substring(1, b.length() - 1) : b;
                    String common = commonSuperClass(aa, bb);
                    return common.startsWith("[") ? "[" + common : "[L" + common + ";";
                }
                return "java/lang/Object";
            }
            if (first.startsWith("[") || second.startsWith("[")) {
                String other = first.startsWith("[") ? second : first;
                return other.equals("java/lang/Cloneable") || other.equals("java/io/Serializable") ? other : "java/lang/Object";
            }
            if (assignable(first, second, new HashSet<>())) return first;
            if (assignable(second, first, new HashSet<>())) return second;
            String parent = first;
            while (!parent.equals("java/lang/Object")) {
                Info metadata = type(parent);
                if ((metadata.access & org.objectweb.asm.Opcodes.ACC_INTERFACE) != 0 || metadata.parents.isEmpty()) return "java/lang/Object";
                parent = metadata.parents.get(0);
                if (assignable(parent, second, new HashSet<>())) return parent;
            }
            return parent;
        } catch (Exception failure) {
            throw new IllegalStateException("Cannot compute Lunar stack frames for " + first + " and " + second, failure);
        }
    }

    private static boolean assignable(String target, String source, Set<String> seen) throws Exception {
        if (target.equals(source) || target.equals("java/lang/Object")) return true;
        if (!seen.add(source)) return false;
        for (String parent : type(source).parents) if (assignable(target, parent, seen)) return true;
        return false;
    }
}
