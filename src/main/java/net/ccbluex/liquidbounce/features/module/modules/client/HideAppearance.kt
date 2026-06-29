/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

object HideAppearance : Module(
    "HideAppearance",
    Category.CLIENT,
    Category.SubCategory.CLIENT_GENERAL,
    subjective = true,
    defaultHidden = true,
) {
    override fun onEnable() {
        mc.displayGuiScreen(null)
    }
}
