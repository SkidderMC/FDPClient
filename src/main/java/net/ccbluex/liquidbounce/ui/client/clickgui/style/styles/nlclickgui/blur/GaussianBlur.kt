package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.blur

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.ShaderUtil
import net.ccbluex.liquidbounce.utils.extensions.calculateGaussianValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import java.nio.FloatBuffer

object GaussianBlur {
    @JvmField
    val blurShader = ShaderUtil("fdpclient/shaders/gaussian.frag")

    @JvmField
    var framebuffer = Framebuffer(1, 1, false)

    private val weightBuffer: FloatBuffer = BufferUtils.createFloatBuffer(256)

    fun setupUniforms(dir1: Float, dir2: Float, radius: Float) {
        blurShader.setUniformi("textureIn", 0)
        blurShader.setUniformf("texelSize", 1.0f / RenderUtil.mc.displayWidth, 1.0f / RenderUtil.mc.displayHeight)
        blurShader.setUniformf("direction", dir1, dir2)

        GL20.glUniform1f(blurShader.getUniform("radius"), radius)

        weightBuffer.clear()
        val kernelRadius = radius.toInt()
        for (i in 0..kernelRadius) {
            weightBuffer.put(calculateGaussianValue(i.toFloat(), radius / 2f))
        }
        weightBuffer.rewind()

        OpenGlHelper.glUniform1(blurShader.getUniform("weights"), weightBuffer)
    }

    fun renderBlur(radius: Float) {
        val depthEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST)
        val depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK)

        GlStateManager.disableDepth()
        GlStateManager.depthMask(false)
        GlStateManager.enableBlend()
        GlStateManager.color(1f, 1f, 1f, 1f)
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)

        framebuffer = RenderUtil.createFrameBuffer(framebuffer)

        framebuffer.framebufferClear()
        framebuffer.bindFramebuffer(true)
        blurShader.init()
        setupUniforms(1f, 0f, radius)

        RenderUtil.bindTexture(RenderUtil.mc.framebuffer.framebufferTexture)
        ShaderUtil.drawQuads(0f, 0f, RenderUtil.mc.displayWidth.toFloat(), RenderUtil.mc.displayHeight.toFloat())

        framebuffer.unbindFramebuffer()
        blurShader.unload()

        RenderUtil.mc.framebuffer.bindFramebuffer(true)
        blurShader.init()
        setupUniforms(0f, 1f, radius)

        RenderUtil.bindTexture(framebuffer.framebufferTexture)
        ShaderUtil.drawQuads(0f, 0f, RenderUtil.mc.displayWidth.toFloat(), RenderUtil.mc.displayHeight.toFloat())
        blurShader.unload()

        RenderUtil.resetColor()
        GlStateManager.bindTexture(0)

        GlStateManager.depthMask(depthMask)
        if (depthEnabled) {
            GlStateManager.enableDepth()
        } else {
            GlStateManager.disableDepth()
        }
    }
}