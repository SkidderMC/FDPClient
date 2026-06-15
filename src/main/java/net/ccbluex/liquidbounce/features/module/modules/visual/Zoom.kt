/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

/**
 * Reduces the field of view while enabled to zoom the camera in. Bind it to a key and toggle it.
 * The actual FOV change is applied in MixinEntityRenderer's camera transform.
 */
object Zoom : Module("Zoom", Category.VISUAL, Category.SubCategory.RENDER_SELF, gameDetecting = false) {

    private val zoom by float("Zoom", 3f, 1.5f..10f)

    /** Multiplier applied to the rendered FOV (smaller = more zoom). */
    val fovMultiplier: Float
        get() = 1f / zoom
}
