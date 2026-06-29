/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color

object ItemChams : Module("ItemChams", Category.VISUAL, Category.SubCategory.RENDER_SELF, subjective = true) {
    private val throughWalls by boolean("ThroughWalls", true)
        .describe("Render the held item without depth occlusion.")
    private val blend by boolean("Blend", true)
        .describe("Blend a configurable tint over the held item.")
    private val tint by color("Tint", Color(35, 120, 255, 165)) { blend }

    @JvmStatic
    fun beginRender() {
        if (!handleEvents()) return
        if (throughWalls) {
            GlStateManager.disableDepth()
            GlStateManager.depthMask(false)
        }
        if (blend) {
            GlStateManager.enableBlend()
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
            val color = tint
            GlStateManager.color(color.red / 255F, color.green / 255F, color.blue / 255F, color.alpha / 255F)
        }
    }

    @JvmStatic
    fun endRender() {
        if (!handleEvents()) return
        GlStateManager.color(1F, 1F, 1F, 1F)
        GlStateManager.disableBlend()
        GlStateManager.depthMask(true)
        GlStateManager.enableDepth()
    }
}
