/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.FloatValue
import kotlin.math.cos
import kotlin.math.sin

@ModuleInfo(name = "BoatFly", category = ModuleCategory.MOVEMENT)
class BoatFly : Module() {
    private val speedValue = FloatValue("Speed", 0.3f, 0.0f, 1.0f)

    override fun onEnable() {
    }

    override fun onDisable() {
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if(!mc.thePlayer.isRiding) return
        ClientUtils.displayChatMessage("Hi")

        val vehicle = mc.thePlayer.ridingEntity
            vehicle.setVelocity(-sin(MovementUtils.direction) * speedValue.get(), if(mc.gameSettings.keyBindJump.pressed) speedValue.get().toDouble() else vehicle.motionY, cos(MovementUtils.direction) * speedValue.get())
    }

    /**
     * 读取mode中的value并和本体中的value合并
     * 所有的value必须在这个之前初始化
     */
}
