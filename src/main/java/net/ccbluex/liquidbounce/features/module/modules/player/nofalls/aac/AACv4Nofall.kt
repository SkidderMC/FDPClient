package net.ccbluex.liquidbounce.features.module.modules.player.nofalls.aac

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofalls.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB

class AACv4Nofall : NoFallMode("AACv4") {
    private var aac4Fakelag = false
    private var packetModify = false
    private val aac4Packets = mutableListOf<C03PacketPlayer>()
    override fun onEnable() {
        aac4Packets.clear()
        packetModify =false
        aac4Fakelag = false
    }

    override fun onPacket(event: PacketEvent) {
        if(event.packet is C03PacketPlayer && aac4Fakelag) {
            event.cancelEvent()
            if (packetModify) {
                event.packet.onGround = true
                packetModify = false
            }
            aac4Packets.add(event.packet)
        }
    }

    override fun onMotion(event: MotionEvent) {
        if(event.eventState == EventState.PRE) {
            if (!inVoid()) {
                if (aac4Fakelag) {
                    aac4Fakelag = false
                    if (aac4Packets.size > 0) {
                        for (packet in aac4Packets) {
                            mc.thePlayer.sendQueue.addToSendQueue(packet)
                        }
                        aac4Packets.clear()
                    }
                }
                return
            }
            if (mc.thePlayer.onGround && aac4Fakelag) {
                aac4Fakelag = false
                if (aac4Packets.size > 0) {
                    for (packet in aac4Packets) {
                        mc.thePlayer.sendQueue.addToSendQueue(packet)
                    }
                    aac4Packets.clear()
                }
                return
            }
            if (mc.thePlayer.fallDistance > 2.5 && aac4Fakelag) {
                packetModify = true
                mc.thePlayer.fallDistance = 0f
            }
            if (inAir(4.0, 1.0)) {
                return
            }
            if (!aac4Fakelag) {
                aac4Fakelag = true
            }
        }
    }
    private fun inVoid(): Boolean {
        if (mc.thePlayer.posY < 0) {
            return false
        }
        var off = 0
        while (off < mc.thePlayer.posY + 2) {
            val bb = AxisAlignedBB(
                mc.thePlayer.posX,
                mc.thePlayer.posY,
                mc.thePlayer.posZ,
                mc.thePlayer.posX,
                off.toDouble(),
                mc.thePlayer.posZ
            )
            if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isNotEmpty()) {
                return true
            }
            off += 2
        }
        return false
    }
    private fun inAir(height: Double, plus: Double): Boolean {
        if (mc.thePlayer.posY < 0) return false
        var off = 0
        while (off < height) {
            val bb = AxisAlignedBB(
                mc.thePlayer.posX,
                mc.thePlayer.posY,
                mc.thePlayer.posZ,
                mc.thePlayer.posX,
                mc.thePlayer.posY - off,
                mc.thePlayer.posZ
            )
            if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                return true
            }
            off += plus.toInt()
        }
        return false
    }
}