package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.vanilla

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.StrafeEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import kotlin.math.cos
import kotlin.math.sin

class LegitVelocity : VelocityMode("Legit") {
    private val legitStrafeValue = BoolValue("${valuePrefix}Strafe", false)
    private val legitFaceValue = BoolValue("${valuePrefix}Face", true)
    private var pos: BlockPos? = null

    override fun onEnable() {
        pos = null
    }
    override fun onVelocityPacket(event: PacketEvent) {
        pos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)
    }

    override fun onStrafe(event: StrafeEvent) {
        if ((velocity.onlyGroundValue.get() && !mc.thePlayer.onGround) || (velocity.onlyCombatValue.get() && !LiquidBounce.combatManager.inCombat)) {
            return
        }
        if (pos == null || mc.thePlayer.hurtTime <= 0) {
            return
        }

        val rot = RotationUtils.getRotations(pos!!.x.toDouble(), pos!!.y.toDouble(), pos!!.z.toDouble())
        if (legitFaceValue.get()) {
            RotationUtils.setTargetRotation(rot)
        }
        val yaw = rot.yaw
        if (legitStrafeValue.get()) {
            val speed = MovementUtils.getSpeed()
            val yaw1 = Math.toRadians(yaw.toDouble())
            mc.thePlayer.motionX = -sin(yaw1) * speed
            mc.thePlayer.motionZ = cos(yaw1) * speed
        } else {
            var strafe = event.strafe
            var forward = event.forward
            val friction = event.friction

            var f = strafe * strafe + forward * forward

            if (f >= 1.0E-4F) {
                f = MathHelper.sqrt_float(f)

                if (f < 1.0F) {
                    f = 1.0F
                }

                f = friction / f
                strafe *= f
                forward *= f

                val yawSin = MathHelper.sin((yaw * Math.PI / 180F).toFloat())
                val yawCos = MathHelper.cos((yaw * Math.PI / 180F).toFloat())

                mc.thePlayer.motionX += strafe * yawCos - forward * yawSin
                mc.thePlayer.motionZ += forward * yawCos + strafe * yawSin
            }
        }
    }
}