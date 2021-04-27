/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.server.S08PacketPlayerPosLook

@ModuleInfo(name = "FreeCam", description = "Allows you to move out of your body.", category = ModuleCategory.RENDER)
class FreeCam : Module() {
    private val speedValue = FloatValue("Speed", 0.8f, 0.1f, 2f)
    private val flyValue = BoolValue("Fly", true)
    private val noClipValue = BoolValue("NoClip", true)
    private var fakePlayer: EntityOtherPlayerMP? = null
    private var oldX = 0.0
    private var oldY = 0.0
    private var oldZ = 0.0

    override fun onEnable() {
        if (mc.thePlayer == null) return
        oldX = mc.thePlayer.posX
        oldY = mc.thePlayer.posY
        oldZ = mc.thePlayer.posZ
        fakePlayer = EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.gameProfile)
        fakePlayer!!.clonePlayer(mc.thePlayer, true)
        fakePlayer!!.rotationYawHead = mc.thePlayer.rotationYawHead
        fakePlayer!!.copyLocationAndAnglesFrom(mc.thePlayer)
        mc.theWorld.addEntityToWorld(-(Math.random() * 10000).toInt(), fakePlayer)
        if (noClipValue.get()) mc.thePlayer.noClip = true
    }

    override fun onDisable() {
        if (mc.thePlayer == null || fakePlayer == null) return
        mc.thePlayer.setPositionAndRotation(oldX, oldY, oldZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
        mc.theWorld.removeEntityFromWorld(fakePlayer!!.entityId)
        fakePlayer = null
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionY = 0.0
        mc.thePlayer.motionZ = 0.0
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (noClipValue.get()) mc.thePlayer.noClip = true
        mc.thePlayer.fallDistance = 0f
        if (flyValue.get()) {
            val value = speedValue.get()
            mc.thePlayer.motionY = 0.0
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
            if (mc.gameSettings.keyBindJump.isKeyDown) mc.thePlayer.motionY += value.toDouble()
            if (mc.gameSettings.keyBindSneak.isKeyDown) mc.thePlayer.motionY -= value.toDouble()
            MovementUtils.strafe(value)
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C03PacketPlayer || packet is C0BPacketEntityAction)
            event.cancelEvent()

        if (packet is S08PacketPlayerPosLook) {
            oldX = packet.x
            oldY = packet.y
            oldZ = packet.z
            fakePlayer!!.setPosition(oldX, oldY, oldZ)
            event.cancelEvent()
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent?) {
        state = false
    }
}