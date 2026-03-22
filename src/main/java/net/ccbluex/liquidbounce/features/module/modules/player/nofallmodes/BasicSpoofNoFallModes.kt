package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes

import net.ccbluex.liquidbounce.event.PacketEvent
import net.minecraft.network.play.client.C03PacketPlayer

open class GroundSpoofNoFallMode(
    modeName: String,
    private val forcedOnGround: Boolean,
    private val minFallDistance: Float? = null,
) : NoFallMode(modeName) {
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet as? C03PacketPlayer ?: return

        if (minFallDistance != null) {
            val player = mc.thePlayer ?: return

            if (player.fallDistance <= minFallDistance) {
                return
            }
        }

        packet.onGround = forcedOnGround
    }
}

open class IntervalGroundSpoofNoFallMode(
    modeName: String,
    private val fallDivisor: Int,
) : NoFallMode(modeName) {
    private var packetCount = 0
    private var packetModify = false

    override fun onEnable() {
        packetCount = 0
        packetModify = false
    }

    override fun onUpdate() {
        val player = mc.thePlayer ?: return
        val currentPacketCount = player.fallDistance.toInt() / fallDivisor

        if (currentPacketCount > packetCount) {
            packetCount = currentPacketCount
            packetModify = true
        }

        if (player.onGround) {
            packetCount = 0
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet as? C03PacketPlayer ?: return

        if (!packetModify) {
            return
        }

        packet.onGround = true
        packetModify = false
    }
}
