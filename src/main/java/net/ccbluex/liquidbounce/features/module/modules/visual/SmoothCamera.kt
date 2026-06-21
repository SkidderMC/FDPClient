/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

/**
 * Enables the vanilla cinematic (filtered) camera so look movement is eased instead of snapping,
 * giving a smoother feel for recording or aiming. Restores your previous setting when disabled.
 */
object SmoothCamera : Module("SmoothCamera", Category.VISUAL, Category.SubCategory.RENDER_SELF, gameDetecting = false) {

    private var previous = false

    override fun onEnable() {
        val settings = mc.gameSettings ?: return
        previous = settings.smoothCamera
        settings.smoothCamera = true
    }

    override fun onDisable() {
        mc.gameSettings?.smoothCamera = previous
    }
}
