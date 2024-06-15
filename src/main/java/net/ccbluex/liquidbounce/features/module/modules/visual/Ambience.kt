/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import me.zywl.fdpclient.event.EventTarget
import me.zywl.fdpclient.event.PacketEvent
import me.zywl.fdpclient.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import me.zywl.fdpclient.value.impl.BoolValue
import me.zywl.fdpclient.value.impl.FloatValue
import me.zywl.fdpclient.value.impl.IntegerValue
import me.zywl.fdpclient.value.impl.ListValue
import net.minecraft.network.play.server.S03PacketTimeUpdate
import net.minecraft.network.play.server.S2BPacketChangeGameState

@ModuleInfo(name = "Ambience", category = ModuleCategory.VISUAL)
object Ambience : Module() {

    private val timeModeValue = ListValue("TimeMode", arrayOf("Normal", "Custom", "Day", "Dusk", "Night", "Dynamic", "None"), "Custom")

    private val customWorldTimeValue = IntegerValue("CustomTime", 6, 0, 24).displayable { timeModeValue.equals("Custom") }
    private val changeWorldTimeSpeedValue = IntegerValue("ChangeWorldTimeSpeed", 150, 10, 500).displayable { timeModeValue.equals("Normal") }
    private val dynamicSpeed = IntegerValue("DynamicSpeed", 20, 1, 50).displayable { timeModeValue.equals("Dynamic") }

    private val weatherModeValue = ListValue("WeatherMode", arrayOf("None", "Sun", "Rain", "Thunder"), "None")
    private val weatherStrengthValue = FloatValue("WeatherStrength", 1f, 0f, 1f).displayable { !weatherModeValue.equals("None") }

    val worldColorValue = BoolValue("WorldColor", false)
    val worldColorRValue = IntegerValue("WorldRed", 255, 0, 255) { worldColorValue.get() }
    val worldColorGValue = IntegerValue("WorldGreen", 255, 0, 255) { worldColorValue.get() }
    val worldColorBValue = IntegerValue("WorldBlue", 255, 0, 255) { worldColorValue.get() }

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
                mc.theWorld.worldTime = customWorldTimeValue.get().toLong() * 1000
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
                    i += dynamicSpeed.get()
                } else {
                    i = 0
                }
                mc.theWorld.worldTime = i
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