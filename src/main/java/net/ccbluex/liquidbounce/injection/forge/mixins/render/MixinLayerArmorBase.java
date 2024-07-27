/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.ccbluex.liquidbounce.features.module.modules.visual.CustomModel;
import net.ccbluex.liquidbounce.features.module.modules.visual.FreeCam;
import net.ccbluex.liquidbounce.features.module.modules.visual.Glint;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin({LayerArmorBase.class})
public class MixinLayerArmorBase {

    @Inject(method = {"doRenderLayer"}, at = {@At("HEAD")}, cancellable = true)
    public void doRenderLayer(final EntityLivingBase entitylivingbaseIn, final float limbSwing, final float limbSwingAmount, final float partialTicks, final float ageInTicks, final float netHeadYaw, final float headPitch, final float scale, final CallbackInfo ci) {
        final CustomModel customModel = CustomModel.INSTANCE;
        final FreeCam freecam = FreeCam.INSTANCE;

        if (customModel.getState() || freecam.getState())
            ci.cancel();
    }

    @ModifyArgs(method="renderGlint", slice=@Slice(from=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/GlStateManager;disableLighting()V", ordinal=0)), at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V", ordinal=0), require=1, allow=1)
    private void renderGlint(Args args) {
        final Glint glint = Glint.INSTANCE;

        if (glint.getState()) {
            int n = glint.getColor().getRGB();
            args.set(0, (Object) ((float) (n >> 16 & 0xFF) / 255.0f));
            args.set(1, (Object) ((float) (n >> 8 & 0xFF) / 255.0f));
            args.set(2, (Object) ((float) (n & 0xFF) / 255.0f));
            args.set(3, (Object) ((float) (n >> 24 & 0xFF) / 255.0f));
        }
    }
}