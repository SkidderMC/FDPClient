package net.ccbluex.liquidbounce.features.module.modules.movement.flys.verus

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement

class VerusBoostFly : FlyMode("VerusBoost") {
    private val speedValue = FloatValue("${valuePrefix}Speed", 1.5f, 0f, 10f)

    private var ticks = 0

    override fun onEnable() {
        super.onEnable()
        ticks = 0;
    }

    override fun onMove(event: MoveEvent) {
        var speed = speedValue.get()
        val pos = mc.thePlayer.position.add(0.0, -1.5, 0.0)
        PacketUtils.sendPacketNoEvent(
            C08PacketPlayerBlockPlacement(pos, 1,
                ItemStack(Blocks.stone.getItem(mc.theWorld, pos)), 0.0F, 0.5F + Math.random().toFloat() * 0.44.toFloat(), 0.0F)
        );
        if (ticks < 3)
            event.cancelEvent()
        if(ticks > 4)
            mc.thePlayer.motionY = 0.0684
        if(ticks <= 25) {
            mc.timer.timerSpeed = 0.8f;
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
            MovementUtils.strafe(speed);
        }else {
            MovementUtils.strafe(0.29F);
        }
    }



    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C03PacketPlayer && ticks < 3) {
            packet.onGround = true
        }
    }

    override fun onMotion(event: MotionEvent) {
        if(!event.isPre())
            return;
        ticks++;
        if(ticks == 3) {
            PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX,mc.thePlayer.posY+3.25,mc.thePlayer.posZ,false));
            PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX,mc.thePlayer.posY,mc.thePlayer.posZ,false));
            PacketUtils.sendPacketNoEvent(C03PacketPlayer(true));
            mc.timer.timerSpeed = 0.4f;
            mc.thePlayer.jump();
        }else {
            if(ticks == 4)
                mc.thePlayer.motionY += 0.3;
        }
    }

    override fun onJump(event: JumpEvent) {
        event.cancelEvent()
    }
}
