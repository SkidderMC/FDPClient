/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.tweaks;

import net.minecraft.client.particle.EntityFX;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityFX.class)
public class MixinEntityFX {
    // Cache the brightness value
    @Unique
    private static final int BRIGHTNESS_VALUE = 0xF000F0;

    @Redirect(method={"renderParticle"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/particle/EntityFX;getBrightnessForRender(F)I"))
    private int renderParticle(EntityFX entityFX, float f) {
        return BRIGHTNESS_VALUE;
    }
}
