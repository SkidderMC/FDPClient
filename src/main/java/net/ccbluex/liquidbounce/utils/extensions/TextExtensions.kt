/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.extensions

import net.ccbluex.liquidbounce.utils.render.AnimationUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import java.awt.Color

fun String.toLowerCamelCase() = this.replaceFirst(this.toCharArray()[0], this.toCharArray()[0].lowercaseChar())

fun Color.darker(factor: Float) = Color(this.red / 255F * factor.coerceIn(0F, 1F), this.green / 255F * factor.coerceIn(0F, 1F), this.blue / 255F * factor.coerceIn(0F, 1F), this.alpha / 255F)
fun Color.setAlpha(factor: Int) = Color(this.red, this.green, this.blue, factor.coerceIn(0, 255))

fun Float.animSmooth(target: Float, speed: Float): Float {
    return AnimationUtils.animate(target, this, speed * RenderUtils.deltaTime * 0.025F)
}

fun Float.animLinear(speed: Float, min: Float, max: Float): Float {
    return (this + speed).coerceIn(min, max)
}