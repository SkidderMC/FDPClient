/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.RotationSetEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.rotation.Rotation
import net.ccbluex.liquidbounce.utils.extensions.prevRotation
import net.ccbluex.liquidbounce.utils.extensions.rotation

object FreeLook : Module("FreeLook", Category.VISUAL) {

    // The module's rotations
    private var currRotation = Rotation.ZERO
    private var prevRotation = currRotation

    // The player's rotations
    private var savedCurrRotation = Rotation.ZERO
    private var savedPrevRotation = Rotation.ZERO

    private var modifySavedRotations = true

    override fun onEnable() {
        mc.thePlayer?.run {
            currRotation = rotation
            prevRotation = prevRotation
        }
    }

    val onRotationSet = handler<RotationSetEvent> { event ->
        if (mc.gameSettings.thirdPersonView != 0) {
            event.cancelEvent()
        } else {
            currRotation = mc.thePlayer.rotation
            prevRotation = currRotation
        }

        prevRotation = currRotation
        currRotation += Rotation(event.yawDiff, -event.pitchDiff)

        currRotation.withLimitedPitch()
    }

    fun useModifiedRotation() {
        val player = mc.thePlayer ?: return

        if (mc.gameSettings.thirdPersonView == 0)
            return

        if (modifySavedRotations) {
            savedCurrRotation = player.rotation
            savedPrevRotation = player.prevRotation
        }

        if (!handleEvents())
            return

        player.rotation = currRotation
        player.prevRotation = prevRotation
    }

    fun restoreOriginalRotation() {
        val player = mc.thePlayer ?: return

        if (!handleEvents() || mc.gameSettings.thirdPersonView == 0)
            return

        player.rotation = savedCurrRotation
        player.prevRotation = savedPrevRotation
    }

    fun runWithoutSavingRotations(f: () -> Unit) {
        modifySavedRotations = false

        try {
            f()
        } catch (_: Exception) {
        }

        modifySavedRotations = true
    }
}