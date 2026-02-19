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
import net.ccbluex.liquidbounce.utils.render.shader.shaders.GlowShader
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.isEntityHeightVisible
import net.minecraft.client.renderer.GlStateManager.enableTexture2D
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11.*
import org.lwjgl.util.vector.Vector3f
import java.awt.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object ESP : Module("ESP", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY) {

    val mode by choices(
        "Mode",
        arrayOf("Box", "OtherBox", "WireFrame", "2D", "Real2D", "Gaussian", "Outline", "Glow"), "Box"
    )

    val outlineWidth by float("Outline-Width", 3f, 0.5f..5f) { mode == "Outline" }

    val wireframeWidth by float("WireFrame-Width", 2f, 0.5f..5f) { mode == "WireFrame" }

    private val glowRenderScale by float("Glow-Renderscale", 1f, 0.5f..2f) { mode == "Glow" }
    private val glowRadius by int("Glow-Radius", 4, 1..5) { mode == "Glow" }
    private val glowFade by int("Glow-Fade", 10, 0..30) { mode == "Glow" }
    private val glowTargetAlpha by float("Glow-Target-Alpha", 0f, 0f..1f) { mode == "Glow" }

    private val espColor = ColorSettingsInteger(this, "ESPColor").with(255, 255, 255)

    private val maxRenderDistance by int("MaxRenderDistance", 50, 1..200).onChanged { value ->
        maxRenderDistanceSq = value.toDouble().pow(2)
    }

    private val onLook by boolean("OnLook", false)
    private val maxAngleDifference by float("MaxAngleDifference", 90f, 5.0f..90f) { onLook }

    private val thruBlocks by boolean("ThruBlocks", true)

    private var maxRenderDistanceSq = 0.0
        set(value) {
            field = if (value <= 0.0) maxRenderDistance.toDouble().pow(2.0) else value
        }

    private val colorTeam by boolean("TeamColor", false)
    private val bot by boolean("Bots", true)

    var renderNameTags = true

    private val entities by EntityLookup<EntityLivingBase>().filter { shouldRender(it) }

    val onRender3D = handler<Render3DEvent> {
        if (entities.isEmpty())
            return@handler

        val mvMatrix = WorldToScreen.getMatrix(GL_MODELVIEW_MATRIX)
        val projectionMatrix = WorldToScreen.getMatrix(GL_PROJECTION_MATRIX)
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
                    val boxVertices = arrayOf(
                        doubleArrayOf(bb.minX, bb.minY, bb.minZ),
                        doubleArrayOf(bb.minX, bb.maxY, bb.minZ),
                        doubleArrayOf(bb.maxX, bb.maxY, bb.minZ),
                        doubleArrayOf(bb.maxX, bb.minY, bb.minZ),
                        doubleArrayOf(bb.minX, bb.minY, bb.maxZ),
                        doubleArrayOf(bb.minX, bb.maxY, bb.maxZ),
                        doubleArrayOf(bb.maxX, bb.maxY, bb.maxZ),
                        doubleArrayOf(bb.maxX, bb.minY, bb.maxZ)
                    )
                    var minX = Float.MAX_VALUE
                    var minY = Float.MAX_VALUE
                    var maxX = -1f
                    var maxY = -1f
                    for (boxVertex in boxVertices) {
                        val screenPos = WorldToScreen.worldToScreen(
                            Vector3f(
                                boxVertex[0].toFloat(),
                                boxVertex[1].toFloat(),
                                boxVertex[2].toFloat()
                            ), mvMatrix, projectionMatrix, mc.displayWidth, mc.displayHeight
                        )
                            ?: continue
                        minX = min(screenPos.x, minX)
                        minY = min(screenPos.y, minY)
                        maxX = max(screenPos.x, maxX)
                        maxY = max(screenPos.y, maxY)
                    }
                    if (minX > 0 || minY > 0 || maxX <= mc.displayWidth || maxY <= mc.displayWidth) {
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
                GlowShader.startDraw(event.partialTicks, glowRenderScale)

                for (entity in entities) {
                    mc.renderManager.renderEntitySimple(entity, event.partialTicks)
                }

                GlowShader.stopDraw(color, glowRadius, glowFade, glowTargetAlpha)
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

            if (entity is EntityPlayer && entity.isClientFriend())
                return Color.BLUE

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

        return (player.getDistanceSqToEntity(entity) <= maxRenderDistanceSq
                && (thruBlocks || isEntityHeightVisible(entity))
                && (!onLook || player.isLookingOnEntity(entity, maxAngleDifference.toDouble()))
                && isSelected(entity, false)
                && (bot || !isBot(entity)))
    }

}
