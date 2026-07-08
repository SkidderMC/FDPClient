/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.ccbluex.liquidbounce.features.module.modules.visual.XRay;
import net.minecraft.client.renderer.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteOrder;
import java.nio.IntBuffer;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {

    @Shadow
    private IntBuffer rawIntBuffer;

    @Shadow
    public abstract int getColorIndex(int vertexIndex);

    @Shadow
    public abstract void putColorRGBA(int vertexIndex, int red, int green, int blue, int alpha);

    @Inject(method = "putColorMultiplier", at = @At("RETURN"))
    private void applyXRayBackgroundAlpha(float red, float green, float blue, int vertexIndex, CallbackInfo ci) {
        final int alpha = XRay.INSTANCE.currentBackgroundAlpha();
        if (alpha >= 255) {
            return;
        }

        final int color = rawIntBuffer.get(getColorIndex(vertexIndex));
        final boolean littleEndian = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;
        final int currentRed = littleEndian ? color & 255 : color >>> 24 & 255;
        final int currentGreen = littleEndian ? color >>> 8 & 255 : color >>> 16 & 255;
        final int currentBlue = littleEndian ? color >>> 16 & 255 : color >>> 8 & 255;
        putColorRGBA(vertexIndex, currentRed, currentGreen, currentBlue, alpha);
    }
}
