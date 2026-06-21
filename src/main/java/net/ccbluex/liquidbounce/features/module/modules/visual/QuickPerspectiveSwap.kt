/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

object QuickPerspectiveSwap : Module("QuickPerspectiveSwap", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY, gameDetecting = false) {

    private val rearView by boolean("RearView", false)

    private var previousView = 0

    override fun onEnable() {
        previousView = mc.gameSettings.thirdPersonView
        mc.gameSettings.thirdPersonView = if (rearView) 2 else 1
    }

    override fun onDisable() {
        mc.gameSettings.thirdPersonView = previousView
    }
}
