package net.ccbluex.liquidbounce.utils

import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.shader.ShaderGroup
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11

/**
 * @author potion, liuli
 */
object BlurUtils : MinecraftInstance() {
    private val blurShader = ShaderGroup(mc.textureManager, mc.resourceManager, mc.framebuffer, ResourceLocation("fdpclient/blur.json"))
    private var lastScale = 0
    private var lastScaleWidth = 0
    private var lastScaleHeight = 0

    private fun initFboAndShader() {
        blurShader.createBindFramebuffers(mc.displayWidth, mc.displayHeight)
    }

    private fun setShaderConfigs(intensity: Float, blurWidth: Float, blurHeight: Float, opacity: Float) {
        blurShader.listShaders[0].shaderManager.getShaderUniform("Radius").set(intensity)
        blurShader.listShaders[1].shaderManager.getShaderUniform("Radius").set(intensity)
//		blurShader.listShaders[0].shaderManager.getShaderUniform("Opacity").set(opacity);
//		blurShader.listShaders[1].shaderManager.getShaderUniform("Opacity").set(opacity);
        blurShader.listShaders[0].shaderManager.getShaderUniform("BlurDir")[blurWidth] = blurHeight
        blurShader.listShaders[1].shaderManager.getShaderUniform("BlurDir")[blurHeight] = blurWidth
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
            blurShader.mainFramebuffer.framebufferClear()
            GL11.glScissor(x * factor, mc.displayHeight - y * factor - height * factor, width * factor, height * factor)
            GL11.glEnable(3089)
            setShaderConfigs(intensity, blurWidth, blurHeight, 1.0f)
            blurShader.mainFramebuffer.bindFramebuffer(true)
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
        blurShader.mainFramebuffer.framebufferClear()
        GL11.glScissor(x * factor, mc.displayHeight - y * factor - height * factor, width * factor, height * factor)
        GL11.glEnable(3089)
        setShaderConfigs(intensity, 1.0f, 0.0f, 1.0f)
        blurShader.mainFramebuffer.bindFramebuffer(true)
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
        blurShader.mainFramebuffer.bindFramebuffer(true)
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
        blurShader.mainFramebuffer.bindFramebuffer(true)
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
        blurShader.mainFramebuffer.bindFramebuffer(true)
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
        blurShader.mainFramebuffer.bindFramebuffer(true)
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
        blurShader.mainFramebuffer.bindFramebuffer(true)
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
        blurShader.mainFramebuffer.bindFramebuffer(true)
        blurShader.loadShaderGroup(mc.timer.renderPartialTicks)
        mc.framebuffer.bindFramebuffer(true)
    }
}