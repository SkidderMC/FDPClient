/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.timing

import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils
import java.util.Random
import kotlin.math.max

interface ClickPattern {
    fun fill(clickArray: IntArray, cps: IntRange)
}

object ClickPatterns {

    val gaussianRandom = Random()

    val STABILIZED: ClickPattern = object : ClickPattern {
        override fun fill(clickArray: IntArray, cps: IntRange) {
            val clicks = cps.random()
            val interval = if (clicks > 0) clickArray.size / clicks else 0
            var remainder = if (clicks > 0) clickArray.size % clicks else 0
            var currentIndex = 0

            repeat(clicks) {
                clickArray[currentIndex % clickArray.size]++
                currentIndex += max(interval, 1)
                if (remainder > 0) {
                    currentIndex++
                    remainder--
                }
            }
        }
    }

    val SPAMMING: ClickPattern = object : ClickPattern {
        override fun fill(clickArray: IntArray, cps: IntRange) {
            val clicks = cps.random()

            repeat(clicks) {
                clickArray[clickArray.indices.random()]++
            }
        }
    }

    val BUTTERFLY: ClickPattern = object : ClickPattern {
        override fun fill(clickArray: IntArray, cps: IntRange) {
            val clicks = cps.random()

            while (clickArray.sum() < clicks) {
                val empty = clickArray.indices.filter { clickArray[it] == 0 }

                if (empty.isNotEmpty()) {
                    clickArray[empty.random()] = RandomUtils.nextInt(1, 3)
                } else {
                    clickArray[clickArray.indices.random()]++
                }
            }
        }
    }

    val DOUBLE_CLICK: ClickPattern = object : ClickPattern {
        override fun fill(clickArray: IntArray, cps: IntRange) {
            val clicks = cps.random()

            repeat(clicks) {
                clickArray[clickArray.indices.random()] += 2
            }
        }
    }

    val DRAG: ClickPattern = object : ClickPattern {
        override fun fill(clickArray: IntArray, cps: IntRange) {
            val clicks = cps.random()
            val travelTime = RandomUtils.nextInt(17, 19)

            while (clickArray.sum() < clicks) {
                val index = clickArray.copyOf(travelTime).indices.minByOrNull { clickArray[it] } ?: 0
                clickArray[index]++
            }
        }
    }

    val EFFICIENT: ClickPattern = object : ClickPattern {
        override fun fill(clickArray: IntArray, cps: IntRange) {
            val clicks = cps.random()

            if (clicks < 10) {
                STABILIZED.fill(clickArray, cps)
                return
            }

            for (i in 0 until clicks) {
                clickArray[i * 2 % clickArray.size]++
            }
        }
    }

    val NORMAL_DISTRIBUTION: ClickPattern = object : ClickPattern {
        override fun fill(clickArray: IntArray, cps: IntRange) {
            val bands = arrayOf(
                doubleArrayOf(10.0 / 110.0, 179.5242718446602, 20.416937885616676),
                doubleArrayOf(0.0, 87.88, 13.420088130563776)
            )

            var t = 0.0

            while (true) {
                val v = RandomUtils.nextDouble(0.0, 1.0)
                val band = bands.first { v >= it[0] }

                t += (band[1] + gaussianRandom.nextGaussian() * band[2]) * 20.0 / 1000.0

                if (t > 20.0) {
                    break
                }

                val index = t.toInt()
                if (index in clickArray.indices) {
                    clickArray[index]++
                }
            }
        }
    }

    fun newClickArray(cycleLength: Int = 20): IntArray = IntArray(cycleLength)
}
