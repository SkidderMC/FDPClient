/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.entity;

import net.ccbluex.liquidbounce.features.module.modules.movement.EntityControl;
import net.minecraft.entity.passive.EntityHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityHorse.class)
public abstract class MixinEntityHorse {

    @Inject(method = "isHorseSaddled", at = @At("HEAD"), cancellable = true)
    private void fdp$allowUnsaddledControl(CallbackInfoReturnable<Boolean> callback) {
        if (EntityControl.getEnforceSaddled()) {
            callback.setReturnValue(true);
        }
    }

    @Inject(method = "getHorseJumpStrength", at = @At("HEAD"), cancellable = true)
    private void fdp$overrideJumpStrength(CallbackInfoReturnable<Double> callback) {
        if (EntityControl.getEnforceJumpStrength()) {
            callback.setReturnValue(EntityControl.getControlledJumpStrength());
        }
    }
}
