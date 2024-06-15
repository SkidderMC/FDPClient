package net.ccbluex.liquidbounce.features.module.modules.player.nofalls.other

import me.zywl.fdpclient.event.PacketEvent
import me.zywl.fdpclient.event.Render2DEvent
import me.zywl.fdpclient.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofalls.NoFallMode
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.BlinkUtils
import net.ccbluex.liquidbounce.utils.misc.FallingPlayer
import me.zywl.fdpclient.value.impl.BoolValue
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.network.play.client.C03PacketPlayer

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
        if (!enabled) return
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
