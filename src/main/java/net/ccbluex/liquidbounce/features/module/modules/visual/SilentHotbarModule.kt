/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

object SilentHotbarModule : Module("SilentHotbar", Category.VISUAL, Category.SubCategory.RENDER_SELF) {
    val keepHighlightedName by boolean("KeepHighlightedName", false)
    val keepHotbarSlot by boolean("KeepHotbarSlot", false)
    val keepItemInHandInFirstPerson by boolean("KeepItemInHandInFirstPerson", false)
    val keepItemInHandInThirdPerson by boolean("KeepItemInHandInThirdPerson", false)
}