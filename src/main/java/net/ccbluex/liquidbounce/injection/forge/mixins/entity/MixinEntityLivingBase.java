/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.entity;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.event.JumpEvent;
import net.ccbluex.liquidbounce.features.module.modules.client.Animations;
import net.ccbluex.liquidbounce.features.module.modules.movement.Jesus;
import net.ccbluex.liquidbounce.features.module.modules.player.DelayRemover;
import net.ccbluex.liquidbounce.features.module.modules.visual.VanillaTweaks;
import net.ccbluex.liquidbounce.handler.protocol.ProtocolBase;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends MixinEntity {

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

    @Shadow
    public float moveStrafing;

    @Shadow
    public float moveForward;

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

    @Shadow
    protected void updateEntityActionState() {
    }

    /**
     * Update distance float.
     *
     * @return the float
     * @author opZywl
     * @reason Rotation
     */
    @Overwrite
    protected void jump() {
        final JumpEvent jumpEvent = new JumpEvent(this.getJumpUpwardsMotion(), this.rotationYaw);
        FDPClient.eventManager.callEvent(jumpEvent);
        if (jumpEvent.isCancelled())
            return;

        this.motionY = jumpEvent.getMotion();

        if (this.isPotionActive(Potion.jump))
            this.motionY += (float) (this.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;

        if (this.isSprinting()) {
            float f = jumpEvent.getYaw() * 0.017453292F;

            this.motionX -= (MathHelper.sin(f) * 0.2F);
            this.motionZ += (MathHelper.cos(f) * 0.2F);
        }

        this.isAirBorne = true;
    }

    @Inject(method = "onLivingUpdate", at = @At("HEAD"))
    private void headLiving(CallbackInfo callbackInfo) {
        if (FDPClient.moduleManager.getModule(DelayRemover.class).getState() && FDPClient.moduleManager.getModule(DelayRemover.class).getJumpDelay().get())
            jumpTicks = FDPClient.moduleManager.getModule(DelayRemover.class).getJumpDelayTicks().get();
    }

    @Inject(method = "onLivingUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;isJumping:Z", ordinal = 1))
    private void onJumpSection(CallbackInfo callbackInfo) {
        final Jesus jesus = FDPClient.moduleManager.getModule(Jesus.class);

        if (jesus.getState() && !isJumping && !isSneaking() && isInWater() &&
                jesus.getModeValue().equals("Legit")) {
            this.updateAITick();
        }
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

    @ModifyConstant(method = "onLivingUpdate", constant = @Constant(doubleValue = 0.005D))
    private double ViaVersion_MovementThreshold(double constant) {
        if (ProtocolBase.getManager().getTargetVersion().newerThan(ProtocolVersion.v1_8) && !MinecraftInstance.mc.isIntegratedServerRunning())
            return 0.003D;
        return 0.005D;
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