package net.ccbluex.liquidbounce.injection.transformers;

import net.ccbluex.liquidbounce.script.remapper.injection.utils.ClassUtils;
import net.ccbluex.liquidbounce.script.remapper.injection.utils.NodeUtils;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.IRETURN;

public class ViaForgeSupportTransformer implements IClassTransformer {

    private byte[] target = null;

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(name.equals("net.ccbluex.liquidbounce.injection.forge.mixins.network.MixinNetworkManager")) {
            try {
                final ClassNode classNode = ClassUtils.INSTANCE.toClassNode(basicClass);

                classNode.methods.stream().filter(methodNode -> methodNode.name.equals("createNetworkManagerAndConnect")).forEach(methodNode -> {
                    for(int i = 0; i < methodNode.instructions.size(); ++i) {
                        final AbstractInsnNode abstractInsnNode = methodNode.instructions.get(i);
                        if(abstractInsnNode instanceof TypeInsnNode) {
                            TypeInsnNode tin = (TypeInsnNode) abstractInsnNode;
                            if(tin.desc.equals("net/ccbluex/liquidbounce/injection/forge/mixins/network/MixinNetworkManager$1")) {
                                ((TypeInsnNode) abstractInsnNode).desc = "net/minecraft/network/NetworkManager$5";
                            }
                        } else if(abstractInsnNode instanceof MethodInsnNode) {
                            MethodInsnNode min = (MethodInsnNode) abstractInsnNode;
                            if(min.owner.equals("net/ccbluex/liquidbounce/injection/forge/mixins/network/MixinNetworkManager$1") && min.name.equals("<init>")) {
                                min.owner = "net/minecraft/network/NetworkManager$5";
                            }
                        }
                    }
                });

                return ClassUtils.INSTANCE.toBytes(classNode);
            }catch(final Throwable throwable) {
                throwable.printStackTrace();
            }
        }

        return basicClass;
    }
}