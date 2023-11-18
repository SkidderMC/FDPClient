/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.flys.ncp

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook

class BlockdropFly : FlyMode("BlockDrop") {
    private val hSpeedValue = FloatValue("${valuePrefix}HorizontalSpeed", 1f, 0.1f, 5f)
    private val vSpeedValue = FloatValue("${valuePrefix}VerticalSpeed", 1f, 0.1f, 5f)
    private var startx = 0.0
    private var starty = 0.0
    private var startz = 0.0
    private var startyaw = 0f
    private var startpitch = 0f
    
    override fun onEnable() {
        startx = mc.thePlayer.posX
        starty = mc.thePlayer.posY
        startz = mc.thePlayer.posZ
        startyaw = mc.thePlayer.rotationYaw
        startpitch = mc.thePlayer.rotationPitch
    }
        

    override fun onUpdate(event: UpdateEvent) {
        MovementUtils.resetMotion(true)
        if (mc.gameSettings.keyBindJump.isKeyDown) mc.thePlayer.motionY = vSpeedValue.get().toDouble()
        if (mc.gameSettings.keyBindSneak.isKeyDown) mc.thePlayer.motionY -= vSpeedValue.get().toDouble()
        MovementUtils.strafe(hSpeedValue.get())
        
        repeat(2) {
            PacketUtils.sendPacketNoEvent(
                C03PacketPlayer.C06PacketPlayerPosLook(
                    startx,
                    starty,
                    startz,
                    startyaw,
                    startpitch,
                    true
                )
            )
        }
        repeat(2) {
            PacketUtils.sendPacketNoEvent(
                C03PacketPlayer.C06PacketPlayerPosLook(
                    mc.thePlayer.posX,
                    mc.thePlayer.posY,
                    mc.thePlayer.posZ,
                    startyaw,
                    startpitch,
                    false
                )
            )
        }
    }
    
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C03PacketPlayer) {
            event.cancelEvent()
        }
        if (packet is S08PacketPlayerPosLook) {
            startx = packet.x
            starty = packet.y
            startz = packet.z
            startyaw = packet.getYaw()
            startpitch = packet.getPitch()
            event.cancelEvent()
        }
    }
}
