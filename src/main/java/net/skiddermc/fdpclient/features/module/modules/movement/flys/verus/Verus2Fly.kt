package net.skiddermc.fdpclient.features.module.modules.movement.flys.verus

import net.skiddermc.fdpclient.event.BlockBBEvent
import net.skiddermc.fdpclient.event.JumpEvent
import net.skiddermc.fdpclient.event.PacketEvent
import net.skiddermc.fdpclient.event.UpdateEvent
import net.skiddermc.fdpclient.features.module.modules.movement.flys.FlyMode
import net.skiddermc.fdpclient.utils.MovementUtils
import net.skiddermc.fdpclient.utils.timer.MSTimer
import net.skiddermc.fdpclient.value.FloatValue
import net.skiddermc.fdpclient.value.BoolValue
import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.util.AxisAlignedBB

class Verus2Fly : FlyMode("Verus2") {
    private val speedValue = FloatValue("${valuePrefix}Speed", 1.5f, 0f, 5f)
    private val yMotionZero = BoolValue("${valuePrefix}SetYMotion0",true)
    private val blocksBB = BoolValue("${valuePrefix}useBlocksBBfly",true)
    private val groundSpoof = BoolValue("${valuePrefix}groundSpoof",true)

    private var flyable = false
    private val timer = MSTimer()

    override fun onEnable() {
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 3.35, mc.thePlayer.posZ, false))
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false))
        mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionY = 0.0
        mc.thePlayer.motionZ = 0.0
        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.42, mc.thePlayer.posZ)
        flyable = true
        mc.timer.timerSpeed = 0.5f
        fly.launchY += 0.42
        timer.reset()
    }

    override fun onUpdate(event: UpdateEvent) {
        if (yMotionZero.get()) {
            mc.thePlayer.motionY = 0.0
        }
        if (timer.hasTimePassed(300)) {
            mc.timer.timerSpeed = 1f
        }

        if (timer.hasTimePassed(1500)) {
            if (flyable) {
                MovementUtils.strafe(0.48f)
            }
            flyable = false
        }

        if (flyable && timer.hasTimePassed(100)) {
            MovementUtils.strafe(speedValue.get())
        } else if (!timer.hasTimePassed(100)) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionY = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer) {
            packet.onGround = groundSpoof.get()
        }
    }

    override fun onBlockBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y <= fly.launchY && blocksBB.get()) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, fly.launchY, event.z + 1.0)
        }
    }

    override fun onJump(event: JumpEvent) {
        event.cancelEvent()
    }
}
