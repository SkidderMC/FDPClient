/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft.
 * https://github.com/SkidderMC/FDPClient/
 * Lunar compatibility by opZywl
 * Reference: https://lucas-lima.xyz
 */
package net.ccbluex.liquidbounce.injection.bootstrap;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import java.util.Iterator;

/** Replaces MCEF's Forge lifecycle wiring with FDP's existing frame and shutdown events. */
public final class LunarMcefTransformer {
    private LunarMcefTransformer() { }

    public static byte[] transform(byte[] source) {
        ClassNode node = new ClassNode();
        // MCEF's bundled JCEF renderer also carries MCP 1.12 rendering names.
        // Adapt those references before looking up visibility in the 1.8.9 host.
        new ClassReader(source).accept(new ClassRemapper(node, new Remapper() {
            @Override public String map(String name) {
                return name.equals("net/minecraft/client/renderer/BufferBuilder")
                    ? "net/minecraft/client/renderer/WorldRenderer" : name;
            }
            @Override public String mapMethodName(String owner, String name, String descriptor) {
                return owner.equals("net/minecraft/client/renderer/Tessellator") && name.equals("getBuffer")
                    ? "getWorldRenderer" : name;
            }
        }), ClassReader.EXPAND_FRAMES);
        for (Iterator<MethodNode> methods = node.methods.iterator(); methods.hasNext();) {
            MethodNode method = methods.next();
            if (node.name.equals("net/montoyo/mcef/client/ClientProxy") && method.name.equals("onTick")) {
                method.name = "fdp$renderTick";
                method.desc = "()V";
                method.signature = null;
                method.visibleAnnotations = null;
                method.invisibleAnnotations = null;
                method.localVariables = null;
                method.parameters = null;
                method.visibleParameterAnnotations = null;
                method.invisibleParameterAnnotations = null;
                for (AbstractInsnNode instruction : method.instructions) {
                    if (instruction instanceof FrameNode) {
                        FrameNode frame = (FrameNode) instruction;
                        if (frame.local != null && frame.local.size() > 1) frame.local.set(1, Opcodes.TOP);
                    }
                }
                // The original handler only runs for RenderTickEvent.Phase.START.
                AbstractInsnNode first = method.instructions.getFirst();
                while (first.getOpcode() < 0) first = first.getNext();
                AbstractInsnNode phase = first.getNext();
                AbstractInsnNode start = phase.getNext();
                method.instructions.set(first, new InsnNode(Opcodes.ACONST_NULL));
                method.instructions.remove(phase);
                method.instructions.set(start, new InsnNode(Opcodes.ACONST_NULL));
            } else if (method.desc.contains("net/minecraftforge/")) {
                methods.remove();
                continue;
            }
            if (node.name.equals("net/montoyo/mcef/api/MCEFApi") && method.name.equals("isMCEFLoaded")) {
                method.instructions.clear();
                method.tryCatchBlocks.clear();
                method.localVariables = null;
                method.instructions.add(new InsnNode(Opcodes.ICONST_1));
                method.instructions.add(new InsnNode(Opcodes.IRETURN));
            }
            if (node.name.equals("org/cef/handler/CefAppHandlerAdapter") && method.name.equals("onScheduleMessagePumpWork")) {
                // FDP owns CEF's manual loop. JCEF's Swing timer would run a second
                // native message loop on the AWT thread and race the manual render loop.
                method.instructions.clear();
                method.tryCatchBlocks.clear();
                method.localVariables = null;
                method.instructions.add(new InsnNode(Opcodes.RETURN));
            }
            for (AbstractInsnNode instruction = method.instructions.getFirst(); instruction != null;) {
                AbstractInsnNode next = instruction.getNext();
                if (instruction instanceof LdcInsnNode) {
                    LdcInsnNode constant = (LdcInsnNode) instruction;
                    if (Type.getObjectType("net/minecraftforge/fml/client/SplashProgress").equals(constant.cst)) {
                        constant.cst = Type.getObjectType("net/ccbluex/liquidbounce/injection/lunar/McefCompat");
                    } else if (node.name.equals("net/montoyo/mcef/client/ClientProxy") &&
                        Type.getType(ClassLoader.class).equals(constant.cst)) {
                        constant.cst = Type.getObjectType("net/ccbluex/liquidbounce/injection/lunar/McefCompat");
                    } else if (node.name.equals("net/montoyo/mcef/client/ClientProxy") && "usr_paths".equals(constant.cst)) {
                        // Java 17 uses absolute System.load calls below; the removed JDK 8 field is unnecessary.
                        constant.cst = "nativePaths";
                    }
                } else if (instruction instanceof FieldInsnNode) {
                    FieldInsnNode field = (FieldInsnNode) instruction;
                    if (field.owner.equals("net/minecraftforge/common/MinecraftForge") && field.name.equals("EVENT_BUS")) {
                        method.instructions.set(instruction, new InsnNode(Opcodes.ACONST_NULL));
                    }
                } else if (instruction instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if (call.owner.equals("net/minecraftforge/fml/common/eventhandler/EventBus") && call.name.equals("register")) {
                        method.instructions.set(instruction, new InsnNode(Opcodes.POP2));
                    } else if (call.owner.equals("net/montoyo/mcef/coremod/ShutdownPatcher") && call.name.equals("didPatchSucceed")) {
                        method.instructions.set(instruction, new InsnNode(Opcodes.ICONST_1));
                    }
                }
                instruction = next;
            }
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
