package net.ccbluex.liquidbounce.features.module.modules.player.nofalls.other

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofalls.NoFallMode
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.minecraft.network.play.client.C03PacketPlayer
import net.ccbluex.liquidbounce.utils.BlinkUtils
import net.ccbluex.liquidbounce.utils.misc.FallingPlayer
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.client.gui.ScaledResolution

class HypixelBlinkNofall : NoFallMode("HypixelBlink") {

    private val indicator = BoolValue("Indicatior", true)

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
            if (mc.thePlayer.motionY < 0 && (FallingPlayer(mc.thePlayer).findCollision(60) != null)) {
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

    override fun onRender2D(event: Render2DEvent) {
        val scaledResolution = ScaledResolution(mc)
        Fonts.minecraftFont.drawString(
            "Blinking " + BlinkUtils.bufferSize().toString(),
            scaledResolution.scaledWidth / 1.95f,
            (scaledResolution.scaledHeight / 2 + 20).toFloat(),
            -1,
            true
        )
    }
}
