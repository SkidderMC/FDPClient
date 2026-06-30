/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.client.AntiBot.isBot
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.colorFromDisplayName
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.client.EntityLookup
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.render.ColorSettingsInteger
import net.ccbluex.liquidbounce.utils.render.RenderUtils.draw2D
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityBox
import net.ccbluex.liquidbounce.utils.render.WorldToScreen
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.isEntityHeightVisible
import net.minecraft.client.renderer.GlStateManager.enableTexture2D
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object ESP : Module("ESP", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY) {

    private val projectedPoint = FloatArray(3)

    val mode by choices(
        "Mode",
        arrayOf("Box", "OtherBox", "WireFrame", "2D", "Real2D", "Gaussian", "Outline", "Glow"), "Box"
    )
        .describe("Rendering style for highlighted entities.")

    val outlineWidth by float("Outline-Width", 3f, 0.5f..5f) { mode == "Outline" }
        .describe("Line width of the outline mode.")

    val wireframeWidth by float("WireFrame-Width", 2f, 0.5f..5f) { mode == "WireFrame" }
        .describe("Line width of the wireframe mode.")

    private val glowSettings = GlowRenderSettings(isSupported = { mode == "Glow" }).also { addValues(it.values) }

    private val espColor = ColorSettingsInteger(this, "ESPColor").with(255, 255, 255)

    private val friendColor by color("Friend Color", Color.BLUE)
        .describe("Color used for friends.")
    private val invisibleColor by color("Invisible Color", Color(255, 255, 255))
        .describe("Color used for invisible entities.")

    private val renderFilters = RenderFilterSettings(50, 1..200).also { addValues(it.values) }

    private val colorTeam by boolean("TeamColor", false)
        .describe("Color entities by their team color.")
    private val bot by boolean("Bots", true)
        .describe("Also render detected bots.")

    var renderNameTags = true

    private val entities by EntityLookup<EntityLivingBase>().filter { shouldRender(it) }

    val onRender3D = handler<Render3DEvent> {
        if (entities.isEmpty())
            return@handler

        val projection = WorldToScreen.capture()
        val real2d = mode == "Real2D"

        if (real2d) {
            glPushAttrib(GL_ENABLE_BIT)
            glEnable(GL_BLEND)
            glDisable(GL_TEXTURE_2D)
            glDisable(GL_DEPTH_TEST)
            glMatrixMode(GL_PROJECTION)
            glPushMatrix()
            glLoadIdentity()
            glOrtho(0.0, mc.displayWidth.toDouble(), mc.displayHeight.toDouble(), 0.0, -1.0, 1.0)
            glMatrixMode(GL_MODELVIEW)
            glPushMatrix()
            glLoadIdentity()
            glDisable(GL_DEPTH_TEST)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
            enableTexture2D()
            glDepthMask(true)
            glLineWidth(1f)
        }

        for (entity in entities) {
            val color = getColor(entity)

            val pos = entity.interpolatedPosition(entity.lastTickPos) - mc.renderManager.renderPos

            when (mode) {
                "Box", "OtherBox" -> drawEntityBox(entity, color, mode != "OtherBox")

                "2D" -> {
                    draw2D(entity, pos.xCoord, pos.yCoord, pos.zCoord, color.rgb, Color.BLACK.rgb)
                }

                "Real2D" -> {
                    val bb = entity.hitBox.offset(-entity.currPos + pos)
                    var minX = Float.MAX_VALUE
                    var minY = Float.MAX_VALUE
                    var maxX = -1f
                    var maxY = -1f
                    for (corner in 0 until 8) {
                        val x = if (corner and 1 == 0) bb.minX else bb.maxX
                        val y = if (corner and 2 == 0) bb.minY else bb.maxY
                        val z = if (corner and 4 == 0) bb.minZ else bb.maxZ
                        if (!projection.project(x.toFloat(), y.toFloat(), z.toFloat(), projectedPoint)) continue
                        minX = min(projectedPoint[0], minX)
                        minY = min(projectedPoint[1], minY)
                        maxX = max(projectedPoint[0], maxX)
                        maxY = max(projectedPoint[1], maxY)
                    }
                    // Draw only when at least one corner projected (minX<=maxX rules out the all-failed
                    // sentinels) and the box actually overlaps the screen rect [0,w] x [0,h].
                    if (minX <= maxX && minY <= maxY &&
                        maxX >= 0f && minX <= mc.displayWidth && maxY >= 0f && minY <= mc.displayHeight) {
                        glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, 1f)
                        glBegin(GL_LINE_LOOP)
                        glVertex2f(minX, minY)
                        glVertex2f(minX, maxY)
                        glVertex2f(maxX, maxY)
                        glVertex2f(maxX, minY)
                        glEnd()
                    }
                }
            }
        }

        if (real2d) {
            glColor4f(1f, 1f, 1f, 1f)
            glEnable(GL_DEPTH_TEST)
            glMatrixMode(GL_PROJECTION)
            glPopMatrix()
            glMatrixMode(GL_MODELVIEW)
            glPopMatrix()
            glPopAttrib()
        }
    }

    val onRender2D = handler<Render2DEvent> { event ->
        if (mc.theWorld == null || mode != "Glow" || entities.isEmpty())
            return@handler

        renderNameTags = false

        try {
            entities.groupBy(::getColor).forEach { (color, entities) ->
                renderGlow(event.partialTicks, color, glowSettings) {
                    for (entity in entities) {
                        mc.renderManager.renderEntitySimple(entity, event.partialTicks)
                    }
                }
            }
        } catch (ex: Exception) {
            LOGGER.error("An error occurred while rendering all entities for shader esp", ex)
        }

        renderNameTags = true
    }

    override val tag
        get() = mode

    fun getColor(entity: Entity? = null): Color {
        if (entity != null && entity is EntityLivingBase) {
            if (entity.hurtTime > 0)
                return Color.RED

            if (entity.isInvisible)
                return invisibleColor

            if (entity is EntityPlayer && entity.isClientFriend())
                return friendColor

            if (colorTeam) {
                entity.colorFromDisplayName()?.let {
                    return it
                }
            }
        }

        return espColor.color()
    }

    fun shouldRender(entity: EntityLivingBase): Boolean {
        val player = mc.thePlayer ?: return false

        return (renderFilters.withinDistance(player.getDistanceSqToEntity(entity))
                && (renderFilters.thruBlocks || isEntityHeightVisible(entity))
                && (!renderFilters.onLook || player.isLookingOn(entity, renderFilters.maxAngleDifference.toDouble()))
                && isSelected(entity, false)
                && (bot || !isBot(entity)))
    }

}
