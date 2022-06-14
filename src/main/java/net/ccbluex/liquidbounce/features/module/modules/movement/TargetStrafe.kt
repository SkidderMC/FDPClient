/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.StrafeEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.entity.EntityLivingBase


@ModuleInfo(name = "TargetStrafe",  category = ModuleCategory.MOVEMENT)
class TargetStrafe : Module() {
    private val thirdPersonViewValue = BoolValue("ThirdPersonView", false)
    private val ongroundValue = BoolValue("OnGround",true)
    private val radiusValue = FloatValue("Radius", 0.1f, 0.5f, 5.0f)
    private var direction = -1.0

    var targetEntity : EntityLivingBase?=null
    var isEnabled = false
    var doStrafe = false

    var callBackYaw = 0.0

    fun modifyStrafe(event: StrafeEvent):Boolean {
        if(!isEnabled || event.isCancelled) {
            return false
        }else {
            MovementUtils.strafe()
            return true
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if(doStrafe && (!ongroundValue.get() || mc.thePlayer.onGround)) {
            val _entity : EntityLivingBase = targetEntity?:return
            MovementUtils.doTargetStrafe(_entity, direction.toFloat(), radiusValue.get(), event)
            callBackYaw = RotationUtils.getRotationsEntity(_entity).yaw.toDouble()
            isEnabled = true
        }else {
            isEnabled = false
        }
    }

    private fun checkVoid(): Boolean {
        for (x in -2..2) for (z in -2..2) if (isVoid(x, z)) return true
        return false
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
