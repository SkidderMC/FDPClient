/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.ccbluex.liquidbounce.value.float

object NoClip : Module("NoClip", Category.MOVEMENT, hideModule = false) {
    val speed by float("Speed", 0.5f, 0f..10f)

    override fun onDisable() {
        mc.thePlayer?.noClip = false
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        val thePlayer = mc.thePlayer ?: return

        strafe(speed, stopWhenNoInput = true, event)

        thePlayer.noClip = true
        thePlayer.onGround = false

        thePlayer.capabilities.isFlying = false

        var ySpeed = 0.0

        if (mc.gameSettings.keyBindJump.isKeyDown)
            ySpeed += speed

        if (mc.gameSettings.keyBindSneak.isKeyDown)
            ySpeed -= speed

        thePlayer.motionY = ySpeed
        event.y = ySpeed
    }
}
