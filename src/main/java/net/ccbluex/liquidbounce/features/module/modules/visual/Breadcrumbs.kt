/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.kotlin.removeEach
import net.ccbluex.liquidbounce.utils.render.ColorSettingsInteger
import net.ccbluex.liquidbounce.utils.render.ColorUtils.withAlpha
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import org.lwjgl.opengl.GL11.*
import java.awt.Color

object Breadcrumbs : Module("Breadcrumbs", Category.VISUAL) {
    val colors = ColorSettingsInteger(this, "Color").with(132, 102, 255)
    private val lineHeight by float("LineHeight", 0.25F, 0.25F..2F)
    private val temporary by boolean("Temporary", true)
    private val fade by boolean("Fade", true) { temporary }
    private val lifeTime by float("LifeTime", 1F, 0F..10F) { temporary }

    private val positions = ArrayDeque<PositionData>()

    val onRender3D = handler<Render3DEvent> {
        val player = mc.thePlayer ?: return@handler

        if (positions.isEmpty() && !player.isMoving) {
            return@handler
        }

        val currentTime = System.currentTimeMillis()
        val fadeSeconds = lifeTime * 1000L

        glPushAttrib(GL_ALL_ATTRIB_BITS)
        glPushMatrix()

        glDisable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glDisable(GL_DEPTH_TEST)
        glDisable(GL_CULL_FACE)
        glEnable(GL_ALPHA_TEST)
        glAlphaFunc(GL_GREATER, 0.0f)

        player.interpolatedPosition(player.prevPos).let { pos ->
            val data = PositionData(pos.toDoubleArray(), currentTime)

            val lastData = positions.lastOrNull()?.array

            if (lastData == null || !lastData.contentEquals(data.array)) positions += data
        }

        mc.entityRenderer.disableLightmap()

        glBegin(GL_QUADS)

        val renderPosX = mc.renderManager.viewerPosX
        val renderPosY = mc.renderManager.viewerPosY
        val renderPosZ = mc.renderManager.viewerPosZ

        positions.removeEach {
            val timestamp = System.currentTimeMillis() - it.time
            val transparency = if (fade) {
                (0f..150f).lerpWith(1 - (timestamp / fadeSeconds).coerceAtMost(1.0F))
            } else 150f

            val startPos = it.array
            val endPos = positions.getOrNull(positions.indexOf(it) + 1)?.array
                ?: return@removeEach temporary && timestamp > fadeSeconds

            glColor(colors.color().withAlpha(transparency.toInt()))

            glVertex3d(startPos[0] - renderPosX, startPos[1] - renderPosY, startPos[2] - renderPosZ)
            glVertex3d(startPos[0] - renderPosX, startPos[1] - renderPosY + lineHeight, startPos[2] - renderPosZ)
            glVertex3d(endPos[0] - renderPosX, endPos[1] - renderPosY + lineHeight, endPos[2] - renderPosZ)
            glVertex3d(endPos[0] - renderPosX, endPos[1] - renderPosY, endPos[2] - renderPosZ)

            temporary && timestamp > fadeSeconds
        }

        glEnd()

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

    val onWorld = handler<WorldEvent> {
        positions.clear()
    }

    override fun onDisable() {
        positions.clear()
    }
}

private class PositionData(val array: DoubleArray, val time: Long)