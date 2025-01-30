/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.config.*
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.minecraft.network.play.server.S03PacketTimeUpdate
import net.minecraft.network.play.server.S2BPacketChangeGameState
import java.awt.Color

object Ambience : Module("Ambience", Category.VISUAL, gameDetecting = false) {

    private val timeMode by choices("Mode", arrayOf("None", "Normal", "Custom", "Day", "Dusk", "Night", "Dynamic"), "Custom")
        private val customWorldTime by int("Time", 6, 0..24) { timeMode == "Custom" }
        private val changeWorldTimeSpeed by int("TimeSpeed", 150, 10..500) { timeMode == "Normal" }
        private val dynamicSpeed by int("DynamicSpeed", 20, 1.. 50) { timeMode =="Dynamic" }

    private val weatherMode by choices("WeatherMode", arrayOf("None", "Sun", "Rain", "Thunder"), "None")
        private val weatherStrength by FloatValue("WeatherStrength", 1f, 0f..1f)

    // world color
    val worldColor by boolean("WorldColor", false)
    val color by color("Color", Color(0, 90, 255))

    private var i = 0L

    override fun onDisable() {
        i = 0
    }


    val onUpdate = handler<UpdateEvent> {
        when (timeMode.lowercase()) {
            "normal" -> {
                i += changeWorldTimeSpeed
                i %= 24000
                mc.theWorld.worldTime = i
            }
            "custom" -> {
                mc.theWorld.worldTime = customWorldTime.toLong() * 1000
            }
            "day" -> {
                mc.theWorld.worldTime = 2000
            }
            "dusk" -> {
                mc.theWorld.worldTime = 13050
            }
            "night" -> {
                mc.theWorld.worldTime = 16000
            }
            "dynamic" -> {
                if (i < 24000) {
                    i += dynamicSpeed
                } else {
                    i = 0
                }
                mc.theWorld.worldTime = i
            }
        }

		val strength = weatherStrength.coerceIn(0F, 1F)

        when (weatherMode.lowercase()) {
            "sun" -> {
                mc.theWorld.setRainStrength(0f)
                mc.theWorld.setThunderStrength(0f)
            }
            "rain" -> {
                mc.theWorld.setRainStrength(strength)
                mc.theWorld.setThunderStrength(0f)
            }
            "thunder" -> {
                mc.theWorld.setRainStrength(strength)
                mc.theWorld.setThunderStrength(strength)
            }
        }
    }


    val onPacket = handler<PacketEvent> { event ->
        val packet = event.packet

        if (timeMode != "None" && packet is S03PacketTimeUpdate)
            event.cancelEvent()

        if (weatherMode != "None" && packet is S2BPacketChangeGameState) {
            if (packet.gameState in 7..8) { // change weather packet
                event.cancelEvent()
            }
        }
    }
}