/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.network.play.server.S03PacketTimeUpdate
import net.minecraft.network.play.server.S2BPacketChangeGameState

@ModuleInfo(name = "Ambience", category = ModuleCategory.WORLD)
class Ambience : Module() {
    private val timeModeValue = ListValue("TimeMode", arrayOf("None", "Normal", "Custom"), "Normal")
    private val weatherModeValue = ListValue("WeatherMode", arrayOf("None", "Sun", "Rain", "Thunder"), "None")
    private val customWorldTimeValue = IntegerValue("CustomTime", 1000, 0, 24000).displayable { timeModeValue.equals("Custom") }
    private val changeWorldTimeSpeedValue = IntegerValue("ChangeWorldTimeSpeed", 150, 10, 500).displayable { timeModeValue.equals("Normal") }
    private val weatherStrengthValue = FloatValue("WeatherStrength", 1f, 0f, 1f).displayable { !weatherModeValue.equals("None") }

    var i = 0L

    override fun onDisable() {
        i = 0
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        when (timeModeValue.get().lowercase()) {
            "normal" -> {
                if (i < 24000) {
                    i += changeWorldTimeSpeedValue.get()
                } else {
                    i = 0
                }
                mc.theWorld.worldTime = i
            }
            "custom" -> {
                mc.theWorld.worldTime = customWorldTimeValue.get().toLong()
            }
        }

        when (weatherModeValue.get().lowercase()) {
            "sun" -> {
                mc.theWorld.setRainStrength(0f)
                mc.theWorld.setThunderStrength(0f)
            }
            "rain" -> {
                mc.theWorld.setRainStrength(weatherStrengthValue.get())
                mc.theWorld.setThunderStrength(0f)
            }
            "thunder" -> {
                mc.theWorld.setRainStrength(weatherStrengthValue.get())
                mc.theWorld.setThunderStrength(weatherStrengthValue.get())
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (!timeModeValue.equals("none") && packet is S03PacketTimeUpdate) {
            event.cancelEvent()
        }

        if (!weatherModeValue.equals("none") && packet is S2BPacketChangeGameState) {
            if (packet.gameState in 7..8) { // change weather packet
                event.cancelEvent()
            }
        }
    }
}
