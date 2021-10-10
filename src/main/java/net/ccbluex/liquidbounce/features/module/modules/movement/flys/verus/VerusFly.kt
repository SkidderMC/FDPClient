package net.ccbluex.liquidbounce.features.module.modules.movement.flys.verus

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.util.AxisAlignedBB

class VerusFly : FlyMode("Verus") {
    private var flyable=false
    private var enabled=false
    private val timer=MSTimer()

    override fun onEnable() {
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 3.35, mc.thePlayer.posZ, false))
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false))
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionY = 0.0
        mc.thePlayer.motionZ = 0.0
        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.42, mc.thePlayer.posZ)
        flyable = true
        enabled = true
        timer.reset()
    }

    override fun onUpdate(event: UpdateEvent) {
        if(timer.hasTimePassed(1500))
            flyable=false

        if(flyable&&timer.hasTimePassed(100)){
            MovementUtils.strafe(1.5F)
            if(enabled){
                fly.launchY+=0.42
                enabled=false
                mc.thePlayer.motionY=0.0
            }
        }else if(!timer.hasTimePassed(100)) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionY = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet=event.packet

        if(packet is C03PacketPlayer){
            packet.onGround=true
        }
    }

    override fun onBlockBB(event: BlockBBEvent) {
        if(event.block is BlockAir && event.y<=fly.launchY)
            event.boundingBox= AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, fly.launchY, event.z + 1.0)
    }

    override fun onJump(event: JumpEvent) {
        event.cancelEvent()
    }
}