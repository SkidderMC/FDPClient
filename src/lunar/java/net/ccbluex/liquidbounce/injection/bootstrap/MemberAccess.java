/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft.
 * https://github.com/SkidderMC/FDPClient/
 * Lunar compatibility by opZywl
 * Reference: https://lucas-lima.xyz
 */
package net.ccbluex.liquidbounce.injection.bootstrap;

import java.lang.invoke.*;
import java.lang.reflect.*;

/** Linked once per call site; avoids per-frame reflective lookups and boxing. */
public final class MemberAccess {
    private MemberAccess() { }

    public static CallSite bootstrap(MethodHandles.Lookup caller, String ignored, MethodType type,
                                     String owner, String name, int opcode) throws Exception {
        Class<?> target = Class.forName(owner.replace('/', '.'), false, caller.lookupClass().getClassLoader());
        MethodHandle handle;
        if (opcode >= 178 && opcode <= 181) {
            Field field = field(target, name);
            field.setAccessible(true);
            handle = opcode == 178 || opcode == 180 ? caller.unreflectGetter(field) : caller.unreflectSetter(field);
        } else {
            MethodType memberType = opcode == 184 ? type : type.dropParameterTypes(0, 1);
            Method method = method(target, name, memberType.toMethodDescriptorString());
            method.setAccessible(true);
            handle = opcode == 183 ? caller.unreflectSpecial(method, caller.lookupClass()) : caller.unreflect(method);
        }
        return new ConstantCallSite(handle.asType(type));
    }

    static Field field(Class<?> type, String name) throws NoSuchFieldException {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try { return current.getDeclaredField(name); } catch (NoSuchFieldException next) { }
            for (Class<?> itf : current.getInterfaces()) {
                try { return field(itf, name); } catch (NoSuchFieldException next) { }
            }
        }
        throw new NoSuchFieldException(type.getName() + "." + name);
    }

    static Method method(Class<?> type, String name, String descriptor) throws NoSuchMethodException {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(name) && MethodType.methodType(method.getReturnType(), method.getParameterTypes())
                    .toMethodDescriptorString().equals(descriptor)) return method;
            }
        }
        // Class implementations take precedence over inherited abstract interface declarations.
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Class<?> itf : current.getInterfaces()) {
                try { return method(itf, name, descriptor); } catch (NoSuchMethodException next) { }
            }
        }
        throw new NoSuchMethodException(type.getName() + "." + name + descriptor);
    }
}
