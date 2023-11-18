/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.entity;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.event.StrafeEvent;
import net.ccbluex.liquidbounce.features.module.modules.client.Performance;
import net.ccbluex.liquidbounce.features.module.modules.combat.HitBox;
import net.ccbluex.liquidbounce.features.module.modules.movement.NoFluid;
import net.ccbluex.liquidbounce.features.module.modules.movement.StrafeFix;
import net.ccbluex.liquidbounce.features.module.modules.exploit.ViaVersionFix;
import net.ccbluex.liquidbounce.injection.access.IWorld;
import net.ccbluex.liquidbounce.utils.EntityUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;
import java.util.UUID;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Shadow
    public double posX;

    @Shadow
    public double posY;

    @Shadow
    public double posZ;

    @Shadow
    public abstract boolean isSprinting();

    @Shadow
    public float rotationPitch;

    @Shadow
    public float rotationYaw;

    @Shadow
    public abstract AxisAlignedBB getEntityBoundingBox();

    @Shadow
    public Entity ridingEntity;

    @Shadow
    public double motionX;

    @Shadow
    public double motionY;

    @Shadow
    public double motionZ;

    @Shadow
    public boolean onGround;

    @Shadow
    public boolean isAirBorne;

    @Shadow
    public boolean noClip;

    @Shadow
    public World worldObj;

    @Shadow
    public void moveEntity(double x, double y, double z) {
    }

    @Shadow
    public boolean isInWeb;

    @Shadow
    public float stepHeight;

    @Shadow
    public boolean isCollidedHorizontally;

    @Shadow
    public boolean isCollidedVertically;

    @Shadow
    public boolean isCollided;

    @Shadow
    public float distanceWalkedModified;

    @Shadow
    public float distanceWalkedOnStepModified;

    @Shadow
    public abstract boolean isInWater();

    @Shadow
    protected Random rand;

    @Shadow
    public int fireResistance;

    @Shadow
    protected boolean inPortal;

    @Shadow
    public int timeUntilPortal;

    @Shadow
    public float width;
    @Shadow
    public abstract void setSprinting(boolean sprinting);
    @Shadow
    public abstract boolean isRiding();

    @Shadow
    public abstract void setFire(int seconds);

    @Shadow
    protected abstract void dealFireDamage(int amount);

    @Shadow
    public abstract boolean isWet();

    @Shadow
    public abstract void addEntityCrashInfo(CrashReportCategory category);

    @Shadow
    protected abstract void doBlockCollisions();

    @Shadow
    protected abstract void playStepSound(BlockPos pos, Block blockIn);

    @Shadow
    public abstract void setEntityBoundingBox(AxisAlignedBB bb);

    @Shadow
    private int nextStepDistance;

    @Shadow
    private int fire;

    @Shadow
    public abstract Vec3 getVectorForRotation(float pitch, float yaw);

    @Shadow
    public abstract UUID getUniqueID();

    @Shadow
    public abstract boolean isSneaking();

    @Shadow
    public abstract boolean equals(Object p_equals_1_);

    @Shadow public abstract float getEyeHeight();

    public int getNextStepDistance() {
        return nextStepDistance;
    }

    public void setNextStepDistance(int nextStepDistance) {
        this.nextStepDistance = nextStepDistance;
    }

    public int getFire() {
        return fire;
    }

    @Inject(method = "moveFlying", at = @At("HEAD"), cancellable = true)
    private void handleRotations(float strafe, float forward, float friction, final CallbackInfo callbackInfo) {
        if ((Object) this != Minecraft.getMinecraft().thePlayer)
            return;

        final StrafeEvent strafeEvent = new StrafeEvent(strafe, forward, friction);
        final StrafeFix strafeFix = FDPClient.moduleManager.getModule(StrafeFix.class);
        //alert("Strafe: " + strafe + " Forward: " + forward + " Factor: " + friction + " DoFix: " + strafeFix.getDoFix());
        FDPClient.eventManager.callEvent(strafeEvent);
        if (strafeFix.getDoFix()) { //Run StrafeFix process on Post Strafe 2023/02/15
            strafeFix.runStrafeFixLoop(strafeFix.getSilentFix(), strafeEvent);
        }

        if (strafeEvent.isCancelled())
            callbackInfo.cancel();
    }

    @Inject(method = "isInWater", at = @At("HEAD"), cancellable = true)
    private void isInWater(final CallbackInfoReturnable<Boolean> cir) {
        if (NoFluid.INSTANCE.getState() && NoFluid.INSTANCE.getWaterValue().get()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isInLava", at = @At("HEAD"), cancellable = true)
    private void isInLava(final CallbackInfoReturnable<Boolean> cir) {
        if (NoFluid.INSTANCE.getState() && NoFluid.INSTANCE.getLavaValue().get()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getCollisionBorderSize", at = @At("HEAD"), cancellable = true)
    private void getCollisionBorderSize(final CallbackInfoReturnable<Float> callbackInfoReturnable) {
        final HitBox hitBox = FDPClient.moduleManager.getModule(HitBox.class);
        final ViaVersionFix viaVersionFix = FDPClient.moduleManager.getModule(ViaVersionFix.class);

        if (hitBox.getState() && EntityUtils.INSTANCE.isSelected(((Entity)((Object)this)),true)) {
            if (viaVersionFix.getState()) {
                callbackInfoReturnable.setReturnValue(hitBox.getSizeValue().get());
            } else {
                callbackInfoReturnable.setReturnValue(0.1F + hitBox.getSizeValue().get());
            }
        } else if (viaVersionFix.getState()) {
            callbackInfoReturnable.setReturnValue(0.0F);
        }
    }

    @Inject(method="getBrightnessForRender", at=@At(value="HEAD"), cancellable=true)
    private void getBrightnessForRender(float f, CallbackInfoReturnable<Integer> callbackInfoReturnable) {
        if (Performance.fastEntityLightningValue.get()) {
            int n, n2, n3 = MathHelper.floor_double(this.posX);
            IWorld world = (IWorld)this.worldObj;
            callbackInfoReturnable.setReturnValue(world.isBlockLoaded(n3, n2 = MathHelper.floor_double(this.posY + (double)this.getEyeHeight()), n = MathHelper.floor_double(this.posZ)) ? world.getCombinedLight(n3, n2, n, 0) : 0);
        }
    }

    @Inject(method="getBrightness", at=@At(value="HEAD"), cancellable=true)
    public void getBrightness(float f, CallbackInfoReturnable<Float> callbackInfoReturnable) {
        if (Performance.fastEntityLightningValue.get()) {
            int n, n2, n3 = MathHelper.floor_double(this.posX);
            IWorld world = (IWorld)this.worldObj;
            callbackInfoReturnable.setReturnValue(world.isBlockLoaded(n3, n2 = MathHelper.floor_double(this.posY + (double) this.getEyeHeight()), n = MathHelper.floor_double(this.posZ)) ? world.getLightBrightness(n3, n2, n) : 0.0f);
        }
    }
}