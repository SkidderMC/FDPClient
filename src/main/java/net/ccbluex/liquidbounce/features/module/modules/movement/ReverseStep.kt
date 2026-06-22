/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.extensions.isInLiquid
import net.ccbluex.liquidbounce.utils.extensions.isMoving

/**
 * @author opZywl
 */
object ReverseStep : Module("ReverseStep", Category.MOVEMENT, Category.SubCategory.MOVEMENT_MAIN, gameDetecting = false) {

    private val height by float("Height", 1f, 0.6f..10f)
        .describe("Downward speed applied when stepping down.")
    private val maxFallDistance by float("MaxFallDistance", 3f, 0f..20f)
        .describe("Skip reverse-step above this fall distance.")

    val onUpdate = handler<UpdateEvent> {
        val thePlayer = mc.thePlayer ?: return@handler

        if (thePlayer.onGround || thePlayer.motionY > 0.0)
            return@handler

        if (mc.gameSettings.keyBindJump.isKeyDown)
            return@handler

        if (thePlayer.isOnLadder || thePlayer.isInLiquid || thePlayer.isInWeb)
            return@handler

        if (!thePlayer.isMoving)
            return@handler

        if (thePlayer.fallDistance > maxFallDistance)
            return@handler

        thePlayer.motionY = -height.toDouble()
    }
}
