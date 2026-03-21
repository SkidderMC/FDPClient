package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.aac

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPackets
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB

object AACv4 : NoFallMode("AACv4") {
    private var aac4FakeLag = false
    private var packetModify = false
    private val aac4Packets = mutableListOf<C03PacketPlayer>()

    override fun onEnable() {
        aac4Packets.clear()
        packetModify = false
        aac4FakeLag = false
    }

    override fun onDisable() {
        flushPackets()
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer && aac4FakeLag) {
            event.cancelEvent()

            if (packetModify) {
                packet.onGround = true
                packetModify = false
            }

            aac4Packets += packet
        }
    }

    override fun onMotion(event: MotionEvent) {
        val player = mc.thePlayer ?: return

        if (event.eventState != EventState.PRE) {
            return
        }

        if (!inVoid()) {
            if (aac4FakeLag) {
                aac4FakeLag = false
                flushPackets()
            }

            return
        }

        if (player.onGround && aac4FakeLag) {
            aac4FakeLag = false
            flushPackets()
            return
        }

        if (player.fallDistance > 2.5f && aac4FakeLag) {
            packetModify = true
            player.fallDistance = 0f
        }

        if (inAir(4.0, 1.0)) {
            return
        }

        if (!aac4FakeLag) {
            aac4FakeLag = true
        }
    }

    private fun flushPackets() {
        if (aac4Packets.isNotEmpty()) {
            sendPackets(*aac4Packets.toTypedArray(), triggerEvents = false)
            aac4Packets.clear()
        }
    }

    private fun inVoid(): Boolean {
        val player = mc.thePlayer ?: return false

        if (player.posY < 0) {
            return false
        }

        var off = 0
        while (off < player.posY + 2) {
            val bb = AxisAlignedBB(
                player.posX,
                player.posY,
                player.posZ,
                player.posX,
                off.toDouble(),
                player.posZ
            )

            if (mc.theWorld.getCollidingBoundingBoxes(player, bb).isNotEmpty()) {
                return true
            }

            off += 2
        }

        return false
    }

    private fun inAir(height: Double, plus: Double): Boolean {
        val player = mc.thePlayer ?: return false

        if (player.posY < 0) {
            return false
        }

        var off = 0.0
        while (off < height) {
            val bb = AxisAlignedBB(
                player.posX,
                player.posY,
                player.posZ,
                player.posX,
                player.posY - off,
                player.posZ
            )

            if (mc.theWorld.getCollidingBoundingBoxes(player, bb).isNotEmpty()) {
                return true
            }

            off += plus
        }

        return false
    }
}
