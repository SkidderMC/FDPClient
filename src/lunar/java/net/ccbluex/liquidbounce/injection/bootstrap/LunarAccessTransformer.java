/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft.
 * https://github.com/SkidderMC/FDPClient/
 * Lunar compatibility by opZywl
 * Reference: https://lucas-lima.xyz
 */
package net.ccbluex.liquidbounce.injection.bootstrap;

import org.objectweb.asm.*;
import java.lang.reflect.*;

/** Replaces accesses widened by Forge's access transformers with local method-handle call sites. */
public final class LunarAccessTransformer {
    public interface Lookup { int[] flags(String owner, String name, String descriptor, boolean method); }
    private static final Handle BOOTSTRAP = new Handle(Opcodes.H_INVOKESTATIC,
        "net/ccbluex/liquidbounce/injection/bootstrap/MemberAccess", "bootstrap",
        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;", false);

    private LunarAccessTransformer() { }

    public static byte[] transform(byte[] source, final ClassLoader game) {
        return transform(source, game, null);
    }

    public static byte[] transform(byte[] source, final ClassLoader game, final Lookup lookup) {
        ClassReader reader = new ClassReader(source);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, desc, signature, exceptions)) {
                    @Override
                    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                        if (compatibilityField(this.mv, opcode, owner, name, desc)) return;
                        if (owner.startsWith("net/minecraft/") && bridgeField(game, owner, name, desc, opcode, lookup)) {
                            boolean get = opcode == Opcodes.GETFIELD || opcode == Opcodes.GETSTATIC;
                            boolean instance = opcode == Opcodes.GETFIELD || opcode == Opcodes.PUTFIELD;
                            String call = "(" + (instance ? "L" + owner + ";" : "") + (get ? "" : desc) + ")" + (get ? desc : "V");
                            super.visitInvokeDynamicInsn(name, call, BOOTSTRAP, owner, name, opcode);
                        } else {
                            super.visitFieldInsn(opcode, owner, name, desc);
                        }
                    }

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                        if (compatibilityMethod(this.mv, opcode, owner, name, desc)) return;
                        if (owner.startsWith("net/minecraft/") && !name.startsWith("<") && bridgeMethod(game, owner, name, desc, lookup)) {
                            String call = opcode == Opcodes.INVOKESTATIC ? desc : "(L" + owner + ";" + desc.substring(1);
                            super.visitInvokeDynamicInsn(name, call, BOOTSTRAP, owner, name, opcode);
                        } else {
                            super.visitMethodInsn(opcode, owner, name, desc, itf);
                        }
                    }
                };
            }
        }, 0);
        return writer.toByteArray();
    }

    public static byte[] compatibility(byte[] source) {
        ClassReader reader = new ClassReader(source);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, desc, signature, exceptions)) {
                    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                        if (!compatibilityField(this.mv, opcode, owner, name, desc)) super.visitFieldInsn(opcode, owner, name, desc);
                    }
                    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                        if (!compatibilityMethod(this.mv, opcode, owner, name, desc)) super.visitMethodInsn(opcode, owner, name, desc, itf);
                    }
                };
            }
        }, 0);
        return writer.toByteArray();
    }

    private static final String COMPAT = "net/ccbluex/liquidbounce/injection/lunar/ForgeCompat";

    private static boolean compatibilityField(MethodVisitor out, int opcode, String owner, String name, String desc) {
        if (opcode == Opcodes.GETFIELD && owner.equals("net/minecraft/client/entity/EntityPlayerSP") && name.equals("eyeHeight") && desc.equals("F")) {
            out.visitMethodInsn(Opcodes.INVOKESTATIC, COMPAT, "playerEyeHeight", "(Lnet/minecraft/entity/Entity;)F", false);
            return true;
        }
        if (opcode == Opcodes.GETSTATIC && owner.equals("net/minecraft/world/World") && name.equals("MAX_ENTITY_RADIUS") && desc.equals("D")) {
            out.visitLdcInsn(2.0D);
            return true;
        }
        return false;
    }

    private static boolean compatibilityMethod(MethodVisitor out, int opcode, String owner, String name, String desc) {
        if (name.equals("<init>") && owner.equals("net/minecraft/network/handshake/client/C00Handshake") &&
            desc.equals("(ILjava/lang/String;ILnet/minecraft/network/EnumConnectionState;Z)V")) {
            out.visitInsn(Opcodes.POP);
            out.visitMethodInsn(opcode, owner, name, "(ILjava/lang/String;ILnet/minecraft/network/EnumConnectionState;)V", false);
            return true;
        }
        if (name.equals("<init>") && owner.equals("net/minecraft/creativetab/CreativeTabs") && desc.equals("(Ljava/lang/String;)V")) {
            out.visitMethodInsn(Opcodes.INVOKESTATIC, COMPAT, "nextCreativeTabId", "()I", false);
            out.visitInsn(Opcodes.SWAP);
            out.visitMethodInsn(opcode, owner, name, "(ILjava/lang/String;)V", false);
            return true;
        }
        String receiver = null;
        String helper = null;
        if (owner.startsWith("net/minecraft/tileentity/") && name.equals("getRenderBoundingBox") && desc.equals("()Lnet/minecraft/util/AxisAlignedBB;")) {
            receiver = "net/minecraft/tileentity/TileEntity"; helper = "tileBoundingBox";
        } else if ((owner.startsWith("net/minecraft/entity/") || owner.startsWith("net/minecraft/client/entity/")) && name.equals("canRiderInteract") && desc.equals("()Z")) {
            receiver = "net/minecraft/entity/Entity"; helper = "canRiderInteract";
        } else if (owner.startsWith("net/minecraft/block/") && name.equals("getLightOpacity") && desc.equals("(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/BlockPos;)I")) {
            receiver = "net/minecraft/block/Block"; helper = "blockLightOpacity";
        } else if (owner.startsWith("net/minecraft/block/") && name.equals("isLadder") && desc.equals("(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/BlockPos;Lnet/minecraft/entity/EntityLivingBase;)Z")) {
            receiver = "net/minecraft/block/Block"; helper = "isLadder";
        } else if (owner.startsWith("net/minecraft/item/") && name.equals("doesSneakBypassUse") && desc.equals("(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/entity/player/EntityPlayer;)Z")) {
            receiver = "net/minecraft/item/Item"; helper = "doesSneakBypassUse";
        } else if (owner.startsWith("net/minecraft/item/") && name.equals("isRepairable") && desc.equals("()Z")) {
            receiver = "net/minecraft/item/Item"; helper = "isRepairable";
        } else if (owner.startsWith("net/minecraft/item/") && name.equals("onEntitySwing") && desc.equals("(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;)Z")) {
            receiver = "net/minecraft/item/Item"; helper = "onEntitySwing";
        }
        if (helper == null) return false;
        out.visitMethodInsn(Opcodes.INVOKESTATIC, COMPAT, helper, "(L" + receiver + ";" + desc.substring(1), false);
        return true;
    }

    private static Class<?> host(ClassLoader game, String owner) throws ClassNotFoundException {
        return Class.forName(owner.replace('/', '.'), false, game);
    }

    private static boolean bridgeField(ClassLoader game, String owner, String name, String descriptor, int opcode, Lookup lookup) {
        if (lookup != null) {
            int[] flags = lookup.flags(owner, name, descriptor, false);
            return !Modifier.isPublic(flags[0]) || !Modifier.isPublic(flags[1]) ||
                ((opcode == Opcodes.PUTFIELD || opcode == Opcodes.PUTSTATIC) && Modifier.isFinal(flags[1]));
        }
        try {
            Class<?> type = host(game, owner);
            Field field = MemberAccess.field(type, name);
            return !Modifier.isPublic(type.getModifiers()) || !Modifier.isPublic(field.getModifiers()) ||
                ((opcode == Opcodes.PUTFIELD || opcode == Opcodes.PUTSTATIC) && Modifier.isFinal(field.getModifiers()));
        } catch (ReflectiveOperationException missing) {
            throw new IllegalStateException("Lunar has no field " + owner + "." + name, missing);
        }
    }

    private static boolean bridgeMethod(ClassLoader game, String owner, String name, String descriptor, Lookup lookup) {
        if (lookup != null) {
            int[] flags = lookup.flags(owner, name, descriptor, true);
            return !Modifier.isPublic(flags[0]) || !Modifier.isPublic(flags[1]);
        }
        try {
            Class<?> type = host(game, owner);
            Method method = MemberAccess.method(type, name, descriptor);
            return !Modifier.isPublic(type.getModifiers()) || !Modifier.isPublic(method.getModifiers());
        } catch (ReflectiveOperationException missing) {
            throw new IllegalStateException("Lunar has no method " + owner + "." + name + descriptor, missing);
        }
    }
}
