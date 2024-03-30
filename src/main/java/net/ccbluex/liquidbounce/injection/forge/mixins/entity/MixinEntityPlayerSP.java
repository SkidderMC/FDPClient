/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.entity;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.event.*;
import net.ccbluex.liquidbounce.features.module.modules.combat.Criticals;
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura;
import net.ccbluex.liquidbounce.features.module.modules.exploit.AntiDesync;
import net.ccbluex.liquidbounce.features.module.modules.movement.*;
import net.ccbluex.liquidbounce.handler.protocol.ProtocolBase;
import net.ccbluex.liquidbounce.utils.MinecraftInstance;
import net.ccbluex.liquidbounce.utils.MovementUtils;
import net.ccbluex.liquidbounce.utils.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends MixinAbstractClientPlayer {

    @Unique
    private boolean viaForge$prevOnGround;

    /**
     * The Server sprint state.
     */
    @Shadow
    public boolean serverSprintState;
    /**
     * The Sprinting ticks left.
     */
    @Shadow
    public int sprintingTicksLeft;
    /**
     * The Time in portal.
     */
    @Shadow
    public float timeInPortal;
    /**
     * The Prev time in portal.
     */
    @Shadow
    public float prevTimeInPortal;
    /**
     * The Movement input.
     */
    @Shadow
    public MovementInput movementInput;
    /**
     * The Horse jump power.
     */
    @Shadow
    public float horseJumpPower;
    /**
     * The Horse jump power counter.
     */
    @Shadow
    public int horseJumpPowerCounter;
    /**
     * The Send queue.
     */
    @Shadow
    @Final
    public NetHandlerPlayClient sendQueue;
    /**
     * The Sprint toggle timer.
     */
    @Shadow
    protected int sprintToggleTimer;
    /**
     * The Mc.
     */
    @Shadow
    protected Minecraft mc;
    @Shadow
    private boolean serverSneakState;
    @Shadow
    private double lastReportedPosX;
    @Shadow
    public int positionUpdateTicks;
    @Shadow
    private double lastReportedPosY;
    @Shadow
    private double lastReportedPosZ;
    @Shadow
    private float lastReportedYaw;
    @Shadow
    private float lastReportedPitch;
    @Shadow
    public float renderArmYaw;
    @Shadow
    public float renderArmPitch;
    @Shadow
    public float prevRenderArmYaw;
    @Shadow
    public float prevRenderArmPitch;

    /**
     * Play sound.
     *
     * @param name   the name
     * @param volume the volume
     * @param pitch  the pitch
     */
    @Shadow
    public abstract void playSound(String name, float volume, float pitch);

    /**
     * Sets sprinting.
     *
     * @param sprinting the sprinting
     */
    @Shadow
    public abstract void setSprinting(boolean sprinting);

    /**
     * Push out of blocks boolean.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     * @return the boolean
     */
    @Shadow
    protected abstract boolean pushOutOfBlocks(double x, double y, double z);

    /**
     * Send player abilities.
     */
    @Shadow
    public abstract void sendPlayerAbilities();

    /**
     * Send horse jump.
     */
    @Shadow
    protected abstract void sendHorseJump();

    /**
     * Is riding horse boolean.
     *
     * @return the boolean
     */
    @Shadow
    public abstract boolean isRidingHorse();

    @Shadow
    public abstract boolean isSneaking();

    /**
     * Is current view entity boolean.
     *
     * @return the boolean
     * @author opZywl
     * @reason Fix Video
     */
    @Overwrite
    protected boolean isCurrentViewEntity() {
        final Flight flight = Objects.requireNonNull(FDPClient.moduleManager.getModule(Flight.class));

        return (mc.getRenderViewEntity() != null && mc.getRenderViewEntity().equals(this)) || (FDPClient.moduleManager != null && flight.getState());
    }

    @Redirect(method = "onUpdateWalkingPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/NetHandlerPlayClient;addToSendQueue(Lnet/minecraft/network/Packet;)V", ordinal = 7))
    public void emulateIdlePacket(NetHandlerPlayClient instance, Packet p_addToSendQueue_1_) {
        if (ProtocolBase.getManager().getTargetVersion().newerThan(ProtocolVersion.v1_8) && !MinecraftInstance.mc.isIntegratedServerRunning()) {
            if (this.viaForge$prevOnGround == this.onGround) {
                return;
            }
        }
        instance.addToSendQueue(p_addToSendQueue_1_);
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("RETURN"))
    public void saveGroundState(CallbackInfo ci) {
        this.viaForge$prevOnGround = this.onGround;
    }

    /**
     * @author As_pw
     * @reason Fix Arm
     */
    @Overwrite
    public void updateEntityActionState() {
        super.updateEntityActionState();

        if (this.isCurrentViewEntity()) {
            this.moveStrafing = this.movementInput.moveStrafe;
            this.moveForward = this.movementInput.moveForward;
            this.isJumping = this.movementInput.jump;
            this.prevRenderArmYaw = this.renderArmYaw;
            this.prevRenderArmPitch = this.renderArmPitch;
            this.renderArmPitch = (float) ((double) this.renderArmPitch + (double) (this.rotationPitch - this.renderArmPitch) * 0.5D);
            this.renderArmYaw = (float) ((double) this.renderArmYaw + (double) (this.rotationYaw - this.renderArmYaw) * 0.5D);
        }
    }

    /**
     * @author CCBlueX, liulihaocai
     *
     * use inject to make sure this works with ViaForge mod
     */
    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"), cancellable = true)
    public void onUpdateWalkingPlayer(CallbackInfo ci) {
        try {
            final StrafeFix strafeFix = FDPClient.moduleManager.getModule(StrafeFix.class);
            Objects.requireNonNull(strafeFix).updateOverwrite();
            
            FDPClient.eventManager.callEvent(new MotionEvent(EventState.PRE));

            boolean flag = this.isSprinting();
            //alert("Attempt: " + debug_AttemptSprint + " Actual: " + this.isSprinting() + " Server: " + this.serverSprintState);
            if (flag != this.serverSprintState) {
                if (flag) {
                    this.sendQueue.addToSendQueue(new C0BPacketEntityAction((EntityPlayerSP) (Object) this, C0BPacketEntityAction.Action.START_SPRINTING));
                } else {
                    this.sendQueue.addToSendQueue(new C0BPacketEntityAction((EntityPlayerSP) (Object) this, C0BPacketEntityAction.Action.STOP_SPRINTING));
                }

                this.serverSprintState = flag;
            }

            boolean flag1 = this.isSneaking();
            if (flag1 != this.serverSneakState) {
                if (flag1) {
                    this.sendQueue.addToSendQueue(new C0BPacketEntityAction((EntityPlayerSP) (Object) this, C0BPacketEntityAction.Action.START_SNEAKING));
                } else {
                    this.sendQueue.addToSendQueue(new C0BPacketEntityAction((EntityPlayerSP) (Object) this, C0BPacketEntityAction.Action.STOP_SNEAKING));
                }

                this.serverSneakState = flag1;
            }

            if (this.isCurrentViewEntity()) {
                float yaw = rotationYaw;
                float pitch = rotationPitch;
                float lastReportedYaw = Objects.requireNonNull(RotationUtils.serverRotation).getYaw();
                float lastReportedPitch = RotationUtils.serverRotation.getPitch();

                if (RotationUtils.targetRotation != null) {
                    yaw = RotationUtils.targetRotation.getYaw();
                    pitch = RotationUtils.targetRotation.getPitch();
                }

                double xDiff = this.posX - this.lastReportedPosX;
                double yDiff = this.getEntityBoundingBox().minY - this.lastReportedPosY;
                double zDiff = this.posZ - this.lastReportedPosZ;
                double yawDiff = yaw - lastReportedYaw;
                double pitchDiff = pitch - lastReportedPitch;
                
                final Flight fly = FDPClient.moduleManager.getModule(Flight.class);
                final Criticals criticals = FDPClient.moduleManager.getModule(Criticals.class);
                final AntiDesync antiDesync = FDPClient.moduleManager.getModule(AntiDesync.class);
                boolean moved = xDiff * xDiff + yDiff * yDiff + zDiff * zDiff > 9.0E-4D || this.positionUpdateTicks >= 20 || (Objects.requireNonNull(fly).getState() && fly.getAntiDesync()) || (Objects.requireNonNull(criticals).getState() && criticals.getAntiDesync()) || (Objects.requireNonNull(antiDesync).getState() && xDiff * xDiff + yDiff * yDiff + zDiff * zDiff > 0.0D);
                boolean rotated = yawDiff != 0.0D || pitchDiff != 0.0D;

                if (this.ridingEntity == null) {
                    if (moved && rotated) {
                        this.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(this.posX, this.getEntityBoundingBox().minY, this.posZ, yaw, pitch, this.onGround));
                    } else if (moved) {
                        this.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(this.posX, this.getEntityBoundingBox().minY, this.posZ, this.onGround));
                    } else if (rotated) {
                        this.sendQueue.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(yaw, pitch, this.onGround));
                    } else {
                        this.sendQueue.addToSendQueue(new C03PacketPlayer(this.onGround));
                    }
                } else {
                    this.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(this.motionX, -999.0D, this.motionZ, yaw, pitch, this.onGround));
                    moved = false;
                }

                ++this.positionUpdateTicks;

                if (moved) {
                    this.lastReportedPosX = this.posX;
                    this.lastReportedPosY = this.getEntityBoundingBox().minY;
                    this.lastReportedPosZ = this.posZ;
                    this.positionUpdateTicks = 0;
                }

                if (rotated) {
                    this.lastReportedYaw = this.rotationYaw;
                    this.lastReportedPitch = this.rotationPitch;
                }
            }

            FDPClient.eventManager.callEvent(new MotionEvent(EventState.POST));
        } catch (final Exception e) {
            e.printStackTrace();
        }

        ci.cancel();
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    private void onPushOutOfBlocks(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        PushOutEvent event = new PushOutEvent();
        if (this.noClip) event.cancelEvent();
        FDPClient.eventManager.callEvent(event);

        if (event.isCancelled())
            callbackInfoReturnable.setReturnValue(false);
    }

    /**
     * @author CCBlueX
     * @author CoDynamic
     * Modified by Co Dynamic
     * Date: 2023/02/15
     * @reason Fix Sprint / UpdateEvent
     */
    @Overwrite
    public void onLivingUpdate() {

        boolean lastForwardToggleState = this.movementInput.moveForward > 0.05f;
        boolean lastJumpToggleState = this.movementInput.jump;
        
        this.movementInput.updatePlayerMoveState();
        
        final Sprint sprint = FDPClient.moduleManager.getModule(Sprint.class);
        final KillAura killAura = FDPClient.moduleManager.getModule(KillAura.class);
        final InvMove inventoryMove = FDPClient.moduleManager.getModule(InvMove.class);
        final StrafeFix strafeFix = FDPClient.moduleManager.getModule(StrafeFix.class);
        
        if (this.sprintingTicksLeft > 0) {
            --this.sprintingTicksLeft;

            if (this.sprintingTicksLeft == 0) {
                this.setSprinting(false);
            }
        }

        if (this.sprintToggleTimer > 0) {
            --this.sprintToggleTimer;
        }
        
        boolean isSprintDirection;
        boolean movingStat = Math.abs(this.movementInput.moveForward) > 0.05f || Math.abs(this.movementInput.moveStrafe) > 0.05f;
        
        boolean runStrictStrafe = Objects.requireNonNull(FDPClient.moduleManager.getModule(StrafeFix.class)).getDoFix() && !Objects.requireNonNull(FDPClient.moduleManager.getModule(StrafeFix.class)).getSilentFix();
        boolean noStrafe = RotationUtils.targetRotation == null || !Objects.requireNonNull(FDPClient.moduleManager.getModule(StrafeFix.class)).getDoFix();
        
        if (!movingStat || runStrictStrafe || noStrafe) {
            isSprintDirection = this.movementInput.moveForward > 0.05f;
        }else {
            isSprintDirection = Math.abs(RotationUtils.getAngleDifference(MovementUtils.INSTANCE.getMovingYaw(), RotationUtils.targetRotation.getYaw())) < 67.0f;
        }
        
        if (!movingStat) {
            isSprintDirection = false;
        }
        
        boolean attemptToggle = Objects.requireNonNull(sprint).getState() || this.isSprinting() || this.mc.gameSettings.keyBindSprint.isKeyDown();
        boolean baseIsMoving = (sprint.getState() && sprint.getAllDirectionsValue().get() && (Math.abs(this.movementInput.moveForward) > 0.05f || Math.abs(this.movementInput.moveStrafe) > 0.05f)) || isSprintDirection;
        boolean baseSprintState = ((!sprint.getHungryValue().get() && sprint.getState()) || (float) this.getFoodStats().getFoodLevel() > 6.0F || this.capabilities.allowFlying) && baseIsMoving && (!this.isCollidedHorizontally || sprint.getCollideValue().get()) && (!this.isSneaking() || sprint.getSneakValue().get()) && !this.isPotionActive(Potion.blindness);
        boolean canToggleSprint = this.onGround && !this.movementInput.jump && !this.movementInput.sneak && !this.isPotionActive(Potion.blindness);
        boolean isCurrentUsingItem = getHeldItem() != null && (this.isUsingItem() || (getHeldItem().getItem() instanceof ItemSword && Objects.requireNonNull(killAura).getBlockingStatus())) && this.isRiding();
        boolean isCurrentUsingSword = getHeldItem() != null && getHeldItem().getItem() instanceof ItemSword && (Objects.requireNonNull(killAura).getBlockingStatus() || this.isUsingItem());
        
        baseSprintState = baseSprintState && !(Objects.requireNonNull(inventoryMove).getNoSprintValue().equals("Real") && inventoryMove.getInvOpen());
        
        if (!attemptToggle && !lastForwardToggleState && baseSprintState && !this.isSprinting() && canToggleSprint && !isCurrentUsingItem && !this.isPotionActive(Potion.blindness)) {
            if (this.sprintToggleTimer <= 0 && !this.mc.gameSettings.keyBindSprint.isKeyDown()) {
                this.sprintToggleTimer = 7;
            } else {
                attemptToggle = true;
            }
        }

        this.setSprinting(sprint.getForceSprint() || baseSprintState && (!isCurrentUsingItem || (sprint.getUseItemValue().get() && (!sprint.getUseItemSwordValue().get() || isCurrentUsingSword))) && attemptToggle);
        
        //Run Sprint update before UpdateEvent
        
        FDPClient.eventManager.callEvent(new UpdateEvent());
        
        //Update Portal Effects state (Vanilla)

        this.prevTimeInPortal = this.timeInPortal;

        if (this.inPortal) {

            if (this.timeInPortal == 0.0F) {
                this.mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("portal.trigger"), this.rand.nextFloat() * 0.4F + 0.8F));
            }

            this.timeInPortal += 0.0125F;

            if (this.timeInPortal >= 1.0F) {
                this.timeInPortal = 1.0F;
            }

            this.inPortal = false;
        } else if (this.isPotionActive(Potion.confusion) && this.getActivePotionEffect(Potion.confusion).getDuration() > 60) {
            this.timeInPortal += 0.006666667F;

            if (this.timeInPortal > 1.0F) {
                this.timeInPortal = 1.0F;
            }
        } else {
            if (this.timeInPortal > 0.0F) {
                this.timeInPortal -= 0.05F;
            }

            if (this.timeInPortal < 0.0F) {
                this.timeInPortal = 0.0F;
            }
        }

        if (this.timeUntilPortal > 0) {
            --this.timeUntilPortal;
        }

        this.movementInput.updatePlayerMoveState();

        movingStat = Math.abs(this.movementInput.moveForward) > 0.05f || Math.abs(this.movementInput.moveStrafe) > 0.05f;
        runStrictStrafe = Objects.requireNonNull(strafeFix).getDoFix() && !Objects.requireNonNull(strafeFix).getSilentFix();
        noStrafe = RotationUtils.targetRotation == null || !Objects.requireNonNull(FDPClient.moduleManager.getModule(StrafeFix.class)).getDoFix();
        
        isCurrentUsingItem = getHeldItem() != null && (this.isUsingItem() || (getHeldItem().getItem() instanceof ItemSword && Objects.requireNonNull(killAura).getBlockingStatus())) && this.isRiding();
        isCurrentUsingSword = getHeldItem() != null && getHeldItem().getItem() instanceof ItemSword && ((Objects.requireNonNull(killAura)).getBlockingStatus() || this.isUsingItem());

        if (isCurrentUsingItem) {
            final SlowDownEvent slowDownEvent = new SlowDownEvent(0.2F, 0.2F);
            FDPClient.eventManager.callEvent(slowDownEvent);
            this.movementInput.moveStrafe *= slowDownEvent.getStrafe();
            this.movementInput.moveForward *= slowDownEvent.getForward();
        }

        pushOutOfBlocks(posX - width * 0.35, getEntityBoundingBox().minY + 0.5, posZ + width * 0.35);
        pushOutOfBlocks(posX - width * 0.35, getEntityBoundingBox().minY + 0.5, posZ - width * 0.35);
        pushOutOfBlocks(posX + width * 0.35, getEntityBoundingBox().minY + 0.5, posZ - width * 0.35);
        pushOutOfBlocks(posX + width * 0.35, getEntityBoundingBox().minY + 0.5, posZ + width * 0.35);
        
        if (!movingStat || runStrictStrafe || noStrafe) {
            isSprintDirection = this.movementInput.moveForward > 0.05f;
        }else {
            isSprintDirection = Math.abs(RotationUtils.getAngleDifference(MovementUtils.INSTANCE.getMovingYaw(), RotationUtils.targetRotation.getYaw())) < 67.0f;
        }
        
        baseIsMoving = (sprint.getState() && sprint.getAllDirectionsValue().get() && (Math.abs(this.movementInput.moveForward) > 0.05f || Math.abs(this.movementInput.moveStrafe) > 0.05f)) || isSprintDirection;
        baseSprintState = ((!sprint.getHungryValue().get() && sprint.getState()) || (float) this.getFoodStats().getFoodLevel() > 6.0F || this.capabilities.allowFlying) && baseIsMoving && (!this.isCollidedHorizontally || sprint.getCollideValue().get()) && (!this.isSneaking() || sprint.getSneakValue().get()) && !this.isPotionActive(Potion.blindness);
        
        //Don't check current Sprint state cuz it's not updated in real time :bruh:

        this.setSprinting(sprint.getForceSprint() || baseSprintState && (!isCurrentUsingItem || (sprint.getUseItemValue().get() && (!sprint.getUseItemSwordValue().get() || isCurrentUsingSword))) && attemptToggle);
        
        //Overwrite: Scaffold
        
        if (Objects.requireNonNull(FDPClient.moduleManager.getModule(Scaffold.class)).getState()) {
            this.setSprinting(Objects.requireNonNull(FDPClient.moduleManager.getModule(Scaffold.class)).getCanSprint());
        }

        //aac may check it :(
        if (this.capabilities.allowFlying) {
            if (this.mc.playerController.isSpectatorMode()) {
                if (!this.capabilities.isFlying) {
                    this.capabilities.isFlying = true;
                    this.sendPlayerAbilities();
                }
            } else if (!lastJumpToggleState && this.movementInput.jump) {
                if (this.flyToggleTimer == 0) {
                    this.flyToggleTimer = 7;
                } else {
                    this.capabilities.isFlying = !this.capabilities.isFlying;
                    this.sendPlayerAbilities();
                    this.flyToggleTimer = 0;
                }
            }
        }

        if (this.capabilities.isFlying && this.isCurrentViewEntity()) {
            if (this.movementInput.sneak) {
                this.motionY -= this.capabilities.getFlySpeed() * 3.0F;
            }

            if (this.movementInput.jump) {
                this.motionY += this.capabilities.getFlySpeed() * 3.0F;
            }
        }

        if (this.isRidingHorse()) {
            if (this.horseJumpPowerCounter < 0) {
                ++this.horseJumpPowerCounter;

                if (this.horseJumpPowerCounter == 0) {
                    this.horseJumpPower = 0.0F;
                }
            }

            if (lastJumpToggleState && !this.movementInput.jump) {
                this.horseJumpPowerCounter = -10;
                this.sendHorseJump();
            } else if (!lastJumpToggleState && this.movementInput.jump) {
                this.horseJumpPowerCounter = 0;
                this.horseJumpPower = 0.0F;
            } else if (lastJumpToggleState) {
                ++this.horseJumpPowerCounter;

                if (this.horseJumpPowerCounter < 10) {
                    this.horseJumpPower = (float) this.horseJumpPowerCounter * 0.1F;
                } else {
                    this.horseJumpPower = 0.8F + 2.0F / (float) (this.horseJumpPowerCounter - 9) * 0.1F;
                }
            }
        } else {
            this.horseJumpPower = 0.0F;
        }

        super.onLivingUpdate();

        if (this.onGround && this.capabilities.isFlying && !this.mc.playerController.isSpectatorMode()) {
            this.capabilities.isFlying = false;
            this.sendPlayerAbilities();
        }
    }
}
