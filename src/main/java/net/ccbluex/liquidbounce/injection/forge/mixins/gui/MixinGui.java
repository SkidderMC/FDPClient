/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Gui.class)
public abstract class MixinGui {

    /**
     * The Z level.
     */
    @Shadow
    protected float zLevel;

    /**
     * Draw textured modal rect.
     *
     * @param xCoord the x coord
     * @param yCoord the y coord
     * @param minU   the min u
     * @param minV   the min v
     * @param maxU   the max u
     * @param maxV   the max v
     */
    @Shadow
    public abstract void drawTexturedModalRect(float xCoord, float yCoord, int minU, int minV, int maxU, int maxV);

}