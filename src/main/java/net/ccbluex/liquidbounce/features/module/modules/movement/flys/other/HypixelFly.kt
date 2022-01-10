package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TickTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB

class HypixelFly : FlyMode("Hypixel") {
    private val boostValue = BoolValue("${valuePrefix}Boost", true)
    private val boostDelayValue = IntegerValue("${valuePrefix}BoostDelay", 1200, 0, 2000)
    private val boostTimerValue = FloatValue("${valuePrefix}BoostTimer", 1f, 0f, 5f)

    private val timer = TickTimer()
    private val flyTimer = MSTimer()

    override fun onUpdate(event: UpdateEvent) {
        val boostDelay: Long = boostDelayValue.get().toLong()
        if (boostValue.get() && !flyTimer.hasTimePassed(boostDelay)) {
            mc.timer.timerSpeed = 1f + boostTimerValue.get() * (flyTimer.hasTimeLeft(boostDelay).toFloat() / boostDelay.toFloat())
        }

        timer.update()
        if (timer.hasTimePassed(2)) {
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.0E-5, mc.thePlayer.posZ)
            timer.reset()
        }
    }

    override fun onBlockBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y <= mc.thePlayer.posY) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, mc.thePlayer.posY, event.z + 1.0)
        }
    }

    override fun onJump(event: JumpEvent) {
        event.cancelEvent()
    }

    override fun onStep(event: StepEvent) {
        event.stepHeight = 0f
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer) {
            packet.onGround = false
        }
    }
}