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
}
