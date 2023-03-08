/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement;

import net.ccbluex.liquidbounce.event.*;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.features.value.ListValue;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.MovementUtils;
import net.ccbluex.liquidbounce.utils.PacketUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.features.value.BoolValue;
import net.ccbluex.liquidbounce.features.value.FloatValue;
import net.ccbluex.liquidbounce.features.value.IntegerValue;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.awt.*;

@ModuleInfo(name = "BowJump", category = ModuleCategory.MOVEMENT)
public class BowJump extends Module {

    private final BoolValue hypixelBypassValue = new BoolValue("hypixelBypass", true);
    private final ListValue modeValue = new ListValue("BoostMode", new String[] {"Strafe","SpeedInAir"}, "Strafe");
    private final FloatValue speedInAirBoostValue = new FloatValue("SpeedInAir", 0.5F, 0.02F, 1F);
    private final FloatValue boostValue = new FloatValue("Boost", 4.25F, 0F, 10F);
    private final FloatValue heightValue = new FloatValue("Height", 0.42F, 0F, 10F);
    private final FloatValue timerValue = new FloatValue("Timer", 1F, 0.1F, 10F);
    private final IntegerValue delayBeforeLaunch = new IntegerValue("DelayBeforeArrowLaunch", 1, 1, 20);

    private final BoolValue autoDisable = new BoolValue("AutoDisable", true);
    private final BoolValue renderValue = new BoolValue("RenderStatus", true);

    private int bowState = 0;
    private long lastPlayerTick = 0;


    private int lastSlot = -1;

    public void onEnable() {
        if (mc.thePlayer == null) return;
        bowState = 0;
        lastPlayerTick = -1;
        lastSlot = mc.thePlayer.inventory.currentItem;

        MovementUtils.INSTANCE.strafe(0.0f);
        mc.thePlayer.onGround = false;
        mc.thePlayer.jumpMovementFactor = 0.0f;
    }

    @EventTarget
    public void onMove(MoveEvent event) {
        if (mc.thePlayer.onGround && bowState < 3)
            event.cancelEvent();
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof C09PacketHeldItemChange) {
            C09PacketHeldItemChange c09 = (C09PacketHeldItemChange) event.getPacket();
            lastSlot = c09.getSlotId();
            event.cancelEvent();
        }

        if (event.getPacket() instanceof C03PacketPlayer) {
            C03PacketPlayer c03 = (C03PacketPlayer) event.getPacket();
            if (bowState < 3) c03.setMoving(false);
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        mc.timer.timerSpeed = 1F;

        boolean forceDisable = false;
        switch (bowState) {
        case 0:
            int slot = getBowSlot();
            if (slot < 0 || !mc.thePlayer.inventory.hasItem(Items.arrow)) {
                forceDisable = true;
                bowState = 5;
                break; // nothing to shoot
            } else if (lastPlayerTick == -1) {
                ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(slot + 36).getStack();

                if (lastSlot != slot) PacketUtils.sendPacketNoEvent(new C09PacketHeldItemChange(slot));
                PacketUtils.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, mc.thePlayer.inventoryContainer.getSlot(slot + 36).getStack(), 0, 0, 0));

                lastPlayerTick = mc.thePlayer.ticksExisted;
                bowState = 1;
            }
            break;
        case 1:
            int reSlot = getBowSlot();
            if (mc.thePlayer.ticksExisted - lastPlayerTick > delayBeforeLaunch.get()) {
                PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C05PacketPlayerLook(mc.thePlayer.rotationYaw, -90, mc.thePlayer.onGround));
                PacketUtils.sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));

                if (lastSlot != reSlot) PacketUtils.sendPacketNoEvent(new C09PacketHeldItemChange(lastSlot));
                bowState = 2;
            }
            break;
        case 2:
            if (mc.thePlayer.hurtTime > 0)
                bowState = 3;
            break;
        case 3:
            if (hypixelBypassValue.get()) {
                if (mc.thePlayer.hurtTime >= 8) {
                    mc.thePlayer.motionY = 0.58;
                    mc.thePlayer.jump();
                }
                if (mc.thePlayer.hurtTime == 8) {
                    MovementUtils.INSTANCE.strafe(0.72f);
                }

                if (mc.thePlayer.hurtTime == 7) {
                    mc.thePlayer.motionY += 0.03;
                }

                if (mc.thePlayer.hurtTime <= 6) {
                    mc.thePlayer.motionY += 0.015;
                }
                mc.timer.timerSpeed = timerValue.get();
                if (mc.thePlayer.onGround && mc.thePlayer.ticksExisted - lastPlayerTick >= 1)
                    bowState = 5;
            } else {
                switch (modeValue.get()) {
                    case "Strafe": {
                        MovementUtils.INSTANCE.strafe(boostValue.get());
                        break;
                    }
                    case "SpeedInAir":{
                        mc.thePlayer.speedInAir = speedInAirBoostValue.getValue();
                        mc.thePlayer.jump();
                    }
                }
                mc.thePlayer.motionY = heightValue.get();
                bowState = 4;
                lastPlayerTick = mc.thePlayer.ticksExisted;
                break;
            }
        case 4:
            mc.timer.timerSpeed = timerValue.get();
            if (mc.thePlayer.onGround && mc.thePlayer.ticksExisted - lastPlayerTick >= 1)
                bowState = 5;
            break;
        }

        if (bowState < 3) {
            mc.thePlayer.movementInput.moveForward = 0F;
            mc.thePlayer.movementInput.moveStrafe = 0F;
        }

        if (bowState == 5 && (autoDisable.get() || forceDisable)) 
            this.setState(false);
    }

    @EventTarget
    public void onWorld(WorldEvent event) {
        this.setState(false); //prevent weird things
    }

    public void onDisable(){
        mc.timer.timerSpeed = 1.0F;
        mc.thePlayer.speedInAir = 0.02F;
    }

    private int getBowSlot() {
        for(int i = 36; i < 45; ++i) {
            ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (stack != null && stack.getItem() instanceof ItemBow) {
                return i - 36;
            }
        }
        return -1;
    }

    @EventTarget
    public void onRender2D(final Render2DEvent event) {
        if (!renderValue.get()) return;
        ScaledResolution scaledRes = new ScaledResolution(mc);
            
        float width = (float) bowState / 5F * 60F;

        Fonts.font40.drawCenteredString(getBowStatus(), scaledRes.getScaledWidth() / 2F, scaledRes.getScaledHeight() / 2F + 14F, -1, true);
        RenderUtils.drawRect(scaledRes.getScaledWidth() / 2F - 31F, scaledRes.getScaledHeight() / 2F + 25F, scaledRes.getScaledWidth() / 2F + 31F, scaledRes.getScaledHeight() / 2F + 29F, 0xA0000000);
        RenderUtils.drawRect(scaledRes.getScaledWidth() / 2F - 30F, scaledRes.getScaledHeight() / 2F + 26F, scaledRes.getScaledWidth() / 2F - 30F + width, scaledRes.getScaledHeight() / 2F + 28F, getStatusColor());
        
    }

    public String getBowStatus() {
        switch (bowState) {
            case 0:
            return "Idle...";
            case 1:
            return "Preparing...";
            case 2:
            return "Waiting for damage...";
            case 3:
            case 4:
            return "Boost!";
            default:
            return "Task completed.";
        }
    }

    public Color getStatusColor() {
        switch (bowState) {
            case 0:
            return new Color(21, 21, 21);
            case 1:
            return new Color(48, 48, 48);
            case 2:
            return Color.yellow;
            case 3:
            case 4:
            return Color.green;
            default:
            return new Color(0, 111, 255);
        }
    }
}
