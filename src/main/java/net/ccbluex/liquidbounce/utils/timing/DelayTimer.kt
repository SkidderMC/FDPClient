/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.timing

import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.CLICK_TIMER
import net.ccbluex.liquidbounce.utils.timing.TimeUtils.randomDelay
import net.ccbluex.liquidbounce.config.IntValue

open class DelayTimer(
    private val minDelayValue: IntValue, private val maxDelayValue: IntValue = minDelayValue,
    private val baseTimer: MSTimer = CLICK_TIMER
) {
    private var delay = 0

    open fun hasTimePassed() = baseTimer.hasTimePassed(delay)

    fun resetDelay() {
        delay = randomDelay(minDelayValue.get(), maxDelayValue.get())
    }

    fun resetTimer() = baseTimer.reset()

    fun reset() {
        resetTimer()
        resetDelay()
    }
}

open class TickDelayTimer(
    private val minDelayValue: IntValue, private val maxDelayValue: IntValue = minDelayValue,
    private val baseTimer: TickTimer = TickTimer()
) {
    private var ticks = 0

    open fun hasTimePassed() = baseTimer.hasTimePassed(ticks)

    fun resetTicks() {
        ticks = randomDelay(minDelayValue.get(), maxDelayValue.get())
    }

    fun resetTimer() = baseTimer.reset()

    fun update() = baseTimer.update()

    fun reset() {
        resetTimer()
        resetTicks()
    }
}