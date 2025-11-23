package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.blur

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.ShaderUtil
import net.ccbluex.liquidbounce.utils.extensions.calculateGaussianValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20
import java.nio.FloatBuffer

object BloomUtil {
    val gaussianBloom = ShaderUtil("fdpclient/shaders/bloom.frag")
    var framebuffer = Framebuffer(1, 1, false)

    private val weightBuffer: FloatBuffer = BufferUtils.createFloatBuffer(256)

    fun renderBlur(sourceTexture: Int, radius: Int, offset: Int) {
        framebuffer = RenderUtil.createFrameBuffer(framebuffer)

        val depthEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST)
        val depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK)

        GlStateManager.disableDepth()
        GlStateManager.depthMask(false)
        GlStateManager.enableAlpha()
        GlStateManager.alphaFunc(516, 0.0f)
        GlStateManager.enableBlend()
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)

        weightBuffer.clear()
        for (i in 0..radius) {
            weightBuffer.put(calculateGaussianValue(i.toFloat(), radius.toFloat()))
        }
        weightBuffer.rewind()

        RenderUtil.setAlphaLimit(0.0f)

        framebuffer.framebufferClear()
        framebuffer.bindFramebuffer(true)
        gaussianBloom.init()
        setupUniforms(radius, offset, 0, weightBuffer)
        RenderUtil.bindTexture(sourceTexture)
        ShaderUtil.drawQuads(0f, 0f, RenderUtil.mc.displayWidth.toFloat(), RenderUtil.mc.displayHeight.toFloat())
        gaussianBloom.unload()
        framebuffer.unbindFramebuffer()

        RenderUtil.mc.framebuffer.bindFramebuffer(true)
        gaussianBloom.init()
        setupUniforms(radius, 0, offset, weightBuffer)

        GL13.glActiveTexture(GL13.GL_TEXTURE16)
        RenderUtil.bindTexture(sourceTexture)
        GL13.glActiveTexture(GL13.GL_TEXTURE0)
        RenderUtil.bindTexture(framebuffer.framebufferTexture)

        ShaderUtil.drawQuads(0f, 0f, RenderUtil.mc.displayWidth.toFloat(), RenderUtil.mc.displayHeight.toFloat())
        gaussianBloom.unload()

        GlStateManager.alphaFunc(516, 0.1f)
        GlStateManager.enableAlpha()
        GlStateManager.bindTexture(0)

        GlStateManager.depthMask(depthMask)
        if (depthEnabled) {
            GlStateManager.enableDepth()
        } else {
            GlStateManager.disableDepth()
        }
    }

    fun setupUniforms(radius: Int, directionX: Int, directionY: Int, weights: FloatBuffer) {
        gaussianBloom.setUniformi("inTexture", 0)
        gaussianBloom.setUniformi("textureToCheck", 16)

        GL20.glUniform1f(gaussianBloom.getUniform("radius"), radius.toFloat())

        gaussianBloom.setUniformf("texelSize", 1.0f / RenderUtil.mc.displayWidth, 1.0f / RenderUtil.mc.displayHeight)
        gaussianBloom.setUniformf("direction", directionX.toFloat(), directionY.toFloat())
        OpenGlHelper.glUniform1(gaussianBloom.getUniform("weights"), weights)
    }
}