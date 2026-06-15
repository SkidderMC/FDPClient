/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

/**
 * Zooms the camera in while enabled by lowering the field of view. Bind it to a key and toggle it.
 * The real FOV is saved on enable and restored on disable so it never persists.
 */
object Zoom : Module("Zoom", Category.VISUAL, Category.SubCategory.RENDER_SELF, gameDetecting = false) {

    private val zoom by float("Zoom", 3f, 1.5f..10f)

    private var savedFov = -1f

    override fun onEnable() {
        savedFov = mc.gameSettings.fovSetting
        super.onEnable()
    }

    val onUpdate = handler<UpdateEvent> {
        if (savedFov <= 0f) savedFov = mc.gameSettings.fovSetting
        mc.gameSettings.fovSetting = savedFov / zoom
    }

    override fun onDisable() {
        if (savedFov > 0f) mc.gameSettings.fovSetting = savedFov
        savedFov = -1f
        super.onDisable()
    }
}
