/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.EnumAutoDisableType
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.utils.misc.RandomUtils

@ModuleInfo(name = "Timer", category = ModuleCategory.WORLD, autoDisable = EnumAutoDisableType.RESPAWN)
object Timer : Module() {

    // private val minSpeedValue = FloatValue("Speed", 2F, 0.1F, 10F)
    private val maxSpeedValue: FloatValue = object : FloatValue("Max-Timer", 2F, 0.1F, 10F) {
        fun onChanged(oldValue: Int, newValue: Int) {
            val minTimer = minSpeedValue.get()
            if (minTimer > newValue) {
                set(minTimer)
            }
        }
    }
    private val minSpeedValue: FloatValue = object : FloatValue("Min-Timer", 2F, 0.1F, 10F)  {
        fun onChanged(oldValue: Int, newValue: Int) {
            val maxTimer = maxSpeedValue.get()
            if (maxTimer < newValue) {
                set(maxTimer)
            }
        }
    }
    private val onMoveValue = BoolValue("OnMove", true)

    override fun onDisable() {
        if (mc.thePlayer == null) {
            return
        }

        mc.timer.timerSpeed = 1F
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (MovementUtils.isMoving() || !onMoveValue.get()) {
            mc.timer.timerSpeed = RandomUtils.nextFloat(minSpeedValue.get(), maxSpeedValue.get())
            return
        }

        mc.timer.timerSpeed = 1F
    }

    override val tag: String?
        get() = "${RandomUtils.nextFloat(minSpeedValue.get(), maxSpeedValue.get()).toString()}"
}
