/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

object Aspect : Module("Aspect", Category.OTHER, Category.SubCategory.MISCELLANEOUS, gameDetecting = false) {

    private val ratio by int("Ratio", 100, 1..300, suffix = "%")

    fun ratioMultiplier(): Float = ratio.toFloat() / 100f
}
