/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.matrix

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.minecraft.client.settings.GameSettings
import kotlin.math.sqrt

class MatrixBHopSpeed : SpeedMode("MatrixBHop") {
    private val speedMultiValue = FloatValue("${valuePrefix}Speed", 1f, 0.7f, 1.2f)
    private val noTimerValue = BoolValue("${valuePrefix}NoTimer", false)
    private var recX = 0.0
    private var recY = 0.0
    private var recZ = 0.0
    private var jumped = false
    private var dist = 0.0

    override fun onUpdate() {
        fun setTimer(value: Float) {
            if(!noTimerValue.get()) {
                mc.timer.timerSpeed = value
            }
        }
        if (!mc.thePlayer.onGround) {
            setTimer(1.0f)
        }
        if (jumped) {
            mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
            jumped = false
            setTimer(0.9f)
            dist = sqrt((recX - mc.thePlayer.posX) * (recX - mc.thePlayer.posX) + (recZ - mc.thePlayer.posZ) * (recZ - mc.thePlayer.posZ))
            if (MovementUtils.getSpeed() > 0) {
                val recSpeed = MovementUtils.getSpeed()
                mc.thePlayer.motionX *= (0.912 * dist * speedMultiValue.get()) / recSpeed
                mc.thePlayer.motionZ *= (0.912 * dist * speedMultiValue.get()) / recSpeed
            }
        } else if (!mc.thePlayer.onGround) {
            mc.thePlayer.jumpMovementFactor = 0.0265f
        }
        if (mc.thePlayer.onGround && MovementUtils.isMoving() && !mc.thePlayer.isInWater) {
            recX = mc.thePlayer.posX
            recY = mc.thePlayer.posY
            recZ = mc.thePlayer.posZ
            mc.gameSettings.keyBindJump.pressed = false
            mc.thePlayer.jump()
            MovementUtils.strafe()
            jumped = true
            setTimer(2.0f)
        }
    }

    override fun onDisable() {
        jumped = false
        mc.timer.timerSpeed = 1f
        mc.gameSettings.keyBindJump.pressed = mc.gameSettings.keyBindJump.isKeyDown
        mc.thePlayer.jumpMovementFactor = 0.02f
    }
}
