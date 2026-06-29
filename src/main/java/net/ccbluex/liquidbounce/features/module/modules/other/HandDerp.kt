/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.network.play.client.C0APacketAnimation

/** Cosmetic hand animation randomizer for the single-hand 1.8 renderer. */
object HandDerp : Module("HandDerp", Category.OTHER, Category.SubCategory.MISCELLANEOUS, subjective = true) {
    private val mode by choices("Mode", arrayOf("Delay", "Swing"), "Delay")
    private val delay by int("Delay", 2, 1..20, "ticks") { mode == "Delay" }
    private val silent by boolean("Silent", true)
        .describe("Animate only on the client without sending additional swing packets.")
    private var ticks = 0

    val onTick = handler<GameTickEvent> {
        val player = mc.thePlayer ?: return@handler
        if (mode != "Delay" || ++ticks < delay) return@handler
        ticks = 0
        if (silent) player.swingProgressInt = 0 else player.swingItem()
    }

    val onPacket = handler<PacketEvent> { event ->
        if (mode == "Swing" && event.packet is C0APacketAnimation) {
            mc.thePlayer?.swingProgressInt = 0
        }
    }

    override fun onDisable() {
        ticks = 0
    }
}
