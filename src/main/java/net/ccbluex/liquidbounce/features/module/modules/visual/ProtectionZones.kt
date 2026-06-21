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
import net.ccbluex.liquidbounce.utils.extensions.renderPos
import net.ccbluex.liquidbounce.utils.render.RenderUtils.disableGlCap
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawFilledBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawSelectionBoundingBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.enableGlCap
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.ccbluex.liquidbounce.utils.render.RenderUtils.resetCaps
import net.ccbluex.liquidbounce.utils.render.RenderUtils.resetColor
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import org.lwjgl.opengl.GL11.GL_BLEND
import org.lwjgl.opengl.GL11.GL_DEPTH_TEST
import org.lwjgl.opengl.GL11.GL_LINE_SMOOTH
import org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA
import org.lwjgl.opengl.GL11.GL_SRC_ALPHA
import org.lwjgl.opengl.GL11.GL_TEXTURE_2D
import org.lwjgl.opengl.GL11.glBlendFunc
import org.lwjgl.opengl.GL11.glDepthMask
import org.lwjgl.opengl.GL11.glLineWidth
import java.awt.Color
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Module ProtectionZones
 *
 * Renders configured region boxes so you can visualise claimed/protected areas.
 * Zones are stored in a simple in-memory list and managed with the .protectionzones command.
 * Render-only.
 */
object ProtectionZones : Module("ProtectionZones", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY, gameDetecting = false) {

    private val fill by boolean("Fill", true)
    private val outline by boolean("Outline", true)

    private val fillColor by color("FillColor", Color(0, 255, 0, 51))
    private val outlineColor by color("OutlineColor", Color(0, 255, 0, 255))

    private val renderDistance by int("RenderDistance", 256, 16..1024)

    /**
     * A protected region described by two inclusive block corners.
     * The render box spans the full outer surface of those blocks.
     */
    data class Zone(val from: BlockPos, val to: BlockPos) {

        val box: AxisAlignedBB
            get() = AxisAlignedBB.fromBounds(
                minOf(from.x, to.x).toDouble(),
                minOf(from.y, to.y).toDouble(),
                minOf(from.z, to.z).toDouble(),
                (maxOf(from.x, to.x) + 1).toDouble(),
                (maxOf(from.y, to.y) + 1).toDouble(),
                (maxOf(from.z, to.z) + 1).toDouble()
            )
    }

    val zones = CopyOnWriteArrayList<Zone>()

    fun addZone(from: BlockPos, to: BlockPos) {
        zones += Zone(from, to)
    }

    fun clearZones() {
        zones.clear()
    }

    /**
     * Adds a fixed demo zone around the player so the feature can be tried without manual input.
     */
    fun addDemoZone() {
        val player = mc.thePlayer ?: return
        val center = BlockPos(player.posX, player.posY, player.posZ)
        addZone(center.add(-8, -3, -8), center.add(8, 3, 8))
    }

    val onRender3D = handler<Render3DEvent> {
        if (zones.isEmpty()) {
            return@handler
        }

        val player = mc.thePlayer ?: return@handler
        val origin = mc.renderManager.renderPos
        val maxDistSq = (renderDistance * renderDistance).toDouble()

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        enableGlCap(GL_BLEND)
        disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST)
        glDepthMask(false)

        for (zone in zones) {
            val box = zone.box

            val cx = (box.minX + box.maxX) * 0.5
            val cy = (box.minY + box.maxY) * 0.5
            val cz = (box.minZ + box.maxZ) * 0.5
            val dx = cx - player.posX
            val dy = cy - player.posY
            val dz = cz - player.posZ
            if (dx * dx + dy * dy + dz * dz > maxDistSq) {
                continue
            }

            val rendered = box.offset(-origin.xCoord, -origin.yCoord, -origin.zCoord)

            if (fill) {
                val c = fillColor
                glColor(c.red, c.green, c.blue, if (c.alpha != 255) c.alpha else 35)
                drawFilledBox(rendered)
            }

            if (outline) {
                glLineWidth(1f)
                enableGlCap(GL_LINE_SMOOTH)
                glColor(outlineColor)
                drawSelectionBoundingBox(rendered)
            }
        }

        resetColor()
        glDepthMask(true)
        resetCaps()
    }
}
