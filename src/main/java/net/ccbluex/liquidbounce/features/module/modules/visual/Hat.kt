/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.client.AntiBot.isBot
import net.ccbluex.liquidbounce.features.module.modules.client.Teams
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.client.EntityLookup
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.render.ColorSettingsInteger
import net.ccbluex.liquidbounce.utils.render.ColorUtils.withAlpha
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawCone
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawConesForEntities
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glStateManagerColor
import net.ccbluex.liquidbounce.utils.render.drawWithTessellatorWorldRenderer
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.isEntityHeightVisible
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

object ChineseHat : Module("ChineseHat", Category.VISUAL, Category.SubCategory.RENDER_SELF, gameDetecting = false) {

    private val shape by choices("Shape", arrayOf("Cone", "Halo", "Orbs"), "Cone")
        .describe("Shape of the hat drawn above entities.")

    private val useChineseHatTexture by boolean("UseChineseHatTexture", false) { shape == "Cone" }
        .describe("Apply the Chinese hat texture to the cone.")

    private val colorMode by choices("Color", arrayOf("Custom", "DistanceColor"), "Custom")
        .describe("Color source: custom or by distance.")
    private val colors = ColorSettingsInteger(this) { colorMode == "Custom" }.with(0, 160, 255, 150)

    private val playerHeight by float("PlayerHeight", 0.5f, 0.25f..2f)
        .describe("Height offset of the hat above the entity.")

    private val coneWidth by float("ConeWidth", 0.5f, 0f..2f) { shape == "Cone" }
        .describe("Width of the cone hat.")
    private val coneHeight by float("ConeHeight", 0.5f, 0.1f..2f) { shape == "Cone" }
        .describe("Height of the cone hat.")

    private val haloRadius by float("HaloRadius", 0.4f, 0.1f..2f) { shape == "Halo" }
        .describe("Radius of the halo ring.")
    private val haloThickness by float("HaloThickness", 0.06f, 0.01f..0.5f) { shape == "Halo" }
        .describe("Thickness of the halo ring.")

    private val orbsRadius by float("OrbsRadius", 0.5f, 0.1f..2f) { shape == "Orbs" }
        .describe("Orbit radius of the orbs.")
    private val orbsSize by float("OrbsSize", 0.1f, 0.02f..0.5f) { shape == "Orbs" }
        .describe("Size of each orb.")
    private val orbsCount by int("OrbsCount", 6, 1..16) { shape == "Orbs" }
        .describe("Number of orbiting orbs.")
    private val orbsSpinSpeed by float("OrbsSpinSpeed", 2f, -10f..10f) { shape == "Orbs" }
        .describe("Rotation speed of the orbs.")

    private val renderSelf by boolean("RenderSelf", false)
        .describe("Also draw the hat on your own player.")

    private val maxRenderDistance by int("MaxRenderDistance", 100, 1..200)
        .describe("Maximum distance to render hats.")

    private val onLook by boolean("OnLook", false)
        .describe("Only render hats on entities you look at.")
    private val maxAngleDifference by float("MaxAngleDifference", 90f, 5.0f..90f) { onLook }
        .describe("Max angle from your view to render a hat.")

    private val bots by boolean("Bots", true)
        .describe("Also render hats on detected bots.")
    private val teams by boolean("Teams", false)
        .describe("Color teammates differently.")
    private val thruBlocks by boolean("ThruBlocks", true)
        .describe("Render hats through walls.")

    private val generalGroup = Configurable("General")
    private val colorGroup = Configurable("Color")
    private val coneGroup = Configurable("Cone")
    private val haloGroup = Configurable("Halo")
    private val orbsGroup = Configurable("Orbs")
    private val visibilityGroup = Configurable("Visibility")

    init {
        moveValues(generalGroup,
            "Shape", "PlayerHeight", "RenderSelf", "Bots", "Teams", "ThruBlocks")

        moveValues(colorGroup, "Color")

        moveValues(coneGroup, "UseChineseHatTexture", "ConeWidth", "ConeHeight")

        moveValues(haloGroup, "HaloRadius", "HaloThickness")

        moveValues(orbsGroup, "OrbsRadius", "OrbsSize", "OrbsCount", "OrbsSpinSpeed")

        moveValues(visibilityGroup, "MaxRenderDistance", "OnLook", "MaxAngleDifference")

        addValues(listOf(
            generalGroup, colorGroup, coneGroup, haloGroup, orbsGroup, visibilityGroup
        ))
    }
    private val entityLookup by EntityLookup<EntityLivingBase>()
        .filter { mc.thePlayer.getDistanceSqToEntity(it) <= maxRenderDistance * maxRenderDistance }
        .filter { bots || !isBot(it) }
        .filter { !onLook || mc.thePlayer.isLookingOn(it, maxAngleDifference.toDouble()) }
        .filter { thruBlocks || isEntityHeightVisible(it) }

    val render = handler<Render3DEvent> {
        drawConesForEntities {
            for (entity in entityLookup) {
                val isRenderingSelf =
                    entity == mc.thePlayer && (mc.gameSettings.thirdPersonView != 0 || FreeCam.handleEvents())

                if (!isRenderingSelf || !renderSelf) {
                    if (!isSelected(entity, false)) continue
                }

                if (isRenderingSelf) {
                    FreeCam.restoreOriginalPosition()
                }

                val (x, y, z) = entity.interpolatedPosition(
                    entity.lastTickPos, entity.eyeHeight + playerHeight
                ) - mc.renderManager.renderPos

                val coneWidth = (mc.renderManager.getEntityRenderObject<Entity>(entity)?.shadowSize ?: 0.5F) + coneWidth

                GlStateManager.pushMatrix()
                GlStateManager.translate(x, y, z)

                val color = figureOutColor(entity)
                glStateManagerColor(color)

                when (shape) {
                    "Halo" -> drawHalo(color)
                    "Orbs" -> drawOrbs(color)
                    else -> drawCone(coneWidth, coneHeight, useChineseHatTexture)
                }

                GlStateManager.popMatrix()

                if (isRenderingSelf) {
                    FreeCam.useModifiedPosition()
                }
            }
        }
    }

    private fun drawHalo(color: Color) {
        glColor(color)

        val outerSegments = 96
        val innerSegments = 10
        val outer = haloRadius
        val inner = haloThickness

        drawWithTessellatorWorldRenderer {
            begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION)

            for (o in 0 until outerSegments) {
                val a0 = 2.0 * Math.PI * o / outerSegments
                val a1 = 2.0 * Math.PI * (o + 1) / outerSegments
                val s0 = sin(a0)
                val c0 = cos(a0)
                val s1 = sin(a1)
                val c1 = cos(a1)

                for (i in 0 until innerSegments) {
                    val b0 = 2.0 * Math.PI * i / innerSegments
                    val b1 = 2.0 * Math.PI * (i + 1) / innerSegments
                    val ty0 = inner * sin(b0)
                    val ty1 = inner * sin(b1)
                    val to0 = inner * cos(b0)
                    val to1 = inner * cos(b1)

                    val r00 = outer + to0
                    val r01 = outer + to1

                    val p1x = r00 * s0; val p1z = r00 * c0
                    val p2x = r01 * s0; val p2z = r01 * c0
                    val p3x = r00 * s1; val p3z = r00 * c1
                    val p4x = r01 * s1; val p4z = r01 * c1

                    pos(p1x, ty0, p1z).endVertex()
                    pos(p2x, ty1, p2z).endVertex()
                    pos(p3x, ty0, p3z).endVertex()
                    pos(p2x, ty1, p2z).endVertex()
                    pos(p4x, ty1, p4z).endVertex()
                    pos(p3x, ty0, p3z).endVertex()
                }
            }
        }
    }

    private fun drawOrbs(color: Color) {
        glColor(color)

        val count = orbsCount
        val radius = orbsRadius.toDouble()
        val size = orbsSize.toDouble()
        val spin = (System.currentTimeMillis() % 360000L) * 0.001 * orbsSpinSpeed

        drawWithTessellatorWorldRenderer {
            begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)

            for (i in 0 until count) {
                val angle = 2.0 * Math.PI * i / count + spin
                val cx = sin(angle) * radius
                val cz = cos(angle) * radius

                val minX = cx - size
                val maxX = cx + size
                val minY = -size
                val maxY = size
                val minZ = cz - size
                val maxZ = cz + size

                pos(minX, minY, minZ).endVertex()
                pos(maxX, minY, minZ).endVertex()
                pos(maxX, maxY, minZ).endVertex()
                pos(minX, maxY, minZ).endVertex()

                pos(minX, minY, maxZ).endVertex()
                pos(minX, maxY, maxZ).endVertex()
                pos(maxX, maxY, maxZ).endVertex()
                pos(maxX, minY, maxZ).endVertex()

                pos(minX, minY, minZ).endVertex()
                pos(minX, maxY, minZ).endVertex()
                pos(minX, maxY, maxZ).endVertex()
                pos(minX, minY, maxZ).endVertex()

                pos(maxX, minY, minZ).endVertex()
                pos(maxX, minY, maxZ).endVertex()
                pos(maxX, maxY, maxZ).endVertex()
                pos(maxX, maxY, minZ).endVertex()

                pos(minX, maxY, minZ).endVertex()
                pos(maxX, maxY, minZ).endVertex()
                pos(maxX, maxY, maxZ).endVertex()
                pos(minX, maxY, maxZ).endVertex()

                pos(minX, minY, minZ).endVertex()
                pos(minX, minY, maxZ).endVertex()
                pos(maxX, minY, maxZ).endVertex()
                pos(maxX, minY, minZ).endVertex()
            }
        }
    }

    private fun figureOutColor(entity: EntityLivingBase): Color {
        val dist = mc.thePlayer.getDistanceSqToEntity(entity).coerceAtMost(255.0).toInt()

        return when {
            entity is EntityPlayer && entity.isClientFriend() -> Color(0, 0, 255)
            teams && Teams.isInYourTeam(entity) -> Color(0, 162, 232)
            colorMode == "Custom" -> colors.color()
            colorMode == "DistanceColor" -> Color(255 - dist, dist, 0)
            else -> Color.WHITE
        }.withAlpha(colors.color().alpha)
    }
}
