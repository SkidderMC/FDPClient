package net.ccbluex.liquidbounce.features.module.modules.player.nofalls.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofalls.NoFallMode
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.minecraft.network.play.client.C03PacketPlayer
import net.ccbluex.liquidbounce.utils.BlinkUtils

class HypixelBlinkNofall : NoFallMode("HypixelBlink") {

    private var enabled = false

    override fun onEnable() {
        enabled = false
    }
    
    override fun onPacket(event: PacketEvent) {
        if (mc.thePlayer.fallDistance > 1.5) {
            if (!enabled) {
                BlinkUtils.setBlinkState(all = true)
                enabled = true
            }
        } 
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
