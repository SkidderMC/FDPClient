/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.kotlin

inline fun <T> MutableCollection<T>.removeEach(max: Int = this.size, predicate: (T) -> Boolean) {
    var i = 0
    val iterator = iterator()
    while (iterator.hasNext()) {
        if (i > max) {
            break
        }
        val next = iterator.next()
        if (predicate(next)) {
            iterator.remove()
            i++
        }
    }
}
fun IntRange.coerceIn(range: IntRange): IntRange {
    val newStart = this.first.coerceIn(range)
    val newEnd = this.last.coerceIn(range)
    return newStart..newEnd
}
fun ClosedFloatingPointRange<Float>.coerceIn(range: ClosedFloatingPointRange<Float>): ClosedFloatingPointRange<Float> {
    val newStart = this.start.coerceIn(range.start, range.endInclusive)
    val newEnd = this.endInclusive.coerceIn(range.start, range.endInclusive)
    return newStart..newEnd
}