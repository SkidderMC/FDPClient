package net.ccbluex.liquidbounce.features.module.modules.movement.glides.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.glides.GlideMode
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import kotlin.math.*

class MinemoraGlide : GlideMode("Minemora") {
    private val modeValue = ListValue("${valuePrefix}Mode", arrayOf("Glide1", "Glide2", "Glide3"), "Glide1")
    private val glide2TickValue = IntegerValue("Glide2-Tick", 4 , 2,8).displayable { modeValue.equals("Glide2") }
    private val glide3TickValue = IntegerValue("Glide3-Tick", 4 , 2,8).displayable { modeValue.equals("Glide3") }
    private val glide3BoostSpeed = FloatValue("Glide3-BoostSpeed", 0.1f, 0.0f,0.5f).displayable { modeValue.equals("Glide3") }
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

            "Glide3" -> {
                glide3tick++
                mc.thePlayer.motionY = -0.1
                if(glide3tick>=glide3TickValue.get()) {
                    glide3tick = 0
                    mc.thePlayer.motionY = 0.02
                    val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
                    mc.thePlayer.motionX = (-sin(yaw) * glide3BoostSpeed.get())
                    mc.thePlayer.motionZ = (cos(yaw) * glide3BoostSpeed.get())
                }
            }
        }
    }
}
