/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.other

import net.ccbluex.liquidbounce.config.BoolValue
import net.ccbluex.liquidbounce.config.FloatValue
import net.ccbluex.liquidbounce.config.IntValue
import net.ccbluex.liquidbounce.config.ListValue
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.extensions.tryJump
import kotlin.math.abs

object FastFall : SpeedMode("FastFall") {
    // **************** FastFall Boost Options ****************
    private val boostMode by ListValue("BoostMode", arrayOf("Simple", "Motion"), "Simple")
    // 移除 Number 模式及其对应 FloatValue
    private val boostSimple by FloatValue("BoostSimple", 2f, 1f..10f) { boostMode == "Simple" }
    private val MotionY by FloatValue("MotionNumber", 0.8f, 0.01f..10f) { boostMode == "Motion" }

    private val changeTimer by BoolValue("ChangeTimer", false)
    private val timers by IntValue("Times", 1, 1..2) { changeTimer }
    private val timer1Simple by FloatValue("Timer1Simple", 0.5f, 0.01f..2f) { timers >= 1 && changeTimer }
    private val timer1Ticks by IntValue("Timer1Ticks", 3, 1..20) { timers >= 1 && changeTimer }
    private val timer2Simple by FloatValue("Timer2Simple", 0.5f, 0.01f..150f) { timers >= 2 && changeTimer }
    private val timer2Ticks by IntValue("Timer2Ticks", 3, 1..20) { timers >= 2 && changeTimer }

    private val autoDisable by BoolValue("AutoDisable", false)
    // ********************************************************

    // State variables for boost & timer
    private var boosted = false
    private var tick1Started = false
    private var tick2Started = false
    private var timer1TickCount = 0
    private var timer2TickCount = 0
    private var changingTimer = false

    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        // --- FastFall jump logic ---
        if (player.onGround && player.isMoving) {
            player.tryJump()
        }
        player.isSprinting = player.movementInput.moveForward > 0.8f

        // --- FastFall boost logic ---
        // Check if player is falling
        val isFalling = player.motionY < 0 && !player.onGround && !player.isInWater && !player.isOnLadder

        if (isFalling && !boosted) {
            // Apply boost once (移除 "Number" 分支)
            when (boostMode) {
                "Simple" -> player.motionY *= boostSimple.toDouble()
                "Motion" -> player.motionY = -MotionY.toDouble()
            }
            boosted = true
            if (changeTimer && !changingTimer) {
                changingTimer = true
            }
        }

        // Timer handling (only when boosted and changeTimer is enabled)
        if (boosted && changeTimer && changingTimer) {
            // If we touched ground, reset everything
            if (player.onGround) {
                resetTimer()
                if (autoDisable) {
                    // Since we are in SpeedMode, we can't disable the module easily.
                    // Instead, just reset boost state.
                    // If you want to disable Speed module, you need to get the parent module.
                    // For now, just reset.
                }
                return
            }

            if (!tick1Started && !tick2Started) {
                // Start first timer cycle
                tick1Started = true
                mc.timer.timerSpeed = timer1Simple
                timer1TickCount++
            } else if (tick1Started && !tick2Started) {
                // First timer cycle active
                if (timer1TickCount >= timer1Ticks) {
                    // Move to second cycle if available
                    tick1Started = false
                    if (timers >= 2) {
                        tick2Started = true
                        mc.timer.timerSpeed = timer2Simple
                        timer2TickCount++
                    } else {
                        // Only one timer cycle, finish
                        changingTimer = false
                        resetTimer()
                    }
                } else {
                    mc.timer.timerSpeed = timer1Simple
                    timer1TickCount++
                }
            } else if (!tick1Started && tick2Started) {
                // Second timer cycle active
                if (timer2TickCount >= timer2Ticks) {
                    tick2Started = false
                    changingTimer = false
                    resetTimer()
                } else {
                    mc.timer.timerSpeed = timer2Simple
                    timer2TickCount++
                }
            }
        }

        // Reset boost state if not falling (but still may be in air after boost)
        if (!isFalling && boosted) {
            if (changeTimer && changingTimer) {
                resetTimer()
            }
            boosted = false
            if (autoDisable) {
                // Optional: disable Speed module
                // mc.thePlayer?.sendChatMessage("Auto disabled FastFall speed mode") // just an example
            }
        }
    }

    private fun resetTimer() {
        mc.timer.timerSpeed = 1f
        tick1Started = false
        tick2Started = false
        timer1TickCount = 0
        timer2TickCount = 0
        changingTimer = false
    }
}