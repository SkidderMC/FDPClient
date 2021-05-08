package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.BoolValue
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.RotationUtils

@ModuleInfo(name = "NoRotate", description = "Disable silent rotation", category = ModuleCategory.PLAYER)
class NoRotate : Module() {
    private val legit= BoolValue("Legit",false)

    @EventTarget
    fun onRender3d(event: UpdateEvent){
        if(legit.get()){
            val rotation=RotationUtils.targetRotation?:return
            mc.thePlayer.rotationYaw=rotation.yaw
            mc.thePlayer.rotationPitch=rotation.pitch
        }
        RotationUtils.reset()
    }
}