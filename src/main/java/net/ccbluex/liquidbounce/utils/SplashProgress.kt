/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.ui.font.cf.FontLoaders
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.SimpleTexture
import net.minecraft.client.shader.Framebuffer
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color

object SplashProgress {
    private const val MAX = 4

    var width: Int = 0

    var height: Int = 0
    private var progress = 0
    private var currentText = ""
    private var splash: ResourceLocation? = null

    private var renderProgress = 0

    fun update() {
        if (Minecraft.getMinecraft() == null || MinecraftInstance.mc.languageManager == null) {
            return
        }

        drawSplash()
    }

    fun setProgress(giveProgress: Int, giveText: String) {
        progress = giveProgress
        currentText = giveText
        update()
    }

    fun setProgress(giveText: String) {
        progress++
        currentText = giveText
        update()
    }

    fun drawSplash() {
        val mc = MinecraftInstance.mc ?: return
        val textureManager = mc.textureManager ?: return

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
        GlStateManager.disableLighting()
        GlStateManager.disableFog()
        GlStateManager.disableDepth()
        GlStateManager.enableTexture2D()

        if (splash == null) {
            splash = ResourceLocation("fdpclient/misc/splash.png")
        }

        if (!textureManager.loadTexture(splash, SimpleTexture(splash))) {

            return
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureManager.getTexture(splash).glTextureId)

        GlStateManager.resetColor()
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
        if (MinecraftInstance.mc.gameSettings == null || MinecraftInstance.mc.textureManager == null) {
            return
        }

        val scaledResolution = ScaledResolution(MinecraftInstance.mc)

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

        GlStateManager.resetColor()

        FontLoaders.Roboto.drawString(
            currentText,
            20f,
            (scaledResolution.scaledHeight - 20).toFloat(),
            Color(255, 255, 255).rgb
        )

        val indexText = progress.toString() + "/" + MAX
        FontLoaders.Roboto.drawString(
            indexText,
            (scaledResolution.scaledWidth - 20 - FontLoaders.Roboto.getStringWidth(indexText)).toFloat(),
            (scaledResolution.scaledWidth - 25).toFloat(),
            Color(254, 228, 1).rgb
        )
        GlStateManager.resetColor()

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