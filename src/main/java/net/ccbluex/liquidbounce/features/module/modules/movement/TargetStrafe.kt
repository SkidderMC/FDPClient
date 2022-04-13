/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.utils.PlayerUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.entity.EntityLivingBase
import kotlin.math.*


@ModuleInfo(name = "TargetStrafe",  category = ModuleCategory.MOVEMENT)
class TargetStrafe : Module() {
    private val thirdPersonViewValue = BoolValue("ThirdPersonView", false)
    private val radiusValue = FloatValue("Radius", 0.1f, 0.5f, 5.0f)
    private val killAura = LiquidBounce.moduleManager[KillAura::class.java]
    private var direction = -1

    /**
     *
     * @param event MoveEvent
     */
    @EventTarget
    fun onMove(event: MoveEvent) {
        if(!canStrafe) return
        var aroundVoid = false
        for (x in -1..0) for (z in -1..0) if (isVoid(x, z)) aroundVoid = true

        var yaw = RotationUtils.getRotationFromEyeHasPrev(killAura!!.target).yaw

        if (mc.thePlayer.isCollidedHorizontally || aroundVoid) direction *= -1

        var targetStrafe = (if (mc.thePlayer.moveStrafing != 0F) mc.thePlayer.moveStrafing * direction else direction.toFloat())
        if (!PlayerUtils.isBlockUnder()) targetStrafe = 0f

        val rotAssist = 45 / mc.thePlayer.getDistanceToEntity(killAura.target)
        val moveAssist = (45f / getStrafeDistance(killAura.target!!)).toDouble()

        var mathStrafe = 0f

        if (targetStrafe > 0) {
            if ((killAura.target!!.entityBoundingBox.minY > mc.thePlayer.entityBoundingBox.maxY || killAura.target!!.entityBoundingBox.maxY < mc.thePlayer.entityBoundingBox.minY) && mc.thePlayer.getDistanceToEntity(
                    killAura.target!!
                ) < radiusValue.get()
            ) yaw += -rotAssist
            mathStrafe += -moveAssist.toFloat()
        } else if (targetStrafe < 0) {
            if ((killAura.target!!.entityBoundingBox.minY > mc.thePlayer.entityBoundingBox.maxY || killAura.target!!.entityBoundingBox.maxY < mc.thePlayer.entityBoundingBox.minY) && mc.thePlayer.getDistanceToEntity(
                    killAura.target!!
                ) < radiusValue.get()
            ) yaw += rotAssist
            mathStrafe += moveAssist.toFloat()
        }

        val doSomeMath = doubleArrayOf(
            cos(Math.toRadians((yaw + 90f + mathStrafe).toDouble())),
            sin(Math.toRadians((yaw + 90f + mathStrafe).toDouble()))
        )
        val moveSpeed = sqrt(event.x.pow(2.0) + event.z.pow(2.0))

        val asLast = doubleArrayOf(
            moveSpeed * doSomeMath[0],
            moveSpeed * doSomeMath[1]
        )

        event.x = asLast[0]
        event.z = asLast[1]
        //        if (mc.thePlayer.isCollidedHorizontally || checkVoid()) direction = if (direction == 1) -1 else 1
//        if(checkVoid() && canStrafe) return
//        if (mc.gameSettings.keyBindLeft.isKeyDown) {
//            direction = 1
//        }
//        if (mc.gameSettings.keyBindRight.isKeyDown) {
//            direction = -1
//        }
//
//        if (!isVoid(0, 0) && canStrafe) {
//            MovementUtils.setSpeed(
//                event,
//                sqrt(event.x.pow(2.0) + event.z.pow(2.0)),
//                RotationUtils.toRotation(RotationUtils.getCenter(killAura.target?.entityBoundingBox), true).yaw,
//                direction.toDouble(),
//                if (mc.thePlayer.getDistanceToEntity(killAura.target) <= radiusValue.get()) 0.0 else 1.0
//            )
//        }
        if (!thirdPersonViewValue.get()) return
        mc.gameSettings.thirdPersonView = if (canStrafe) 3 else 0
    }

    private val canStrafe: Boolean
        get() = killAura!!.state && killAura.target != null && !mc.thePlayer.isSneaking

    private fun checkVoid(): Boolean {
        for (x in -2..2) for (z in -2..2) if (isVoid(x, z)) return true
        return false
    }

    private fun getStrafeDistance(target: EntityLivingBase): Float {
        return (mc.thePlayer.getDistanceToEntity(target) - radiusValue.get()).coerceAtLeast(
            mc.thePlayer.getDistanceToEntity(
                target
            ) - (mc.thePlayer.getDistanceToEntity(target) - radiusValue.get() / (radiusValue.get() * 2))
        )
    }

    private fun isVoid(xPos: Int, zPos: Int): Boolean {
        if (mc.thePlayer.posY < 0.0) return true
        var off = 0
        while (off < mc.thePlayer.posY.toInt() + 2) {
            val bb = mc.thePlayer.entityBoundingBox.offset(xPos.toDouble(), -off.toDouble(), zPos.toDouble())
            if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                off += 2
                continue
            }
            return false
        }
        return true
    }

}
