/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.timer

import net.minecraft.util.MathHelper

class TickTimer {
    private var tick = 0
    private var lastMS: Long = 0
    private var previousTime: Long = 0

    fun update() {
        tick++
    }

    fun reset() {
        tick = 0
        lastMS = getCurrentMS()
    }

    fun hasTimePassed(ticks: Int): Boolean {
        return tick >= ticks
    }

    fun Timer() {
        lastMS = 0L
        previousTime = -1L
    }

    fun check(milliseconds: Float): Boolean {
        return System.currentTimeMillis() - previousTime >= milliseconds
    }

    fun delay(milliseconds: Double): Boolean {
        return MathHelper.clamp_float((getCurrentMS() - lastMS).toFloat(), 0f, milliseconds.toFloat()) >= milliseconds
    }

    fun getCurrentMS(): Long {
        return System.nanoTime() / 1000000L
    }

}