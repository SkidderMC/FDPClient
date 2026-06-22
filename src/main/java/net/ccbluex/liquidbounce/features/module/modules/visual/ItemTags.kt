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
import net.ccbluex.liquidbounce.ui.font.Fonts.minecraftFont
import net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor
import net.ccbluex.liquidbounce.utils.render.RenderUtils.interpolate
import net.ccbluex.liquidbounce.utils.render.RenderUtils.isInViewFrustum
import net.ccbluex.liquidbounce.utils.render.RenderUtils.newDrawRect
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GLAllocation
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.item.EntityItem
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.GLU
import java.awt.Color
import java.nio.FloatBuffer
import java.nio.IntBuffer
import kotlin.math.pow

/**
 * ItemTags
 *
 * Renders small tags (display name and stack size) above dropped item entities.
 */
object ItemTags : Module("ItemTags", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY, gameDetecting = false) {

    private val showName by boolean("ShowName", true)
        .describe("Show the item display name in the tag.")
    private val showCount by boolean("ShowCount", true)
        .describe("Show the stack size in the tag.")

    private val scale by float("Scale", 0.5f, 0.1f..1f)
        .describe("Size of the item tag text.")
    private val yOffset by float("YOffset", 0.6f, -1f..2f)
        .describe("Vertical offset of the tag above the item.")

    private val maxDistance by float("MaxDistance", 64f, 1f..256f)
        .describe("Maximum distance to render item tags.")

    private val background by boolean("Background", true)
        .describe("Draw a background behind the tag text.")
    private val backgroundColor by color("BackgroundColor", Color(0, 0, 0, 120)) { background }
        .describe("Color of the tag background.")
    private val textColor by color("TextColor", Color.WHITE)
        .describe("Color of the tag text.")
    private val shadow by boolean("Shadow", true)
        .describe("Draw a shadow behind the tag text.")

    private val viewport: IntBuffer = GLAllocation.createDirectIntBuffer(16)
    private val modelview: FloatBuffer = GLAllocation.createDirectFloatBuffer(16)
    private val projection: FloatBuffer = GLAllocation.createDirectFloatBuffer(16)
    private val vector: FloatBuffer = GLAllocation.createDirectFloatBuffer(4)

    val onRender2D = handler<Render2DEvent> { event ->
        val world = mc.theWorld ?: return@handler
        val player = mc.thePlayer ?: return@handler

        val maxDistanceSq = maxDistance.toDouble().pow(2.0)

        try {
            GL11.glPushMatrix()

            val scaledResolution = ScaledResolution(mc)
            val scaleFactor = scaledResolution.scaleFactor
            val scaling = scaleFactor.toDouble() / scaleFactor.toDouble().pow(2.0)
            GL11.glScaled(scaling, scaling, scaling)

            val renderMng = mc.renderManager
            val entityRenderer = mc.entityRenderer

            try {
                entityRenderer.setupCameraTransform(event.partialTicks, 0)
            } catch (e: Exception) {
                return@handler
            }

            val entities = world.loadedEntityList.toList()
            for (entity in entities) {
                if (entity !is EntityItem || entity.isDead) continue
                if (player.getDistanceSqToEntity(entity) > maxDistanceSq) continue
                if (!isInViewFrustum(entity)) continue

                val stack = entity.entityItem ?: continue

                val x = interpolate(entity.posX, entity.lastTickPosX, event.partialTicks.toDouble())
                val y = interpolate(entity.posY, entity.lastTickPosY, event.partialTicks.toDouble())
                val z = interpolate(entity.posZ, entity.lastTickPosZ, event.partialTicks.toDouble())

                val screen = project2D(
                    scaleFactor,
                    x - renderMng.viewerPosX,
                    y + yOffset.toDouble() - renderMng.viewerPosY,
                    z - renderMng.viewerPosZ
                ) ?: continue

                if (screen[2] < 0.0 || screen[2] > 1.0) continue

                val tag = buildString {
                    if (showName) append(stripColor(stack.displayName))
                    if (showCount && stack.stackSize > 1) {
                        if (isNotEmpty()) append(" ")
                        append("x").append(stack.stackSize)
                    }
                }
                if (tag.isEmpty()) continue

                try {
                    entityRenderer.setupOverlayRendering()
                } catch (e: Exception) {
                    continue
                }

                drawTag(tag, screen[0], screen[1])
            }
        } catch (e: Exception) {
            // fail soft on render errors
        } finally {
            try {
                GL11.glPopMatrix()
                GlStateManager.enableBlend()
                GlStateManager.resetColor()
                mc.entityRenderer.setupOverlayRendering()
            } catch (e: Exception) {
                // ignore cleanup errors
            }
        }
    }

    private fun drawTag(text: String, screenX: Double, screenY: Double) {
        val sc = scale.toDouble()
        val halfWidth = minecraftFont.getStringWidth(text) / 2f * sc
        val halfHeight = minecraftFont.FONT_HEIGHT / 2f * sc

        if (background) {
            newDrawRect(
                screenX - halfWidth - 2.0 * sc,
                screenY - halfHeight - 1.0 * sc,
                screenX + halfWidth + 2.0 * sc,
                screenY + halfHeight + 1.0 * sc,
                backgroundColor.rgb
            )
        }

        GlStateManager.pushMatrix()
        GlStateManager.translate(screenX - halfWidth, screenY - halfHeight, 0.0)
        GlStateManager.scale(sc, sc, sc)
        if (shadow) {
            minecraftFont.drawStringWithShadow(text, 0f, 0f, textColor.rgb)
        } else {
            minecraftFont.drawString(text, 0, 0, textColor.rgb)
        }
        GlStateManager.popMatrix()
    }

    private fun project2D(scaleFactor: Int, x: Double, y: Double, z: Double): DoubleArray? {
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelview)
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projection)
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewport)
        return if (GLU.gluProject(
                x.toFloat(), y.toFloat(), z.toFloat(),
                modelview, projection, viewport, vector
            )
        ) doubleArrayOf(
            (vector.get(0) / scaleFactor).toDouble(),
            ((Display.getHeight().toFloat() - vector.get(1)) / scaleFactor).toDouble(),
            vector.get(2).toDouble()
        ) else null
    }
}
