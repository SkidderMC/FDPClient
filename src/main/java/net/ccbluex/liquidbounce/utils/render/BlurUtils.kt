/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 *
 * This code belongs to WYSI-Foundation. Please give credits when using this in your repository.
 */
package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.injection.access.StaticStorage
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.render.ShadowUtils.shaderGroup
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.*
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.shader.Framebuffer
import net.minecraft.client.shader.ShaderGroup
import net.minecraft.util.ResourceLocation
import org.apache.logging.log4j.LogManager


object BlurUtils : MinecraftInstance() {

    private val blurShader: ShaderGroup = ShaderGroup(mc.textureManager, mc.resourceManager, mc.framebuffer, ResourceLocation("shaders/post/blurArea.json"))
    private val framebuffer = shaderGroup!!.mainFramebuffer
    private lateinit var buffer: Framebuffer
    private val frbuffer = shaderGroup?.getFramebufferRaw("result")

    private var lastFactor = 0
    private var lastWidth = 0
    private var lastHeight = 0
    private var lastWeight = 0
    private var lastScale = 0
    private var lastScaleWidth = 0
    private var lastScaleHeight = 0

    private var lastX = 0F
    private var lastY = 0F
    private var lastW = 0F
    private var lastH = 0F

    private var lastStrength = 5F

    private fun reinitShader() {
        blurShader.createBindFramebuffers(mc.displayWidth, mc.displayHeight)
        buffer = Framebuffer(mc.displayWidth, mc.displayHeight, true)
        buffer.setFramebufferColor(0.0f, 0.0f, 0.0f, 0.0f)
        }

    private fun setupFramebuffers() {
        try {
            shaderGroup!!.createBindFramebuffers(mc.displayWidth, mc.displayHeight)
        } catch (e : Exception) {
            LogManager.getLogger().error("Exception caught while setting up shader group", e)
        }
    }

    private fun setValues(strength: Float, x: Float, y: Float, w: Float, h: Float, width: Float, height: Float) {
        if (strength == lastStrength && lastX == x && lastY == y && lastW == w && lastH == h) return
        lastStrength = strength
        lastX = x
        lastY = y
        lastW = w
        lastH = h

        for (i in 0..1) {
            shaderGroup!!.listShaders[i].shaderManager.getShaderUniform("Radius").set(strength)
            shaderGroup!!.listShaders[i].shaderManager.getShaderUniform("BlurXY")[x] = height - y - h
            shaderGroup!!.listShaders[i].shaderManager.getShaderUniform("BlurCoord")[w] = h
        }
    }

    @JvmStatic
    fun blur(posX: Float, posY: Float, posXEnd: Float, posYEnd: Float, blurStrength: Float, displayClipMask: Boolean, triggerMethod: () -> Unit) {
        if (!OpenGlHelper.isFramebufferEnabled()) return

        var x = posX
        var y = posY
        var x2 = posXEnd
        var y2 = posYEnd

        if (x > x2) {
            val z = x
            x = x2
            x2 = z
        }

        if (y > y2) {
            val z = y
            y = y2
            y2 = y
        }

        val sc = ScaledResolution(mc)
        val scaleFactor = sc.scaleFactor
        val width = sc.scaledWidth
        val height = sc.scaledHeight

        if (sizeHasChanged(scaleFactor, width, height))
            setupFramebuffers()

        lastFactor = scaleFactor
        lastWidth = width
        lastHeight = height

        setValues(blurStrength, x, y, x2 - x, y2 - y, width.toFloat(), height.toFloat())

        framebuffer.bindFramebuffer(true)
        shaderGroup!!.loadShaderGroup(mc.timer.renderPartialTicks)
        mc.getFramebuffer().bindFramebuffer(true)

        Stencil.write(displayClipMask)
        triggerMethod()

        Stencil.erase(true)
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(770, 771)
        GlStateManager.pushMatrix()
        GlStateManager.colorMask(true, true, true, false)
        GlStateManager.disableDepth()
        GlStateManager.depthMask(false)
        GlStateManager.enableTexture2D()
        GlStateManager.disableLighting()
        GlStateManager.disableAlpha()
        frbuffer!!.bindFramebufferTexture()
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F)
        val f2 = frbuffer.framebufferWidth.toDouble() / frbuffer.framebufferTextureWidth.toDouble()
        val f3 = frbuffer.framebufferHeight.toDouble() / frbuffer.framebufferTextureHeight.toDouble()
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.getWorldRenderer()
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
        worldrenderer.pos(0.0, height.toDouble(), 0.0).tex(0.0, 0.0).color(255, 255, 255, 255).endVertex()
        worldrenderer.pos(width.toDouble(), height.toDouble(), 0.0).tex(f2, 0.0).color(255, 255, 255, 255).endVertex()
        worldrenderer.pos(width.toDouble(), 0.0, 0.0).tex(f2, f3).color(255, 255, 255, 255).endVertex()
        worldrenderer.pos(0.0, 0.0, 0.0).tex(0.0, f3).color(255, 255, 255, 255).endVertex()
        tessellator.draw()
        frbuffer.unbindFramebufferTexture()
        GlStateManager.enableDepth()
        GlStateManager.depthMask(true)
        GlStateManager.colorMask(true, true, true, true)
        GlStateManager.popMatrix()
        GlStateManager.disableBlend()

        Stencil.dispose()
        GlStateManager.enableAlpha()
    }

    fun draw(x: Float, y: Float, width: Float, height: Float, radius: Float) {
        val scale = StaticStorage.scaledResolution
        val factor = scale.scaleFactor
        val factor2 = scale.scaledWidth
        val factor3 = scale.scaledHeight
        if (lastScale != factor || lastScaleWidth != factor2 || lastScaleHeight != factor3) {
            reinitShader()
            }
        }

    @JvmStatic
    fun blurArea(x: Float, y: Float, x2: Float, y2: Float, blurStrength: Float) = blur(x, y, x2, y2, blurStrength, false) {
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderUtils.quickDrawRect(x, y, x2, y2)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    @JvmStatic
    fun blurAreaRounded(x: Float, y: Float, x2: Float, y2: Float, rad: Float, blurStrength: Float) = blur(x, y, x2, y2, blurStrength, false) {
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        RenderUtils.fastRoundedRect(x, y, x2, y2, rad)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun sizeHasChanged(scaleFactor: Int, width: Int, height: Int): Boolean = (lastFactor != scaleFactor || lastWidth != width || lastHeight != height)
}