/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.ghost

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.MathUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.entity.EntityLivingBase

@ModuleInfo(name = "BetterTickBase", category = ModuleCategory.GHOST, canEnable = false)
class BetterTickBase : Module() {

    private val fovValue = IntegerValue("TargetFOV", 60, 0, 180)
    private val maxDelayValue = IntegerValue("MaxLagAmount", 400, 0, 1000)


    override fun onEnable() {
        val entity = mc.theWorld.loadedEntityList
            .filter {
                EntityUtils.isSelected(it, true) && mc.thePlayer.canEntityBeSeen(it) &&
                        mc.thePlayer.getDistanceToEntityBox(it) <= 5.0 && RotationUtils.getRotationDifference(it) <= fovValue.get()
            }
            .minByOrNull { RotationUtils.getRotationDifference(it) } ?: return

        val entityMotionX = (entity.posX - entity.lastTickPosX)
        val entityMotionZ = (entity.posZ - entity.lastTickPosZ)
        val predictionTicks = (MathUtils.getDistance(mc.thePlayer.posX, mc.thePlayer.posZ, entity.posX, entity.posZ) - 3.3) / (
                MathUtils.getDistance(0.0,0.0, mc.thePlayer.motionX - entityMotionX, mc.thePlayer.motionZ - entityMotionZ))
        var f = 0
        var startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() < startTime + (predictionTicks * 50).coerceAtMost(maxDelayValue.get().toDouble())) {
            f = 1 - f
        }
    }
}
