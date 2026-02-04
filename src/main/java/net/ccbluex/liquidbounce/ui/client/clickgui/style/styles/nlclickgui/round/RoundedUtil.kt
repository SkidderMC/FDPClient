package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.render.DrRenderUtils.resetColor
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color

class RoundedUtil {
    var mc: Minecraft? = Minecraft.getMinecraft()

    companion object {
        var roundedShader: ShaderUtil = ShaderUtil("roundedRect")
        var roundedOutlineShader: ShaderUtil = ShaderUtil("fdpclient/shaders/roundRectOutline.frag")
        private val roundedTexturedShader = ShaderUtil("fdpclient/shaders/roundRectTextured.frag")
        private val roundedGradientShader = ShaderUtil("roundedRectGradient")

        fun drawRound(x: Float, y: Float, width: Float, height: Float, radius: Float, color: Color) {
            drawRound(x, y, width, height, radius, false, color)
        }

        fun drawSmoothRound(left: Float, top: Float, right: Float, bottom: Float, radius: Float, color: Color) {
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            drawRound(left, top, right, bottom, radius, color)
            GL11.glScalef(0.5f, 0.5f, 0.5f)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glScalef(2f, 2f, 2f)
        }

        fun drawRoundScale(x: Float, y: Float, width: Float, height: Float, radius: Float, color: Color, scale: Float) {
            drawRound(
                x + width - width * scale, y + height / 2f - ((height / 2f) * scale),
                width * scale, height * scale, radius, false, color
            )
        }

        fun drawGradientHorizontal(
            x: Float,
            y: Float,
            width: Float,
            height: Float,
            radius: Float,
            left: Color,
            right: Color
        ) {
            drawGradientRound(x, y, width, height, radius, left, left, right, right)
        }

        fun drawGradientVertical(
            x: Float,
            y: Float,
            width: Float,
            height: Float,
            radius: Float,
            top: Color,
            bottom: Color
        ) {
            drawGradientRound(x, y, width, height, radius, bottom, top, bottom, top)
        }


        fun drawGradientRound(
            x: Float,
            y: Float,
            width: Float,
            height: Float,
            radius: Float,
            bottomLeft: Color,
            topLeft: Color,
            bottomRight: Color,
            topRight: Color
        ) {
            resetColor()
            GlStateManager.enableBlend()
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            roundedGradientShader.init()
            setupRoundedRectUniforms(x, y, width, height, radius, roundedGradientShader)
            roundedGradientShader.setUniformf(
                "color1",
                bottomLeft.red / 255f,
                bottomLeft.green / 255f,
                bottomLeft.blue / 255f,
                bottomLeft.alpha / 255f
            )
            roundedGradientShader.setUniformf(
                "color2",
                topLeft.red / 255f,
                topLeft.green / 255f,
                topLeft.blue / 255f,
                topLeft.alpha / 255f
            )
            roundedGradientShader.setUniformf(
                "color3",
                bottomRight.red / 255f,
                bottomRight.green / 255f,
                bottomRight.blue / 255f,
                bottomRight.alpha / 255f
            )
            roundedGradientShader.setUniformf(
                "color4",
                topRight.red / 255f,
                topRight.green / 255f,
                topRight.blue / 255f,
                topRight.alpha / 255f
            )
            ShaderUtil.drawQuads(x - 1, y - 1, width + 2, height + 2)
            roundedGradientShader.unload()
            GlStateManager.disableBlend()
        }


        fun drawRound(x: Float, y: Float, width: Float, height: Float, radius: Float, blur: Boolean, color: Color) {
            resetColor()
            GlStateManager.enableBlend()
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            roundedShader.init()

            setupRoundedRectUniforms(x, y, width, height, radius, roundedShader)
            roundedShader.setUniformi("blur", if (blur) 1 else 0)
            roundedShader.setUniformf(
                "color",
                color.red / 255f,
                color.green / 255f,
                color.blue / 255f,
                color.alpha / 255f
            )

            ShaderUtil.drawQuads(x - 1, y - 1, width + 2, height + 2)
            roundedShader.unload()
            GlStateManager.disableBlend()
        }

        fun drawCircle(x: Float, y: Float, radius: Float, color: Color) {
            resetColor()
            GlStateManager.enableBlend()
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            roundedShader.init()

            setupRoundedRectUniforms(x, y, radius, radius, radius / 2 - 0.25f, roundedShader)

            roundedShader.setUniformf(
                "color",
                color.red / 255f,
                color.green / 255f,
                color.blue / 255f,
                color.alpha / 255f
            )

            ShaderUtil.drawQuads(x - 1, y - 1, radius + 2, radius + 2)
            roundedShader.unload()
            GlStateManager.disableBlend()
        }


        fun drawRoundOutline(
            x: Float,
            y: Float,
            width: Float,
            height: Float,
            radius: Float,
            outlineThickness: Float,
            color: Color,
            outlineColor: Color
        ) {
            resetColor()
            GlStateManager.enableBlend()
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            roundedOutlineShader.init()

            val sr = ScaledResolution(Minecraft.getMinecraft())
            setupRoundedRectUniforms(x, y, width, height, radius, roundedOutlineShader)
            roundedOutlineShader.setUniformf("outlineThickness", outlineThickness * sr.scaleFactor)
            roundedOutlineShader.setUniformf(
                "color",
                color.red / 255f,
                color.green / 255f,
                color.blue / 255f,
                color.alpha / 255f
            )
            roundedOutlineShader.setUniformf(
                "outlineColor",
                outlineColor.red / 255f,
                outlineColor.green / 255f,
                outlineColor.blue / 255f,
                outlineColor.alpha / 255f
            )


            ShaderUtil.drawQuads(
                x - (2 + outlineThickness),
                y - (2 + outlineThickness),
                width + (4 + outlineThickness * 2),
                height + (4 + outlineThickness * 2)
            )
            roundedOutlineShader.unload()
            GlStateManager.disableBlend()
        }


        fun drawRoundTextured(x: Float, y: Float, width: Float, height: Float, radius: Float, alpha: Float) {
            resetColor()
            roundedTexturedShader.init()
            roundedTexturedShader.setUniformi("textureIn", 0)
            setupRoundedRectUniforms(x, y, width, height, radius, roundedTexturedShader)
            roundedTexturedShader.setUniformf("alpha", alpha)
            ShaderUtil.drawQuads(x - 1, y - 1, width + 2, height + 2)
            roundedTexturedShader.unload()
            GlStateManager.disableBlend()
        }

        private fun setupRoundedRectUniforms(
            x: Float,
            y: Float,
            width: Float,
            height: Float,
            radius: Float,
            roundedTexturedShader: ShaderUtil
        ) {
            val sr = ScaledResolution(Minecraft.getMinecraft())
            roundedTexturedShader.setUniformf(
                "location", x * sr.scaleFactor,
                (Minecraft.getMinecraft().displayHeight - (height * sr.scaleFactor)) - (y * sr.scaleFactor)
            )
            roundedTexturedShader.setUniformf("rectSize", width * sr.scaleFactor, height * sr.scaleFactor)
            roundedTexturedShader.setUniformf("radius", radius * sr.scaleFactor)
        }
    }
}
