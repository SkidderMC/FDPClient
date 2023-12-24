/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.EnumAutoDisableType
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue

@ModuleInfo(name = "Timer", category = ModuleCategory.WORLD, autoDisable = EnumAutoDisableType.RESPAWN)
object Timer : Module() {

    // private val minSpeedValue = FloatValue("Speed", 2F, 0.1F, 10F)
    val maxSpeedValue: FloatValue = object : FloatValue("Max-Timer", 2F, 0.1F, 10F) {
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
    private val autoDisableValue = BoolValue("AutoDisable", true)
    val smart = BoolValue("Smart", false)

    override fun onDisable() {
        mc.timer.timerSpeed = 1F
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer == null || mc.theWorld == null) return

        if(MovementUtils.isMoving() || !onMoveValue.get()) {
            mc.timer.timerSpeed = RandomUtils.nextFloat(minSpeedValue.get(), maxSpeedValue.get())
            return
        }

        mc.timer.timerSpeed = 1F
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        if (event.worldClient != null)
            return

        if (autoDisableValue.get()) state = false
    }

    override val tag: String?
        get() = "${RandomUtils.nextFloat(minSpeedValue.get(), maxSpeedValue.get()).toString()}"
}