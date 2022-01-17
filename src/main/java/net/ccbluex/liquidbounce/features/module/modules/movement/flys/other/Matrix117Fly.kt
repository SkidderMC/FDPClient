package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos

class Matrix117Fly : FlyMode("Matrix1.17") {

    private var dontPlace = false

    override fun onEnable() {
        dontPlace = true
    }

    override fun onDisable() {
        mc.addScheduledTask {
            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
        }
    }

    override fun onMotion(event: MotionEvent) {
        if(event.eventState == EventState.PRE) {
            if(mc.thePlayer.posY < fly.launchY + 0.15 && mc.thePlayer.posY > fly.launchY + 0.05) {
                PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
                mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), -1, null, 0f, 0f, 0f))
            }
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        for(i in 0..8) {
            // find a empty inventory slot
            if(mc.thePlayer.inventory.mainInventory[i] == null) {
                mc.netHandler.addToSendQueue(C09PacketHeldItemChange(i))
                break
            }
        }
        if(!dontPlace || mc.thePlayer.posY + 1 > fly.launchY) {
            dontPlace = true
            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), -1, null, 0f, 0f, 0f))
        }
        if(mc.thePlayer.onGround) {
            mc.thePlayer.jump()
            dontPlace = true
            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), -1, null, 0f, 0f, 0f))
        }
        mc.thePlayer.onGround = false
        if(mc.thePlayer.motionY < 0) {
            mc.thePlayer.motionX *= 0.8
            mc.thePlayer.motionZ *= 0.8
        }
        mc.timer.timerSpeed = 1.7f
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if(packet is C03PacketPlayer) {
            packet.onGround = false
        }
    }

    override fun onBlockBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y <= fly.launchY) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, fly.launchY, event.z + 1.0)
        }
    }
}