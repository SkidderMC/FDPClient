/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.access;

/**
 * The interface Mixin gui slot.
 */
public interface IMixinGuiSlot {

    /**
     * Sets list width.
     *
     * @param listWidth the list width
     */
    void fDPClient$setListWidth(int listWidth);

    /**
     * Sets enable scissor.
     *
     * @param b the b
     */
    void fDPClient$setEnableScissor(boolean b);

}