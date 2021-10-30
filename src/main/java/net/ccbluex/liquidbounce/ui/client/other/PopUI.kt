package net.ccbluex.liquidbounce.ui.client.other

import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import org.lwjgl.opengl.GL11
import java.awt.Color

/**
 * @author liulihaocai
 * FDPClient
 */
open class PopUI(val title: String) {
    val baseWidth = 150
    val baseHeight = 210

    fun onRender(width: Int, height: Int) {
        GL11.glPushMatrix()
        RenderUtils.drawRect(0F, 0F, width.toFloat(), height.toFloat(), Color(0, 0, 0, 50).rgb)
        val scale = (width * 0.2F) / baseWidth
        GL11.glTranslatef(width * 0.4F, height * 0.3F, 0F)
        GL11.glScalef(scale, scale, scale)

        RenderUtils.drawRect(0F, 0F, baseWidth.toFloat(), baseHeight.toFloat(), Color.WHITE.rgb)
        Fonts.font40.drawString(title, 8F, 8F, Color.DARK_GRAY.rgb)
        render()

        GL11.glPopMatrix()
    }

    fun onClick(width: Int, height: Int, mouseX: Int, mouseY: Int) {
        val scale = (width * 0.2F) / baseWidth
        val scaledMouseX = (mouseX - width * 0.4f) / scale
        val scaledMouseY = (mouseY - height * 0.3f) / scale

        if (scaledMouseX> 0 && scaledMouseY> 0 && scaledMouseX <baseWidth && scaledMouseY <baseHeight) {
            click(scaledMouseX, scaledMouseY)
        } else {
            close()
        }
    }

    fun onStroll(width: Int, height: Int, mouseX: Int, mouseY: Int, wheel: Int) {
        val scale = (width * 0.2F) / baseWidth
        val scaledMouseX = (mouseX - width * 0.4f) / scale
        val scaledMouseY = (mouseY - height * 0.3f) / scale

        if (scaledMouseX> 0 && scaledMouseY> 0 && scaledMouseX <baseWidth && scaledMouseY <baseHeight) {
            stroll(scaledMouseX, scaledMouseY, wheel)
        }
    }

    fun onKey(typedChar: Char, keyCode: Int) {
        key(typedChar, keyCode)
    }

    open fun render() {}

    open fun key(typedChar: Char, keyCode: Int) {}

    open fun close() {}

    open fun click(mouseX: Float, mouseY: Float) {}

    open fun stroll(mouseX: Float, mouseY: Float, wheel: Int) {}
}