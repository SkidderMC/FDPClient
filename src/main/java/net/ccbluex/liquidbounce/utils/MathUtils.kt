package net.ccbluex.liquidbounce.utils

object MathUtils {
    @JvmStatic
    fun radians(degrees: Double):Double{
        return degrees * Math.PI / 180
    }
    
    @JvmStatic
    fun getDistance(x1: Double, y1: Double, x2: Double, y2: Double):Double{
        return Math.sqrt(Math.pow(x1-x2,(2).toDouble())+Math.pow(y1-y2,(2).toDouble()))
    }
    //TODO: 两圆相交坐标求解（月底能有时间弄）（TargetStrafe）
}
