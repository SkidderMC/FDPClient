/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.minecraft.network.play.client.C03PacketPlayer

@ModuleInfo(name = "Freeze", description = "Allows you to stay stuck in mid air.", category = ModuleCategory.MOVEMENT)
class Freeze : Module() {
    private var motionX = 0.0
    private var motionY = 0.0
    private var motionZ = 0.0
    private var x = 0.0
    private var y = 0.0
    private var z = 0.0

    override fun onEnable() {
        x=mc.thePlayer.posX
        y=mc.thePlayer.posY
        z=mc.thePlayer.posZ
        motionX=mc.thePlayer.motionX
        motionY=mc.thePlayer.motionY
        motionZ=mc.thePlayer.motionZ
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionY = 0.0
        mc.thePlayer.motionZ = 0.0
        mc.thePlayer.setPositionAndRotation(x,y,z, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
    }

    @EventTarget
    fun onPacket(event: PacketEvent){
        if(event.packet is C03PacketPlayer){
            event.cancelEvent()
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent){
        state=false
    }

    override fun onDisable() {
        mc.thePlayer.motionX=motionX
        mc.thePlayer.motionY=motionY
        mc.thePlayer.motionZ=motionZ
        mc.thePlayer.setPositionAndRotation(x,y,z, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
    }
}
