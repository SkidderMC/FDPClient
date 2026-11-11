/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft.
 * https://github.com/SkidderMC/FDPClient/
 * Lunar compatibility by opZywl
 * Reference: https://lucas-lima.xyz
 */
package net.minecraft.lunarcheck;

/** Deliberately private/final members model the access restrictions of an unmodified host. */
public class Host {
    private int value = 4;
    private final long fixed = Long.parseLong("19");
    private int multiply(int factor) { return value * factor; }
    public int value() { return value; }
    public long fixed() { return fixed; }
}
