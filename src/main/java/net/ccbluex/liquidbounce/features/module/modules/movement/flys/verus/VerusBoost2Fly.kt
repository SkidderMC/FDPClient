package net.ccbluex.liquidbounce.features.module.modules.movement.flys.verus

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.block.BlockAir
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos

class VerusBoost2Fly : FlyMode("VerusBoost2") {
    private val speedValue = FloatValue("${valuePrefix}Speed", 1.5f, 0f, 5f)

    private var times = 0
    private var timer = MSTimer()
    private var ticks = 0

    override fun onEnable() {
        times = 0
        timer.reset()
        ticks = 0
    }

    override fun onMotion(event: MotionEvent) {
        if(!event.isPre())
            return;
        ticks++;
    }

    override fun onMove(event: MoveEvent) {
        if(ticks < 3) {
            event.cancelEvent()
        }
        if(ticks == 3) {
            val pos = mc.thePlayer.position.add(0.0, -1.5, 0.0)
            PacketUtils.sendPacketNoEvent(
                C08PacketPlayerBlockPlacement(pos, 1,
                    ItemStack(Blocks.stone.getItem(mc.theWorld, pos)), 0.0F, 0.5F + Math.random().toFloat() * 0.44.toFloat(), 0.0F)
            );
            val x = mc.thePlayer.posX
            val y = mc.thePlayer.posY
            val z = mc.thePlayer.posZ
            PacketUtils.sendPacketNoEvent(C03PacketPlayer.C06PacketPlayerPosLook(x, y+3+Math.random()*0.07, z, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false));
            PacketUtils.sendPacketNoEvent(C03PacketPlayer.C06PacketPlayerPosLook(x, y, z, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false));
            PacketUtils.sendPacketNoEvent(C03PacketPlayer.C06PacketPlayerPosLook(x, y, z, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, true));
            mc.timer.timerSpeed = 0.25f;
        }
        if(mc.thePlayer.hurtTime > 2) {
            mc.thePlayer.motionY += 0.4f;
            event.y = mc.thePlayer.motionY
            mc.timer.timerSpeed = 1.0f;
            MovementUtils.strafe(speedValue.get());
        }
        if(mc.thePlayer.hurtTime == 3)
            mc.thePlayer.motionY = 0.42;
        if(mc.thePlayer.hurtTime == 0) {
            MovementUtils.strafe(0.36F);
            if(mc.thePlayer.fallDistance > 0)
                mc.thePlayer.motionY = 0.0;
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer && ticks < 3) {
            packet.onGround = true

        }
    }
}
