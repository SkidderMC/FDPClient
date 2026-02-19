/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.KeyStateEvent
import net.ccbluex.liquidbounce.event.ScreenEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import org.lwjgl.input.Keyboard

/**
 * InstantStop module - Instantly stops player movement by briefly pressing the opposite direction key
 *
 * When a movement key is released, the module simulates pressing the opposite direction key
 * for a configurable duration to counteract momentum and stop instantly.
 *
 * @author itsakc-me
 */
object InstantStop : Module("InstantStop", Category.MOVEMENT, Category.SubCategory.MOVEMENT_EXTRAS) {

    private val duration by int("Duration", 1, 1..5, " ticks")
    private val onlyOnGround by boolean("OnlyOnGround", true)
    private val horizontal by boolean("Horizontal", true)
    private val vertical by boolean("Vertical", true)

    // Track which opposite keys need to be held and for how long
    private var holdLeftTicks = 0
    private var holdRightTicks = 0
    private var holdForwardTicks = 0
    private var holdBackTicks = 0

    // Track physical key states
    private var leftPhysicallyPressed = false
    private var rightPhysicallyPressed = false
    private var forwardPhysicallyPressed = false
    private var backPhysicallyPressed = false

    override fun onEnable() {
        syncPhysicalKeyStates()
        resetHoldTicks()
    }

    override fun onDisable() {
        resetHoldTicks()
        restoreVanillaKeyStates()
    }

    private fun resetHoldTicks() {
        holdLeftTicks = 0
        holdRightTicks = 0
        holdForwardTicks = 0
        holdBackTicks = 0
    }

    fun isTicksOnGoing() = holdLeftTicks > 0 || holdRightTicks > 0 || holdForwardTicks > 0 || holdBackTicks > 0

    val onKeyState = handler<KeyStateEvent> { event ->
        mc.thePlayer ?: return@handler

        val left = mc.gameSettings.keyBindLeft
        val right = mc.gameSettings.keyBindRight
        val fwd = mc.gameSettings.keyBindForward
        val back = mc.gameSettings.keyBindBack

        // Detect key releases and trigger opposite key press
        when (event.key) {
            left.keyCode -> {
                if (event.pressed) {
                    leftPhysicallyPressed = true
                } else {
                    leftPhysicallyPressed = false
                    // When left is released, press right to stop
                    if (horizontal && shouldTrigger() && !rightPhysicallyPressed) {
                        holdRightTicks = duration
                    }
                }
            }
            right.keyCode -> {
                if (event.pressed) {
                    rightPhysicallyPressed = true
                } else {
                    rightPhysicallyPressed = false
                    // When right is released, press left to stop
                    if (horizontal && shouldTrigger() && !leftPhysicallyPressed) {
                        holdLeftTicks = duration
                    }
                }
            }
            fwd.keyCode -> {
                if (event.pressed) {
                    forwardPhysicallyPressed = true
                } else {
                    forwardPhysicallyPressed = false
                    // When forward is released, press back to stop
                    if (vertical && shouldTrigger() && !backPhysicallyPressed) {
                        holdBackTicks = duration
                    }
                }
            }
            back.keyCode -> {
                if (event.pressed) {
                    backPhysicallyPressed = true
                } else {
                    backPhysicallyPressed = false
                    // When back is released, press forward to stop
                    if (vertical && shouldTrigger() && !forwardPhysicallyPressed) {
                        holdForwardTicks = duration
                    }
                }
            }
        }
    }

    val onScreenEvent = handler<ScreenEvent> {
        // Sync physical key states when GUI is opened/closed
        syncPhysicalKeyStates()
    }

    val onTick = handler<GameTickEvent> {
        if (mc.thePlayer == null) {
            resetHoldTicks()
            return@handler
        }

        if (mc.currentScreen != null) {
            resetHoldTicks()
            return@handler
        }

        val left = mc.gameSettings.keyBindLeft
        val right = mc.gameSettings.keyBindRight
        val fwd = mc.gameSettings.keyBindForward
        val back = mc.gameSettings.keyBindBack

        // Apply opposite key presses
        if (holdLeftTicks > 0) {
            left.pressed = true
            holdLeftTicks--
            if (holdLeftTicks == 0 && !leftPhysicallyPressed) {
                left.pressed = false
            }
        } else {
            left.pressed = leftPhysicallyPressed
        }

        if (holdRightTicks > 0) {
            right.pressed = true
            holdRightTicks--
            if (holdRightTicks == 0 && !rightPhysicallyPressed) {
                right.pressed = false
            }
        } else {
            right.pressed = rightPhysicallyPressed
        }

        if (holdForwardTicks > 0) {
            fwd.pressed = true
            holdForwardTicks--
            if (holdForwardTicks == 0 && !forwardPhysicallyPressed) {
                fwd.pressed = false
            }
        } else {
            fwd.pressed = forwardPhysicallyPressed
        }

        if (holdBackTicks > 0) {
            back.pressed = true
            holdBackTicks--
            if (holdBackTicks == 0 && !backPhysicallyPressed) {
                back.pressed = false
            }
        } else {
            back.pressed = backPhysicallyPressed
        }
    }

    private fun shouldTrigger(): Boolean {
        val player = mc.thePlayer ?: return false
        return !onlyOnGround || player.onGround
    }

    private fun syncPhysicalKeyStates() {
        val left = mc.gameSettings.keyBindLeft
        val right = mc.gameSettings.keyBindRight
        val fwd = mc.gameSettings.keyBindForward
        val back = mc.gameSettings.keyBindBack

        leftPhysicallyPressed = Keyboard.isKeyDown(left.keyCode)
        rightPhysicallyPressed = Keyboard.isKeyDown(right.keyCode)
        forwardPhysicallyPressed = Keyboard.isKeyDown(fwd.keyCode)
        backPhysicallyPressed = Keyboard.isKeyDown(back.keyCode)
    }

    private fun restoreVanillaKeyStates() {
        val left = mc.gameSettings.keyBindLeft
        val right = mc.gameSettings.keyBindRight
        val fwd = mc.gameSettings.keyBindForward
        val back = mc.gameSettings.keyBindBack

        left.pressed = Keyboard.isKeyDown(left.keyCode)
        right.pressed = Keyboard.isKeyDown(right.keyCode)
        fwd.pressed = Keyboard.isKeyDown(fwd.keyCode)
        back.pressed = Keyboard.isKeyDown(back.keyCode)
    }
}
