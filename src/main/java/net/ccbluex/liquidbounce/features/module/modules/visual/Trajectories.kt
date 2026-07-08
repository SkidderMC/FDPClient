/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.extensions.eyes
import net.ccbluex.liquidbounce.utils.inventory.isSplashPotion
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBlockBox
import net.ccbluex.liquidbounce.utils.render.RenderText.renderNameTag
import net.ccbluex.liquidbounce.utils.render.RenderColor.glColor
import net.ccbluex.liquidbounce.utils.rotation.Rotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemBow
import net.minecraft.item.ItemEgg
import net.minecraft.item.ItemEnderPearl
import net.minecraft.item.ItemFishingRod
import net.minecraft.item.ItemPotion
import net.minecraft.item.ItemSnowball
import net.minecraft.util.MathHelper
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.GL_ALL_ATTRIB_BITS
import org.lwjgl.opengl.GL11.GL_ALPHA_TEST
import org.lwjgl.opengl.GL11.GL_BLEND
import org.lwjgl.opengl.GL11.GL_CULL_FACE
import org.lwjgl.opengl.GL11.GL_DEPTH_TEST
import org.lwjgl.opengl.GL11.GL_GREATER
import org.lwjgl.opengl.GL11.GL_LINE_SMOOTH
import org.lwjgl.opengl.GL11.GL_LINE_STRIP
import org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA
import org.lwjgl.opengl.GL11.GL_SRC_ALPHA
import org.lwjgl.opengl.GL11.GL_TEXTURE_2D
import org.lwjgl.opengl.GL11.glAlphaFunc
import org.lwjgl.opengl.GL11.glBegin
import org.lwjgl.opengl.GL11.glBlendFunc
import org.lwjgl.opengl.GL11.glDisable
import org.lwjgl.opengl.GL11.glEnable
import org.lwjgl.opengl.GL11.glEnd
import org.lwjgl.opengl.GL11.glLineWidth
import org.lwjgl.opengl.GL11.glPopAttrib
import org.lwjgl.opengl.GL11.glPopMatrix
import org.lwjgl.opengl.GL11.glPushAttrib
import org.lwjgl.opengl.GL11.glPushMatrix
import org.lwjgl.opengl.GL11.glVertex3d
import java.awt.Color

object Trajectories : Module("Trajectories", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY, gameDetecting = false) {

    private val maxSimulatedTicks by int("MaxSimulatedTicks", 240, 1..1000)
        .describe("Maximum number of ticks to simulate per arc.")
    private val maxRenderDistance by float("MaxRenderDistance", 96F, 16F..512F)
        .describe("Maximum distance to render a trajectory.")
    private val otherPlayers by boolean("OtherPlayers", false)
        .describe("Also predict arcs for other players.")
    private val alwaysShowBow by boolean("AlwaysShowBow", false)
        .describe("Show the bow arc even when not drawing.")
    private val lineWidth by float("LineWidth", 2F, 0.5F..5F)
        .describe("Line thickness of the trajectory.")
    private val landingBox by boolean("LandingBox", true)
        .describe("Draw a box at the predicted landing spot.")
    private val lineColorMode by choices("LineColorMode", arrayOf("Type", "Custom"), "Type")
        .describe("Use a distinct projectile color or one custom color.")
    private val lineColor by color("LineColor", Color(255, 255, 255, 255)) { lineColorMode == "Custom" }
        .describe("Color of the trajectory line.")
    private val boxColor by color("BoxColor", Color(0, 160, 255, 150))
        .describe("Color of the landing box.")
    private val detailedInfo by boolean("DetailedInfo", false)
        .describe("Show a label with distance and flight time at the landing spot.")
    private val infoDistance by boolean("ShowDistance", true) { detailedInfo }
        .describe("Include distance in the landing label.")
    private val infoTicks by boolean("ShowFlightTime", true) { detailedInfo }
        .describe("Include flight time in ticks in the landing label.")

    init {
        group("Simulation", "MaxSimulatedTicks", "MaxRenderDistance")
        group("Targets", "OtherPlayers", "AlwaysShowBow")
        group("Appearance", "LineWidth", "LandingBox", "LineColorMode", "LineColor", "BoxColor",
            "DetailedInfo", "ShowDistance", "ShowFlightTime")
    }

    private data class ProjectileType(
        val gravity: Float,
        val drag: Float,
        val velocity: Float,
        val color: Color,
    )

    val onRender3D = handler<Render3DEvent> {
        val player = mc.thePlayer ?: return@handler
        val world = mc.theWorld ?: return@handler

        val renderPosX = mc.renderManager.viewerPosX
        val renderPosY = mc.renderManager.viewerPosY
        val renderPosZ = mc.renderManager.viewerPosZ

        val sources = ArrayList<EntityPlayer>()
        sources += player
        if (otherPlayers) {
            world.playerEntities?.forEach { other ->
                if (other != null && other !== player) sources += other
            }
        }

        glPushAttrib(GL_ALL_ATTRIB_BITS)
        glPushMatrix()
        glDisable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glDisable(GL_DEPTH_TEST)
        glDisable(GL_CULL_FACE)
        glEnable(GL_ALPHA_TEST)
        glAlphaFunc(GL_GREATER, 0.0F)
        glLineWidth(lineWidth)

        for (source in sources) {
            val type = resolveProjectile(source) ?: continue

            val rotation = rotationFor(source)
            val (positions, hit) = simulate(source, rotation, type, world)
            if (positions.size < 2) continue

            glColor(if (lineColorMode == "Type") type.color else lineColor)
            glBegin(GL_LINE_STRIP)
            for (pos in positions) {
                glVertex3d(pos.xCoord - renderPosX, pos.yCoord - renderPosY, pos.zCoord - renderPosZ)
            }
            glEnd()

            if (landingBox && hit != null) {
                val landingPos = hit.blockPos
                if (landingPos != null) drawBlockBox(landingPos, boxColor, false)
            }

            if (detailedInfo && positions.isNotEmpty()) {
                val end = positions.last()
                val distancePart = if (infoDistance)
                    "%.1fm".format(mc.thePlayer.getDistance(end.xCoord, end.yCoord, end.zCoord)) else ""
                val ticksPart = if (infoTicks) "${positions.size}t" else ""
                val label = "$distancePart $ticksPart".trim()
                if (label.isNotEmpty()) renderNameTag(label, end.xCoord, end.yCoord + 0.3, end.zCoord)
            }
        }

        glColor(Color.WHITE)
        glEnable(GL_CULL_FACE)
        glEnable(GL_DEPTH_TEST)
        glDisable(GL_ALPHA_TEST)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glPopMatrix()
        glPopAttrib()
    }

    private fun resolveProjectile(source: EntityPlayer): ProjectileType? {
        val stack = source.heldItem ?: return null
        return when (stack.item) {
            is ItemBow -> {
                val velocity = if (source.isUsingItem || alwaysShowBow) {
                    val charge = (if (source.isUsingItem) source.itemInUseDuration else 0) / 20F
                    val power = ((charge * charge + charge * 2F) / 3F).coerceAtMost(1F)
                    (if (power < 0.1F) 1F else power) * 3F
                } else {
                    return null
                }
                ProjectileType(0.05F, 0.99F, velocity, Color(255, 255, 255, 220))
            }
            is ItemSnowball -> ProjectileType(0.03F, 0.99F, 1.5F, Color(210, 220, 230, 220))
            is ItemEgg -> ProjectileType(0.03F, 0.99F, 1.5F, Color(240, 234, 214, 220))
            is ItemEnderPearl -> ProjectileType(0.03F, 0.99F, 1.5F, Color(128, 0, 160, 220))
            is ItemPotion -> {
                if (!stack.isSplashPotion()) return null
                val potionColor = Color(stack.item.getColorFromItemStack(stack, 0))
                ProjectileType(
                    0.05F,
                    0.99F,
                    0.75F,
                    Color(potionColor.red, potionColor.green, potionColor.blue, 220),
                )
            }
            is ItemFishingRod -> ProjectileType(0.03F, 0.92F, 1.5F, Color(0, 210, 220, 220))
            else -> null
        }
    }

    private fun rotationFor(source: EntityPlayer): Rotation {
        if (source is EntityPlayerSP) {
            return RotationUtils.currentRotation ?: RotationUtils.serverRotation
        }
        return Rotation(source.rotationYaw, source.rotationPitch)
    }

    private fun simulate(
        source: EntityPlayer,
        rotation: Rotation,
        type: ProjectileType,
        world: net.minecraft.world.World
    ): Pair<List<Vec3>, MovingObjectPosition?> {
        val yaw = rotation.yaw
        val pitch = rotation.pitch

        val yawRad = Math.toRadians(yaw.toDouble())
        val pitchRad = Math.toRadians(pitch.toDouble())

        var motionX = -MathHelper.sin(yawRad.toFloat()) * MathHelper.cos(pitchRad.toFloat()) * type.velocity
        var motionY = -MathHelper.sin(pitchRad.toFloat()) * type.velocity
        var motionZ = MathHelper.cos(yawRad.toFloat()) * MathHelper.cos(pitchRad.toFloat()) * type.velocity

        val eyes = source.eyes
        var x = eyes.xCoord - MathHelper.cos(yawRad.toFloat()).toDouble() * 0.16
        var y = eyes.yCoord - 0.10000000149011612
        var z = eyes.zCoord - MathHelper.sin(yawRad.toFloat()).toDouble() * 0.16

        val positions = ArrayList<Vec3>()
        positions += Vec3(x, y, z)

        val maxDistanceSq = (maxRenderDistance * maxRenderDistance).toDouble()
        val originX = eyes.xCoord
        val originY = eyes.yCoord
        val originZ = eyes.zCoord

        var ticks = 0
        while (ticks < maxSimulatedTicks) {
            val from = Vec3(x, y, z)
            val to = Vec3(x + motionX, y + motionY, z + motionZ)

            val hit = world.rayTraceBlocks(from, to, false, true, false)
            if (hit != null) {
                positions += hit.hitVec
                return positions to hit
            }

            x += motionX
            y += motionY
            z += motionZ

            motionX *= type.drag
            motionY *= type.drag
            motionZ *= type.drag
            motionY -= type.gravity

            positions += Vec3(x, y, z)

            val dx = x - originX
            val dy = y - originY
            val dz = z - originZ
            if (dx * dx + dy * dy + dz * dz > maxDistanceSq) break
            if (y < -64.0) break

            ticks++
        }

        return positions to null
    }
}
