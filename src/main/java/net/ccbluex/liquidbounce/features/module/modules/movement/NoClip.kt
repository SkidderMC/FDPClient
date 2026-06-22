/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.strafe

object NoClip : Module("NoClip", Category.MOVEMENT, Category.SubCategory.MOVEMENT_MAIN) {
    val speed by float("Speed", 0.5f, 0f..10f)
        .describe("Horizontal movement speed while clipping through blocks.")
    private val verticalSpeed by float("VerticalSpeed", -1f, -1f..10f)
        .describe("Vertical speed for jump/sneak; -1 matches horizontal speed.")
    private val onlyInVehicle by boolean("OnlyInVehicle", false)
        .describe("Only clip through blocks while riding a vehicle.")
    private val onlyOnGround by boolean("OnlyOnGround", false)
        .describe("Only clip through blocks while on the ground.")
    private val notWhileUsingItem by boolean("NotWhileUsingItem", false)
        .describe("Disable clipping while using an item.")

    override fun onDisable() {
        mc.thePlayer?.noClip = false
    }

    val onMove = handler<MoveEvent> { event ->
        val thePlayer = mc.thePlayer ?: return@handler

        if (onlyInVehicle && thePlayer.ridingEntity == null) {
            thePlayer.noClip = false
            return@handler
        }

        if (onlyOnGround && !thePlayer.onGround) {
            thePlayer.noClip = false
            return@handler
        }

        if (notWhileUsingItem && thePlayer.isUsingItem) {
            thePlayer.noClip = false
            return@handler
        }

        strafe(speed, stopWhenNoInput = true, event)

        thePlayer.noClip = true
        thePlayer.onGround = false

        thePlayer.capabilities.isFlying = false

        val verticalAmount = if (verticalSpeed >= 0f) verticalSpeed else speed

        var ySpeed = 0.0

        if (mc.gameSettings.keyBindJump.isKeyDown)
            ySpeed += verticalAmount

        if (mc.gameSettings.keyBindSneak.isKeyDown)
            ySpeed -= verticalAmount

        thePlayer.motionY = ySpeed
        event.y = ySpeed
    }
}
