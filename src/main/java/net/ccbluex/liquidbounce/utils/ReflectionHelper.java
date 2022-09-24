package net.ccbluex.liquidbounce.utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public final class ReflectionHelper {
    private static final Map<String, MethodHandle> HANDLE_CACHE = new HashMap<>();

    private ReflectionHelper() {

    }

    public static MethodHandle lookupStaticFieldHandle(final Class<?> targetClass, final String fieldName) {
        try {
            final Field f = targetClass.getDeclaredField(fieldName);
            if (!f.isAccessible()) f.setAccessible(true);
            return MethodHandles.lookup().unreflectGetter(f);
        } catch (final NoSuchFieldException | IllegalAccessException ignored) {
            return null;
        }
    }

    public static Object invokeOrNull(final MethodHandle handle) {
        if (handle == null) return null;
        try {
            return handle.invoke(null);
        } catch (final Throwable ignored) {
            return null;
        }
    }

    public static Object getStaticField(final Class<?> targetClass, final String fieldName) {
        final String cacheName = targetClass.getTypeName() + "." + fieldName;

        final MethodHandle handle;
        if (HANDLE_CACHE.containsKey(cacheName)) handle = HANDLE_CACHE.get(cacheName);
        else {
            handle = lookupStaticFieldHandle(targetClass, fieldName);
            HANDLE_CACHE.put(cacheName, handle);
        }

        return invokeOrNull(handle);
    }
}
