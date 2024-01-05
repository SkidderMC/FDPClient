/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.StrafeEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.util.MathHelper
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@ModuleInfo(name = "StrafeFix", category = ModuleCategory.MOVEMENT)
object Strafe : Module() {

    val silentFixVaule = BoolValue("Silent", true)

    val strafeMovement = BoolValue("Movement", true)
    private var strengthValue= FloatValue("Strength", 0.5F, 0F, 1F)
    private var noMoveStopValue = BoolValue("NoMoveStop", false)
    private var onGroundStrafeValue = BoolValue("OnGroundStrafe", false)
    private var allDirectionsJumpValue = BoolValue("AllDirectionsJump", false)
    
    /**
     * Strafe Fix
     * Code by Co Dynamic
     * Date: 2023/02/15
     */
    
    var silentFix = false
    var doFix = false
    var isOverwrited = false

    private var wasDown = false
    private var jump = false

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (jump) {
            event.cancelEvent()
        }
    }

    override fun onEnable() {
        wasDown = false
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!isOverwrited) {
            silentFix = silentFixVaule.get()
            doFix = true
        }
        if (!strafeMovement.get()) {
        if (mc.thePlayer!!.onGround && mc.gameSettings.keyBindJump.isKeyDown && allDirectionsJumpValue.get() && (mc.thePlayer!!.movementInput.moveForward != 0F || mc.thePlayer!!.movementInput.moveStrafe != 0F) && !(mc.thePlayer!!.isInWater || mc.thePlayer!!.isInLava || mc.thePlayer!!.isOnLadder || mc.thePlayer!!.isInWeb)) {
            if (mc.gameSettings.keyBindJump.isKeyDown) {
                mc.gameSettings.keyBindJump.pressed = false
                wasDown = true
            }
            val yaw = mc.thePlayer!!.rotationYaw
            mc.thePlayer!!.rotationYaw = getMoveYaw()
            mc.thePlayer!!.jump()
            mc.thePlayer!!.rotationYaw = yaw
            jump = true
            if (wasDown) {
                mc.gameSettings.keyBindJump.pressed = true
                    wasDown = false
            }
        } else {
                jump = false
         }
        }
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        val shotSpeed = sqrt((mc.thePlayer!!.motionX * mc.thePlayer!!.motionX) + (mc.thePlayer!!.motionZ * mc.thePlayer!!.motionZ))
        val speed = (shotSpeed * strengthValue.get())
        val motionX = (mc.thePlayer!!.motionX * (1 - strengthValue.get()))
        val motionZ = (mc.thePlayer!!.motionZ * (1 - strengthValue.get()))
        if (!(mc.thePlayer!!.movementInput.moveForward != 0F || mc.thePlayer!!.movementInput.moveStrafe != 0F)) {
            if (noMoveStopValue.get()) {
                mc.thePlayer!!.motionX = 0.0
                mc.thePlayer!!.motionZ = 0.0
            }
            return
        }
        if (!mc.thePlayer!!.onGround || onGroundStrafeValue.get()) {
            val yaw = getMoveYaw()
            mc.thePlayer!!.motionX = (((-sin(Math.toRadians(yaw.toDouble())) * speed) + motionX))
            mc.thePlayer!!.motionZ = (((cos(Math.toRadians(yaw.toDouble())) * speed) + motionZ))
        }
    }

    fun getMoveYaw(): Float {
        var moveYaw = mc.thePlayer!!.rotationYaw
        if (mc.thePlayer!!.moveForward != 0F && mc.thePlayer!!.moveStrafing == 0F) {
            moveYaw += if(mc.thePlayer!!.moveForward > 0) 0 else 180
        } else if (mc.thePlayer!!.moveForward != 0F && mc.thePlayer!!.moveStrafing != 0F) {
            if (mc.thePlayer!!.moveForward > 0) {
                moveYaw += if (mc.thePlayer!!.moveStrafing > 0) -45 else 45
            } else {
                moveYaw -= if (mc.thePlayer!!.moveStrafing > 0) -45 else 45
            }
            moveYaw += if(mc.thePlayer!!.moveForward > 0) 0 else 180
        } else if (mc.thePlayer!!.moveStrafing != 0F && mc.thePlayer!!.moveForward == 0F) {
            moveYaw += if(mc.thePlayer!!.moveStrafing > 0) -90 else 90
        }
        return moveYaw
    }
    
    override fun onDisable() {
        doFix = false
    }
    
    fun applyForceStrafe(isSilent: Boolean, runStrafeFix: Boolean) {
        silentFix = isSilent
        doFix = runStrafeFix
        isOverwrited = true
    }
    
    fun updateOverwrite() {
        isOverwrited = false
        doFix = state
        silentFix = silentFixVaule.get()
    }
    
    fun runStrafeFixLoop(isSilent: Boolean, event: StrafeEvent) {
        if (event.isCancelled) {
            return
        }
        val (yaw) = RotationUtils.targetRotation ?: return
        var strafe = event.strafe
        var forward = event.forward
        var friction = event.friction
        var factor = strafe * strafe + forward * forward
        
        var angleDiff = ((MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - yaw - 22.5f - 135.0f) + 180.0).toDouble() / (45.0).toDouble()).toInt()
        //alert("Diff: " + angleDiff + " friction: " + friction + " factor: " + factor);
        var calcYaw = if(isSilent) { yaw + 45.0f * angleDiff.toFloat() } else yaw
        
        var calcMoveDir = Math.max(Math.abs(strafe), Math.abs(forward)).toFloat()
        calcMoveDir = calcMoveDir * calcMoveDir
        var calcMultiplier = MathHelper.sqrt_float(calcMoveDir / Math.min(1.0f, calcMoveDir * 2.0f))
        
        if (isSilent) {
            when (angleDiff) {
                1, 3, 5, 7, 9 -> {
                    if ((Math.abs(forward) > 0.005 || Math.abs(strafe) > 0.005) && !(Math.abs(forward) > 0.005 && Math.abs(strafe) > 0.005)) {
                        friction = friction / calcMultiplier
                    } else if (Math.abs(forward) > 0.005 && Math.abs(strafe) > 0.005) {
                        friction = friction * calcMultiplier
                    }
                }
            }
        }
        if (factor >= 1.0E-4F) {
            factor = MathHelper.sqrt_float(factor)

            if (factor < 1.0F) {
                factor = 1.0F
            }

            factor = friction / factor
            strafe *= factor
            forward *= factor

            val yawSin = MathHelper.sin((calcYaw * Math.PI / 180F).toFloat())
            val yawCos = MathHelper.cos((calcYaw * Math.PI / 180F).toFloat())

            mc.thePlayer.motionX += strafe * yawCos - forward * yawSin
            mc.thePlayer.motionZ += forward * yawCos + strafe * yawSin
        }
        event.cancelEvent()
    }
}
