package net.ccbluex.liquidbounce.features.module.modules.movement.flys.verus

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB

class VerusJump2Fly : FlyMode("VerusJump2") {
    private val speedValue = FloatValue("${valuePrefix}Speed", 2f, 0f, 3f)
    private val boostLength = IntegerValue("${valuePrefix}BoostTime", 500, 300, 1000)
    private val moveBeforeDamage = BoolValue("${valuePrefix}MoveBeforeDamage", true)

    private var times = 0
    private var timer = MSTimer()

    override fun onEnable() {
        times = 0
        timer.reset()
    }

    override fun onUpdate(event: UpdateEvent) {
        mc.gameSettings.keyBindJump.pressed = false
        if (times < 5 && !moveBeforeDamage.get()) {
            MovementUtils.strafe(0f)
        }
        if (mc.thePlayer.onGround && times < 5) {
            times++
            timer.reset()
            if (times <5) {
                mc.thePlayer.jump()
                MovementUtils.strafe(0.48F)
            }
        }

        if (times >= 5) {
            if (!timer.hasTimePassed(boostLength.get().toLong())) {
                MovementUtils.strafe(speedValue.get())
            } else {
                times = 0
            }
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer) {
            packet.onGround = (times >= 5 && !timer.hasTimePassed(boostLength.get().toLong()))
        }
    }

    override fun onBlockBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y <= fly.launchY) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, fly.launchY, event.z + 1.0)
        }
    }
}
