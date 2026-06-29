/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.sorting

/** Applies comparators in order and returns the first non-zero result. */
class ComparatorChain<T>(private vararg val comparators: Comparator<in T>) : Comparator<T> {
    override fun compare(first: T, second: T): Int {
        for (comparator in comparators) {
            val result = comparator.compare(first, second)
            if (result != 0) return result
        }
        return 0
    }
}
