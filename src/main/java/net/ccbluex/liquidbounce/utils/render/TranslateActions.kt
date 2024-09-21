/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render

class TranslateActions(var x: Float, var y: Float) {
    fun interpolate(targetX: Float, targetY: Float, smoothing: Double) {
        x = AnimationUtils.doubleAnimate(targetX.toDouble(), this.x.toDouble(), smoothing).toFloat()
        y = AnimationUtils.doubleAnimate(targetY.toDouble(), this.y.toDouble(), smoothing).toFloat()
    }

    fun translate(targetX: Float, targetY: Float, speed: Double) {
        x = AnimationUtils.lstransition(x, targetX, speed)
        y = AnimationUtils.lstransition(y, targetY, speed)
    }

    fun translate(targetX: Float, targetY: Float) {
        x = AnimationUtils.lstransition(x, targetX, 0.0)
        y = AnimationUtils.lstransition(y, targetY, 0.0)
    }
}