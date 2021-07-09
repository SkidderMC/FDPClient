package net.ccbluex.liquidbounce.utils

object MathUtils {
    @JvmStatic
    fun radians(degrees: Double):Double{
        return degrees * Math.PI / 180
    }
}