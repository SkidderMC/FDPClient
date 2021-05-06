package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.IntegerValue
import net.ccbluex.liquidbounce.features.ListValue
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.minecraft.network.play.server.S03PacketTimeUpdate

@ModuleInfo(name = "Ambience", description = "Change the World Time.", category = ModuleCategory.WORLD)
class Ambience : Module() {
    private val modeValue = ListValue("Mode", arrayOf("Normal", "Custom"), "Normal")
    private val customWorldTimeValue = IntegerValue("CustomTime", 1000, 0, 24000)
    private val changeWorldTimeSpeedValue = IntegerValue("ChangeWorldTimeSpeed", 150, 10, 500)

    var i = 0L

    override fun onDisable() {
        i = 0
    }

    @EventTarget
    fun onUpdate(event : UpdateEvent) {
        when (modeValue.get()) {
            "Normal" -> {
                if (i < 24000)
                    i += changeWorldTimeSpeedValue.get()
                else
                    i = 0
                mc.theWorld.worldTime = i
            }
            "Custom" -> {
                mc.theWorld.worldTime = customWorldTimeValue.get().toLong()
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent){
        if(event.packet is S03PacketTimeUpdate){
//            i=event.packet.worldTime
            event.cancelEvent()
        }
    }

    override val tag: String
        get() = modeValue.get()
}