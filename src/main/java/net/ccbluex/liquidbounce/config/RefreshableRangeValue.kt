/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.config

import kotlin.random.Random

/** A float range whose random sample remains stable until the owning action refreshes it. */
class RefreshableRangeValue(
    name: String,
    value: ClosedFloatingPointRange<Float>,
    range: ClosedFloatingPointRange<Float>,
    suffix: String? = null
) : FloatRangeValue(name, value, range, suffix) {

    var sample: Float = choose(Random.Default)
        private set

    init {
        onChanged { refresh() }
    }

    override fun describe(text: String): RefreshableRangeValue = apply { descriptionField = text }

    fun refresh(random: Random = Random.Default): Float {
        sample = choose(random)
        return sample
    }

    private fun choose(random: Random): Float {
        val lower = value.start
        val upper = value.endInclusive
        require(lower.isFinite() && upper.isFinite()) { "Refreshable range bounds must be finite" }
        return if (lower >= upper) lower else random.nextDouble(lower.toDouble(), upper.toDouble()).toFloat()
    }
}
