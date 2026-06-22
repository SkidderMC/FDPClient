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

/**
 * Anchor module - Locks the player to the X/Z position captured when enabled.
 *
 * Zeroes horizontal motion every tick and snaps the player back to the anchored
 * X/Z, holding a fixed spot. Vertical movement can stay enabled so gravity and
 * jumping still work.
 */
object Anchor : Module("Anchor", Category.MOVEMENT, Category.SubCategory.MOVEMENT_EXTRAS, gameDetecting = false) {

    private val allowVertical by boolean("AllowVertical", true)
        .describe("Allow gravity and jumping while the anchor holds X and Z.")
    private val snapBack by boolean("SnapBack", true)
        .describe("Snap the player back to the captured X and Z each tick.")

    private var anchorX = 0.0
    private var anchorZ = 0.0

    override fun onEnable() {
        val player = mc.thePlayer ?: return
        anchorX = player.posX
        anchorZ = player.posZ
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        player.motionX = 0.0
        player.motionZ = 0.0

        if (!allowVertical)
            player.motionY = 0.0

        if (snapBack)
            player.setPosition(anchorX, player.posY, anchorZ)
    }
}
