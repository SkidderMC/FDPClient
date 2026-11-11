/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft.
 * https://github.com/SkidderMC/FDPClient/
 * Lunar compatibility by opZywl
 * Reference: https://lucas-lima.xyz
 */
package net.ccbluex.liquidbounce.injection.lunar.engine;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.*;

/** Applies the same tracked access rules Forge uses, before the Lunar classes are defined. */
final class AccessRules {
    private static final Map<String, Boolean> CLASSES = new HashMap<>();
    private static final Map<String, Map<String, Boolean>> MEMBERS = new HashMap<>();

    static {
        try (JarFile jar = new JarFile(System.getProperty("fdpclient.lunar.runtimeJar"))) {
            JarEntry entry = jar.getJarEntry("META-INF/fdpclient_at.cfg");
            if (entry == null) throw new IOException("Missing FDP access rules");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(jar.getInputStream(entry), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String rule = line.split("#", 2)[0].trim();
                    if (rule.isEmpty()) continue;
                    String[] parts = rule.split("\\s+");
                    if (parts.length < 2 || !parts[0].startsWith("public")) throw new IOException("Unsupported FDP access rule: " + rule);
                    String owner = parts[1].replace('.', '/');
                    boolean finalRemoved = parts[0].endsWith("-f");
                    if (parts.length == 2) CLASSES.put(owner, finalRemoved);
                    else MEMBERS.computeIfAbsent(owner, key -> new HashMap<>()).put(parts[2], finalRemoved);
                }
            }
        } catch (IOException failure) { throw new ExceptionInInitializerError(failure); }
    }

    private AccessRules() { }

    static byte[] transform(byte[] input) {
        ClassReader reader = new ClassReader(input);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);
        if (!apply(node)) return input;
        ClassWriter writer = new ClassWriter(reader, 0);
        node.accept(writer);
        return writer.toByteArray();
    }

    static boolean apply(ClassNode node) {
        boolean changed = false;
        if (CLASSES.containsKey(node.name)) { node.access = expose(node.access, CLASSES.get(node.name)); changed = true; }
        Map<String, Boolean> members = MEMBERS.get(node.name);
        if (members != null) {
            for (FieldNode field : node.fields) {
                if (members.containsKey(field.name)) { field.access = expose(field.access, members.get(field.name)); changed = true; }
            }
            for (MethodNode method : node.methods) {
                String signature = method.name + method.desc;
                if (members.containsKey(signature)) { method.access = expose(method.access, members.get(signature)); changed = true; }
            }
        }
        for (InnerClassNode inner : node.innerClasses) {
            if (CLASSES.containsKey(inner.name)) { inner.access = expose(inner.access, CLASSES.get(inner.name)); changed = true; }
        }
        if (node.name.equals("net/minecraft/creativetab/CreativeTabs")) {
            for (FieldNode field : node.fields) {
                if (field.desc.equals("[L" + node.name + ";")) { field.access = expose(field.access, true); changed = true; }
            }
        }
        return changed;
    }

    private static int expose(int flags, boolean finalRemoved) {
        return (flags & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED | (finalRemoved ? Opcodes.ACC_FINAL : 0))) | Opcodes.ACC_PUBLIC;
    }
}
