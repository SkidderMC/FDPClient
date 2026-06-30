/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.ccbluex.liquidbounce.features.module.modules.visual.CrystalView;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.RenderEnderCrystal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderEnderCrystal.class)
public abstract class MixinRenderEnderCrystal {

    @Inject(
        method = "doRender(Lnet/minecraft/entity/item/EntityEnderCrystal;DDDFF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;translate(FFF)V", shift = At.Shift.AFTER, ordinal = 0)
    )
    private void fdp$transformCrystal(CallbackInfo callback) {
        if (!CrystalView.INSTANCE.handleEvents()) {
            return;
        }

        final float size = CrystalView.INSTANCE.getSize();
        GlStateManager.scale(size, size, size);
        GlStateManager.translate(0F, CrystalView.INSTANCE.getYTranslate(), 0F);
    }

    @ModifyArg(
        method = "doRender(Lnet/minecraft/entity/item/EntityEnderCrystal;DDDFF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;rotate(FFFF)V", ordinal = 0),
        index = 0
    )
    private float fdp$changeSpin(float vanillaAngle) {
        return CrystalView.INSTANCE.handleEvents()
            ? vanillaAngle * CrystalView.INSTANCE.getSpinSpeed() / 3F
            : vanillaAngle;
    }

    @ModifyArg(
        method = "doRender(Lnet/minecraft/entity/item/EntityEnderCrystal;DDDFF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelBase;render(Lnet/minecraft/entity/Entity;FFFFFF)V", ordinal = 0),
        index = 3
    )
    private float fdp$changeBounce(float vanillaBounce) {
        return CrystalView.INSTANCE.handleEvents()
            ? vanillaBounce * CrystalView.INSTANCE.getBounce()
            : vanillaBounce;
    }
}
