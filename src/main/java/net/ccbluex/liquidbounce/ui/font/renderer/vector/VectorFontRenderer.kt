/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.font.renderer.vector

import net.ccbluex.liquidbounce.features.module.modules.client.HUD
import net.ccbluex.liquidbounce.ui.font.renderer.AbstractAwtFontRender
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Font
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform

/**
 * 矢量字体渲染器
 * @author liulihaocai
 */
class VectorFontRenderer(font: Font) : AbstractAwtFontRender(font) {

    private var texId = 0

    override fun drawChar(char: String): Int {
        val cached =  if (!cachedChars.containsKey(char)) {
            val list = GL11.glGenLists(1)
            // list is faster than buffer
            GL11.glNewList(list, GL11.GL_COMPILE_AND_EXECUTE)
            RenderUtils.directDrawAWTShape(font.createGlyphVector(FontRenderContext(AffineTransform(), true, false), char)
                .getOutline(0f, fontMetrics.ascent.toFloat()), HUD.fontEpsilonValue.get().toDouble())
            GL11.glEndList()

            CachedVectorFont(list, fontMetrics.stringWidth(char)).also { cachedChars[char] = it }
        } else {
            cachedChars[char]!! as CachedVectorFont
        }

        val list = cached.list
        GL11.glCallList(list)
        GL11.glCallList(list)
        cached.lastUsage = System.currentTimeMillis()

        return cached.width
    }

    override fun preGlHints() {
        GlStateManager.enableColorMaterial()
        GlStateManager.enableAlpha()
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        RenderUtils.clearCaps()
        RenderUtils.enableGlCap(GL11.GL_POLYGON_SMOOTH)
        RenderUtils.disableGlCap(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(false)
        RenderUtils.disableGlCap(GL11.GL_CULL_FACE)
    }

    override fun postGlHints() {
        RenderUtils.resetCaps()
        GL11.glDepthMask(true)
        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
    }
}