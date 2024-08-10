/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.extensions

import java.awt.Color

fun String.toLowerCamelCase() = this.replaceFirst(this.toCharArray()[0], this.toCharArray()[0].lowercaseChar())

fun Color.setAlpha(factor: Int) = Color(this.red, this.green, this.blue, factor.coerceIn(0, 255))