package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue

@ModuleInfo(name = "AntiAim", category = ModuleCategory.PLAYER)
class AntiAim : Module() {
    private val yawMode=ListValue("YawMove", arrayOf("Jitter","Spin"),"Spin")
    private val pitchMode=ListValue("PitchMode", arrayOf("Down","Up","Jitter"),"Down")
    private val rotateValue=BoolValue("SlientRotate",true)

    private var yaw = 0f
    private var pitch = 0f

    @EventTarget
    fun onUpdate(event: UpdateEvent){
        if(mc.thePlayer.isSwingInProgress||mc.thePlayer.isUsingItem)
            return

        when (yawMode.get().toLowerCase()) {
            "spin" -> {
                yaw += 20.0f
                if (yaw > 180.0f) {
                    yaw= -180.0f

                }else if (yaw < -180.0f){
                    yaw = 180.0f
                }
            }
            "jitter" -> {
                yaw = if (mc.thePlayer.ticksExisted % 2 === 0) 90F else -90F
            }
        }

        when (pitchMode.get().toLowerCase()) {
            "up" -> {
                pitch = -90.0f
            }
            "down" -> {
                pitch = 90.0f
            }
            "jitter" -> {
                pitch += 30.0f
                if (pitch > 90.0f) {
                    pitch = -90.0f
                }else if (pitch < -90.0f) {
                    pitch = 90.0f
                }
            }
        }

        if(rotateValue.get()){
            RotationUtils.setTargetRotation(Rotation(yaw, pitch))
        }else{
            mc.thePlayer.rotationYaw=yaw
            mc.thePlayer.rotationPitch=pitch
        }
    }
}