/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.features.module.modules.client.Rotations;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.ccbluex.liquidbounce.utils.RotationUtils;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ModelBiped.class)
public abstract class MixinModelBiped {

    /**
     * The Biped right arm.
     */
    @Shadow
    public ModelRenderer bipedRightArm;

    /**
     * The Held item right.
     */
    @Shadow
    public int heldItemRight;

    /**
     * The Biped head.
     */
    @Shadow
    public ModelRenderer bipedHead;

    @Inject(method = "setRotationAngles", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelBiped;swingProgress:F"))
    private void revertSwordAnimation(float p_setRotationAngles1, float p_setRotationAngles2, float p_setRotationAngles3, float p_setRotationAngles4, float p_setRotationAngles5, float p_setRotationAngles6, Entity p_setRotationAngles7, CallbackInfo callbackInfo) {
        final Rotations rotations = Objects.requireNonNull(FDPClient.moduleManager.getModule(Rotations.class));
        if (p_setRotationAngles7 instanceof EntityPlayer && p_setRotationAngles7.equals(MinecraftInstance.mc.thePlayer) && rotations.getRotationMode().get().equals("Normal") && rotations.getState() && (RotationUtils.targetRotation != null && rotations.getRotatingCheckValue().get() || !rotations.getRotatingCheckValue().get())) {
            bipedHead.rotateAngleX = (float) Math.toRadians(Rotations.lerp(MinecraftInstance.mc.timer.renderPartialTicks, Rotations.getPrevHeadPitch(), Rotations.getHeadPitch()));
        }
        if (heldItemRight == 3) {
            this.bipedRightArm.rotateAngleZ = 0F;
            this.bipedRightArm.rotateAngleY = -0.5235988F;
            return;
        }
        if (heldItemRight == 0 || heldItemRight == 2) {
        }
    }
}