/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.render.Render3D.drawAxisAlignedBB
import net.ccbluex.liquidbounce.utils.render.RenderColor.glColor
import net.ccbluex.liquidbounce.utils.render.RenderText.renderNameTag
import net.minecraft.entity.item.EntityEnderEye
import net.minecraft.util.AxisAlignedBB
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

object StrongholdFinder : Module("StrongholdFinder", Category.OTHER, Category.SubCategory.MISCELLANEOUS, gameDetecting = false, spacedName = "Stronghold Finder") {

    private val color by color("Color", Color(0, 200, 255))
        .describe("Color used for the rays and stronghold marker.")
    private val minAngleDifference by float("MinAngleDifference", 2F, 0.5F..20F)
        .describe("Minimum throw angle gap needed to triangulate.")

    private val renderRays by boolean("RenderRays", false)
        .describe("Draw the direction rays of thrown ender eyes.")
    private val renderTopChunks by boolean("RenderTopChunks", true)
        .describe("Draw a box at the predicted stronghold location.")
    private val resetOnWorldChange by boolean("ResetOnWorldChange", true)
        .describe("Clear recorded throws when the world changes.")
    private val announcePrediction by boolean("AnnouncePrediction", true)
        .describe("Print the predicted stronghold coordinates in chat.")

    private val rayLength = 2048.0

    private data class EyeThrow(val x: Double, val z: Double, val dx: Double, val dz: Double)

    private val knownEyes = HashSet<Int>()
    private val throws = ArrayList<EyeThrow>()
    private var prevThrow: EyeThrow? = null
    private var lastThrow: EyeThrow? = null
    private var resultX: Double? = null
    private var resultZ: Double? = null

    override fun onEnable() = reset()
    override fun onDisable() = reset()

    private fun reset() {
        knownEyes.clear()
        throws.clear()
        prevThrow = null
        lastThrow = null
        resultX = null
        resultZ = null
    }

    val onWorld = handler<WorldEvent> { if (resetOnWorldChange) reset() }

    val onUpdate = handler<UpdateEvent> {
        val world = mc.theWorld ?: return@handler

        for (entity in world.loadedEntityList.toList()) {
            if (entity !is EntityEnderEye) continue
            if (!knownEyes.add(entity.entityId)) continue

            val dx = entity.motionX
            val dz = entity.motionZ
            if (sqrt(dx * dx + dz * dz) < 1e-4) continue

            prevThrow = lastThrow
            lastThrow = EyeThrow(entity.posX, entity.posZ, dx, dz)
            throws.add(lastThrow!!)
            triangulate()
        }
    }

    private fun triangulate() {
        val a = prevThrow ?: return
        val b = lastThrow ?: return

        // Minimal angle difference between the two throw directions, normalized to [0, 180]
        val rawDiff = Math.toDegrees(atan2(a.dz, a.dx) - atan2(b.dz, b.dx))
        val angleDiff = abs(((rawDiff + 540.0) % 360.0) - 180.0)
        if (angleDiff < minAngleDifference) {
            chat("§e[StrongholdFinder] §7Throw directions too similar (${"%.1f".format(angleDiff)}°). Move further to the side and throw another eye.")
            return
        }

        val denom = a.dx * b.dz - a.dz * b.dx
        if (abs(denom) < 1e-6) return

        val t = ((b.x - a.x) * b.dz - (b.z - a.z) * b.dx) / denom
        val x = a.x + t * a.dx
        val z = a.z + t * a.dz

        resultX = x
        resultZ = z

        if (announcePrediction) {
            val self = mc.thePlayer
            val distance = if (self != null) sqrt((x - self.posX) * (x - self.posX) + (z - self.posZ) * (z - self.posZ)).toInt() else 0
            chat("§b[StrongholdFinder] §aStronghold at X §f${x.toInt()} §aZ §f${z.toInt()} §7(~${distance}m)")
        }
    }

    val onRender3D = handler<Render3DEvent> {
        val self = mc.thePlayer ?: return@handler
        val renderManager = mc.renderManager

        if (renderRays && throws.isNotEmpty()) {
            drawRays(renderManager.renderPosX, self.posY - renderManager.renderPosY, renderManager.renderPosZ)
        }

        val x = resultX ?: return@handler
        val z = resultZ ?: return@handler

        if (renderTopChunks) {
            val baseY = self.posY - 2.0
            val topY = self.posY + 60.0

            drawAxisAlignedBB(
                AxisAlignedBB.fromBounds(
                    x - 0.5 - renderManager.renderPosX, baseY - renderManager.renderPosY, z - 0.5 - renderManager.renderPosZ,
                    x + 0.5 - renderManager.renderPosX, topY - renderManager.renderPosY, z + 0.5 - renderManager.renderPosZ
                ), color
            )

            val distance = sqrt((x - self.posX) * (x - self.posX) + (z - self.posZ) * (z - self.posZ)).toInt()
            renderNameTag("Stronghold [${distance}m]", x, self.posY + 2.0, z)
        }
    }

    private fun drawRays(renderPosX: Double, y: Double, renderPosZ: Double) {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(1.5F)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)

        glBegin(GL_LINES)
        glColor(color.red, color.green, color.blue, 170)

        for (t in throws) {
            val len = sqrt(t.dx * t.dx + t.dz * t.dz)
            if (len < 1e-6) continue
            val nx = t.dx / len
            val nz = t.dz / len

            val sx = t.x - renderPosX
            val sz = t.z - renderPosZ
            glVertex3d(sx, y, sz)
            glVertex3d(sx + nx * rayLength, y, sz + nz * rayLength)
        }
        glEnd()

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_LINE_SMOOTH)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_BLEND)
        glColor4f(1F, 1F, 1F, 1F)
    }
}
