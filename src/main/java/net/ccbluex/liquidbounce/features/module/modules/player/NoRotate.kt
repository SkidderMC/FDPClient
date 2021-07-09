package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.network.play.client.C03PacketPlayer

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

    @EventTarget
    fun onPacket(event: PacketEvent){
        val packet=event.packet
        if(packet is C03PacketPlayer){
            packet.yaw=mc.thePlayer.rotationYaw
            packet.pitch=mc.thePlayer.rotationPitch
        }
    }
}