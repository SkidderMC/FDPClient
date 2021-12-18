package net.ccbluex.liquidbounce.features.module.modules.movement.flys.hypixel

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacketNoEvent
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition


class HypixelGlide : FlyMode("HypixelGlide") {
    private val hypixelGlideCustom = BoolValue("HypixelGlide-Custom", false)
    private val hypixelGlideDelay = IntegerValue("HypixelGlide-DelayTick", 25, 1, 50)
    private val hypixelGlideForward = FloatValue("HypixelGlide-Forward", 7.9f, 0f, 10f)
    private val hypixelGlideDown = FloatValue("HypixelGlide-Down", 1.75f, 0f, 5f)


    private fun doMove(h: Double, v: Double) {
        if (mc.thePlayer == null) return
        val x = mc.thePlayer.posX
        val y = mc.thePlayer.posY
        val z = mc.thePlayer.posZ
        val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
        val expectedX = x + -Math.sin(yaw) * h
        val expectedY = y + v
        val expectedZ = z + Math.cos(yaw) * h
        sendPacketNoEvent(C04PacketPlayerPosition(expectedX, expectedY, expectedZ, mc.thePlayer.onGround))
        mc.thePlayer.setPosition(expectedX, expectedY, expectedZ)
    }

    @EventTarget
    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.motionY = 0.0
        if (mc.thePlayer.ticksExisted % (if (hypixelGlideCustom.get()) hypixelGlideDelay.get() else 25) == 0) doMove(if (hypixelGlideCustom.get()) hypixelGlideForward.get().toDouble() else 7.9, if (hypixelGlideCustom.get()) (-hypixelGlideDown.get()).toDouble() else -1.75)

    }

}
