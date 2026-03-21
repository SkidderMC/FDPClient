package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.modules.player.NoFall.hypixelBlinkIndicator
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.client.BlinkUtils
import net.ccbluex.liquidbounce.utils.movement.FallingPlayer
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.network.play.client.C03PacketPlayer

object HypixelBlink : NoFallMode("HypixelBlink") {
    private var enabled = false
    private var wasOnGround = false

    override fun onEnable() {
        enabled = false
        wasOnGround = false
    }

    override fun onDisable() {
        if (enabled) {
            BlinkUtils.unblink()
        }

        enabled = false
        wasOnGround = false
    }

    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        if (player.onGround) {
            wasOnGround = true

            if (enabled) {
                BlinkUtils.unblink()
                enabled = false
            }
        } else if (wasOnGround) {
            wasOnGround = false

            if (player.motionY < 0 && FallingPlayer(player).findCollision(60) != null) {
                enabled = true
            }
        }
    }

    override fun onPacket(event: PacketEvent) {
        val player = mc.thePlayer ?: return

        if (!enabled) {
            return
        }

        if (event.packet is C03PacketPlayer) {
            event.packet.onGround = true
        }

        BlinkUtils.blink(event.packet, event)

        if (player.onGround && enabled) {
            BlinkUtils.unblink()
            enabled = false
        }
    }

    override fun onRender2D(event: Render2DEvent) {
        if (!enabled || !hypixelBlinkIndicator) {
            return
        }

        val scaledResolution = ScaledResolution(mc)
        Fonts.minecraftFont.drawStringWithShadow(
            "Blinking ${BlinkUtils.packets.size + BlinkUtils.packetsReceived.size}",
            scaledResolution.scaledWidth / 1.95f,
            (scaledResolution.scaledHeight / 2 + 20).toFloat(),
            -1
        )
    }
}
