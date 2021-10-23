package net.ccbluex.liquidbounce.utils

import kotlin.math.pow
import kotlin.math.sqrt

object MathUtils {
    @JvmStatic
    fun radians(degrees: Double): Double {
        return degrees * Math.PI / 180
    }

    @JvmStatic
    fun getDistance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        return sqrt((x1 - x2).pow((2).toDouble()) + (y1 - y2).pow((2).toDouble()))
    }
    // TODO: 两圆相交坐标求解（月底能有时间弄）（TargetStrafe）

    @JvmStatic
    fun lerp(a: Array<Double>, b: Array<Double>, t: Double) = arrayOf(a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t)

    @JvmStatic
    fun calcCurvePoint(points: Array<Array<Double>>, t: Double): Array<Double> {
        val cpoints = mutableListOf<Array<Double>>()
        for (i in 0 until (points.size - 1)) {
            cpoints.add(lerp(points[i], points[i + 1], t))
        }
        return if (cpoints.size == 1) {
            cpoints[0]
        } else {
            calcCurvePoint(cpoints.toTypedArray(), t)
        }
    }

    @JvmStatic
    fun getPointsOnCurve(points: Array<Array<Double>>, num: Int): Array<Array<Double>> {
        val cpoints = mutableListOf<Array<Double>>()
        for (i in 0 until num) {
            val t = i / (num - 1.0)
            cpoints.add(calcCurvePoint(points, t))
        }
        return cpoints.toTypedArray()
    }
}
