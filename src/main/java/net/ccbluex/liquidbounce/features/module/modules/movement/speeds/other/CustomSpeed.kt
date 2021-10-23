/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue

class CustomSpeed : SpeedMode("Custom") {
    private val speedValue = FloatValue("CustomSpeed", 1.6f, 0.2f, 2f)
    private val launchSpeedValue = FloatValue("CustomLaunchSpeed", 1.6f, 0.2f, 2f)
    private val addYMotionValue = FloatValue("CustomAddYMotion", 0f, 0f, 2f)
    private val yValue = FloatValue("CustomY", 0f, 0f, 4f)
    private val upTimerValue = FloatValue("CustomUpTimer", 1f, 0.1f, 2f)
    private val downTimerValue = FloatValue("CustomDownTimer", 1f, 0.1f, 2f)
    private val strafeValue = ListValue("CustomStrafe", arrayOf("Strafe", "Boost", "Plus", "PlusOnlyUp", "Non-Strafe"), "Boost")
    private val groundStay = IntegerValue("CustomGroundStay", 0, 0, 10)
    private val groundResetXZValue = BoolValue("CustomGroundResetXZ", false)
    private val resetXZValue = BoolValue("CustomResetXZ", false)
    private val resetYValue = BoolValue("CustomResetY", false)
    private val doLaunchSpeedValue = BoolValue("CustomDoLaunchSpeed", true)

    private var groundTick = 0

    override fun onPreMotion() {
        if (MovementUtils.isMoving()) {
            mc.timer.timerSpeed = if (mc.thePlayer.motionY> 0) { upTimerValue.get() } else { downTimerValue.get() }

            when {
                mc.thePlayer.onGround -> {
                    if (groundTick >= groundStay.get()) {
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
                    when (strafeValue.get().lowercase()) {
                        "strafe" -> MovementUtils.strafe(speedValue.get())
                        "boost" -> MovementUtils.strafe()
                        "plus" -> MovementUtils.move(speedValue.get() * 0.1f)
                        "plusonlyup" -> if (mc.thePlayer.motionY> 0) {
                            MovementUtils.move(speedValue.get() * 0.1f)
                        } else {
                            MovementUtils.strafe()
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