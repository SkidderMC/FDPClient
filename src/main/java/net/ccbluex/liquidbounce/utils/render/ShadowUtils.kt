/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.shader.Framebuffer
import net.minecraft.client.shader.ShaderGroup
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE

import java.io.IOException

object ShadowUtils : MinecraftInstance() {

    private var initFramebuffer: Framebuffer? = null
    private var frameBuffer: Framebuffer? = null
    var resultBuffer: Framebuffer? = null

    private var shaderGroup: ShaderGroup? = null
    private var lastWidth = 0
    private var lastHeight = 0
    private var lastStrength = 0F

    private val blurDirectory = ResourceLocation("fdpclient/shadow.json")

    @Throws(IOException::class)
    fun initShaderIfRequired(sc: ScaledResolution, strength: Float) {
        val width = sc.scaledWidth
        val height = sc.scaledHeight
        val factor = sc.scaleFactor
        if (lastWidth != width || lastHeight != height
            || initFramebuffer == null || frameBuffer == null || shaderGroup == null) {
            initFramebuffer = Framebuffer(width * factor, height * factor, true)
            initFramebuffer!!.setFramebufferColor(0F, 0F, 0F, 0F)
            initFramebuffer!!.setFramebufferFilter(GL_LINEAR)
            shaderGroup = ShaderGroup(mc.textureManager, mc.getResourceManager(), initFramebuffer, blurDirectory)
            shaderGroup!!.createBindFramebuffers(width * factor, height * factor)
            frameBuffer = shaderGroup!!.mainFramebuffer
            resultBuffer = shaderGroup!!.getFramebufferRaw("braindead")
    
            lastWidth = width
            lastHeight = height
            lastStrength = strength
            for (i in 0..1)
                shaderGroup!!.listShaders[i].shaderManager.getShaderUniform("Radius").set(strength)
        }
        if (lastStrength != strength) {
            lastStrength = strength
            for (i in 0..1)
                shaderGroup!!.listShaders[i].shaderManager.getShaderUniform("Radius").set(strength)
        }
    }

    fun shadow(strength: Float, drawMethod: (() -> Unit), cutMethod: (() -> Unit)) {
        if (!OpenGlHelper.isFramebufferEnabled()) return

        val sc = ScaledResolution(mc)
        val width = sc.scaledWidth
        val height = sc.scaledHeight
        initShaderIfRequired(sc, strength)

        initFramebuffer ?: return
        resultBuffer ?: return
        frameBuffer ?: return

        mc.getFramebuffer().unbindFramebuffer()
        initFramebuffer!!.framebufferClear()
        resultBuffer!!.framebufferClear()
        initFramebuffer!!.bindFramebuffer(true)
        drawMethod()
        frameBuffer!!.bindFramebuffer(true)
        shaderGroup!!.loadShaderGroup(mc.timer.renderPartialTicks)
        mc.getFramebuffer().bindFramebuffer(true)

        val fr_width = resultBuffer!!.framebufferWidth.toDouble() / resultBuffer!!.framebufferTextureWidth.toDouble()
        val fr_height = resultBuffer!!.framebufferHeight.toDouble() / resultBuffer!!.framebufferTextureHeight.toDouble()

        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.getWorldRenderer()

        glPushMatrix()
        GlStateManager.disableLighting()
        GlStateManager.disableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.disableDepth()
        GlStateManager.depthMask(false)
        GlStateManager.colorMask(true, true, true, true)

        Stencil.write(false)
        cutMethod()
        Stencil.erase(false)

        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.color(1F, 1F, 1F, 1F)

        resultBuffer!!.bindFramebufferTexture()
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
        worldrenderer.pos(0.0, height.toDouble(), 0.0).tex(0.0, 0.0).color(255, 255, 255, 255).endVertex()
        worldrenderer.pos(width.toDouble(), height.toDouble(), 0.0).tex(fr_width, 0.0).color(255, 255, 255, 255).endVertex()
        worldrenderer.pos(width.toDouble(), 0.0, 0.0).tex(fr_width, fr_height).color(255, 255, 255, 255).endVertex()
        worldrenderer.pos(0.0, 0.0, 0.0).tex(0.0, fr_height).color(255, 255, 255, 255).endVertex()

        tessellator.draw()
        resultBuffer!!.unbindFramebufferTexture()

        GlStateManager.disableBlend() 
        GlStateManager.enableAlpha()
        GlStateManager.enableDepth()
        GlStateManager.depthMask(true)

        Stencil.dispose()
        glPopMatrix()

        GlStateManager.resetColor()
        GlStateManager.color(1F, 1F, 1F, 1F)
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
    }

}