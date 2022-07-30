/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.utils.extensions

import java.awt.Color

fun Color.darker(factor: Float) = Color(this.red / 255F * factor.coerceIn(0F, 1F), this.green / 255F * factor.coerceIn(0F, 1F), this.blue / 255F * factor.coerceIn(0F, 1F), this.alpha / 255F)
fun Color.setAlpha(factor: Float) = Color(this.red / 255F, this.green / 255F, this.blue / 255F, factor.coerceIn(0F, 1F))
fun Color.setAlpha(factor: Int) = Color(this.red, this.green, this.blue, factor.coerceIn(0, 255))