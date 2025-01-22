/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.extensions.fixedSensitivityPitch
import net.ccbluex.liquidbounce.utils.extensions.fixedSensitivityYaw
import net.ccbluex.liquidbounce.utils.extensions.tryJump
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextFloat
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextInt
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.event.handler
import net.minecraft.client.settings.GameSettings

object AntiAFK : Module("AntiAFK", Category.PLAYER, gameDetecting = false) {

    private val mode by choices("Mode", arrayOf("Old", "Random", "Custom"), "Random")

    private val rotateValue = boolean("Rotate", true) { mode == "Custom" }
    private val rotationDelay by int("RotationDelay", 100, 0..1000) { rotateValue.isActive() }
    private val rotationAngle by float("RotationAngle", 1f, -180F..180F) { rotateValue.isActive() }

    private val swingValue = boolean("Swing", true) { mode == "Custom" }
    private val swingDelay by int("SwingDelay", 100, 0..1000) { swingValue.isActive() }

    private val jump by boolean("Jump", true) { mode == "Custom" }
    private val move by boolean("Move", true) { mode == "Custom" }

    private var shouldMove = false
    private var randomTimerDelay = 500L

    private val swingDelayTimer = MSTimer()
    private val delayTimer = MSTimer()


    val onUpdate = handler<UpdateEvent> {
        val thePlayer = mc.thePlayer ?: return@handler

        when (mode.lowercase()) {
            "old" -> {
                mc.gameSettings.keyBindForward.pressed = true

                if (delayTimer.hasTimePassed(500)) {
                    thePlayer.fixedSensitivityYaw += 180F
                    delayTimer.reset()
                }
            }

            "random" -> {
                getRandomMoveKeyBind().pressed = shouldMove

                if (!delayTimer.hasTimePassed(randomTimerDelay)) return@handler
                shouldMove = false
                randomTimerDelay = 500L
                when (nextInt(0, 6)) {
                    0 -> {
                        if (thePlayer.onGround) thePlayer.tryJump()
                        delayTimer.reset()
                    }

                    1 -> {
                        if (!thePlayer.isSwingInProgress) thePlayer.swingItem()
                        delayTimer.reset()
                    }

                    2 -> {
                        randomTimerDelay = nextInt(0, 1000).toLong()
                        shouldMove = true
                        delayTimer.reset()
                    }

                    3 -> {
                        thePlayer.inventory.currentItem = nextInt(0, 9)
                        mc.playerController.syncCurrentPlayItem()
                        delayTimer.reset()
                    }

                    4 -> {
                        thePlayer.fixedSensitivityYaw += nextFloat(-180f, 180f)
                        delayTimer.reset()
                    }

                    5 -> {
                        thePlayer.fixedSensitivityPitch += nextFloat(-10f, 10f)
                        delayTimer.reset()
                    }
                }
            }

            "custom" -> {
                if (move)
                    mc.gameSettings.keyBindForward.pressed = true

                if (jump && thePlayer.onGround)
                    thePlayer.tryJump()

                if (rotateValue.get() && delayTimer.hasTimePassed(rotationDelay)) {
                    thePlayer.fixedSensitivityYaw += rotationAngle
                    thePlayer.fixedSensitivityPitch += nextFloat(0F, 1F) * 2 - 1
                    delayTimer.reset()
                }

                if (swingValue.get() && !thePlayer.isSwingInProgress && swingDelayTimer.hasTimePassed(swingDelay)) {
                    thePlayer.swingItem()
                    swingDelayTimer.reset()
                }
            }
        }
    }

    private val moveKeyBindings =
        arrayOf(
            mc.gameSettings.keyBindForward,
            mc.gameSettings.keyBindLeft,
            mc.gameSettings.keyBindBack,
            mc.gameSettings.keyBindRight
        )

    private fun getRandomMoveKeyBind() = moveKeyBindings.random()

    override fun onDisable() {
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindForward))
            mc.gameSettings.keyBindForward.pressed = false
    }
}