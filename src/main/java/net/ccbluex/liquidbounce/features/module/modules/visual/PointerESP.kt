/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.config.*
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.client.AntiBot.isBot
import net.ccbluex.liquidbounce.features.module.modules.client.Teams
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer.Companion.getColorIndex
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils
import net.ccbluex.liquidbounce.utils.attack.EntityUtils
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.getHealth
import net.ccbluex.liquidbounce.utils.render.ColorSettingsInteger
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.*

object PointerESP : Module("PointerESP", Category.VISUAL, hideModule = false) {
    private val dimension by choices("Dimension", arrayOf("2d", "3d"), "2d")
    private val mode by choices("Mode", arrayOf("Solid", "Line", "LoopLine"), "Solid")
    private val thickness by float("Thickness", 3f, 1f..5f) { mode.contains("Line") }

    private val colorMode by choices("Color-Mode", arrayOf("Custom", "Theme Client", "Rainbow", "Fade"), "Custom")
    { healthMode == "None" }
    private val colors = ColorSettingsInteger(this, "Colors", withAlpha = false)
    { colorMode == "Custom" && healthMode == "None" }.with(255, 111, 255)
    private val fadeDistance by int("Fade Distance", 50, 0..100) { colorMode == "Fade" }

    private val healthMode by choices("Health-Mode", arrayOf("None", "Custom"), "Custom")
    private val healthColors = ColorSettingsInteger(this, "Health", withAlpha = false)
    { healthMode == "Custom" }.with(255, 255, 0)

    private val absorption by boolean("Absorption", true) { healthMode == "Custom" }
    private val healthFromScoreboard by boolean("HealthFromScoreboard", true) { healthMode == "Custom" }

    private val alpha by int("Alpha", 255, 0..255)
    private val distanceAlpha by boolean("DistanceAlpha", true)
    private val alphaMin by int("AlphaMin", 100, -50..255) { distanceAlpha }

    private val maxRenderDistance by object : IntegerValue("MaxRenderDistance", 100, 1..200) {
        override fun onUpdate(value: Int) {
            maxRenderDistanceSq = value.toDouble().pow(2.0)
        }
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


    val onRender2D = handler<Render2DEvent> { event ->
        if (dimension != "2d") return@handler

        val scaledResolution = ScaledResolution(mc)

        glPushMatrix()
        glTranslatef(scaledResolution.scaledWidth / 2f, scaledResolution.scaledHeight / 2f, 0f)

        draw(event.partialTicks)

        glPopMatrix()
    }

    private fun fade(color: Color, distance: Int, maxDistance: Int): Color {
        val alpha = ((1.0 - (distance.toFloat() / maxDistance.toFloat())).coerceIn(0.0, 1.0) * 255).toInt()
        return Color(color.red, color.green, color.blue, alpha)
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
        val player = mc.thePlayer ?: return
        val world = mc.theWorld ?: return

        val arrowRadius = -arrowRadius
        val halfAngle = arrowAngle / 2

        val playerPosX = player.lastTickPosX + (player.posX - player.lastTickPosX) * ticks
        val playerPosZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * ticks

        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)

        for (entity in world.loadedEntityList.filterNotNull()) {
            if (entity !is EntityLivingBase || !bot && isBot(entity)) continue
            if (!team && Teams.isInYourTeam(entity)) continue

            if (EntityUtils.isSelected(entity, false)) {
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

                val alpha = if (distanceAlpha) {
                    (alpha - (sqrt((playerPosX - interpolatedPosX).pow(2) + (playerPosZ - interpolatedPosZ).pow(2)) / maxRenderDistance)
                        .coerceAtMost(1.0) * (alpha - alphaMin)).toInt()
                } else alpha

                val targetHealth = getHealth(entity, healthFromScoreboard, absorption)
                val arrowsColor = when {
                    targetHealth <= 0 -> Color(255, 0, 0, alpha)
                    colorTeam -> {
                        val chars = (entity.displayName ?: return).formattedText.toCharArray()
                        var color = Int.MAX_VALUE
                        for (i in chars.indices) {
                            if (chars[i] != '§' || i + 1 >= chars.size) continue
                            val index = getColorIndex(chars[i + 1])
                            if (index < 0 || index > 15) continue
                            color = ColorUtils.hexColors[index]
                            break
                        }
                        Color(color)
                    }
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
                    colorMode == "Rainbow" -> ColorUtils.rainbow()
                    colorMode ==  "Theme Client" -> ClientThemesUtils.getColor(1)
                    colorMode == "Fade" -> fade(Color(colors.color().red, colors.color().green, colors.color().blue),
                        player.getDistanceToEntity(entity).toInt(), fadeDistance)
                    else -> Color(colors.color().red, colors.color().green, colors.color().blue, alpha)
                }

                glColor4f(
                    arrowsColor.red / 255f,
                    arrowsColor.green / 255f,
                    arrowsColor.blue / 255f,
                    arrowsColor.alpha / 255f
                )

                glRotatef(arrowAngle, 0f, 0f, 1f)

                when (mode.lowercase()) {
                    "solid" -> {
                        glBegin(GL_TRIANGLES)
                        glVertex2f(0f, arrowRadius)
                        glVertex2d(sin(-halfAngle * PI / 180) * arrowSize, arrowRadius + cos(-halfAngle * PI / 180) * arrowSize)
                        glVertex2d(sin(halfAngle * PI / 180) * arrowSize, arrowRadius + cos(halfAngle * PI / 180) * arrowSize)
                        glEnd()
                    }
                    "line", "loopline" -> {
                        glLineWidth(thickness)
                        glBegin(GL_LINE_STRIP)
                        glVertex2d(sin(-halfAngle * PI / 180) * arrowSize, arrowRadius + cos(-halfAngle * PI / 180) * arrowSize)
                        glVertex2f(0f, arrowRadius)
                        glVertex2d(sin(halfAngle * PI / 180) * arrowSize, arrowRadius + cos(halfAngle * PI / 180) * arrowSize)
                        if (mode == "LoopLine") {
                            glVertex2d(sin(-halfAngle * PI / 180) * arrowSize, arrowRadius + cos(-halfAngle * PI / 180) * arrowSize)
                        }
                        glEnd()
                    }
                }

                glRotatef(-arrowAngle, 0f, 0f, 1f)
            }
        }

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
    }
}