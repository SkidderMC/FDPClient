/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.TickEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.minecraft.util.MathHelper
import java.io.IOException

object TickBase : Module("TickBase", category = ModuleCategory.COMBAT) {

    private val rangeValue = FloatValue("Range", 3.0f, 1f, 8f)
    private var skippedTick = 0
    private var preTick = 0
    private var flag = false
    private val killAura = FDPClient.moduleManager.getModule(KillAura::class.java)
    override fun onDisable() {
        mc.timer.timerSpeed = 1.0f
    }

    override fun onEnable() {
        mc.timer.timerSpeed = 1.0f
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (!isMoving() || killAura!!.currentTarget == null) {
            mc.timer.timerSpeed = 1.0f
        }
    }

    @EventTarget
    fun onTick(event: TickEvent?) {
        if (flag) return
        if (killAura!!.currentTarget == null) {
            sleep()
        } else {
            if (shouldSkip()) {
                flag = true
                for (i in 0 until preTick) {
                    try {
                        mc.runTick()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                flag = false
            } else {
                sleep()
            }
        }
    }

    private fun sleep() {
        if (skippedTick > 0) {
            try {
                Thread.sleep(2L * skippedTick)
                skippedTick = 0
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            mc.timer.timerSpeed = 0.054f + skippedTick
        }
    }

    fun shouldSkip(): Boolean {
        val target = killAura!!.currentTarget
        if (target == null || skippedTick > 5 || !mc.thePlayer.isSprinting) return false
        val dx = mc.thePlayer.posX - target.posX
        val dz = mc.thePlayer.posZ - target.posZ
        if (MathHelper.sqrt_double(dx * dx + dz * dz) > rangeValue.value) {
            preTick = (2 * (MathHelper.sqrt_double(dx * dx + dz * dz) - rangeValue.value)).toInt()
            skippedTick += preTick
            return true
        }
        return false
    }
}