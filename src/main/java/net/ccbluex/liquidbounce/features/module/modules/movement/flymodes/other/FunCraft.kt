package net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.other

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.StepEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.funCraftTimer
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.FlyMode
import net.ccbluex.liquidbounce.utils.timing.TickTimer
import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB

object FunCraft : FlyMode("FunCraft") {
    private val timer = TickTimer()

    override fun onEnable() {
        timer.reset()
    }

    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        mc.timer.timerSpeed = funCraftTimer
        timer.update()

        if (timer.hasTimePassed(2)) {
            player.setPosition(player.posX, player.posY + 1.0E-5, player.posZ)
            timer.reset()
        }
    }

    override fun onBB(event: BlockBBEvent) {
        val player = mc.thePlayer ?: return

        if (event.block is BlockAir && event.y <= player.posY) {
            event.boundingBox = AxisAlignedBB.fromBounds(
                event.x.toDouble(),
                event.y.toDouble(),
                event.z.toDouble(),
                event.x + 1.0,
                player.posY,
                event.z + 1.0
            )
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
