package net.ccbluex.liquidbounce.utils

import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.shader.Framebuffer
import net.minecraft.client.shader.Shader
import net.minecraft.client.shader.ShaderGroup
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11

/**
 * @author potion, liuli
 */
object BlurUtils : MinecraftInstance() {
    private val shader = ResourceLocation("shaders/post/blur.json")

    private val blurShader: ShaderGroup = ShaderGroup(mc.textureManager, mc.resourceManager, mc.framebuffer, shader)
    private var listShaders: List<Shader>
    private var buffer: Framebuffer
    private var lastScale = 0
    private var lastScaleWidth = 0
    private var lastScaleHeight = 0

    init {
        val field = blurShader.javaClass.getDeclaredField("listShaders")
        field.isAccessible = true
        listShaders = field.get(blurShader) as List<Shader>

        blurShader.createBindFramebuffers(mc.displayWidth, mc.displayHeight)
        val field1 = blurShader.javaClass.getDeclaredField("mainFramebuffer")
        field1.isAccessible = true
        buffer = field1[blurShader] as Framebuffer
    }

    private fun initFboAndShader() {
        blurShader.createBindFramebuffers(mc.displayWidth, mc.displayHeight)
        val field = blurShader.javaClass.getDeclaredField("mainFramebuffer")
        field.isAccessible = true
        buffer = field[blurShader] as Framebuffer
    }

    private fun setShaderConfigs(intensity: Float, blurWidth: Float, blurHeight: Float, opacity: Float) {
        listShaders[0].shaderManager.getShaderUniform("Radius").set(intensity)
        listShaders[1].shaderManager.getShaderUniform("Radius").set(intensity)
//		listShaders[0].shaderManager.getShaderUniform("Opacity").set(opacity);
//		listShaders[1].shaderManager.getShaderUniform("Opacity").set(opacity);
        listShaders[0].shaderManager.getShaderUniform("BlurDir")[blurWidth] = blurHeight
        listShaders[1].shaderManager.getShaderUniform("BlurDir")[blurHeight] = blurWidth
    }

    fun blurArea(x: Int, y: Int, width: Int, height: Int, intensity: Float, blurWidth: Float, blurHeight: Float) {
        val scale = ScaledResolution(mc)
        val factor = scale.scaleFactor
        val factor2 = scale.scaledWidth
        val factor3 = scale.scaledHeight
        if (lastScale != factor || lastScaleWidth != factor2 || lastScaleHeight != factor3) {
            initFboAndShader()
        }
        lastScale = factor
        lastScaleWidth = factor2
        lastScaleHeight = factor3
        if (OpenGlHelper.isFramebufferEnabled()) {
            buffer.framebufferClear()
            GL11.glScissor(x * factor, mc.displayHeight - y * factor - height * factor, width * factor, height * factor)
            GL11.glEnable(3089)
            setShaderConfigs(intensity, blurWidth, blurHeight, 1.0f)
            buffer.bindFramebuffer(true)
            blurShader.loadShaderGroup(mc.timer.renderPartialTicks)
            mc.framebuffer.bindFramebuffer(true)
            GL11.glDisable(3089)
            GlStateManager.enableBlend()
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1)
            GlStateManager.disableBlend()
            GL11.glScalef(factor.toFloat(), factor.toFloat(), 0.0f)
        }
    }

    fun blurArea(x: Int, y: Int, width: Int, height: Int, intensity: Float) {
        val scale = ScaledResolution(mc)
        val factor = scale.scaleFactor
        val factor2 = scale.scaledWidth
        val factor3 = scale.scaledHeight
        if (lastScale != factor || lastScaleWidth != factor2 || lastScaleHeight != factor3) {
            initFboAndShader()
        }
        lastScale = factor
        lastScaleWidth = factor2
        lastScaleHeight = factor3
        buffer.framebufferClear()
        GL11.glScissor(x * factor, mc.displayHeight - y * factor - height * factor, width * factor, height * factor)
        GL11.glEnable(3089)
        setShaderConfigs(intensity, 1.0f, 0.0f, 1.0f)
        buffer.bindFramebuffer(true)
        blurShader.loadShaderGroup(mc.timer.renderPartialTicks)
        mc.framebuffer.bindFramebuffer(true)
        GL11.glDisable(3089)
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1)
        GlStateManager.disableBlend()
        GL11.glScalef(factor.toFloat(), factor.toFloat(), 0.0f)
        RenderHelper.enableGUIStandardItemLighting()
    }

    fun blurAreaBoarder(x: Float, f: Float, width: Float, height: Float, intensity: Float, blurWidth: Float, blurHeight: Float) {
        val scale = ScaledResolution(mc)
        val factor = scale.scaleFactor
        val factor2 = scale.scaledWidth
        val factor3 = scale.scaledHeight
        if (lastScale != factor || lastScaleWidth != factor2 || lastScaleHeight != factor3) {
            initFboAndShader()
        }
        lastScale = factor
        lastScaleWidth = factor2
        lastScaleHeight = factor3
        GL11.glScissor(
            (x * factor.toFloat()).toInt(),
            (mc.displayHeight.toFloat() - f * factor.toFloat() - height * factor.toFloat() + 1).toInt(),
            (width * factor.toFloat()).toInt(),
            (height * factor.toFloat()).toInt()
        )
        GL11.glEnable(3089)
        setShaderConfigs(intensity, blurWidth, blurHeight, 1.0f)
        buffer.bindFramebuffer(true)
        blurShader.loadShaderGroup(mc.timer.renderPartialTicks)
        mc.framebuffer.bindFramebuffer(true)
        GL11.glDisable(3089)
    }

    fun blurAreaBoarder(x: Float, f: Float, width: Float, height: Float, intensity: Float, opacity: Float, blurWidth: Float, blurHeight: Float) {
        val scale = ScaledResolution(mc)
        val factor = scale.scaleFactor
        val factor2 = scale.scaledWidth
        val factor3 = scale.scaledHeight
        if (lastScale != factor || lastScaleWidth != factor2 || lastScaleHeight != factor3) {
            initFboAndShader()
        }
        lastScale = factor
        lastScaleWidth = factor2
        lastScaleHeight = factor3
        GL11.glScissor(
            (x * factor.toFloat()).toInt(),
            (mc.displayHeight.toFloat() - f * factor.toFloat() - height * factor.toFloat() + 1).toInt(),
            (width * factor.toFloat()).toInt(),
            (height * factor.toFloat()).toInt()
        )
        GL11.glEnable(3089)
        setShaderConfigs(intensity, blurWidth, blurHeight, opacity)
        buffer.bindFramebuffer(true)
        blurShader.loadShaderGroup(mc.timer.renderPartialTicks)
        mc.framebuffer.bindFramebuffer(true)
        GL11.glDisable(3089)
    }

    fun blurShape(g: Float, f: Float, h: Float, height: Float, intensity: Float, blurWidth: Float, blurHeight: Float) {
        val scale = ScaledResolution(mc)
        val factor = scale.scaleFactor
        val factor2 = scale.scaledWidth
        val factor3 = scale.scaledHeight
        if (lastScale != factor || lastScaleWidth != factor2 || lastScaleHeight != factor3) {
            initFboAndShader()
        }
        lastScale = factor
        lastScaleWidth = factor2
        lastScaleHeight = factor3
        GL11.glScissor(
            (g * factor.toFloat()).toInt(),
            (mc.displayHeight.toFloat() - f * factor.toFloat() - height * factor.toFloat() + 1).toInt(),
            (h * factor.toFloat()).toInt(),
            (height * factor.toFloat()).toInt()
        )
        GL11.glEnable(3089)
        setShaderConfigs(intensity, blurWidth, blurHeight, 1.0f)
        buffer.bindFramebuffer(true)
        blurShader.loadShaderGroup(mc.timer.renderPartialTicks)
        mc.framebuffer.bindFramebuffer(true)
        GL11.glDisable(3089)
    }

    fun blurAreaBoarder(x: Int, y: Int, width: Int, height: Int, intensity: Float) {
        val scale = ScaledResolution(mc)
        val factor = scale.scaleFactor
        val factor2 = scale.scaledWidth
        val factor3 = scale.scaledHeight
        if (lastScale != factor || lastScaleWidth != factor2 || lastScaleHeight != factor3) {
            initFboAndShader()
        }
        lastScale = factor
        lastScaleWidth = factor2
        lastScaleHeight = factor3
        GL11.glScissor(x * factor, mc.displayHeight - y * factor - height * factor, width * factor, height * factor)
        GL11.glEnable(3089)
        setShaderConfigs(intensity, 1.0f, 0.0f, 1.0f)
        buffer.bindFramebuffer(true)
        blurShader.loadShaderGroup(mc.timer.renderPartialTicks)
        mc.framebuffer.bindFramebuffer(true)
        GL11.glDisable(3089)
    }

    fun blurAll(intensity: Float) {
        val scale = ScaledResolution(mc)
        val factor = scale.scaleFactor
        val factor2 = scale.scaledWidth
        val factor3 = scale.scaledHeight
        if (lastScale != factor || lastScaleWidth != factor2 || lastScaleHeight != factor3) {
            initFboAndShader()
        }
        lastScale = factor
        lastScaleWidth = factor2
        lastScaleHeight = factor3
        setShaderConfigs(intensity, 0.5f, 0.5f, 1.0f)
        buffer.bindFramebuffer(true)
        blurShader.loadShaderGroup(mc.timer.renderPartialTicks)
        mc.framebuffer.bindFramebuffer(true)
    }

    fun blurAll(intensity: Float, opacity: Float) {
        val scale = ScaledResolution(mc)
        val factor = scale.scaleFactor
        val factor2 = scale.scaledWidth
        val factor3 = scale.scaledHeight
        if (lastScale != factor || lastScaleWidth != factor2 || lastScaleHeight != factor3) {
            initFboAndShader()
        }
        lastScale = factor
        lastScaleWidth = factor2
        lastScaleHeight = factor3
        setShaderConfigs(intensity, 0.0f, 1.0f, opacity)
        buffer.bindFramebuffer(true)
        blurShader.loadShaderGroup(mc.timer.renderPartialTicks)
        mc.framebuffer.bindFramebuffer(true)
    }
}