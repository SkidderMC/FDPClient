/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue

class CustomSpeed : SpeedMode("Custom") {
    private val speedValue = FloatValue("CustomSpeed", 1.6f, 0f, 2f)
    private val launchSpeedValue = FloatValue("CustomLaunchSpeed", 1.6f, 0.2f, 2f)
    private val minimumSpeedValue = FloatValue("CustomMinimumSpeed", 0.25f, 0.1f, 2f)
    private val addYMotionValue = FloatValue("CustomAddYMotion", 0f, 0f, 2f)
    private val yValue = FloatValue("CustomY", 0f, 0f, 4f)
    private val upTimerValue = FloatValue("CustomUpTimer", 1f, 0.1f, 2f)
    private val jumpTimerValue = FloatValue("CustomJumpTimer", 1.25f, 0.1f, 2f)
    private val downTimerValue = FloatValue("CustomDownTimer", 1f, 0.1f, 2f)
    private val strafeValue = ListValue("CustomStrafe", arrayOf("Strafe", "Boost", "Plus", "PlusOnlyUp", "PlusOnlyDown", "Non-Strafe"), "Boost")
    private val plusMode = ListValue("PlusBoostMode", arrayOf("Add", "Multiply"), "Add").displayable { strafeValue.equals("Plus") || strafeValue.equals("PlusOnlyUp") || strafeValue.equals("PlusOnlyDown") }
    private val plusMultiply = FloatValue("PlusMultiplyAmount", 1.1f, 1f, 2f).displayable { plusMode.equals("Multiply") && (strafeValue.equals("Plus") || strafeValue.equals("PlusOnlyUp") || strafeValue.equals("PlusOnlyDown")) }
    private val groundStay = IntegerValue("CustomGroundStay", 0, 0, 10)
    private val groundResetXZValue = BoolValue("CustomGroundResetXZ", false)
    private val resetXZValue = BoolValue("CustomResetXZ", false)
    private val resetYValue = BoolValue("CustomResetY", false)
    private val doLaunchSpeedValue = BoolValue("CustomDoLaunchSpeed", true)
    private val doMinimumSpeedValue = BoolValue("CustomDoMinimumSpeed", true)
    private val GroundSpaceKeyPressed = BoolValue("CustomPressSpaceKeyOnGround", true)
    private val AirSpaceKepPressed = BoolValue("CustomPressSpaceKeyInAir", false)

    

    private var groundTick = 0


    override fun onPreMotion() {
        if (MovementUtils.isMoving()) {
            mc.timer.timerSpeed = if (mc.thePlayer.motionY> 0) { upTimerValue.get() } else { downTimerValue.get() }

            when {
                mc.thePlayer.onGround -> {
                    if (groundTick >= groundStay.get()) {
                        if (GroundSpaceKeyPressed.get()) {
                            mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
                        }
                        mc.timer.timerSpeed = jumpTimerValue.get()
                        mc.thePlayer.jump()
                        if (doLaunchSpeedValue.get()) {
                            MovementUtils.strafe(launchSpeedValue.get())
                        }
                        if (yValue.get() != 0f) {
                            mc.thePlayer.motionY = yValue.get().toDouble()
                        }
                    } else if (groundResetXZValue.get()) {
                        mc.thePlayer.motionX = 0.0
                        mc.thePlayer.motionZ = 0.0
                    }
                    groundTick++
                }
                else -> {
                    groundTick = 0
                    if (AirSpaceKeyPressed.get()) {
                        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
                    }
                    if (doMinimumSpeedValue.get() && MovementUtils.getSpeed() < minimumSpeedValue) {
                        MovementUtils.strafe(minimumSpeedValue.get())
                    }
                    when (strafeValue.get().lowercase()) {
                        "strafe" -> MovementUtils.strafe(speedValue.get())
                        "non-strafe" -> MovementUtils.strafe()
                        "boost" -> MovementUtils.strafe()
                        "plus" -> {
                            when (plusMode.get().lowercase()) {
                                "plus" -> MovementUtils.move(speedValue.get() * 0.1f)
                                "multiply" -> { 
                                    mc.thePlayer.motionX *= plusMultiply.get()
                                    mc.thePlayer.motionZ *= plusMultiply.get()
                                }
                            }
                        }
                        "plusonlyup" -> { 
                            if (mc.thePlayer.motionY > 0) {
                                when (plusMode.get().lowercase()) {
                                    "plus" -> MovementUtils.move(speedValue.get() * 0.1f)
                                    "multiply" -> { 
                                        mc.thePlayer.motionX *= plusMultiply.get()
                                        mc.thePlayer.motionZ *= plusMultiply.get()
                                    }
                                }
                            } else {
                                MovementUtils.strafe()
                            }
                        }
                        "plusonlydown" -> {
                            if (mc.thePlayer.motionY < 0) {
                                when (plusMode.get().lowercase()) {
                                    "plus" -> MovementUtils.move(speedValue.get() * 0.1f)
                                    "multiply" -> { 
                                        mc.thePlayer.motionX *= plusMultiply.get()
                                        mc.thePlayer.motionZ *= plusMultiply.get()
                                    }
                                }
                            } else {
                                MovementUtils.strafe()
                            }
                        }
                    }
                    mc.thePlayer.motionY += addYMotionValue.get() * 0.03
                }
            }
        } else if (resetXZValue.get()) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }


    override fun onEnable() {
        if (resetXZValue.get()) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
        if (resetYValue.get()) mc.thePlayer.motionY = 0.0
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }
}
