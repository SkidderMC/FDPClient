/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.other

import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.notOnConsuming
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.notOnFalling
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed.notOnVoid
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.extensions.stop
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.extensions.stopXZ
import net.ccbluex.liquidbounce.utils.extensions.stopY
import net.ccbluex.liquidbounce.utils.extensions.tryJump
import net.ccbluex.liquidbounce.utils.movement.FallingPlayer
import net.minecraft.client.settings.GameSettings
import net.minecraft.item.ItemBucketMilk
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemPotion
import kotlin.math.max

object CustomSpeed : SpeedMode("Custom") {
    private var groundTick = 0

    override fun onMotion() {
        if (Speed.customBehavior != "Current") {
            return
        }

        handleCurrent()
    }

    override fun onPreMotion() {
        if (Speed.customBehavior == "Legacy" && Speed.legacyCustomUsePreMotion) {
            handleLegacy()
        }
    }

    override fun onUpdate() {
        if (Speed.customBehavior == "Legacy" && !Speed.legacyCustomUsePreMotion) {
            handleLegacy()
        }
    }

    private fun handleCurrent() {
        val player = mc.thePlayer ?: return
        val heldItem = player.heldItem

        val fallingPlayer = FallingPlayer()
        if (notOnVoid && fallingPlayer.findCollision(500) == null
            || notOnFalling && player.fallDistance > 2.5f
            || notOnConsuming && player.isUsingItem
            && (heldItem.item is ItemFood
                    || heldItem.item is ItemPotion
                    || heldItem.item is ItemBucketMilk)
        ) {

            if (player.onGround) player.tryJump()
            mc.timer.timerSpeed = 1f
            return
        }

        if (player.isMoving) {
            if (player.onGround) {
                if (Speed.customGroundStrafe > 0) {
                    strafe(Speed.customGroundStrafe)
                }

                mc.timer.timerSpeed = Speed.customGroundTimer
                player.motionY = Speed.customY.toDouble()
            } else {
                if (Speed.customAirStrafe > 0) {
                    strafe(Speed.customAirStrafe)
                }

                if (player.ticksExisted % Speed.customAirTimerTick == 0) {
                    mc.timer.timerSpeed = Speed.customAirTimer
                } else {
                    mc.timer.timerSpeed = 1f
                }
            }
        }
    }

    private fun handleLegacy() {
        val player = mc.thePlayer ?: return

        if (shouldPauseCurrentCustom(player)) {
            if (Speed.resetXZ) {
                player.stopXZ()
            } else {
                player.stop()
            }
            mc.timer.timerSpeed = 1f
            return
        }

        if (player.isMoving) {
            mc.timer.timerSpeed = if (player.motionY > 0) {
                Speed.legacyCustomUpTimer
            } else {
                Speed.legacyCustomDownTimer
            }

            when {
                player.onGround -> {
                    if (groundTick >= Speed.legacyCustomGroundStay) {
                        if (Speed.legacyCustomPressSpaceKeyOnGround) {
                            mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
                        }
                        mc.timer.timerSpeed = Speed.legacyCustomJumpTimer

                        if (Speed.legacyCustomDoLaunchSpeed && Speed.legacyCustomLaunchMoveBeforeJump) {
                            strafe(Speed.legacyCustomLaunchSpeed)
                        }

                        if (Speed.legacyCustomDoJump) {
                            player.jump()
                        } else if (!Speed.legacyCustomDoModifyJumpY) {
                            player.motionY = 0.42
                        }

                        if (Speed.legacyCustomDoLaunchSpeed && !Speed.legacyCustomLaunchMoveBeforeJump) {
                            strafe(Speed.legacyCustomLaunchSpeed)
                        }

                        if (Speed.legacyCustomDoModifyJumpY && Speed.customY != 0f) {
                            player.motionY = Speed.customY.toDouble()
                        }
                    } else if (Speed.legacyCustomGroundResetXZ) {
                        player.motionX = 0.0
                        player.motionZ = 0.0
                    }
                    groundTick++
                }

                else -> {
                    groundTick = 0

                    if (Speed.legacyCustomPressSpaceKeyInAir) {
                        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
                    }

                    if (Speed.legacyCustomDoMinimumSpeed) {
                        strafe(max(net.ccbluex.liquidbounce.utils.movement.MovementUtils.speed, Speed.legacyCustomMinimumSpeed))
                    }

                    when (Speed.legacyCustomStrafe.lowercase()) {
                        "strafe" -> strafe(Speed.legacyCustomSpeed)
                        "non-strafe" -> strafe()
                        "boost" -> strafe()
                        "airspeed" -> {
                            player.speedInAir = if (player.motionY > 0) {
                                0.01f * Speed.legacyCustomUpAirSpeed
                            } else {
                                0.01f * Speed.legacyCustomDownAirSpeed
                            }
                            strafe()
                        }

                        "plus" -> applyLegacyPlus(player)

                        "plusonlyup" -> {
                            if (player.motionY > 0) {
                                applyLegacyPlus(player)
                            } else {
                                strafe()
                            }
                        }

                        "plusonlydown" -> {
                            if (player.motionY < 0) {
                                applyLegacyPlus(player)
                            } else {
                                strafe()
                            }
                        }
                    }

                    player.motionY += Speed.legacyCustomAddYMotion * 0.03
                }
            }
        } else if (Speed.resetXZ) {
            player.motionX = 0.0
            player.motionZ = 0.0
        }
    }

    private fun applyLegacyPlus(player: net.minecraft.client.entity.EntityPlayerSP) {
        when (Speed.legacyCustomPlusMode.lowercase()) {
            "add" -> {
                val extra = Speed.legacyCustomSpeed * 0.1f
                strafe(net.ccbluex.liquidbounce.utils.movement.MovementUtils.speed + extra)
            }

            "multiply" -> {
                player.motionX *= Speed.legacyCustomPlusMultiplyAmount
                player.motionZ *= Speed.legacyCustomPlusMultiplyAmount
            }
        }
    }

    private fun shouldPauseCurrentCustom(player: net.minecraft.client.entity.EntityPlayerSP): Boolean {
        val heldItem = player.heldItem
        val fallingPlayer = FallingPlayer()

        return notOnVoid && fallingPlayer.findCollision(500) == null
            || notOnFalling && player.fallDistance > 2.5f
            || notOnConsuming && player.isUsingItem &&
            (heldItem.item is ItemFood || heldItem.item is ItemPotion || heldItem.item is ItemBucketMilk)
    }

    override fun onEnable() {
        val player = mc.thePlayer ?: return
        groundTick = 0

        if (Speed.resetXZ) player.stopXZ()
        if (Speed.resetY) player.stopY()

        super.onEnable()
    }

    override fun onDisable() {
        groundTick = 0
        mc.timer.timerSpeed = 1f
        mc.thePlayer?.speedInAir = 0.02f
    }

}
