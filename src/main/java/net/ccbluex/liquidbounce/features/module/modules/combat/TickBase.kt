/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import me.zywl.fdpclient.event.AttackEvent
import me.zywl.fdpclient.event.UpdateEvent
import me.zywl.fdpclient.event.EventTarget
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.entity.EntityLivingBase

@ModuleInfo(name = "TickBase", category = ModuleCategory.COMBAT)
class TickBase : Module() {

    private var ticks = 0

    private val ticksAmount = IntegerValue("BoostTicks", 10, 3, 20)
    private val BoostAmount = FloatValue("BoostTimer", 10f, 1f, 50f)
    private val ChargeAmount = FloatValue("ChargeTimer", 0.11f, 0.05f, 1f)

    private val test = BoolValue("Test", false)

    private var prev_fps = 0

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (event.targetEntity is EntityLivingBase && ticks == 0) {
            ticks = ticksAmount.get()
            if (test.get()) {
                ticks = 3
                prev_fps = mc.gameSettings.limitFramerate
                mc.gameSettings.limitFramerate = 4
            }
        }
    }

    override fun onEnable() {
        mc.timer.timerSpeed = 1f
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }


    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (ticks < 0) ticks = 0
        if (test.get()) {
            if (ticks > 0) ticks --
            if (ticks == 1) {
                mc.gameSettings.limitFramerate = prev_fps
            }
            return
        }
        if (ticks == ticksAmount.get()) {
            mc.timer.timerSpeed = ChargeAmount.get()
            ticks --
        } else if (ticks > 1) {
            mc.timer.timerSpeed = BoostAmount.get()
            ticks --
        } else if (ticks == 1) {
            mc.timer.timerSpeed = 1f
            ticks --
        }
    }
}
