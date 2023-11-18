/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.aac

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

class AACGroundSpeed : SpeedMode("AACGround") {
    private val timerValue = FloatValue("${valuePrefix}Timer", 3f, 1.1f, 10f)
    private val speedValue = FloatValue("${valuePrefix}GroundSpeed", 0.25f, 0.00f, 0.36f)
    private val extraValue = BoolValue("${valuePrefix}ExtraPacket", true)

    override fun onUpdate() {
        if (!MovementUtils.isMoving() || !mc.thePlayer.onGround) return
        mc.timer.timerSpeed = timerValue.get()
        MovementUtils.strafe(speedValue.get())
        if (extraValue.get()) mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }
}
