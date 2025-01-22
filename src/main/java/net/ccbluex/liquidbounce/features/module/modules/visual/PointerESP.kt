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
import net.ccbluex.liquidbounce.features.module.modules.client.Teams
import net.ccbluex.liquidbounce.utils.attack.EntityUtils
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.colorFromDisplayName
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.getHealth
import net.ccbluex.liquidbounce.utils.client.EntityLookup
import net.ccbluex.liquidbounce.utils.render.ColorSettingsInteger
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.*

object PointerESP : Module("PointerESP", Category.VISUAL) {
    private val dimension by choices("Dimension", arrayOf("2d", "3d"), "2d")
    private val mode by choices("Mode", arrayOf("Solid", "Line", "LoopLine"), "Solid")
    private val thickness by float("Thickness", 3f, 1f..5f) { mode.contains("Line") }

    private val colors = ColorSettingsInteger(this, "Colors") { healthMode == "None" }.with(255, 111, 255)

    private val healthMode by choices("Health-Mode", arrayOf("None", "Custom"), "Custom")
    private val healthColors = ColorSettingsInteger(this, "Health")
    { healthMode == "Custom" }.with(255, 255, 0)

    private val absorption by boolean("Absorption", true) { healthMode == "Custom" }
    private val healthFromScoreboard by boolean("HealthFromScoreboard", true) { healthMode == "Custom" }

    private val distanceAlpha by boolean("DistanceAlpha", true)
    private val alphaMin by int("AlphaMin", 100, -50..255) { distanceAlpha }

    private val maxRenderDistance by int("MaxRenderDistance", 50, 1..200).onChanged { value ->
        maxRenderDistanceSq = value.toDouble().pow(2)
    }

    private var maxRenderDistanceSq = 0.0
        set(value) {
            field = if (value <= 0.0) maxRenderDistance.toDouble().pow(2.0) else value
        }

    private val arrowSize by int("ArrowSize", 10, 1..30)
    private val arrowAngle by int("ArrowAngle", 50, 10..90)
    private val arrowRadius by float("ArrowRadius", 50f, 10f..100f)

    private val team by boolean("Team", true)
    private val colorTeam by boolean("TeamColor", false)
    private val bot by boolean("Bots", true)

    private val entities by EntityLookup<EntityLivingBase>()
        .filter { bot || !isBot(it) }
        .filter { team || !Teams.isInYourTeam(it) }
        .filter { EntityUtils.isSelected(it, false) }

    val onRender2D = handler<Render2DEvent> { event ->
        if (dimension != "2d") return@handler

        val scaledResolution = ScaledResolution(mc)

        glPushAttrib(GL_ALL_ATTRIB_BITS)
        glPushMatrix()
        glTranslatef(scaledResolution.scaledWidth / 2f, scaledResolution.scaledHeight / 2f, 0f)

        draw(event.partialTicks)

        glPopMatrix()
        glPopAttrib()
    }

    val onRender3D = handler<Render3DEvent> { event ->
        if (dimension == "2d") return@handler

        val player = mc.thePlayer ?: return@handler

        glDisable(GL_CULL_FACE)
        glEnable(GL_POLYGON_OFFSET_FILL)
        glPolygonOffset(1f, -1000000f)

        glPushMatrix()
        glScaled(0.01, 0.01, 0.01)
        glRotatef(90f, 1f, 0f, 0f)
        glRotatef(180f + player.rotationYaw, 0f, 0f, 1f)

        draw(event.partialTicks)

        glPopMatrix()

        glPolygonOffset(1f, 1000000f)
        glDisable(GL_POLYGON_OFFSET_FILL)
        glEnable(GL_CULL_FACE)
    }

    private fun draw(ticks: Float) {
        if (entities.isEmpty()) {
            return
        }

        val player = mc.thePlayer ?: return

        val arrowRadius = -arrowRadius
        val halfAngle = arrowAngle / 2

        val playerPosX = player.lastTickPosX + (player.posX - player.lastTickPosX) * ticks
        val playerPosZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * ticks

        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)

        for (entity in entities) {
            val interpolatedPosX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * ticks
            val interpolatedPosZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * ticks
            val pos1 = (interpolatedPosX - playerPosX) * 0.2
            val pos2 = (interpolatedPosZ - playerPosZ) * 0.2

            val cos = cos(player.rotationYaw * (PI / 180))
            val sin = sin(player.rotationYaw * (PI / 180))
            val rotY = -(pos2 * cos - pos1 * sin)
            val rotX = -(pos1 * cos + pos2 * sin)
            val arrowAngle = (atan2(rotY, rotX) * 180 / PI).toFloat() + 90f

            if (player.getDistanceSqToEntity(entity) > maxRenderDistanceSq) continue

            val colorAlpha = colors.color().alpha

            val alpha = if (distanceAlpha) {
                (colorAlpha - (sqrt((playerPosX - interpolatedPosX).pow(2) + (playerPosZ - interpolatedPosZ).pow(2)) / maxRenderDistance)
                    .coerceAtMost(1.0) * (colorAlpha - alphaMin)).toInt()
            } else colorAlpha

            val targetHealth = getHealth(entity, healthFromScoreboard, absorption)
            val arrowsColor = when {
                targetHealth <= 0 -> Color(255, 0, 0, alpha)

                colorTeam -> entity.colorFromDisplayName() ?: continue

                healthMode == "Custom" -> {
                    ColorUtils.interpolateHealthColor(
                        entity,
                        healthColors.color().red,
                        healthColors.color().green,
                        healthColors.color().blue,
                        alpha,
                        healthFromScoreboard,
                        absorption
                    )
                }
                else -> colors.color(a = alpha)
            }

            glColor(arrowsColor)

            glRotatef(arrowAngle, 0f, 0f, 1f)

            when (mode.lowercase()) {
                "solid" -> {
                    glBegin(GL_TRIANGLES)
                    glVertex2f(0f, arrowRadius)
                    glVertex2d(
                        sin(-halfAngle * PI / 180) * arrowSize,
                        arrowRadius + cos(-halfAngle * PI / 180) * arrowSize
                    )
                    glVertex2d(
                        sin(halfAngle * PI / 180) * arrowSize,
                        arrowRadius + cos(halfAngle * PI / 180) * arrowSize
                    )
                    glEnd()
                }

                "line", "loopline" -> {
                    glLineWidth(thickness)
                    glBegin(GL_LINE_STRIP)
                    glVertex2d(
                        sin(-halfAngle * PI / 180) * arrowSize,
                        arrowRadius + cos(-halfAngle * PI / 180) * arrowSize
                    )
                    glVertex2f(0f, arrowRadius)
                    glVertex2d(
                        sin(halfAngle * PI / 180) * arrowSize,
                        arrowRadius + cos(halfAngle * PI / 180) * arrowSize
                    )
                    if (mode == "LoopLine") {
                        glVertex2d(
                            sin(-halfAngle * PI / 180) * arrowSize,
                            arrowRadius + cos(-halfAngle * PI / 180) * arrowSize
                        )
                    }
                    glEnd()
                }
            }

            glRotatef(-arrowAngle, 0f, 0f, 1f)
        }

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glColor4f(1f, 1f, 1f, 1f)
    }
}