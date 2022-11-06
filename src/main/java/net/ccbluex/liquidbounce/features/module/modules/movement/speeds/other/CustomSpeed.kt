/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.client.settings.GameSettings

@Suppress("UnclearPrecedenceOfBinaryExpression")
class CustomSpeed : SpeedMode("Custom") {
    private val speedValue = FloatValue("CustomSpeed", 1.6f, 0f, 2f)
    private val doLaunchSpeedValue = BoolValue("CustomDoLaunchSpeed", true)
    private val launchSpeedValue = FloatValue("CustomLaunchSpeed", 1.6f, 0.2f, 2f).displayable { doLaunchSpeedValue.get() }
    private val strafeBeforeJump = BoolValue("CustomLaunchMoveBeforeJump", false)
    private val doMinimumSpeedValue = BoolValue("CustomDoMinimumSpeed", true)
    private val minimumSpeedValue = FloatValue("CustomMinimumSpeed", 0.25f, 0.1f, 2f).displayable { doMinimumSpeedValue.get() }
    private val addYMotionValue = FloatValue("CustomAddYMotion", 0f, 0f, 2f)
    private val doCustomYValue = BoolValue("CustomDoModifyJumpY", true)
    private val yValue = FloatValue("CustomY", 0.42f, 0f, 4f).displayable { doCustomYValue.get() }
    private val upTimerValue = FloatValue("CustomUpTimer", 1f, 0.1f, 2f)
    private val jumpTimerValue = FloatValue("CustomJumpTimer", 1.25f, 0.1f, 2f)
    private val downTimerValue = FloatValue("CustomDownTimer", 1f, 0.1f, 2f)
    private val upAirSpeedValue = FloatValue("CustomUpAirSpeed", 2.03f, 0.5f, 3.5f)
    private val downAirSpeedValue = FloatValue("CustomDownAirSpeed", 2.01f, 0.5f, 3.5f)
    private val strafeValue = ListValue("CustomStrafe", arrayOf("Strafe", "Boost", "AirSpeed", "Plus", "PlusOnlyUp", "PlusOnlyDown", "Non-Strafe"), "Boost")
    private val plusMode = ListValue("PlusBoostMode", arrayOf("Add", "Multiply"), "Add").displayable { strafeValue.equals("Plus") || strafeValue.equals("PlusOnlyUp") || strafeValue.equals("PlusOnlyDown") }
    private val plusMultiply = FloatValue("PlusMultiplyAmount", 1.1f, 1f, 2f).displayable { plusMode.equals("Multiply") && (strafeValue.equals("Plus") || strafeValue.equals("PlusOnlyUp") || strafeValue.equals("PlusOnlyDown")) }
    private val groundStay = IntegerValue("CustomGroundStay", 0, 0, 10)
    private val groundResetXZValue = BoolValue("CustomGroundResetXZ", false)
    private val resetXZValue = BoolValue("CustomResetXZ", false)
    private val resetYValue = BoolValue("CustomResetY", false)
    private val doJump = BoolValue("CustomDoJump",true)
    private val GroundSpaceKeyPressed = BoolValue("CustomPressSpaceKeyOnGround", true)
    private val AirSpaceKepPressed = BoolValue("CustomPressSpaceKeyInAir", false)
    private val usePreMotion = BoolValue("CustomUsePreMotion", true)

    

    private var groundTick = 0


    override fun onPreMotion() {
        if (!usePreMotion.get()) return
        if (MovementUtils.isMoving()) {
            mc.timer.timerSpeed = if (mc.thePlayer.motionY> 0) { upTimerValue.get() } else { downTimerValue.get() }

            when {
                mc.thePlayer.onGround -> {
                    if (groundTick >= groundStay.get()) {
                        if (GroundSpaceKeyPressed.get()) {
                            mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
                        }
                        mc.timer.timerSpeed = jumpTimerValue.get()
                        if (doLaunchSpeedValue.get() && strafeBeforeJump.get()) {
                            MovementUtils.strafe(launchSpeedValue.get())
                        }
                        if (doJump.get()) {
                            mc.thePlayer.jump()
                        } else {
                            if (!doCustomYValue.get()) {
                                mc.thePlayer.motionY = 0.42
                            }
                        }
                        if (doLaunchSpeedValue.get() && !strafeBeforeJump.get()) {
                            MovementUtils.strafe(launchSpeedValue.get())
                        }
                        if (doCustomYValue.get()) {
                            if (yValue.get() != 0f) {
                                mc.thePlayer.motionY = yValue.get().toDouble()
                            }
                        }
                    } else if (groundResetXZValue.get()) {
                        mc.thePlayer.motionX = 0.0
                        mc.thePlayer.motionZ = 0.0
                    }
                    groundTick++
                }
                
                else -> {
                    groundTick = 0
                    if (AirSpaceKepPressed.get()) {
                        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
                    }
                    if (doMinimumSpeedValue.get() && MovementUtils.getSpeed() < minimumSpeedValue.get()) {
                        MovementUtils.strafe(minimumSpeedValue.get())
                    }
                    when (strafeValue.get().lowercase()) {
                        "strafe" -> MovementUtils.strafe(speedValue.get())
                        "non-strafe" -> MovementUtils.strafe()
                        "boost" -> MovementUtils.strafe()
                        "airspeed" -> {
                            if (mc.thePlayer.motionY > 0) {
                                mc.thePlayer.speedInAir = 0.01f * upAirSpeedValue.get()
                                MovementUtils.strafe()
                            } else {
                                mc.thePlayer.speedInAir = 0.01f * downAirSpeedValue.get()
                                MovementUtils.strafe()
                            }
                        }
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
    
    override fun onUpdate() {
        if (usePreMotion.get()) return
        if (MovementUtils.isMoving()) {
            mc.timer.timerSpeed = if (mc.thePlayer.motionY> 0) { upTimerValue.get() } else { downTimerValue.get() }

            when {
                mc.thePlayer.onGround -> {
                    if (groundTick >= groundStay.get()) {
                        if (GroundSpaceKeyPressed.get()) {
                            mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
                        }
                        mc.timer.timerSpeed = jumpTimerValue.get()
                        if (doLaunchSpeedValue.get() && strafeBeforeJump.get()) {
                            MovementUtils.strafe(launchSpeedValue.get())
                        }
                        if (doJump.get()) {
                            mc.thePlayer.jump()
                        } else {
                            if (!doCustomYValue.get()) {
                                mc.thePlayer.motionY = 0.42
                            }
                        }
                        if (doLaunchSpeedValue.get() && !strafeBeforeJump.get()) {
                            MovementUtils.strafe(launchSpeedValue.get())
                        }
                        if (doCustomYValue.get()) {
                            if (yValue.get() != 0f) {
                                mc.thePlayer.motionY = yValue.get().toDouble()
                            }
                        }
                    } else if (groundResetXZValue.get()) {
                        mc.thePlayer.motionX = 0.0
                        mc.thePlayer.motionZ = 0.0
                    }
                    groundTick++
                }
                
                else -> {
                    groundTick = 0
                    if (AirSpaceKepPressed.get()) {
                        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
                    }
                    if (doMinimumSpeedValue.get() && MovementUtils.getSpeed() < minimumSpeedValue.get()) {
                        MovementUtils.strafe(minimumSpeedValue.get())
                    }
                    when (strafeValue.get().lowercase()) {
                        "strafe" -> MovementUtils.strafe(speedValue.get())
                        "non-strafe" -> MovementUtils.strafe()
                        "boost" -> MovementUtils.strafe()
                        "airspeed" -> {
                            if (mc.thePlayer.motionY > 0) {
                                mc.thePlayer.speedInAir = 0.01f * upAirSpeedValue.get()
                                MovementUtils.strafe()
                            } else {
                                mc.thePlayer.speedInAir = 0.01f * downAirSpeedValue.get()
                                MovementUtils.strafe()
                            }
                        }
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
        mc.thePlayer!!.speedInAir = 0.02f
    }
}
