/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.extensions

import net.ccbluex.liquidbounce.config.*
import net.ccbluex.liquidbounce.utils.block.toVec
import net.ccbluex.liquidbounce.utils.rotation.Rotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.getFixedAngleDelta
import net.minecraft.block.Block
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.entity.Entity
import net.minecraft.util.*
import java.math.BigDecimal
import javax.vecmath.Vector2f
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Provides:
 * ```
 * val (x, y, z) = blockPos
 */
operator fun Vec3i.component1() = x
operator fun Vec3i.component2() = y
operator fun Vec3i.component3() = z

/**
 * Provides:
 * ```
 * val (x, y, z) = vec
 */
operator fun Vec3.component1() = xCoord
operator fun Vec3.component2() = yCoord
operator fun Vec3.component3() = zCoord

/**
 * Provides:
 * ```
 * val (x, y) = vec
 */
operator fun Vector2f.component1() = x
operator fun Vector2f.component2() = y

/**
 * Provides:
 * ```
 * val (x, y, z) = mc.thePlayer
 */
operator fun Entity.component1() = posX
operator fun Entity.component2() = posY
operator fun Entity.component3() = posZ

/**
 * Provides:
 * ```
 * val (width, height) = ScaledResolution(mc)
 */
operator fun ScaledResolution.component1() = this.scaledWidth
operator fun ScaledResolution.component2() = this.scaledHeight

/**
 * Provides:
 * `vec + othervec`, `vec - othervec`, `vec * number`, `vec / number`, `-vec`
 * */
operator fun Vec3.plus(vec: Vec3): Vec3 = add(vec)
operator fun Vec3.minus(vec: Vec3): Vec3 = subtract(vec)
operator fun Vec3.times(number: Double) = Vec3(xCoord * number, yCoord * number, zCoord * number)
operator fun Vec3.div(number: Double) = times(1 / number)
operator fun Vec3.unaryMinus(): Vec3 = this.times(-1.0)

fun Vec3i.manhattanDistance(another: Vec3i): Int {
    return abs(x - another.x) + abs(y - another.y) + abs(z - another.z)
}

fun Vec3i.copy(x: Int = this.x, y: Int = this.y, z: Int = this.z) = Vec3i(x, y, z)
fun BlockPos.copy(x: Int = this.x, y: Int = this.y, z: Int = this.z) = BlockPos(x, y, z)
fun BlockPos.MutableBlockPos.copy(x: Int = this.x, y: Int = this.y, z: Int = this.z) = BlockPos.MutableBlockPos(x, y, z)
fun Vec3.copy(x: Double = this.xCoord, y: Double = this.yCoord, z: Double = this.zCoord) = Vec3(x, y, z)

fun BlockPos.immutableCopy() = BlockPos(x, y, z)
fun BlockPos.mutableCopy() = BlockPos.MutableBlockPos(x, y, z)

fun Vec3.offset(direction: EnumFacing, value: Double): Vec3 {
    val vec3i = direction.directionVec

    return Vec3(
        this.xCoord + value * vec3i.x.toDouble(),
        this.yCoord + value * vec3i.y.toDouble(),
        this.zCoord + value * vec3i.z.toDouble()
    )
}

fun Vec3.withY(value: Double, useCurrentY: Boolean = false): Vec3 {
    return Vec3(xCoord, (if (useCurrentY) yCoord else 0.0) + value, zCoord)
}

val Vec3_ZERO: Vec3
    get() = Vec3(0.0, 0.0, 0.0)

val RenderManager.renderPos
    get() = Vec3(renderPosX, renderPosY, renderPosZ)

fun Vec3.toFloatArray() = floatArrayOf(xCoord.toFloat(), yCoord.toFloat(), zCoord.toFloat())
fun Vec3.toDoubleArray() = doubleArrayOf(xCoord, yCoord, zCoord)

fun Float.ceilInt() = MathHelper.ceiling_float_int(this)
fun Float.floorInt() = MathHelper.floor_float(this)
fun Float.toRadians() = this * 0.017453292f
fun Float.toRadiansD() = toRadians().toDouble()
fun Float.toDegrees() = this * 57.29578f
fun Float.toDegreesD() = toDegrees().toDouble()
fun Float.withGCD() = (this / getFixedAngleDelta()).roundToInt() * getFixedAngleDelta()

/**
 * Prevents possible NaN / (-) Infinity results.
 */
infix fun Int.safeDiv(b: Int) = if (b == 0) 0f else this.toFloat() / b.toFloat()
infix fun Int.safeDivI(b: Int) = if (b == 0) 0 else this / b
infix fun Int.safeDivD(b: Double) = if (b == 0.0) 0.0 else this / b

infix fun Float.safeDiv(b: Float) = if (b == 0f) 0f else this / b

fun Double.ceilInt() = MathHelper.ceiling_double_int(this)
fun Double.floorInt() = MathHelper.floor_double(this)
fun Double.toRadians() = this * 0.017453292
fun Double.toRadiansF() = toRadians().toFloat()
fun Double.toDegrees() = this * 57.295779513
fun Double.toDegreesF() = toDegrees().toFloat()
fun Double.withGCD() = (this / getFixedAngleDelta()).roundToInt() * getFixedAngleDelta().toDouble()

val Vector2f.abs
    get() = Vector2f(abs(x), abs(y))

/**
 * Provides: (step is 0.1 by default)
 * ```
 *      for (x in 0.1..0.9 step 0.05) {}
 *      for (y in 0.1..0.9) {}
 */
class RangeIterator(
    private val range: ClosedFloatingPointRange<Double>, private val step: Double = 0.1,
) : Iterator<Double> {
    private var value = range.start

    override fun hasNext() = value < range.endInclusive

    override fun next(): Double {
        val returned = value
        value = (value + step).coerceAtMost(range.endInclusive)
        return returned
    }
}

operator fun ClosedFloatingPointRange<Double>.iterator() = RangeIterator(this)
infix fun ClosedFloatingPointRange<Double>.step(step: Double) = RangeIterator(this, step)

fun ClosedFloatingPointRange<Float>.random(): Float {
    require(start.isFinite())
    require(endInclusive.isFinite())
    return (start + (endInclusive - start) * Math.random()).toFloat()
}

/**
 * Conditionally shuffles an `Iterable`
 * @param shuffle determines if the returned `Iterable` is shuffled
 */
fun <T> Iterable<T>.shuffled(shuffle: Boolean) = toMutableList().apply { if (shuffle) shuffle() }

fun AxisAlignedBB.lerpWith(x: Double, y: Double, z: Double) =
    Vec3(minX + (maxX - minX) * x, minY + (maxY - minY) * y, minZ + (maxZ - minZ) * z)

fun AxisAlignedBB.lerpWith(point: Vec3) = lerpWith(point.xCoord, point.yCoord, point.zCoord)
fun AxisAlignedBB.lerpWith(value: Double) = lerpWith(value, value, value)
fun AxisAlignedBB.offset(other: Vec3) = offset(other.xCoord, other.yCoord, other.zCoord)
fun AxisAlignedBB.offset(other: BlockPos) = offset(other.toVec())

val AxisAlignedBB.center
    get() = lerpWith(0.5)

fun AxisAlignedBB.getPointSequence(step: Double): Sequence<Vec3> {
    require(step in 0.0..1.0)

    return sequence {
        var x = 0.0
        while (x <= 1.0) {
            var y = 0.0
            while (y <= 1.0) {
                var z = 0.0
                while (z <= 1.0) {
                    yield(lerpWith(x, y, z))
                    z += step
                }
                y += step
            }
            x += step
        }
    }
}

fun Block.lerpWith(x: Double, y: Double, z: Double) = Vec3(
    blockBoundsMinX + (blockBoundsMaxX - blockBoundsMinX) * x,
    blockBoundsMinY + (blockBoundsMaxY - blockBoundsMinY) * y,
    blockBoundsMinZ + (blockBoundsMaxZ - blockBoundsMinZ) * z
)

fun Vec3.lerpWith(other: Vec3, tickDelta: Double) = Vec3(
    xCoord + (other.xCoord - xCoord) * tickDelta,
    yCoord + (other.yCoord - yCoord) * tickDelta,
    zCoord + (other.zCoord - zCoord) * tickDelta
)

fun Rotation.lerpWith(other: Rotation, tickDelta: Number) =
    Rotation((yaw..other.yaw).lerpWith(tickDelta), (pitch..other.pitch).lerpWith(tickDelta))

fun Vec3.lerpWith(other: Vec3, tickDelta: Float) = lerpWith(other, tickDelta.toDouble())

fun ClosedFloatingPointRange<Double>.lerpWith(t: Number) = start + (endInclusive - start) * t.toDouble()

fun ClosedFloatingPointRange<Float>.lerpWith(t: Number) = start + (endInclusive - start) * t.toFloat()

fun IntRangeValue.lerpWith(t: Float) = (minimum + (maximum - minimum) * t).roundToInt()

fun FloatRangeValue.lerpWith(t: Float) = minimum + (maximum - minimum) * t

fun IntValue.lerpWith(t: Float) = (minimum + (maximum - minimum) * t).roundToInt()

fun FloatValue.lerpWith(t: Float) = minimum + (maximum - minimum) * t

fun IntRange.lerpWith(t: Float) = start + (endInclusive - start) * t

fun Int.lerpWith(other: Int, t: Float) = this + (other - this) * t

fun decimalPlaces(value: Float): Int {
    var count = 0
    var v = value

    while (v != v.toInt().toFloat()) {
        v *= 10
        count++
    }

    return count
}

fun interpolate(oldValue: Double, newValue: Double, interpolationValue: Double): Double {
    return oldValue + (newValue - oldValue) * interpolationValue
}

fun interpolateFloat(oldValue: Float, newValue: Float, interpolationValue: Double): Float {
    return interpolate(oldValue.toDouble(), newValue.toDouble(), interpolationValue.toFloat().toDouble()).toFloat()
}

fun round(value: Double, inc: Double): Double {
    return if (inc == 0.0) value else if (inc == 1.0) Math.round(value).toDouble() else {
        val halfOfInc = inc / 2.0
        val floored = floor(value / inc) * inc
        if (value >= floored + halfOfInc) BigDecimal(ceil(value / inc) * inc)
            .toDouble() else BigDecimal(floored)
            .toDouble()
    }
}

fun roundToHalf(d: Double): Double {
    return Math.round(d * 2.0) / 2.0
}

fun roundX(value: Double, inc: Double): Double {
    when (inc) {
        0.0 -> return value
        1.0 -> return Math.round(value).toDouble()
        else -> {
            val halfOfInc = inc / 2.0
            val floored = floor(value / inc) * inc

            return if (value >= floored + halfOfInc) BigDecimal(ceil(value / inc) * inc)
                .toDouble()
            else BigDecimal(floored)
                .toDouble()
        }
    }
}

fun randomizeDouble(min: Double, max: Double): Double {
    return Math.random() * (max - min) + min
}

fun lerp(min: Float, max: Float, delta: Float): Float {
    return min + (max - min) * delta
}