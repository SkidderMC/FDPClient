/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
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

fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
    require(index1 in indices && index2 in indices)
    if (index1 == index2) {
        return
    }
    val elem = this[index1]
    this[index1] = this[index2]
    this[index2] = elem
}