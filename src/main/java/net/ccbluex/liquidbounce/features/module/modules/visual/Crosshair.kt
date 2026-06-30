/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

object Crosshair : Module("Crosshair", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY, gameDetecting = false) {

    private val style by choices("Style", arrayOf("Cross", "CSGO", "Dot", "Circle"), "Cross")
        .describe("Shape style of the crosshair.")

    // When on, the vanilla game crosshair is hidden so only this module's crosshair shows.
    val hideVanilla by boolean("HideVanilla", true)
        .describe("Hide the default vanilla crosshair.")

    private val size by float("Size", 5f, 1f..20f) { style == "Cross" || style == "CSGO" }
        .describe("Length of the crosshair arms.")
    private val gap by float("Gap", 3f, 0f..15f) { style == "Cross" || style == "CSGO" }
        .describe("Gap between the center and the arms.")
    private val thickness by float("Thickness", 1f, 1f..6f) { style == "Cross" || style == "CSGO" }
        .describe("Thickness of the crosshair arms.")
    private val centerDot by boolean("CenterDot", false) { style == "Cross" }
        .describe("Draw a dot in the center of the cross.")
    private val outline by boolean("Outline", true)
        .describe("Draw a black outline around the crosshair.")

    // CSGO dynamic spread: the gap shrinks/expands while sprinting just like in-game.
    private val dynamicGap by boolean("DynamicGap", true) { style == "CSGO" }
        .describe("Expand the gap while sprinting.")
    private val sprintGap by float("SprintGap", 2f, 0f..15f) { style == "CSGO" && dynamicGap }
        .describe("Gap size to use while sprinting.")

    private val dotRadius by float("DotRadius", 2.5f, 0.5f..10f) { style == "Dot" }
        .describe("Radius of the dot crosshair.")

    private val filled by boolean("Filled", false) { style == "Circle" }
        .describe("Fill the circle instead of a ring.")
    private val radius by float("Radius", 6f, 1f..30f) { style == "Circle" }
        .describe("Radius of the circle crosshair.")
    private val circleWidth by float("CircleWidth", 1.5f, 0.5f..6f) { style == "Circle" && !filled }
        .describe("Line width of the circle ring.")

    private val crosshairColor by color("Color", Color(255, 255, 255), rainbow = true)
        .describe("Color of the crosshair.")

    private val generalGroup = Configurable("General")
    private val crossGroup = Configurable("Cross")
    private val csgoGroup = Configurable("CSGO")
    private val dotGroup = Configurable("Dot")
    private val circleGroup = Configurable("Circle")

    init {
        moveValues(generalGroup, "Style", "HideVanilla", "Outline", "Color")
        moveValues(crossGroup, "Size", "Gap", "Thickness", "CenterDot")
        moveValues(csgoGroup, "DynamicGap", "SprintGap")
        moveValues(dotGroup, "DotRadius")
        moveValues(circleGroup, "Filled", "Radius", "CircleWidth")

        addValues(listOf(generalGroup, crossGroup, csgoGroup, dotGroup, circleGroup))
    }

    private fun moveValues(group: Configurable, vararg names: String) {
        for (name in names) {
            values.firstOrNull { it.matchesKey(name) }?.let(group::addValue)
        }
    }

    val onRender2D = handler<Render2DEvent> {
        // Only draw in first person while actually in the world.
        if (mc.gameSettings.thirdPersonView != 0 || mc.currentScreen != null || mc.thePlayer == null) {
            return@handler
        }

        val sr = ScaledResolution(mc)
        val centerX = sr.scaledWidth / 2f
        val centerY = sr.scaledHeight / 2f

        when (style) {
            "Cross" -> drawCross(centerX, centerY, gap, centerDot)
            "CSGO" -> {
                val effGap = if (dynamicGap && mc.thePlayer.isSprinting) sprintGap else gap
                drawCross(centerX, centerY, effGap, true)
            }
            "Dot" -> drawDot(centerX, centerY)
            "Circle" -> drawCircleStyle(centerX, centerY)
        }
    }

    private fun drawCross(centerX: Float, centerY: Float, gapValue: Float, withDot: Boolean) {
        val color = crosshairColor.rgb
        val half = thickness / 2f

        if (outline) {
            val outlineColor = Color(0, 0, 0, crosshairColor.alpha).rgb
            drawArms(centerX, centerY, gapValue, half + 0.5f, outlineColor)
            if (withDot) {
                RenderUtils.drawRect(centerX - half - 0.5f, centerY - half - 0.5f, centerX + half + 0.5f, centerY + half + 0.5f, outlineColor)
            }
        }

        drawArms(centerX, centerY, gapValue, half, color)
        if (withDot) {
            RenderUtils.drawRect(centerX - half, centerY - half, centerX + half, centerY + half, color)
        }
    }

    private fun drawArms(centerX: Float, centerY: Float, gapValue: Float, half: Float, color: Int) {
        // right, left, bottom, top
        RenderUtils.drawRect(centerX + gapValue, centerY - half, centerX + gapValue + size, centerY + half, color)
        RenderUtils.drawRect(centerX - gapValue - size, centerY - half, centerX - gapValue, centerY + half, color)
        RenderUtils.drawRect(centerX - half, centerY + gapValue, centerX + half, centerY + gapValue + size, color)
        RenderUtils.drawRect(centerX - half, centerY - gapValue - size, centerX + half, centerY - gapValue, color)
    }

    private fun drawDot(centerX: Float, centerY: Float) {
        if (outline) {
            RenderUtils.drawFilledCircle(centerX.toInt(), centerY.toInt(), dotRadius + 0.6f, Color(0, 0, 0, crosshairColor.alpha))
        }
        RenderUtils.drawFilledCircle(centerX.toInt(), centerY.toInt(), dotRadius, crosshairColor)
    }

    private fun drawCircleStyle(centerX: Float, centerY: Float) {
        if (filled) {
            if (outline) {
                RenderUtils.drawFilledCircle(centerX.toInt(), centerY.toInt(), radius + 0.6f, Color(0, 0, 0, crosshairColor.alpha))
            }
            RenderUtils.drawFilledCircle(centerX.toInt(), centerY.toInt(), radius, crosshairColor)
        } else {
            if (outline) {
                drawRing(centerX, centerY, radius, circleWidth + 1f, Color(0, 0, 0, crosshairColor.alpha))
            }
            drawRing(centerX, centerY, radius, circleWidth, crosshairColor)
        }
    }

    private fun drawRing(cx: Float, cy: Float, r: Float, width: Float, color: Color) {
        GL11.glPushMatrix()
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glLineWidth(width)
        GL11.glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
        GL11.glBegin(GL11.GL_LINE_LOOP)
        var i = 0
        while (i < 360) {
            val rad = Math.toRadians(i.toDouble())
            GL11.glVertex2d(cx + cos(rad) * r, cy + sin(rad) * r)
            i += 4
        }
        GL11.glEnd()
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)
        GlStateManager.resetColor()
        GL11.glPopMatrix()
    }
}
