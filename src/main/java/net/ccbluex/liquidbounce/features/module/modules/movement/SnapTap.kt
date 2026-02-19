/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.KeyStateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import org.lwjgl.input.Keyboard

/**
 * SnapTap module - Razer-style SOCD (Simultaneous Opposing Cardinal Directions) handling
 *
 * When opposing keys (A+D or W+S) are pressed simultaneously, only the most recently
 * pressed key is registered. When the newer key is released, the older key resumes if still held.
 *
 * @author itsakc-me
 */
object SnapTap : Module("SnapTap", Category.MOVEMENT, Category.SubCategory.MOVEMENT_EXTRAS) {

    private val mode by choices("Mode", arrayOf("LastInput", "Neutral"), "LastInput")

    /**
     * Axis state tracks opposing keys on an axis (e.g., left/right or forward/back)
     */
    private class AxisState {
        var negPressed = false
        var posPressed = false
        var negLastPressedNs = 0L
        var posLastPressedNs = 0L

        fun onPress(positive: Boolean) {
            if (positive) {
                posPressed = true
                posLastPressedNs = System.nanoTime()
            } else {
                negPressed = true
                negLastPressedNs = System.nanoTime()
            }
        }

        fun onRelease(positive: Boolean) {
            if (positive) posPressed = false else negPressed = false
        }

        fun reset() {
            negPressed = false
            posPressed = false
            negLastPressedNs = 0L
            posLastPressedNs = 0L
        }
    }

    private data class Resolved(val negative: Boolean, val positive: Boolean)

    // Horizontal axis: Left (negative) / Right (positive)
    private val horiz = AxisState()
    // Vertical axis: Back (negative) / Forward (positive)
    private val vert = AxisState()

    override fun onEnable() {
        syncAxisFromActual()
    }

    override fun onDisable() {
        restoreVanilla()
        horiz.reset()
        vert.reset()
    }

    val onKeyState = handler<KeyStateEvent> { event ->
        mc.thePlayer ?: return@handler

        // Don't work when [InstantStop] ticks is ongoing to avoid conflicts
        if (InstantStop.isTicksOnGoing()) return@handler

        val left = mc.gameSettings.keyBindLeft
        val right = mc.gameSettings.keyBindRight
        val fwd = mc.gameSettings.keyBindForward
        val back = mc.gameSettings.keyBindBack

        when (event.key) {
            left.keyCode -> if (event.pressed) horiz.onPress(false) else horiz.onRelease(false)
            right.keyCode -> if (event.pressed) horiz.onPress(true) else horiz.onRelease(true)
            back.keyCode -> if (event.pressed) vert.onPress(false) else vert.onRelease(false)
            fwd.keyCode -> if (event.pressed) vert.onPress(true) else vert.onRelease(true)
        }

        resolveAndApply()
    }

    val onTick = handler<GameTickEvent> {
        mc.thePlayer ?: return@handler

        // Don't work when [InstantStop] ticks is ongoing to avoid conflicts
        if (InstantStop.isTicksOnGoing()) return@handler

        // If in a GUI, disable all movement keys
        if (mc.currentScreen != null) {
            val left = mc.gameSettings.keyBindLeft
            val right = mc.gameSettings.keyBindRight
            val fwd = mc.gameSettings.keyBindForward
            val back = mc.gameSettings.keyBindBack

            left.pressed = false
            right.pressed = false
            fwd.pressed = false
            back.pressed = false
            return@handler
        }

        syncAxisFromActual()
        resolveAndApply()
    }

    private fun resolveAndApply() {
        val left = mc.gameSettings.keyBindLeft
        val right = mc.gameSettings.keyBindRight
        val fwd = mc.gameSettings.keyBindForward
        val back = mc.gameSettings.keyBindBack

        val rH = resolveAxis(horiz)
        left.pressed = rH.negative
        right.pressed = rH.positive

        val rV = resolveAxis(vert)
        back.pressed = rV.negative
        fwd.pressed = rV.positive
    }

    private fun resolveAxis(axis: AxisState): Resolved {
        return if (axis.negPressed && axis.posPressed) {
            when (mode) {
                "LastInput" -> {
                    val posIsLast = axis.posLastPressedNs >= axis.negLastPressedNs
                    Resolved(negative = !posIsLast, positive = posIsLast)
                }
                "Neutral" -> Resolved(negative = false, positive = false)
                else -> Resolved(negative = false, positive = false)
            }
        } else if (axis.negPressed || axis.posPressed) {
            Resolved(negative = axis.negPressed, positive = axis.posPressed)
        } else {
            Resolved(negative = false, positive = false)
        }
    }

    private fun restoreVanilla() {
        syncAxisFromActual()

        val left = mc.gameSettings.keyBindLeft
        val right = mc.gameSettings.keyBindRight
        val fwd = mc.gameSettings.keyBindForward
        val back = mc.gameSettings.keyBindBack

        left.pressed = horiz.negPressed
        right.pressed = horiz.posPressed
        back.pressed = vert.negPressed
        fwd.pressed = vert.posPressed
    }

    private fun syncAxisFromActual() {
        val left = mc.gameSettings.keyBindLeft
        val right = mc.gameSettings.keyBindRight
        val fwd = mc.gameSettings.keyBindForward
        val back = mc.gameSettings.keyBindBack

        val leftDown = Keyboard.isKeyDown(left.keyCode)
        val rightDown = Keyboard.isKeyDown(right.keyCode)
        val fwdDown = Keyboard.isKeyDown(fwd.keyCode)
        val backDown = Keyboard.isKeyDown(back.keyCode)

        if (leftDown != horiz.negPressed) {
            if (leftDown) horiz.onPress(false) else horiz.onRelease(false)
        }
        if (rightDown != horiz.posPressed) {
            if (rightDown) horiz.onPress(true) else horiz.onRelease(true)
        }
        if (backDown != vert.negPressed) {
            if (backDown) vert.onPress(false) else vert.onRelease(false)
        }
        if (fwdDown != vert.posPressed) {
            if (fwdDown) vert.onPress(true) else vert.onRelease(true)
        }
    }
}
