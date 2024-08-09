/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.server.S03PacketTimeUpdate
import net.minecraft.network.play.server.S2BPacketChangeGameState

object Ambience : Module("Ambience", Category.VISUAL, gameDetecting = false, hideModule = false) {

    private val timeMode by ListValue("Mode", arrayOf("None", "Normal", "Custom", "Day", "Dusk", "Night", "Dynamic"), "Custom")
        private val customWorldTime by IntegerValue("Time", 6, 0..24) { timeMode == "Custom" }
        private val changeWorldTimeSpeed by IntegerValue("TimeSpeed", 150, 10..500) { timeMode == "Normal" }
        private val dynamicSpeed by IntegerValue("DynamicSpeed", 20, 1.. 50) { timeMode =="Dynamic" }

    private val weatherMode by ListValue("WeatherMode", arrayOf("None", "Sun", "Rain", "Thunder"), "None")
        private val weatherStrength by FloatValue("WeatherStrength", 1f, 0f..1f)
            { weatherMode == "Rain" || weatherMode == "Thunder" }

    // world color
    val worldColor by BoolValue("WorldColor", false)
    val worldColorRed by IntegerValue("WorldRed", 255, 0..255) { worldColor }
    val worldColorGreen by IntegerValue("WorldGreen", 255, 0..255) { worldColor }
    val worldColorBlue by IntegerValue("WorldBlue", 255, 0.. 255) { worldColor }

    private var i = 0L

    override fun onDisable() {
        i = 0
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
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

    @EventTarget
    fun onPacket(event: PacketEvent) {
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