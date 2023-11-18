/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.entity;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.event.*;
import net.ccbluex.liquidbounce.features.module.modules.combat.Criticals;
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura;
import net.ccbluex.liquidbounce.features.module.modules.exploit.AntiDesync;
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight;
import net.ccbluex.liquidbounce.features.module.modules.movement.InvMove;
import net.ccbluex.liquidbounce.features.module.modules.movement.NoSlow;
import net.ccbluex.liquidbounce.features.module.modules.movement.Sprint;
import net.ccbluex.liquidbounce.features.module.modules.movement.StrafeFix;
import net.ccbluex.liquidbounce.features.module.modules.world.Scaffold;
import net.ccbluex.liquidbounce.utils.RotationUtils;
import net.ccbluex.liquidbounce.utils.MovementUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends MixinAbstractClientPlayer {

    @Shadow
    private boolean serverSneakState;

    @Shadow
    public boolean serverSprintState;

    @Shadow
    public abstract void playSound(String name, float volume, float pitch);

    @Shadow
    public int sprintingTicksLeft;

    @Shadow
    protected int sprintToggleTimer;

    @Shadow
    public float timeInPortal;

    @Shadow
    public float prevTimeInPortal;

    @Shadow
    protected Minecraft mc;

    @Shadow
    public MovementInput movementInput;

    @Shadow
    public abstract void setSprinting(boolean sprinting);

    @Shadow
    protected abstract boolean pushOutOfBlocks(double x, double y, double z);

    @Shadow
    public abstract void sendPlayerAbilities();

    @Shadow
    public float horseJumpPower;

    @Shadow
    public int horseJumpPowerCounter;

    @Shadow
    protected abstract void sendHorseJump();

    @Shadow
    public abstract boolean isRidingHorse();

    @Shadow
    @Final
    public NetHandlerPlayClient sendQueue;

    @Shadow
    public abstract boolean isSneaking();

    @Shadow
    private double lastReportedPosX;

    @Shadow
    private int positionUpdateTicks;

    @Shadow
    private double lastReportedPosY;

    @Shadow
    private double lastReportedPosZ;

    @Shadow
    private float lastReportedYaw;

    @Shadow
    private float lastReportedPitch;

    @Shadow
    protected abstract boolean isCurrentViewEntity();
    private boolean debug_AttemptSprint = false;

    /**
     * @author CCBlueX, liulihaocai
     *
     * use inject to make sure this works with ViaForge mod
     */
    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"), cancellable = true)
    public void onUpdateWalkingPlayer(CallbackInfo ci) {
        try {
            final StrafeFix strafeFix = FDPClient.moduleManager.getModule(StrafeFix.class);
            strafeFix.updateOverwrite();
            
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
                float lastReportedYaw = RotationUtils.serverRotation.getYaw();
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
                boolean moved = xDiff * xDiff + yDiff * yDiff + zDiff * zDiff > 9.0E-4D || this.positionUpdateTicks >= 20 || (fly.getState() && fly.getAntiDesync()) || (criticals.getState() && criticals.getAntiDesync()) || (antiDesync.getState() && xDiff * xDiff + yDiff * yDiff + zDiff * zDiff > 0.0D);
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
        
        /**
         * Update Sprint State - Pre
         * - Run Sprint update before UpdateEvent
         * - Update base sprint state (Vanilla)
         * @param attemptToggle attempt to toggle sprint
         * @param baseIsMoving is player moving with the "Sprint-able" direction
         * @param baseSprintState whether you can sprint or not (Vanilla)
         * @param canToggleSprint whether you can sprint by double-tapping MoveForward key
         * @param isCurrentUsingItem is player using item
         * @return
         */
         
        boolean lastForwardToggleState = this.movementInput.moveForward > 0.05f;
        boolean lastJumpToggleState = this.movementInput.jump;
        
        this.movementInput.updatePlayerMoveState();
        
        final Sprint sprint = FDPClient.moduleManager.getModule(Sprint.class);
        final NoSlow noSlow = FDPClient.moduleManager.getModule(NoSlow.class);
        final KillAura killAura = FDPClient.moduleManager.getModule(KillAura.class);
        final InvMove inventoryMove = FDPClient.moduleManager.getModule(InvMove.class);
        final Scaffold scaffold = FDPClient.moduleManager.getModule(Scaffold.class);
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
        
        boolean isSprintDirection = false;
        boolean movingStat = Math.abs(this.movementInput.moveForward) > 0.05f || Math.abs(this.movementInput.moveStrafe) > 0.05f;
        
        boolean runStrictStrafe = strafeFix.getDoFix() && !strafeFix.getSilentFix();
        boolean noStrafe = RotationUtils.targetRotation == null || !strafeFix.getDoFix();
        
        if (!movingStat || runStrictStrafe || noStrafe) {
            isSprintDirection = this.movementInput.moveForward > 0.05f;
        }else {
            isSprintDirection = Math.abs(RotationUtils.getAngleDifference(MovementUtils.INSTANCE.getMovingYaw(), RotationUtils.targetRotation.getYaw())) < 67.0f;
        }
        
        if (!movingStat) {
            isSprintDirection = false;
        }
        
        boolean attemptToggle = sprint.getState() || this.isSprinting() || this.mc.gameSettings.keyBindSprint.isKeyDown();
        boolean baseIsMoving = (sprint.getState() && sprint.getAllDirectionsValue().get() && (Math.abs(this.movementInput.moveForward) > 0.05f || Math.abs(this.movementInput.moveStrafe) > 0.05f)) || isSprintDirection;
        boolean baseSprintState = ((!sprint.getHungryValue().get() && sprint.getState()) || (float) this.getFoodStats().getFoodLevel() > 6.0F || this.capabilities.allowFlying) && baseIsMoving && (!this.isCollidedHorizontally || sprint.getCollideValue().get()) && (!this.isSneaking() || sprint.getSneakValue().get()) && !this.isPotionActive(Potion.blindness);
        boolean canToggleSprint = this.onGround && !this.movementInput.jump && !this.movementInput.sneak && !this.isPotionActive(Potion.blindness);
        boolean isCurrentUsingItem = getHeldItem() != null && (this.isUsingItem() || (getHeldItem().getItem() instanceof ItemSword && killAura.getBlockingStatus())) && !this.isRiding();
        boolean isCurrentUsingSword = getHeldItem() != null && getHeldItem().getItem() instanceof ItemSword && (killAura.getBlockingStatus() || this.isUsingItem());
        
        baseSprintState = baseSprintState && !(inventoryMove.getNoSprintValue().equals("Real") && inventoryMove.getInvOpen());
        
        if (!attemptToggle && !lastForwardToggleState && baseSprintState && !this.isSprinting() && canToggleSprint && !isCurrentUsingItem && !this.isPotionActive(Potion.blindness)) {
            if (this.sprintToggleTimer <= 0 && !this.mc.gameSettings.keyBindSprint.isKeyDown()) {
                this.sprintToggleTimer = 7;
            } else {
                attemptToggle = true;
            }
        }
        
        if (sprint.getForceSprint() || baseSprintState && (!isCurrentUsingItem || (sprint.getUseItemValue().get() && (!sprint.getUseItemSwordValue().get() || isCurrentUsingSword))) && attemptToggle) {
            this.setSprinting(true);
        } else {
            this.setSprinting(false);
        }
        
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
        
        /**
         * Update Sprint State - Post
         * Apply Item Slowdown
         * Update sprint state for modules
         * TODO: This part can be skipped if there's no rotation change
         */

        movingStat = Math.abs(this.movementInput.moveForward) > 0.05f || Math.abs(this.movementInput.moveStrafe) > 0.05f;
        runStrictStrafe = strafeFix.getDoFix() && !strafeFix.getSilentFix();
        noStrafe = RotationUtils.targetRotation == null || !strafeFix.getDoFix();
        
        isCurrentUsingItem = getHeldItem() != null && (this.isUsingItem() || (getHeldItem().getItem() instanceof ItemSword && killAura.getBlockingStatus())) && !this.isRiding();
        isCurrentUsingSword = getHeldItem() != null && getHeldItem().getItem() instanceof ItemSword && (killAura.getBlockingStatus() || this.isUsingItem());

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
        
        if (sprint.getForceSprint() || baseSprintState && (!isCurrentUsingItem || (sprint.getUseItemValue().get() && (!sprint.getUseItemSwordValue().get() || isCurrentUsingSword))) && attemptToggle) {
            this.setSprinting(true);
        } else {
            this.setSprinting(false);
        }
        
        //Overwrite: Scaffold
        
        if (scaffold.getState()) {
            this.setSprinting(scaffold.getCanSprint());
        }

        debug_AttemptSprint = this.isSprinting();
        
        attemptToggle = false;

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

    @Override
    public void moveEntity(double x, double y, double z) {
        MoveEvent moveEvent = new MoveEvent(x, y, z);
        FDPClient.eventManager.callEvent(moveEvent);

        if (moveEvent.isCancelled())
            return;

        x = moveEvent.getX();
        y = moveEvent.getY();
        z = moveEvent.getZ();

        if (noClip) {
            setEntityBoundingBox(getEntityBoundingBox().offset(x, y, z));
            posX = (getEntityBoundingBox().minX + getEntityBoundingBox().maxX) / 2;
            posY = getEntityBoundingBox().minY;
            posZ = (getEntityBoundingBox().minZ + getEntityBoundingBox().maxZ) / 2;
        } else {
            worldObj.theProfiler.startSection("move");
            double d0 = posX;
            double d1 = posY;
            double d2 = posZ;

            if (this.isInWeb) {
                this.isInWeb = false;
                x *= 0.25;
                y *= 0.05000000074505806;
                z *= 0.25;
                motionX = 0;
                motionY = 0;
                motionZ = 0;
            }

            double d3 = x;
            double d4 = y;
            double d5 = z;
            boolean flag = onGround && isSneaking();

            if (flag || moveEvent.isSafeWalk()) {
                double d6;

                //noinspection ConstantConditions
                for (d6 = 0.05; x != 0 && worldObj.getCollidingBoundingBoxes((Entity) (Object) this, getEntityBoundingBox().offset(x, -1, 0)).isEmpty(); d3 = x) {
                    if (x < d6 && x >= -d6) {
                        x = 0;
                    } else if (x > 0) {
                        x -= d6;
                    } else {
                        x += d6;
                    }
                }

                //noinspection ConstantConditions
                for (; z != 0 && worldObj.getCollidingBoundingBoxes((Entity) (Object) this, getEntityBoundingBox().offset(0, -1, z)).isEmpty(); d5 = z) {
                    if (z < d6 && z >= -d6) {
                        z = 0;
                    } else if (z > 0) {
                        z -= d6;
                    } else {
                        z += d6;
                    }
                }

                //noinspection ConstantConditions
                for (; x != 0 && z != 0 && worldObj.getCollidingBoundingBoxes((Entity) (Object) this, getEntityBoundingBox().offset(x, -1, z)).isEmpty(); d5 = z) {
                    if (x < d6 && x >= -d6) {
                        x = 0;
                    } else if (x > 0) {
                        x -= d6;
                    } else {
                        x += d6;
                    }

                    d3 = x;

                    if (z < d6 && z >= -d6) {
                        z = 0;
                    } else if (z > 0) {
                        z -= d6;
                    } else {
                        z += d6;
                    }
                }
            }

            // noinspection ConstantConditions
            List<AxisAlignedBB> list1 = worldObj.getCollidingBoundingBoxes((Entity) (Object) this, getEntityBoundingBox().addCoord(x, y, z));
            AxisAlignedBB axisalignedbb = getEntityBoundingBox();

            for (AxisAlignedBB axisalignedbb1 : list1) {
                y = axisalignedbb1.calculateYOffset(getEntityBoundingBox(), y);
            }

            setEntityBoundingBox(getEntityBoundingBox().offset(0, y, 0));
            boolean flag1 = onGround || d4 != y && d4 < 0;

            for (AxisAlignedBB axisalignedbb2 : list1) {
                x = axisalignedbb2.calculateXOffset(getEntityBoundingBox(), x);
            }

            setEntityBoundingBox(getEntityBoundingBox().offset(x, 0, 0));

            for (AxisAlignedBB axisalignedbb13 : list1) {
                z = axisalignedbb13.calculateZOffset(getEntityBoundingBox(), z);
            }

            setEntityBoundingBox(getEntityBoundingBox().offset(0, 0, z));

            if (this.stepHeight > 0.0F && flag1 && (d3 != x || d5 != z)) {
                StepEvent stepEvent = new StepEvent(this.stepHeight, EventState.PRE);
                FDPClient.eventManager.callEvent(stepEvent);
                double d11 = x;
                double d7 = y;
                double d8 = z;
                AxisAlignedBB axisalignedbb3 = getEntityBoundingBox();
                setEntityBoundingBox(axisalignedbb);
                y = stepEvent.getStepHeight();
                //noinspection ConstantConditions
                List<AxisAlignedBB> list = worldObj.getCollidingBoundingBoxes((Entity) (Object) this, getEntityBoundingBox().addCoord(d3, y, d5));
                AxisAlignedBB axisalignedbb4 = getEntityBoundingBox();
                AxisAlignedBB axisalignedbb5 = axisalignedbb4.addCoord(d3, 0, d5);
                double d9 = y;

                for (AxisAlignedBB axisalignedbb6 : list) {
                    d9 = axisalignedbb6.calculateYOffset(axisalignedbb5, d9);
                }

                axisalignedbb4 = axisalignedbb4.offset(0, d9, 0);
                double d15 = d3;

                for (AxisAlignedBB axisalignedbb7 : list) {
                    d15 = axisalignedbb7.calculateXOffset(axisalignedbb4, d15);
                }

                axisalignedbb4 = axisalignedbb4.offset(d15, 0, 0);
                double d16 = d5;

                for (AxisAlignedBB axisalignedbb8 : list) {
                    d16 = axisalignedbb8.calculateZOffset(axisalignedbb4, d16);
                }

                axisalignedbb4 = axisalignedbb4.offset(0, 0, d16);
                AxisAlignedBB axisalignedbb14 = getEntityBoundingBox();
                double d17 = y;

                for (AxisAlignedBB axisalignedbb9 : list) {
                    d17 = axisalignedbb9.calculateYOffset(axisalignedbb14, d17);
                }

                axisalignedbb14 = axisalignedbb14.offset(0, d17, 0);
                double d18 = d3;

                for (AxisAlignedBB axisalignedbb10 : list) {
                    d18 = axisalignedbb10.calculateXOffset(axisalignedbb14, d18);
                }

                axisalignedbb14 = axisalignedbb14.offset(d18, 0, 0);
                double d19 = d5;

                for (AxisAlignedBB axisalignedbb11 : list) {
                    d19 = axisalignedbb11.calculateZOffset(axisalignedbb14, d19);
                }

                axisalignedbb14 = axisalignedbb14.offset(0, 0, d19);
                double d20 = d15 * d15 + d16 * d16;
                double d10 = d18 * d18 + d19 * d19;

                if (d20 > d10) {
                    x = d15;
                    z = d16;
                    y = -d9;
                    setEntityBoundingBox(axisalignedbb4);
                } else {
                    x = d18;
                    z = d19;
                    y = -d17;
                    setEntityBoundingBox(axisalignedbb14);
                }

                for (AxisAlignedBB axisalignedbb12 : list) {
                    y = axisalignedbb12.calculateYOffset(getEntityBoundingBox(), y);
                }

                setEntityBoundingBox(getEntityBoundingBox().offset(0, y, 0));

                if (d11 * d11 + d8 * d8 >= x * x + z * z) {
                    x = d11;
                    y = d7;
                    z = d8;
                    setEntityBoundingBox(axisalignedbb3);
                } else {
                    FDPClient.eventManager.callEvent(new StepEvent(-1f, EventState.POST));
                }
            }

            worldObj.theProfiler.endSection();
            worldObj.theProfiler.startSection("rest");
            posX = (getEntityBoundingBox().minX + getEntityBoundingBox().maxX) / 2;
            posY = getEntityBoundingBox().minY;
            posZ = (getEntityBoundingBox().minZ + getEntityBoundingBox().maxZ) / 2;
            isCollidedHorizontally = d3 != x || d5 != z;
            isCollidedVertically = d4 != y;
            onGround = isCollidedVertically && d4 < 0;
            isCollided = isCollidedHorizontally || isCollidedVertically;
            int i = MathHelper.floor_double(posX);
            int j = MathHelper.floor_double(posY - 0.20000000298023224);
            int k = MathHelper.floor_double(posZ);
            BlockPos blockpos = new BlockPos(i, j, k);
            Block block1 = worldObj.getBlockState(blockpos).getBlock();

            if (block1.getMaterial() == Material.air) {
                Block block = worldObj.getBlockState(blockpos.down()).getBlock();

                if (block instanceof BlockFence || block instanceof BlockWall || block instanceof BlockFenceGate) {
                    block1 = block;
                    blockpos = blockpos.down();
                }
            }

            updateFallState(y, onGround, block1, blockpos);

            if (d3 != x) {
                motionX = 0;
            }

            if (d5 != z) {
                motionZ = 0;
            }

            if (d4 != y) {
                //noinspection ConstantConditions
                block1.onLanded(worldObj, (Entity) (Object) this);
            }

            if (canTriggerWalking() && !flag && ridingEntity == null) {
                double d12 = posX - d0;
                double d13 = posY - d1;
                double d14 = posZ - d2;

                if (block1 != Blocks.ladder) {
                    d13 = 0;
                }

                if (onGround) {
                    //noinspection ConstantConditions
                    block1.onEntityCollidedWithBlock(worldObj, blockpos, (Entity) (Object) this);
                }

                distanceWalkedModified = (float) (distanceWalkedModified + MathHelper.sqrt_double(d12 * d12 + d14 * d14) * 0.6);
                distanceWalkedOnStepModified = (float) (distanceWalkedOnStepModified + MathHelper.sqrt_double(d12 * d12 + d13 * d13 + d14 * d14) * 0.6);

                if (distanceWalkedOnStepModified > (float) getNextStepDistance() && block1.getMaterial() != Material.air) {
                    setNextStepDistance((int) distanceWalkedOnStepModified + 1);

                    if (isInWater()) {
                        float f = MathHelper.sqrt_double(motionX * motionX * 0.20000000298023224 + motionY * motionY + motionZ * motionZ * 0.20000000298023224) * 0.35F;

                        if (f > 1f) {
                            f = 1f;
                        }

                        playSound(getSwimSound(), f, 1f + (rand.nextFloat() - rand.nextFloat()) * 0.4F);
                    }

                    playStepSound(blockpos, block1);
                }
            }

            try {
                doBlockCollisions();
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Checking entity block collision");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being checked for collision");
                addEntityCrashInfo(crashreportcategory);
                throw new ReportedException(crashreport);
            }

            boolean flag2 = isWet();

            if (worldObj.isFlammableWithin(getEntityBoundingBox().contract(0.001, 0.001, 0.001))) {
                dealFireDamage(1);

                if (!flag2) {
                    setFire(getFire() + 1);

                    if (getFire() == 0) {
                        setFire(8);
                    }
                }
            } else if (getFire() <= 0) {
                setFire(-fireResistance);
            }

            if (flag2 && getFire() > 0) {
                playSound("random.fizz", 0.7F, 1.6F + (rand.nextFloat() - rand.nextFloat()) * 0.4F);
                setFire(-fireResistance);
            }

            worldObj.theProfiler.endSection();
        }
    }
}

