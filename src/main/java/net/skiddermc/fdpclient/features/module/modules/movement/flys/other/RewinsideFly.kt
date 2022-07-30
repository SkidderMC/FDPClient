package net.skiddermc.fdpclient.features.module.modules.movement.flys.other

import net.skiddermc.fdpclient.event.BlockBBEvent
import net.skiddermc.fdpclient.event.JumpEvent
import net.skiddermc.fdpclient.event.PacketEvent
import net.skiddermc.fdpclient.event.StepEvent
import net.skiddermc.fdpclient.features.module.modules.movement.flys.FlyMode
import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB

class RewinsideFly : FlyMode("Rewinside") {
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer) {
            packet.onGround = true
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
}