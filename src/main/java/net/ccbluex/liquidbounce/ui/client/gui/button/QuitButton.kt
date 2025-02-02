/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui.button

import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.ccbluex.liquidbounce.ui.font.Fonts.fontSmall
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawCustomShapeWithRadius
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundOutline
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color

class QuitButton(x: Int, y: Int) : ImageButton(
    text = "QUIT",
    image = ResourceLocation("${CLIENT_NAME.lowercase()}/texture/mainmenu/exit.png"),
    x = x,
    y = y
) {
    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        val hovered = updateHover(mouseX, mouseY)
        if (hovered) {
            if (hoverFade < 40) hoverFade += 10
            drawHoverEffect()
        } else {
            if (hoverFade > 0) hoverFade -= 10
        }
        drawCustomShapeWithRadius((x - 1).toFloat(), (y - 1).toFloat(), (width + 2).toFloat(), (height + 2).toFloat(), 2f, Color(30, 30, 30, 60))
        drawCustomShapeWithRadius(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), 2f, Color(255 - hoverFade * 4, 255 - hoverFade * 4, 255, 38 + hoverFade))
        drawRoundOutline(x, y, x + width, y + height, 2f, 3f, Color(255, 255, 255, 30).rgb)
        val color = Color(232, 232, 232, 183).rgb
        val f1 = (color shr 24 and 0xFF) / 255.0f
        val f2 = (color shr 16 and 0xFF) / 255.0f
        val f3 = (color shr 8 and 0xFF) / 255.0f
        val f4 = (color and 0xFF) / 255.0f
        GL11.glColor4f(f2, f3, f4, f1)
        GlStateManager.enableAlpha()
        GlStateManager.enableBlend()
        mc.textureManager.bindTexture(image)
        Gui.drawModalRectWithCustomSizedTexture(x + 3, y + 3, 0F, 0F, 6, 6, 6F, 6F)
        GlStateManager.disableBlend()
        GlStateManager.disableAlpha()
    }

    private fun drawHoverEffect() {
        val textWidth = (fontSmall.getStringWidth(text) * 0.9f).toInt()
        val offset = 4
        drawCustomShapeWithRadius((x + (width - textWidth) / 2f), (y + height + offset).toFloat(), textWidth.toFloat(), 7f, 2f, Color(0, 0, 0, 126))
        fontSmall.drawCenteredTextScaled(text, (x + width / 2f).toInt(), (y + height + offset + 1), Color(255, 255, 255, 135).rgb, 0.9)
    }
}