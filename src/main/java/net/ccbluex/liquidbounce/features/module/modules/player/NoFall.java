/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.*;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.features.module.modules.render.FreeCam;
import net.ccbluex.liquidbounce.utils.block.BlockUtils;
import net.ccbluex.liquidbounce.utils.misc.FallingPlayer;
import net.ccbluex.liquidbounce.utils.timer.TickTimer;
import net.ccbluex.liquidbounce.value.IntegerValue;
import net.ccbluex.liquidbounce.value.ListValue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

// TODO: phase mode bypass matrix
@ModuleInfo(name = "NoFall", description = "Prevents you from taking fall damage.", category = ModuleCategory.PLAYER)
public class NoFall extends Module {
    public final ListValue modeValue = new ListValue("Mode", new String[]{"SpoofGround", "NoGround", "Packet", "OldAAC", "LAAC", "AAC3.3.11", "AAC3.3.15", "AACv4", "AAC5.0.14", "Spartan", "CubeCraft", "Hypixel", "Phase", "Verus", "HypixelNew", "HypixelAnother"}, "SpoofGround");

    private final IntegerValue phaseOffsetValue = new IntegerValue("PhaseOffset",1,0,5);

    private int state;
    private boolean jumped;
    private final TickTimer spartanTimer = new TickTimer();

    private boolean aac4Fakelag=false;
    private boolean aac4PacketModify=false;
    private boolean aac5doFlag=false;
    private boolean aac5Check=false;
    private final TickTimer aac5Timer = new TickTimer();
    private final ArrayList<C03PacketPlayer> aac4Packets=new ArrayList<>();

    private boolean NeedSpoof=false;
    @Override
    public void onEnable(){
        aac4Fakelag=false;
        aac5Check=false;
        aac4PacketModify=false;
        aac4Packets.clear();
        NeedSpoof=false;
        aac5doFlag=false;
    }

    @EventTarget(ignoreCondition = true)
    public void onUpdate(UpdateEvent event) {
        if(mc.thePlayer.onGround)
            jumped = false;

        if(mc.thePlayer.motionY > 0)
            jumped = true;

        if (!getState() || LiquidBounce.moduleManager.getModule(FreeCam.class).getState())
            return;

        if (mc.thePlayer.isSpectator() || mc.thePlayer.capabilities.allowFlying || mc.thePlayer.capabilities.disableDamage)
            return;
        
        if(BlockUtils.collideBlock(mc.thePlayer.getEntityBoundingBox(), block -> block instanceof BlockLiquid) ||
                BlockUtils.collideBlock(new AxisAlignedBB(mc.thePlayer.getEntityBoundingBox().maxX, mc.thePlayer.getEntityBoundingBox().maxY, mc.thePlayer.getEntityBoundingBox().maxZ, mc.thePlayer.getEntityBoundingBox().minX, mc.thePlayer.getEntityBoundingBox().minY - 0.01D, mc.thePlayer.getEntityBoundingBox().minZ), block -> block instanceof BlockLiquid))
            return;

        switch(modeValue.get().toLowerCase()) {
            case "packet": {
                if (mc.thePlayer.fallDistance > 2F)
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer(true));
                break;
            }
            case "hypixelnew":{
                if(mc.thePlayer.onGround){
                    mc.thePlayer.fallDistance = 0.5F;
                }
                if(mc.thePlayer.fallDistance > 2) {
                    mc.thePlayer.onGround = false;
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer(true));
                }
                break;
            }
            case "hypixelanother":{
                if(mc.thePlayer.fallDistance> 2.74) {
                    mc.thePlayer.onGround=false;
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer(true));
                }
                break;
            }
            case "cubecraft": {
                if (mc.thePlayer.fallDistance > 2F) {
                    mc.thePlayer.onGround = false;
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
                }
                break;
            }
            case "oldaac": {
                if (mc.thePlayer.fallDistance > 2F) {
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer(true));
                    state = 2;
                } else if (state == 2 && mc.thePlayer.fallDistance < 2) {
                    mc.thePlayer.motionY = 0.1D;
                    state = 3;
                    return;
                }

                switch (state) {
                    case 3:
                        mc.thePlayer.motionY = 0.1D;
                        state = 4;
                        break;
                    case 4:
                        mc.thePlayer.motionY = 0.1D;
                        state = 5;
                        break;
                    case 5:
                        mc.thePlayer.motionY = 0.1D;
                        state = 1;
                        break;
                }
                break;
            }
            case "laac": {
                if (!jumped && mc.thePlayer.onGround && !mc.thePlayer.isOnLadder() && !mc.thePlayer.isInWater()
                        && !mc.thePlayer.isInWeb)
                    mc.thePlayer.motionY = -6;
                break;
            }
            case "aac3.3.11": {
                if (mc.thePlayer.fallDistance > 2) {
                    mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX,
                            mc.thePlayer.posY - 10E-4D, mc.thePlayer.posZ, mc.thePlayer.onGround));
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer(true));
                }
                break;
            }
            case "aac3.3.15": {
                if (mc.thePlayer.fallDistance > 2) {
                    if (!mc.isIntegratedServerRunning())
                        mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX,
                                Double.NaN, mc.thePlayer.posZ, false));
                    mc.thePlayer.fallDistance = -9999;
                }
                break;
            }
            case "spartan": {
                spartanTimer.update();

                if (mc.thePlayer.fallDistance > 1.5 && spartanTimer.hasTimePassed(10)) {
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX,
                            mc.thePlayer.posY + 10, mc.thePlayer.posZ, true));
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX,
                            mc.thePlayer.posY - 10, mc.thePlayer.posZ, true));
                    spartanTimer.reset();
                }
                break;
            }
            case "aac5.0.14": {
                double offsetYs = 0.0;
                aac5Check=false;
                while (mc.thePlayer.motionY-0.5 < offsetYs) {
                    final BlockPos blockPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + offsetYs, mc.thePlayer.posZ);
                    final Block block = BlockUtils.getBlock(blockPos);
                    final AxisAlignedBB axisAlignedBB = block.getCollisionBoundingBox(mc.theWorld, blockPos, BlockUtils.getState(blockPos));
                    if(axisAlignedBB != null) {
                        offsetYs = -999.9;
                        aac5Check=true;
                    }
                    offsetYs -= 0.5;
                }
                if(mc.thePlayer.onGround) {
                    mc.thePlayer.fallDistance=-5;
                    aac5Check=false;
                }
                if(aac5Check && mc.thePlayer.fallDistance>3.125 && !mc.thePlayer.onGround) {
                    aac5doFlag=true;
                }else {
                    aac5doFlag=false;
                }
                if(aac5doFlag) {
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX,
                            mc.thePlayer.posY + 0.5, mc.thePlayer.posZ, true));
                }
                break;
            }
            case "phase":{
                if(mc.thePlayer.fallDistance > (3+phaseOffsetValue.get())){
                    BlockPos fallPos=new FallingPlayer(mc.thePlayer.posX,mc.thePlayer.posY,mc.thePlayer.posZ,mc.thePlayer.motionX,mc.thePlayer.motionY,mc.thePlayer.motionZ,mc.thePlayer.rotationYaw,mc.thePlayer.moveStrafing,mc.thePlayer.moveForward)
                            .findCollision(5);
                    if(fallPos==null)
                        return;

                    if((fallPos.getY()-(mc.thePlayer.motionY/20.0))<mc.thePlayer.posY){
                        mc.timer.timerSpeed=0.05F;
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(fallPos.getX(),fallPos.getY(),fallPos.getZ(),true));
                                mc.timer.timerSpeed=1F;
                            }
                        },100);
                    }
                }
            }
            case "verus":{
                if(mc.thePlayer.fallDistance - mc.thePlayer.motionY > 3) {
                    mc.thePlayer.motionY = 0.0;
                    mc.thePlayer.fallDistance = 0.0f;
                    mc.thePlayer.motionX *= 0.6;
                    mc.thePlayer.motionZ *= 0.6;
                    NeedSpoof = true;
                }
            }
        }
    }

    @EventTarget
    public void onMotion(final MotionEvent event){
        if(modeValue.get().equalsIgnoreCase("AACv4")&&event.isPre()){
            if (!inVoid()) {
                if (aac4Fakelag) {
                    aac4Fakelag = false;
                    if (aac4Packets.size() > 0) {
                        for (C03PacketPlayer packet : aac4Packets) {
                            mc.thePlayer.sendQueue.addToSendQueue(packet);
                        }
                        aac4Packets.clear();
                    }
                }
                return;
            }
            if (mc.thePlayer.onGround && aac4Fakelag) {
                aac4Fakelag = false;
                if (aac4Packets.size() > 0) {
                    for (C03PacketPlayer packet : aac4Packets) {
                        mc.thePlayer.sendQueue.addToSendQueue(packet);
                    }
                    aac4Packets.clear();
                }
                return;
            }
            if (mc.thePlayer.fallDistance > 2.5 && aac4Fakelag) {
                aac4PacketModify = true;
                mc.thePlayer.fallDistance = 0;
            }
            if (inAir(4.0, 1.0)) {
                return;
            }
            if (!aac4Fakelag) {
                aac4Fakelag = true;
            }
        }
    }

    @EventTarget
    public void onPacket(final PacketEvent event) {
        final String mode = modeValue.get();

        if (event.getPacket() instanceof C03PacketPlayer) {
            final C03PacketPlayer packet = (C03PacketPlayer) event.getPacket();

            if (mode.equalsIgnoreCase("SpoofGround") && mc.thePlayer.fallDistance>2.5)
                packet.onGround = true;

            if (mode.equalsIgnoreCase("NoGround"))
                packet.onGround = false;

            if (mode.equalsIgnoreCase("Hypixel")
                    && mc.thePlayer != null && mc.thePlayer.fallDistance > 1.5)
                packet.onGround = mc.thePlayer.ticksExisted % 2 == 0;


            if (mode.equalsIgnoreCase("AACv4")&&aac4Fakelag){
                event.cancelEvent();
                if (aac4PacketModify) {
                    packet.onGround = true;
                    aac4PacketModify = false;
                }
                aac4Packets.add(packet);
            }
            
            if (mode.equalsIgnoreCase("Verus") && NeedSpoof) {
                packet.onGround = true;
                NeedSpoof = false;
            }
        }
    }

    @EventTarget
    public void onMove(MoveEvent event) {
        if(BlockUtils.collideBlock(mc.thePlayer.getEntityBoundingBox(), block -> block instanceof BlockLiquid) || BlockUtils.collideBlock(new AxisAlignedBB(mc.thePlayer.getEntityBoundingBox().maxX, mc.thePlayer.getEntityBoundingBox().maxY, mc.thePlayer.getEntityBoundingBox().maxZ, mc.thePlayer.getEntityBoundingBox().minX, mc.thePlayer.getEntityBoundingBox().minY - 0.01D, mc.thePlayer.getEntityBoundingBox().minZ), block -> block instanceof BlockLiquid))
            return;

        if (modeValue.get().equalsIgnoreCase("laac")) {
            if (!jumped && !mc.thePlayer.onGround && !mc.thePlayer.isOnLadder() && !mc.thePlayer.isInWater() && !mc.thePlayer.isInWeb && mc.thePlayer.motionY < 0D) {
                event.setX(0);
                event.setZ(0);
            }
        }
    }

    @EventTarget(ignoreCondition = true)
    public void onJump(final JumpEvent event) {
        jumped = true;
    }

    private boolean inVoid(){
        if (mc.thePlayer.posY < 0) {
            return false;
        }
        for (int off = 0; off < mc.thePlayer.posY + 2; off += 2) {
            AxisAlignedBB bb = new AxisAlignedBB(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.posX, off, mc.thePlayer.posZ);
            if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean inAir(final double height,final double plus){
        if (mc.thePlayer.posY < 0)
            return false;
        for (int off = 0; off < height; off += plus) {
            AxisAlignedBB bb = new AxisAlignedBB(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.posX, mc.thePlayer.posY - off, mc.thePlayer.posZ);
            if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getTag() {
        return modeValue.get();
    }
}
