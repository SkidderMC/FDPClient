/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category

object NoFOV : Module("NoFOV", Category.VISUAL, Category.SubCategory.RENDER_SELF, gameDetecting = false) {
    private val baseFov by float("FOV", 1f, 0f..1.5f)
    private val multiplier by float("Multiplier", 1f, 0.1f..1.5f)
    private val offset by float("Offset", 0f, 0f..1.5f)
    private val limit by float("Limit", 1.5f, 0f..1.5f)

    val fov: Float
        get() = (baseFov * multiplier + offset).coerceIn(0f, limit)
}
