/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.ccbluex.liquidbounce.value.boolean
import net.ccbluex.liquidbounce.value.int
import org.lwjgl.opengl.GL11.*
import java.awt.Color

object Breadcrumbs : Module("Breadcrumbs", Category.VISUAL, hideModule = false) {
    val colorRainbow by boolean("Rainbow", false)
    val colorRed by int("R", 255, 0..255) { !colorRainbow }
    val colorGreen by int("G", 179, 0..255) { !colorRainbow }
    val colorBlue by int("B", 72, 0..255) { !colorRainbow }

    private val positions = mutableListOf<DoubleArray>()

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val color = if (colorRainbow) rainbow() else Color(colorRed, colorGreen, colorBlue)

        glPushMatrix()
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glEnable(GL_BLEND)
        glDisable(GL_DEPTH_TEST)

        mc.entityRenderer.disableLightmap()

        glBegin(GL_LINE_STRIP)
        glColor(color)

        val renderPosX = mc.renderManager.viewerPosX
        val renderPosY = mc.renderManager.viewerPosY
        val renderPosZ = mc.renderManager.viewerPosZ

        for (pos in positions)
            glVertex3d(pos[0] - renderPosX, pos[1] - renderPosY, pos[2] - renderPosZ)

        glColor4d(1.0, 1.0, 1.0, 1.0)
        glEnd()
        glEnable(GL_DEPTH_TEST)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glPopMatrix()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        positions += doubleArrayOf(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY, mc.thePlayer.posZ)
    }

    override fun onEnable() {
        val thePlayer = mc.thePlayer ?: return

        positions += doubleArrayOf(thePlayer.posX, thePlayer.posY + thePlayer.eyeHeight * 0.5f, thePlayer.posZ)
        positions += doubleArrayOf(thePlayer.posX, thePlayer.posY, thePlayer.posZ)
    }

    override fun onDisable() {
        positions.clear()
    }
}