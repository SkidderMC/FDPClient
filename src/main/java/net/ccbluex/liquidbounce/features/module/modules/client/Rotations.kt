/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.BoolValue
import net.ccbluex.liquidbounce.features.ListValue
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.minecraft.network.play.client.C03PacketPlayer

@ModuleInfo(name = "Rotations", description = "Rotation settings.", category = ModuleCategory.CLIENT, canEnable = false, array = false)
object Rotations : Module() {
    val headValue = BoolValue("Head", true)
    val bodyValue = BoolValue("Body", true)
    val fixedValue = ListValue("SensitivityFixed", arrayOf("None", "Old", "New"), "New")
    val nanValue = BoolValue("NaNCheck", true)

    private var playerYaw: Float? = null

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (RotationUtils.serverRotation != null && !bodyValue.get() && headValue.get())
            mc.thePlayer?.rotationYawHead = RotationUtils.serverRotation.yaw
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val thePlayer = mc.thePlayer ?: return

        val packet = event.packet

        if (packet is C03PacketPlayer.C06PacketPlayerPosLook || packet is C03PacketPlayer.C05PacketPlayerLook) {
            val packetPlayer = packet as C03PacketPlayer

            playerYaw = packetPlayer.yaw
            if (bodyValue.get())
                thePlayer.renderYawOffset = packetPlayer.yaw
            if (headValue.get())
                thePlayer.rotationYawHead = packetPlayer.yaw
        } else {
            if (playerYaw != null && bodyValue.get())
                thePlayer.renderYawOffset = playerYaw!!
            if (headValue.get())
                thePlayer.rotationYawHead = thePlayer.renderYawOffset
        }
    }

    //always handle event
    override fun handleEvents() = true
}
