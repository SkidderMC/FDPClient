/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft.
 * https://github.com/SkidderMC/FDPClient/
 * Lunar compatibility by opZywl
 * Reference: https://lucas-lima.xyz
 */
package net.ccbluex.liquidbounce.injection.lunar.engine;

import org.spongepowered.asm.service.*;
import java.util.concurrent.*;

/** Keeps this engine's environment separate from Genesis and Forge. */
public final class RuntimeProperties implements IGlobalPropertyService {
    private static final ConcurrentMap<String, Key> KEYS = new ConcurrentHashMap<>();
    private static final ConcurrentMap<IPropertyKey, Object> VALUES = new ConcurrentHashMap<>();
    private static final class Key implements IPropertyKey { }

    public IPropertyKey resolveKey(String name) { return KEYS.computeIfAbsent(name, key -> new Key()); }
    @SuppressWarnings("unchecked")
    public <T> T getProperty(IPropertyKey key) { return (T) VALUES.get(key); }
    public void setProperty(IPropertyKey key, Object value) { if (value == null) VALUES.remove(key); else VALUES.put(key, value); }
    public <T> T getProperty(IPropertyKey key, T fallback) { T value = getProperty(key); return value == null ? fallback : value; }
    public String getPropertyString(IPropertyKey key, String fallback) { Object value = VALUES.get(key); return value == null ? fallback : value.toString(); }
}
