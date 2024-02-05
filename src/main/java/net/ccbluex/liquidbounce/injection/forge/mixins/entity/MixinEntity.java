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
import net.ccbluex.liquidbounce.injection.access.IWorld;
import net.ccbluex.liquidbounce.handler.protocol.ProtocolBase;
import net.ccbluex.liquidbounce.utils.EntityUtils;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.raphimc.vialoader.util.VersionEnum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.Random;
import java.util.UUID;

@Mixin(Entity.class)
public abstract class MixinEntity implements ICommandSender {

    /**
     * The Pos x.
     */
    @Shadow
    public double posX;

    /**
     * The Pos y.
     */
    @Shadow
    public double posY;

    /**
     * The Pos z.
     */
    @Shadow
    public double posZ;
    /**
     * The Rotation pitch.
     */
    @Shadow
    public float rotationPitch;
    /**
     * The Rotation yaw.
     */
    @Shadow
    public float rotationYaw;
    /**
     * The Riding entity.
     */
    @Shadow
    public Entity ridingEntity;
    /**
     * The Motion x.
     */
    @Shadow
    public double motionX;
    /**
     * The Motion y.
     */
    @Shadow
    public double motionY;
    /**
     * The Motion z.
     */
    @Shadow
    public double motionZ;
    /**
     * The On ground.
     */
    @Shadow
    public boolean onGround;
    /**
     * The Is air borne.
     */
    @Shadow
    public boolean isAirBorne;
    /**
     * The No clip.
     */
    @Shadow
    public boolean noClip;
    /**
     * The World obj.
     */
    @Shadow
    public World worldObj;

    /**
     * The Is in web.
     */
    @Shadow
    public boolean isInWeb;
    /**
     * The Step height.
     */
    @Shadow
    public float stepHeight;
    /**
     * The Is collided horizontally.
     */
    @Shadow
    public boolean isCollidedHorizontally;
    /**
     * The Is collided vertically.
     */
    @Shadow
    public boolean isCollidedVertically;
    /**
     * The Is collided.
     */
    @Shadow
    public boolean isCollided;
    /**
     * The Distance walked modified.
     */
    @Shadow
    public float distanceWalkedModified;
    /**
     * The Distance walked on step modified.
     */
    @Shadow
    public float distanceWalkedOnStepModified;
    /**
     * The Fire resistance.
     */
    @Shadow
    public int fireResistance;
    /**
     * The Time until portal.
     */
    @Shadow
    public int timeUntilPortal;
    /**
     * The Width.
     */
    @Shadow
    public float width;
    /**
     * The Prev rotation pitch.
     */
    @Shadow
    public float prevRotationPitch;
    /**
     * The Prev rotation yaw.
     */
    @Shadow
    public float prevRotationYaw;
    /**
     * The Rand.
     */
    @Shadow
    protected Random rand;
    /**
     * The In portal.
     */
    @Shadow
    protected boolean inPortal;
    @Shadow
    private int nextStepDistance;
    @Shadow
    private int fire;
    @Shadow(remap = false)
    private CapabilityDispatcher capabilities;

    /**
     * Is sprinting boolean.
     *
     * @return the boolean
     */
    @Shadow
    public abstract void setSprinting(boolean sprinting);

    @Shadow
    public abstract boolean isSprinting();

    /**
     * Gets entity bounding box.
     *
     * @return the entity bounding box
     */
    @Shadow
    public abstract AxisAlignedBB getEntityBoundingBox();

    /**
     * Sets entity bounding box.
     *
     * @param bb the bb
     */
    @Shadow
    public abstract void setEntityBoundingBox(AxisAlignedBB bb);

    /**
     * Gets distance to entity.
     *
     * @param entityIn the entity in
     * @return the distance to entity
     */
    @Shadow
    public abstract float getDistanceToEntity(Entity entityIn);

    /**
     * Move entity.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     */
    @Shadow
    public void moveEntity(double x, double y, double z) {
    }

    /**
     * Is in water boolean.
     *
     * @return the boolean
     */
    @Shadow
    public abstract boolean isInWater();

    /**
     * Is riding boolean.
     *
     * @return the boolean
     */
    @Shadow
    public abstract boolean isRiding();

    /**
     * Deal fire damage.
     *
     * @param amount the amount
     */
    @Shadow
    protected abstract void dealFireDamage(int amount);

    /**
     * Is wet boolean.
     *
     * @return the boolean
     */
    @Shadow
    public abstract boolean isWet();

    /**
     * Add entity crash info.
     *
     * @param category the category
     */
    @Shadow
    public abstract void addEntityCrashInfo(CrashReportCategory category);

    /**
     * Do block collisions.
     */
    @Shadow
    protected abstract void doBlockCollisions();

    /**
     * Play step sound.
     *
     * @param pos     the pos
     * @param blockIn the block in
     */
    @Shadow
    protected abstract void playStepSound(BlockPos pos, Block blockIn);

    /**
     * Gets vector for rotation.
     *
     * @param pitch the pitch
     * @param yaw   the yaw
     * @return the vector for rotation
     */
    @Shadow
    protected abstract Vec3 getVectorForRotation(float pitch, float yaw);

    /**
     * Gets unique id.
     *
     * @return the unique id
     */
    @Shadow
    public abstract UUID getUniqueID();

    @Shadow
    public abstract boolean isSneaking();

    @Shadow
    public abstract boolean equals(Object p_equals_1_);

    /**
     * Is inside of material boolean.
     *
     * @param materialIn the material in
     * @return the boolean
     */
    @Shadow
    public abstract boolean isInsideOfMaterial(Material materialIn);

    /**
     * Gets next step distance.
     *
     * @return the next step distance
     */
    public int getNextStepDistance() {
        return nextStepDistance;
    }

    /**
     * Sets next step distance.
     *
     * @param nextStepDistance the next step distance
     */
    public void setNextStepDistance(int nextStepDistance) {
        this.nextStepDistance = nextStepDistance;
    }

    /**
     * Gets fire.
     *
     * @return the fire
     */
    public int getFire() {
        return fire;
    }

    /**
     * Sets fire.
     *
     * @param seconds the seconds
     */
    @Shadow
    public abstract void setFire(int seconds);

    @Shadow
    public abstract float getEyeHeight();

    @Shadow
    public abstract Vec3 getLook(float p_getLook_1_);

    @Shadow
    protected abstract boolean getFlag(int p_getFlag_1_);

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
        final HitBox hitBoxes = Objects.requireNonNull(FDPClient.moduleManager.getModule(HitBox.class));

        if (hitBoxes.getState() && EntityUtils.INSTANCE.isSelected(((Entity) ((Object) this)), true)) {
            if (ProtocolBase.getManager().getTargetVersion().isNewerThan(VersionEnum.r1_8) && !MinecraftInstance.mc.isIntegratedServerRunning()) {
                callbackInfoReturnable.setReturnValue(hitBoxes.getSizeValue().get());
            } else {
                callbackInfoReturnable.setReturnValue(0.1F + hitBoxes.getSizeValue().get());
            }
        } else if (ProtocolBase.getManager().getTargetVersion().isNewerThan(VersionEnum.r1_8) && !MinecraftInstance.mc.isIntegratedServerRunning()) {
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
