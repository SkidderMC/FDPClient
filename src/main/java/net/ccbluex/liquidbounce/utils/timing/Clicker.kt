/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.timing

import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils
import java.util.ArrayDeque

class Clicker(private val cycleLength: Int = 20, private val tickMillis: Int = 50) {

    private val queue = ArrayDeque<Int>()
    private var activePattern: ClickPattern? = null
    private var lastMinCps = Int.MIN_VALUE
    private var lastMaxCps = Int.MIN_VALUE
    private var carryTicks = 0

    fun reset() {
        queue.clear()
        carryTicks = 0
        activePattern = null
        lastMinCps = Int.MIN_VALUE
        lastMaxCps = Int.MIN_VALUE
    }

    fun nextDelay(pattern: ClickPattern, minCps: Int, maxCps: Int): Int {
        val low = minCps.coerceAtLeast(1)
        val high = maxCps.coerceAtLeast(low)

        if (pattern !== activePattern || minCps != lastMinCps || maxCps != lastMaxCps) {
            activePattern = pattern
            lastMinCps = minCps
            lastMaxCps = maxCps
            queue.clear()
            carryTicks = 0
        }

        if (queue.isEmpty()) {
            refill(pattern, low, high)
        }

        val next = queue.pollFirst() ?: (1000 / ((low + high) / 2).coerceAtLeast(1))
        return next.coerceAtLeast(1)
    }

    private fun refill(pattern: ClickPattern, low: Int, high: Int) {
        val cycle = IntArray(cycleLength)
        pattern.fill(cycle, low..high)

        var sinceLast = carryTicks
        for (tick in cycle.indices) {
            sinceLast++

            val count = cycle[tick]
            if (count <= 0) continue

            queue.addLast((sinceLast * tickMillis + RandomUtils.nextInt(-8, 9)).coerceAtLeast(1))
            repeat(count - 1) {
                queue.addLast(RandomUtils.nextInt(0, 6))
            }
            sinceLast = 0
        }
        carryTicks = sinceLast

        if (queue.isEmpty()) {
            queue.addLast((cycleLength * tickMillis).coerceAtLeast(1))
            carryTicks = 0
        }
    }
}
