/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

object Crosshair : Module("Crosshair", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY, gameDetecting = false) {

    private val size by float("Size", 5f, 1f..20f)
    private val gap by float("Gap", 3f, 0f..15f)
    private val thickness by float("Thickness", 1f, 1f..6f)
    private val dot by boolean("Dot", false)
    private val outline by boolean("Outline", true)
    private val crosshairColor by color("Color", Color(255, 255, 255))

    val onRender2D = handler<Render2DEvent> {
        // Only draw the static crosshair in first person while actually in the world.
        if (mc.gameSettings.thirdPersonView != 0 || mc.currentScreen != null || mc.thePlayer == null) {
            return@handler
        }

        val sr = ScaledResolution(mc)
        val centerX = sr.scaledWidth / 2f
        val centerY = sr.scaledHeight / 2f
        val color = crosshairColor.rgb
        val half = thickness / 2f

        if (outline) {
            val outlineColor = Color(0, 0, 0, crosshairColor.alpha).rgb
            drawArm(centerX, centerY, half + 0.5f, outlineColor)
            if (dot) {
                RenderUtils.drawRect(centerX - half - 0.5f, centerY - half - 0.5f, centerX + half + 0.5f, centerY + half + 0.5f, outlineColor)
            }
        }

        drawArm(centerX, centerY, half, color)
        if (dot) {
            RenderUtils.drawRect(centerX - half, centerY - half, centerX + half, centerY + half, color)
        }
    }

    private fun drawArm(centerX: Float, centerY: Float, half: Float, color: Int) {
        // right, left, bottom, top
        RenderUtils.drawRect(centerX + gap, centerY - half, centerX + gap + size, centerY + half, color)
        RenderUtils.drawRect(centerX - gap - size, centerY - half, centerX - gap, centerY + half, color)
        RenderUtils.drawRect(centerX - half, centerY + gap, centerX + half, centerY + gap + size, color)
        RenderUtils.drawRect(centerX - half, centerY - gap - size, centerX + half, centerY - gap, color)
    }
}
