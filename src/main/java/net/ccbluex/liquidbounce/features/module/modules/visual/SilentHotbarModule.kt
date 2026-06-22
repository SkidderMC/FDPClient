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
        .describe("Keep the held item name highlighted on screen.")
    val keepHotbarSlot by boolean("KeepHotbarSlot", false)
        .describe("Keep showing your visible hotbar slot selection.")
    val keepItemInHandInFirstPerson by boolean("KeepItemInHandInFirstPerson", false)
        .describe("Keep rendering the held item in first person.")
    val keepItemInHandInThirdPerson by boolean("KeepItemInHandInThirdPerson", false)
        .describe("Keep rendering the held item in third person.")
}