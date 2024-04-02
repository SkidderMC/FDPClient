/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.TickEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.FloatValue

@ModuleInfo(name = "MemoryFix", category = ModuleCategory.OTHER)
object MemoryFix : Module() {
    private const val DEFAULT_DELAY_MS = 120000L
    private const val DEFAULT_LIMIT = 80.0f
    private const val DEFAULT_SPEED = 0.05f

    private val delay = FloatValue("Delay", DEFAULT_DELAY_MS.toFloat(), 10000f, 600000f)
    private val limit = FloatValue("Limit", DEFAULT_LIMIT, 20.0f, 95.0f)
    private val speed = FloatValue("Speed", DEFAULT_SPEED, 0.0f, 1.0f)

    private val timer = MSTimer()

    override fun onEnable() {
        super.onEnable()
        Runtime.getRuntime().gc()
    }

    @EventTarget
    fun onTick(tickEvent: TickEvent?) {
        val maxMemory = Runtime.getRuntime().maxMemory().toFloat()
        val usedMemory = (maxMemory - Runtime.getRuntime().freeMemory()).toFloat()
        val memoryUsagePercentage = usedMemory * 100.0f / maxMemory

        if (timer.hasTimePassed(delay.get().toLong()) && memoryUsagePercentage >= limit.get()) {
            System.gc()
            timer.reset()
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent?) {
        val keyBind = mc.gameSettings.keyBindForward
        val player = mc.thePlayer

        if (keyBind.pressed && player.onGround) {
            player.cameraPitch = speed.get()
        } else {
            player.cameraPitch = 0.0f
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent?) {
        System.gc()
    }
}
