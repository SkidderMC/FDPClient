package net.ccbluex.liquidbounce.features.module.modules.player.nofalls.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofalls.NoFallMode
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.minecraft.network.play.client.C03PacketPlayer
import net.ccbluex.liquidbounce.utils.BlinkUtils

class HypixelBlinkNofall : NoFallMode("HypixelBlink") {

    private var enabled = false
    private var wasOnGround = false

    override fun onEnable() {
        enabled = false
    }

    override fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.onGround) {
            wasOnGround = true
        } else if (wasOnGround) {
            wasOnGround = false
            if (mc.thePlayer.motionY < 0) {
                enabled = true
                BlinkUtils.setBlinkState(all = true)
            }
        }
    }
    
    override fun onPacket(event: PacketEvent) {
        if(enabled && event.packet is C03PacketPlayer) {
            event.packet.onGround = true
        }

        if (mc.thePlayer.onGround) {
            if (enabled) {
                BlinkUtils.setBlinkState(off = true, release = true)
                enabled = false
            }
        }
    }
}
