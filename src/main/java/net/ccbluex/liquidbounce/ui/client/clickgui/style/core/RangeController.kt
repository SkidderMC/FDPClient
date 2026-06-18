/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.core

import kotlin.math.abs

/**
 * Pure range-slider thumb selection shared by every style.
 */
object RangeController {

    /**
     * Index of the thumb closest to [mouseX]: 0 for the first thumb, 1 for the
     * second. Ties favour the first thumb.
     */
    fun nearerThumb(mouseX: Double, firstThumbX: Double, secondThumbX: Double): Int =
        if (abs(mouseX - firstThumbX) <= abs(mouseX - secondThumbX)) 0 else 1
}
