/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render.shader

import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance.Companion.mc
import net.ccbluex.liquidbounce.utils.render.drawWithTessellatorWorldRenderer
import net.ccbluex.liquidbounce.utils.render.shader.shaders.BackgroundShader
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager.color
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import java.io.File
import java.util.concurrent.CountDownLatch
import javax.imageio.ImageIO

sealed class Background(val backgroundFile: File) {
    companion object {
        fun fromFile(backgroundFile: File): Background {
            return when (backgroundFile.extension) {
                "png" -> ImageBackground(backgroundFile)
                "frag", "glsl", "shader" -> ShaderBackground(backgroundFile)
                else -> throw IllegalArgumentException("Invalid background file extension")
            }.also {
                it.initBackground()
            }
        }
    }

    protected abstract fun initBackground()

    abstract fun drawBackground(width: Int, height: Int)
}

private class ImageBackground(backgroundFile: File) : Background(backgroundFile) {

    private val resourceLocation = ResourceLocation("${CLIENT_NAME.lowercase()}/background.png")

    override fun initBackground() {
        try {
            val image = ImageIO.read(backgroundFile.inputStream())
            mc.textureManager.loadTexture(resourceLocation, DynamicTexture(image))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun drawBackground(width: Int, height: Int) {
        mc.textureManager.bindTexture(resourceLocation)
        color(1f, 1f, 1f, 1f)
        Gui.drawScaledCustomSizeModalRect(0, 0, 0f, 0f, width, height, width, height, width.toFloat(), height.toFloat())
    }
}

private class ShaderBackground(backgroundFile: File) : Background(backgroundFile) {

    private var shaderInitialized = false
    private lateinit var shader: Shader
    private val initializationLatch = CountDownLatch(1)

    override fun initBackground() {
        runCatching {
            shader = BackgroundShader(backgroundFile)
        }.onFailure {
            LOGGER.error("Failed to load background.", it)
        }.onSuccess {
            initializationLatch.countDown()
            shaderInitialized = true
            LOGGER.info("Successfully loaded background.")
        }
    }

    override fun drawBackground(width: Int, height: Int) {
        if (!shaderInitialized) {
            try {
                initializationLatch.await()
            } catch (e: Exception) {
                LOGGER.error(e.message)
                return
            }
        }

        if (shaderInitialized) {
            shader.startShader()

            drawWithTessellatorWorldRenderer {
                begin(7, DefaultVertexFormats.POSITION)
                pos(0.0, height.toDouble(), 0.0).endVertex()
                pos(width.toDouble(), height.toDouble(), 0.0).endVertex()
                pos(width.toDouble(), 0.0, 0.0).endVertex()
                pos(0.0, 0.0, 0.0).endVertex()
            }

            shader.stopShader()
        }
    }
}