/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.entity;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.event.JumpEvent;
import net.ccbluex.liquidbounce.features.module.modules.client.Animations;
import net.ccbluex.liquidbounce.features.module.modules.client.Rotations;
import net.ccbluex.liquidbounce.features.module.modules.movement.*;
import net.ccbluex.liquidbounce.features.module.modules.visual.VanillaTweaks;
import net.ccbluex.liquidbounce.handler.protocol.ProtocolBase;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.ccbluex.liquidbounce.utils.MovementUtils;
import net.ccbluex.liquidbounce.utils.RotationUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.raphimc.vialoader.util.VersionEnum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Objects;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends MixinEntity {

    /**
     * The Swing progress int.
     */
    @Shadow
    public int swingProgressInt;
    /**
     * The Is swing in progress.
     */
    @Shadow
    public boolean isSwingInProgress;
    /**
     * The Swing progress.
     */
    @Shadow
    public float swingProgress;
    /**
     * The Is jumping.
     */
    @Shadow
    protected boolean isJumping;
    /**
     * The Jump ticks.
     */
    @Shadow
    public int jumpTicks;

    /**
     * Gets jump upwards motion.
     *
     * @return the jump upwards motion
     */
    @Shadow
    protected abstract float getJumpUpwardsMotion();

    /**
     * Gets active potion effect.
     *
     * @param potionIn the potion in
     * @return the active potion effect
     */
    @Shadow
    public abstract PotionEffect getActivePotionEffect(Potion potionIn);

    /**
     * Is potion active boolean.
     *
     * @param potionIn the potion in
     * @return the boolean
     */
    @Shadow
    public abstract boolean isPotionActive(Potion potionIn);

    /**
     * On living update.
     */
    @Shadow
    public void onLivingUpdate() {
    }

    /**
     * Update fall state.
     *
     * @param y          the y
     * @param onGroundIn the on ground in
     * @param blockIn    the block in
     * @param pos        the pos
     */
    @Shadow
    protected abstract void updateFallState(double y, boolean onGroundIn, Block blockIn, BlockPos pos);

    /**
     * Gets health.
     *
     * @return the health
     */
    @Shadow
    public abstract float getHealth();

    /**
     * Gets held item.
     *
     * @return the held item
     */
    @Shadow
    public abstract ItemStack getHeldItem();

    /**
     * Update ai tick.
     */
    @Shadow
    protected abstract void updateAITick();

    /**
     * The Render yaw offset.
     */
    @Shadow
    public float renderYawOffset;

    /**
     * The Rotation yaw head.
     */
    @Shadow
    public float rotationYawHead;

    @Shadow
    public float prevRotationYawHead;

    @Shadow
    public float prevRenderYawOffset;

    @Inject(method = "updatePotionEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/potion/PotionEffect;onUpdate(Lnet/minecraft/entity/EntityLivingBase;)Z"),
            locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void checkPotionEffect(CallbackInfo ci, Iterator<Integer> iterator, Integer integer, PotionEffect potioneffect) {
        if (potioneffect == null)
            ci.cancel();
    }

    /**
     * Update distance float.
     *
     * @param p_1101461 the p 1101461
     * @param p_1101462 the p 1101462
     * @return the float
     * @author opZywl
     * @reason Rotation
     */
    @Overwrite
    protected float updateDistance(float p_1101461, float p_1101462) {
        float rotationYaw = this.rotationYaw;
        final Rotations rotations = Objects.requireNonNull(FDPClient.moduleManager.getModule(Rotations.class));
        if (rotations.getRotationMode().get().equals("Normal") && rotations.getState() && rotations.getPlayerYaw() != null && (EntityLivingBase) (Object) this instanceof EntityPlayerSP) {
            if (this.swingProgress > 0F && !rotations.getBodyValue().get()) {
                p_1101461 = rotations.getPlayerYaw();
            }
            rotationYaw = rotations.getPlayerYaw();
            rotationYawHead = rotations.getPlayerYaw();
        }
        float f = MathHelper.wrapAngleTo180_float(p_1101461 - this.renderYawOffset);
        if (rotations.getRotationMode().get().equals("Normal") && rotations.getBodyValue().get() && rotations.getState() && rotations.getPlayerYaw() != null && (EntityLivingBase) (Object) this instanceof EntityPlayerSP)
            this.renderYawOffset += f;
        else this.renderYawOffset += f * 0.3F;
        float f1 = MathHelper.wrapAngleTo180_float(rotationYaw - this.renderYawOffset);
        boolean flag = f1 < 90.0F || f1 >= 90.0F;

        if (rotations.getRotationMode().get().equals("Normal") && rotations.getBodyValue().get() && rotations.getState() && rotations.getPlayerYaw() != null && (EntityLivingBase) (Object) this instanceof EntityPlayerSP) {
            f1 = 0.0F;
        }

        if (f1 < -75.0F) {
            f1 = -75.0F;
        }

        if (f1 >= 75.0F) {
            f1 = 75.0F;
        }

        this.renderYawOffset = rotationYaw - f1;

        if (f1 * f1 > 2500.0F) {
            this.renderYawOffset += f1 * 0.2F;
        }

        if (flag) {
            p_1101462 *= -1.0F;
        }

        return p_1101462;
    }

    /**
     * @author CCBlueX
     * @author CoDynamic
     * Modified by Co Dynamic
     * Date: 2023/02/15
     */
    @Overwrite
    protected void jump() {
        if (!this.equals(Minecraft.getMinecraft().thePlayer)) {
            return;
        }
        
        /**
         * Jump Process Fix
         * use updateFixState to reset Jump Fix state
         * @param fixedYaw  The yaw player should have (NOT RotationYaw)
         * @param strafeFix StrafeFix Module
         */

        final JumpEvent jumpEvent = new JumpEvent(MovementUtils.INSTANCE.getJumpMotion());
        FDPClient.eventManager.callEvent(jumpEvent);
        if (jumpEvent.isCancelled())
            return;

        this.motionY = jumpEvent.getMotion();
        final Sprint sprint = FDPClient.moduleManager.getModule(Sprint.class);
        final StrafeFix strafeFix = FDPClient.moduleManager.getModule(StrafeFix.class);

        if (this.isSprinting()) {
            float fixedYaw = this.rotationYaw;
            if(RotationUtils.targetRotation != null && strafeFix.getDoFix()) {
                fixedYaw = RotationUtils.targetRotation.getYaw();
            }
            if(sprint.getState() && sprint.getJumpDirectionsValue().get()) {
                fixedYaw += MovementUtils.INSTANCE.getMovingYaw() - this.rotationYaw;
            }
            this.motionX -= MathHelper.sin(fixedYaw / 180F * 3.1415927F) * 0.2F;
            this.motionZ += MathHelper.cos(fixedYaw / 180F * 3.1415927F) * 0.2F;
        }

        this.isAirBorne = true;
    }


    @Inject(method = "onLivingUpdate", at = @At("HEAD"))
    private void headLiving(CallbackInfo callbackInfo) {
        if (FDPClient.moduleManager.getModule(NoJumpDelay.class).getState())
            jumpTicks = FDPClient.moduleManager.getModule(NoJumpDelay.class).getTicks().get();
    }

    @Inject(method = "onLivingUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;isJumping:Z", ordinal = 1))
    private void onJumpSection(CallbackInfo callbackInfo) {
        final Jesus jesus = FDPClient.moduleManager.getModule(Jesus.class);

        if (jesus.getState() && !isJumping && !isSneaking() && isInWater() &&
                jesus.getModeValue().equals("Legit")) {
            this.updateAITick();
        }
    }

    @ModifyConstant(method = "onLivingUpdate", constant = @Constant(doubleValue = 0.005D))
    private double ViaVersion_MovementThreshold(double constant) {
        if (ProtocolBase.getManager().getTargetVersion().getProtocol() != VersionEnum.r1_8.getProtocol() && !MinecraftInstance.mc.isIntegratedServerRunning())
            return 0.003D;
        return 0.005D;
    }

    @Inject(method = "getLook", at = @At("HEAD"), cancellable = true)
    private void getLook(CallbackInfoReturnable<Vec3> callbackInfoReturnable) {
        if (((EntityLivingBase) (Object) this) instanceof EntityPlayerSP)
            callbackInfoReturnable.setReturnValue(getVectorForRotation(this.rotationPitch, this.rotationYaw));
    }

    @Inject(method = "isPotionActive(Lnet/minecraft/potion/Potion;)Z", at = @At("HEAD"), cancellable = true)
    private void isPotionActive(Potion p_isPotionActive_1_, final CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        final VanillaTweaks camera = FDPClient.moduleManager.getModule(VanillaTweaks.class);

        if ((p_isPotionActive_1_ == Potion.confusion || p_isPotionActive_1_ == Potion.blindness) && camera.getState() && camera.getConfusionEffectValue().get())
            callbackInfoReturnable.setReturnValue(false);
    }

    /**
     * @author Liuli
     */
    @Overwrite
    private int getArmSwingAnimationEnd() {
        int speed = this.isPotionActive(Potion.digSpeed) ? 6 - (1 + this.getActivePotionEffect(Potion.digSpeed).getAmplifier()) : (this.isPotionActive(Potion.digSlowdown) ? 6 + (1 + this.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) * 2 : 6);

        if (this.equals(Minecraft.getMinecraft().thePlayer)) {
            speed = (int) (speed * Animations.INSTANCE.getSwingSpeedValue().get());
        }

        return speed;
    }
}
