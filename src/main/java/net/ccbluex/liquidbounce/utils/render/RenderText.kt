/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.block.center
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.extensions.*
import net.minecraft.client.gui.FontRenderer
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import java.awt.Color

/**
 * Text and name tag rendering utilities
 *
 * @author Zywl
 */
object RenderText : MinecraftInstance {

    /**
     * Renders block damage percentage text at a block position.
     *
     * @param currentDamage Damage progress (0.0 to 1.0)
     * @param font Font renderer to use
     * @param fontShadow Whether to render with shadow
     * @param color Text color
     * @param scale Text scale multiplier
     */
    @JvmStatic
    fun BlockPos.drawBlockDamageText(
        currentDamage: Float,
        font: FontRenderer,
        fontShadow: Boolean,
        color: Int,
        scale: Float,
    ) {
        require(currentDamage in 0f..1f)
        val renderManager = mc.renderManager
        val progress = (currentDamage * 100).coerceIn(0f, 100f).toInt()
        val progressText = "$progress%"

        glPushAttrib(GL_ENABLE_BIT)
        glPushMatrix()

        val (x, y, z) = this.center - renderManager.renderPos

        glTranslated(x, y, z)
        glRotatef(-renderManager.playerViewY, 0F, 1F, 0F)
        glRotatef(renderManager.playerViewX, 1F, 0F, 0F)

        RenderUtils.disableGlCap(GL_LIGHTING, GL_DEPTH_TEST)
        RenderUtils.enableGlCap(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        val renderScale = (mc.thePlayer.getDistanceSq(this) / 8F).coerceAtLeast(1.5) / 150F * scale
        glScaled(-renderScale, -renderScale, renderScale)

        val width = font.getStringWidth(progressText) * 0.5f
        font.drawString(
            progressText, -width, if (font == Fonts.minecraftFont) 1F else 1.5F, color, fontShadow
        )

        RenderUtils.resetCaps()
        glPopMatrix()
        glPopAttrib()
    }

    /**
     * Renders a name tag at a 3D world position.
     *
     * @param string Text to display
     * @param x World X coordinate
     * @param y World Y coordinate
     * @param z World Z coordinate
     */
    @JvmStatic
    fun renderNameTag(string: String, x: Double, y: Double, z: Double) {
        val renderManager = mc.renderManager
        val (x1, y1, z1) = Vec3(x, y, z) - renderManager.renderPos

        glPushMatrix()
        glTranslated(x1, y1, z1)
        glNormal3f(0f, 1f, 0f)
        glRotatef(-renderManager.playerViewY, 0f, 1f, 0f)
        glRotatef(renderManager.playerViewX, 1f, 0f, 0f)
        glScalef(-0.05f, -0.05f, 0.05f)

        RenderUtils.setGlCap(GL_LIGHTING, false)
        RenderUtils.setGlCap(GL_DEPTH_TEST, false)
        RenderUtils.setGlCap(GL_BLEND, true)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        val width = Fonts.fontSemibold35.getStringWidth(string) / 2
        RenderUtils.drawRect(-width - 1, -1, width + 1, Fonts.fontSemibold35.FONT_HEIGHT, Int.MIN_VALUE)
        Fonts.fontSemibold35.drawString(string, -width.toFloat(), 1.5f, Color.WHITE.rgb, true)

        RenderUtils.resetCaps()
        RenderUtils.resetColor()
        glPopMatrix()
    }

    /**
     * Draws text with an outline effect.
     *
     * @param text Text to draw
     * @param x X position
     * @param y Y position
     * @param borderColor Color of the outline
     * @param mainColor Color of the main text
     * @return Final X position after drawing
     */
    @JvmStatic
    fun drawOutlinedText(text: String, x: Float, y: Float, borderColor: Int, mainColor: Int): Float {
        val offset = 0.35f

        Fonts.fontSmall.drawString(text, x, y - offset, borderColor)
        Fonts.fontSmall.drawString(text, x, y + offset, borderColor)
        Fonts.fontSmall.drawString(text, x - offset, y, borderColor)
        Fonts.fontSmall.drawString(text, x + offset, y, borderColor)
        Fonts.fontSmall.drawString(text, x, y, mainColor)

        return x + Fonts.fontSmall.getStringWidth(text) - 2f
    }
}
