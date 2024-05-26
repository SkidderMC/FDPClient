/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.client.renderer.texture.SimpleTexture
import net.minecraft.client.shader.Framebuffer
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color

object SplashProgress {
    private const val MAX = 4

    var progress = 0
    var currentText = ""
    var secondaryText = ""
    private var splash: ResourceLocation? = null
    private var renderProgress = 0

    fun update() {
        if (Minecraft.getMinecraft().languageManager == null) {
            return
        }

        drawSplash()
    }

    fun setProgress(giveProgress: Int, giveText: String) {
        progress = giveProgress
        currentText = giveText
        secondaryText = ""
        update()
    }

    fun setSecondary(giveText: String) {
        secondaryText = giveText
        update()
    }

    fun setProgress(giveText: String) {
        progress++
        currentText = giveText
        update()
    }

    fun drawSplash() {
        val mc = Minecraft.getMinecraft()
        val textureManager = mc.textureManager

        val scaledResolution = ScaledResolution(mc)
        val scaleFactor = scaledResolution.scaleFactor

        val framebuffer = Framebuffer(scaledResolution.scaledWidth * scaleFactor, scaledResolution.scaledHeight * scaleFactor, true)
        framebuffer.bindFramebuffer(false)

        GlStateManager.matrixMode(GL11.GL_PROJECTION)
        GlStateManager.loadIdentity()
        GlStateManager.ortho(0.0, scaledResolution.scaledWidth.toDouble(), scaledResolution.scaledHeight.toDouble(), 0.0, 1000.0, 3000.0)
        GlStateManager.matrixMode(GL11.GL_MODELVIEW)
        GlStateManager.loadIdentity()
        GlStateManager.translate(0.0f, 0.0f, -2000.0f)
        disableLighting()
        disableFog()
        disableDepth()
        enableTexture2D()

        if (splash == null) {
            splash = ResourceLocation("fdpclient/gui/design/splash.png")
        }

        if (!textureManager.loadTexture(splash, SimpleTexture(splash))) {
            return
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureManager.getTexture(splash).glTextureId)

        resetColor()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)

        Gui.drawScaledCustomSizeModalRect(
            0,
            0,
            0f,
            0f,
            1920,
            1080,
            scaledResolution.scaledWidth,
            scaledResolution.scaledHeight,
            1920f,
            1080f
        )
        drawProgress()
        framebuffer.unbindFramebuffer()
        framebuffer.framebufferRender(scaledResolution.scaledWidth * scaleFactor, scaledResolution.scaledHeight * scaleFactor)

        GlStateManager.enableAlpha()
        GlStateManager.alphaFunc(516, 0.1f)

        mc.updateDisplay()
    }

    private fun drawProgress() {
        val mc = Minecraft.getMinecraft()
        if (mc.gameSettings == null || mc.textureManager == null) {
            return
        }

        val scaledResolution = ScaledResolution(mc)

        val nProgress = progress.toDouble()
        val calculation = (nProgress / MAX) * scaledResolution.scaledWidth

        renderProgress = calculation.toInt()

        Gui.drawRect(
            0,
            scaledResolution.scaledHeight - 35,
            scaledResolution.scaledWidth,
            scaledResolution.scaledHeight,
            Color(0, 0, 0, 50).rgb
        )

        resetColor()

        mc.fontRendererObj.drawString(
            currentText,
            20,
            scaledResolution.scaledHeight - if (secondaryText == "") {20} else {38},
            Color(255, 255, 255).rgb
        )

        mc.fontRendererObj.drawString(
            secondaryText,
            20,
            scaledResolution.scaledHeight - 20,
            Color(200, 200, 200).rgb
        )

        val indexText = "$progress/$MAX"
        mc.fontRendererObj.drawString(
            indexText,
            (scaledResolution.scaledWidth - 20 - mc.fontRendererObj.getStringWidth(indexText)),
            (scaledResolution.scaledHeight - 20),
            Color(254, 228, 1).rgb
        )

        Gui.drawRect(
            0,
            scaledResolution.scaledHeight - 2,
            renderProgress,
            scaledResolution.scaledHeight,
            Color(149, 201, 144).rgb
        )

        Gui.drawRect(
            0,
            scaledResolution.scaledHeight - 2,
            scaledResolution.scaledWidth,
            scaledResolution.scaledHeight,
            Color(0, 0, 0, 10).rgb
        )
    }
}
