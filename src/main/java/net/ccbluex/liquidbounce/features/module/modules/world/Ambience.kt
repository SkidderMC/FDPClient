package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.server.S03PacketTimeUpdate

@ModuleInfo(name = "Ambience", description = "Change the World Time.", category = ModuleCategory.WORLD)
class Ambience : Module() {
    private val timeModeValue = ListValue("TimeMode", arrayOf("None","Normal", "Custom"), "Normal")
    private val weatherModeValue = ListValue("WeatherMode", arrayOf("None","Sunny","Rainy","Thunder"), "Normal")
    private val customWorldTimeValue = IntegerValue("CustomTime", 1000, 0, 24000)
    private val changeWorldTimeSpeedValue = IntegerValue("ChangeWorldTimeSpeed", 150, 10, 500)
    private val weatherStrengthValue = FloatValue("WeatherStrength", 1f, 0f, 1f)

    var i = 0L

    override fun onDisable() {
        i = 0
    }

    @EventTarget
    fun onUpdate(event : UpdateEvent) {
        when (timeModeValue.get().toLowerCase()) {
            "normal" -> {
                if (i < 24000)
                    i += changeWorldTimeSpeedValue.get()
                else
                    i = 0
                mc.theWorld.worldTime = i
            }
            "custom" -> {
                mc.theWorld.worldTime = customWorldTimeValue.get().toLong()
            }
        }

        when(weatherModeValue.get().toLowerCase()){
            "sunny" -> {
                mc.theWorld.setRainStrength(0f)
                mc.theWorld.setThunderStrength(0f)
            }
            "rainy" -> {
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
    fun onPacket(event: PacketEvent){
        if(!timeModeValue.get().equals("none",true)&&event.packet is S03PacketTimeUpdate){
            event.cancelEvent()
        }
        // TODO: what is the change weather packet
    }

    override val tag: String
        get() = timeModeValue.get()
}