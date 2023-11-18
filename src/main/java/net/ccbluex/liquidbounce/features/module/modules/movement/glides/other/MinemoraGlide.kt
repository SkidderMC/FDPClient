package net.ccbluex.liquidbounce.features.module.modules.movement.glides.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.glides.GlideMode
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue

class MinemoraGlide : GlideMode("Minemora") {
    private val modeValue = ListValue("${valuePrefix}Mode", arrayOf("Glide1", "Glide2"), "Glide1")
    private val glide2TickValue = IntegerValue("${valuePrefix}Glide2-Tick", 4 , 2,8).displayable { modeValue.equals("Glide2") }
    private var glide2tick = 0
    private var glide3tick = 0
    override fun onEnable() {
        glide2tick = 0
        glide3tick = 0
    }
    override fun onUpdate(event: UpdateEvent) {
        when(modeValue.get()) {
            "Glide1" -> mc.thePlayer.motionY = -0.0784000015258789

            "Glide2" -> {
                glide2tick++
                mc.thePlayer.motionY = -0.0784000015258789
                if(glide2tick>=glide2TickValue.get()) {
                    glide2tick = 0
                    mc.thePlayer.motionY = 0.04
                }
            }
        }
    }
}
