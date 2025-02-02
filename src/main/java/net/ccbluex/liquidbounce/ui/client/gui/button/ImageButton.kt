/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui.button

import net.ccbluex.liquidbounce.ui.font.Fonts.fontSmall
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawCustomShapeWithRadius
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundOutline
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color

open class ImageButton(
    text: String,
    val image: ResourceLocation,
    x: Int,
    y: Int,
    open var imageWidth: Int = 6,
    open var imageHeight: Int = 6,
    customWidth: Int = 12,
    customHeight: Int = 12,
    open var hoverEffectYOffset: Int = -12
) : ButtonState(text, x, y, customWidth, customHeight) {

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        val hovered = updateHover(mouseX, mouseY)
        if (hovered) {
            if (hoverFade < 40) hoverFade += 10
            drawHoverEffect()
        } else {
            if (hoverFade > 0) hoverFade -= 10
        }

        drawCustomShapeWithRadius(
            (x - 1).toFloat(),
            (y - 1).toFloat(),
            (width + 2).toFloat(),
            (height + 2).toFloat(),
            2f,
            Color(30, 30, 30, 60)
        )
        drawCustomShapeWithRadius(
            x.toFloat(),
            y.toFloat(),
            width.toFloat(),
            height.toFloat(),
            2f,
            Color(255, 255, 255, 38 + hoverFade)
        )

        drawRoundOutline(
            x,
            y,
            x + width,
            y + height,
            2f,
            3f,
            Color(255, 255, 255, 30).rgb
        )

        val color = Color(232, 232, 232, 183).rgb
        val f1 = (color shr 24 and 0xFF) / 255.0f
        val f2 = (color shr 16 and 0xFF) / 255.0f
        val f3 = (color shr 8 and 0xFF) / 255.0f
        val f4 = (color and 0xFF) / 255.0f
        GL11.glColor4f(f2, f3, f4, f1)
        GlStateManager.enableAlpha()
        GlStateManager.enableBlend()

        mc.textureManager.bindTexture(image)
        Gui.drawModalRectWithCustomSizedTexture(
            x + (width - imageWidth) / 2,
            y + (height - imageHeight) / 2,
            0F,
            0F,
            imageWidth,
            imageHeight,
            imageWidth.toFloat(),
            imageHeight.toFloat()
        )

        GlStateManager.disableBlend()
        GlStateManager.disableAlpha()
    }

    private fun drawHoverEffect() {
        val textWidth = (fontSmall.getStringWidth(text) * 0.9f).toInt()
        val shadowWidth = textWidth + 4

        val shadowX = x + ((width - shadowWidth) / 2f)
        val shadowY = y + hoverEffectYOffset.toFloat()

        drawCustomShapeWithRadius(
            shadowX,
            shadowY,
            shadowWidth.toFloat(),
            7f,
            2f,
            Color(0, 0, 0, 126)
        )

        fontSmall.drawCenteredTextScaled(
            text,
            (x + width / 2f).toInt(),
            (y + hoverEffectYOffset + 2).toInt(),
            Color(255, 255, 255, 135).rgb,
            0.9
        )
    }
}