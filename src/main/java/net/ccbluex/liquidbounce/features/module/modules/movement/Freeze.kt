/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo

@ModuleInfo(name = "Freeze", description = "Allows you to stay stuck in mid air.", category = ModuleCategory.MOVEMENT)
class Freeze : Module() {
    private var x = 0.0
    private var y = 0.0
    private var z = 0.0

    override fun onEnable() {
        x=mc.thePlayer.posX
        y=mc.thePlayer.posY
        z=mc.thePlayer.posZ
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
        val packet=event.packet.javaClass.simpleName
        if (!(packet.contains("keepalive",ignoreCase = true)||packet.startsWith("S")
                    ||packet.contains("chat",ignoreCase = true))) event.cancelEvent()
    }

    override fun onDisable() {
        mc.thePlayer?.isDead = false
    }
}
