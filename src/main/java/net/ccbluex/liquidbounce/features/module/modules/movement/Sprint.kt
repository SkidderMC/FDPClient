/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.combat.SuperKnockback
import net.ccbluex.liquidbounce.features.module.modules.player.scaffolds.Scaffold
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.extensions.setSprintSafely
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverOpenInventory
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.activeSettings
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.currentRotation
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.potion.Potion
import net.minecraft.util.MovementInput
import kotlin.math.abs

object Sprint : Module("Sprint", Category.MOVEMENT, Category.SubCategory.MOVEMENT_MAIN, gameDetecting = false) {
    val mode by choices("Mode", arrayOf("Legit", "Vanilla"), "Vanilla")

    val onlyOnSprintPress by boolean("OnlyOnSprintPress", false)
    private val alwaysCorrect by boolean("AlwaysCorrectSprint", false)

    val allDirections by boolean("AllDirections", true) { mode == "Vanilla" }
    val jumpDirections by boolean("JumpDirections", false) { mode == "Vanilla" && allDirections }

    private val allDirectionsLimitSpeed by float("AllDirectionsLimitSpeed", 1f, 0.75f..1f)
    { mode == "Vanilla" && allDirections }
    private val allDirectionsLimitSpeedGround by boolean("AllDirectionsLimitSpeedOnlyGround", true)
    { mode == "Vanilla" && allDirections }

    private val blindness by boolean("Blindness", true) { mode == "Vanilla" }
    private val usingItem by boolean("UsingItem", false) { mode == "Vanilla" }
    private val inventory by boolean("Inventory", false) { mode == "Vanilla" }
    private val food by boolean("Food", true) { mode == "Vanilla" }

    private val checkServerSide by boolean("CheckServerSide", false) { mode == "Vanilla" }
    private val checkServerSideGround by boolean("CheckServerSideOnlyGround", false)
    { mode == "Vanilla" && checkServerSide }
    private val noPackets by boolean("NoPackets", false) { mode == "Vanilla" }

    private var isSprinting = false

    override val tag
        get() = mode

    fun correctSprintState(movementInput: MovementInput, isUsingItem: Boolean) {
        val player = mc.thePlayer ?: return

        if (SuperKnockback.breakSprint()) {
            player setSprintSafely false
            return
        }

        if (!handleEvents() || onlyOnSprintPress && !player.isSprinting && !mc.gameSettings.keyBindSprint.isKeyDown && !SuperKnockback.startSprint() && !isSprinting)
            return

        if (Scaffold.handleEvents()) {
            if (!Scaffold.sprint) {
                player setSprintSafely false
                isSprinting = false
                return
            } else if (Scaffold.sprint && Scaffold.eagle == "Normal" && player.isMoving && player.onGround && Scaffold.eagleSneaking && Scaffold.eagleSprint) {
                player setSprintSafely true
                isSprinting = true
                return
            }
        }

        if (handleEvents() || alwaysCorrect) {
            player setSprintSafely !shouldStopSprinting(movementInput, isUsingItem)
            isSprinting = player.isSprinting

            if (player.isSprinting && allDirections && mode != "Legit") {
                if (!allDirectionsLimitSpeedGround || player.onGround) {
                    player.motionX *= allDirectionsLimitSpeed
                    player.motionZ *= allDirectionsLimitSpeed
                }
            }
        }
    }

    private fun shouldStopSprinting(movementInput: MovementInput, isUsingItem: Boolean): Boolean {
        val player = mc.thePlayer ?: return false

        val isLegitModeActive = mode == "Legit"

        val modifiedForward = if (currentRotation != null && activeSettings?.strict == true) {
            player.movementInput.moveForward
        } else {
            movementInput.moveForward
        }

        if (!player.isMoving) {
            return true
        }

        if (player.isCollidedHorizontally) {
            return true
        }

        if ((blindness || isLegitModeActive) && player.isPotionActive(Potion.blindness) && !player.isSprinting) {
            return true
        }

        if ((food || isLegitModeActive) && !(player.foodStats.foodLevel > 6f || player.capabilities.allowFlying)) {
            return true
        }

        if ((usingItem || isLegitModeActive) && !NoSlow.handleEvents() && isUsingItem) {
            return true
        }

        if ((inventory || isLegitModeActive) && serverOpenInventory) {
            return true
        }

        if (isLegitModeActive) {
            return modifiedForward < 0.8
        }

        if (allDirections) {
            return false
        }

        val threshold = if ((!usingItem || NoSlow.handleEvents()) && isUsingItem) 0.2 else 0.8
        val playerForwardInput = player.movementInput.moveForward

        if (!checkServerSide) {
            return if (currentRotation != null) {
                abs(playerForwardInput) < threshold || playerForwardInput < 0 && modifiedForward < threshold
            } else {
                playerForwardInput < threshold
            }
        }

        if (checkServerSideGround && !player.onGround) {
            return currentRotation == null && modifiedForward < threshold
        }

        return modifiedForward < threshold
    }

    val onPacket = handler<PacketEvent> { event ->
        if (mode == "Legit") {
            return@handler
        }

        val packet = event.packet
        if (packet !is C0BPacketEntityAction || !noPackets || event.isCancelled) {
            return@handler
        }
        if (packet.action == C0BPacketEntityAction.Action.STOP_SPRINTING || packet.action == C0BPacketEntityAction.Action.START_SPRINTING) {
            event.cancelEvent()
        }
    }
}