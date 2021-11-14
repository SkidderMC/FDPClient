package net.ccbluex.liquidbounce.features.module.modules.movement.flys.verus

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB

class Verus4Fly : FlyMode("Verus4") {
    private val speedValue = FloatValue("${valuePrefix}Speed", 2f, 0f, 3f)

    private var times = 0
    private var timer = MSTimer()

    override fun onEnable() {
        times = 0
        timer.reset()
    }

    override fun onUpdate(event: UpdateEvent) {
        mc.gameSettings.keyBindJump.pressed = false
        if (mc.thePlayer.onGround && times < 5) {
            times++
            timer.reset()
            if (times <5) {
                mc.thePlayer.jump()
                MovementUtils.strafe(0.48F)
            }
        }

        if (times >= 5) {
            if (!timer.hasTimePassed(500)) {
                MovementUtils.strafe(speedValue.get())
            } else {
                times = 0
            }
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer) {
            packet.onGround = (times >= 5 && !timer.hasTimePassed(500))
        }
    }

    override fun onBlockBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y <= fly.launchY) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, fly.launchY, event.z + 1.0)
        }
    }
}
