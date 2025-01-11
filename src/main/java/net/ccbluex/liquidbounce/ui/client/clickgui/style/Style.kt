/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style

import net.ccbluex.liquidbounce.config.ColorValue
import net.ccbluex.liquidbounce.config.Value
import net.ccbluex.liquidbounce.file.FileManager.saveConfig
import net.ccbluex.liquidbounce.file.FileManager.valuesConfig
import net.ccbluex.liquidbounce.ui.client.clickgui.Panel
import net.ccbluex.liquidbounce.ui.client.clickgui.elements.ButtonElement
import net.ccbluex.liquidbounce.ui.client.clickgui.elements.ModuleElement
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.client.asResourceLocation
import net.ccbluex.liquidbounce.utils.client.playSound
import net.ccbluex.liquidbounce.utils.timing.WaitTickUtils
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.math.BigDecimal
import java.nio.ByteBuffer
import kotlin.math.max

abstract class Style : MinecraftInstance {
    protected var sliderValueHeld: Value<*>? = null
        get() {
            if (!Mouse.isButtonDown(0)) field = null
            return field
        }

    abstract fun drawPanel(mouseX: Int, mouseY: Int, panel: Panel)
    abstract fun drawHoverText(mouseX: Int, mouseY: Int, text: String)
    abstract fun drawButtonElement(mouseX: Int, mouseY: Int, buttonElement: ButtonElement)
    abstract fun drawModuleElementAndClick(mouseX: Int, mouseY: Int, moduleElement: ModuleElement, mouseButton: Int?): Boolean

    data class ColorValueCache(val lastHue: Float, val cachedTextureID: Int)

    private val colorValueCache: MutableMap<ColorValue, MutableMap<Int, ColorValueCache>> = mutableMapOf()

    fun ColorValue.updateTextureCache(
        id: Int, hue: Float, width: Int, height: Int, generateImage: (BufferedImage, Graphics2D) -> Unit,
        drawAt: (Int) -> Unit
    ) {
        val cached = colorValueCache[this]?.get(id)
        val lastHue = cached?.lastHue

        if (lastHue == null || lastHue != hue) {
            val image = createRGBImageDrawing(width, height) { img, graphics -> generateImage(img, graphics) }
            val texture = convertImageToTexture(image)
            colorValueCache.getOrPut(this, ::mutableMapOf)[id] = ColorValueCache(hue, texture)
        }

        colorValueCache[this]?.get(id)?.cachedTextureID?.let(drawAt)
    }

    private fun createRGBImageDrawing(width: Int, height: Int, f: (BufferedImage, Graphics2D) -> Unit): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()

        f(image, g)

        g.dispose()
        return image
    }

    private fun convertImageToTexture(image: BufferedImage): Int {
        val width = image.width
        val height = image.height

        val pixels = IntArray(width * height)

        image.getRGB(0, 0, width, height, pixels, 0, width)

        val buffer = ByteBuffer.allocateDirect(width * height * 4)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            buffer.put(((pixel shr 16) and 0xFF).toByte())
            buffer.put(((pixel shr 8) and 0xFF).toByte())
            buffer.put(((pixel shr 0) and 0xFF).toByte())
            buffer.put(((pixel shr 24) and 0xFF).toByte())
        }

        buffer.flip()

        val textureID = glGenTextures()

        glPushAttrib(GL_ALL_ATTRIB_BITS)
        glPushMatrix()

        glBindTexture(GL_TEXTURE_2D, textureID)

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer)

        glPopMatrix()
        glPopAttrib()

        return textureID
    }

    fun drawTexture(textureID: Int, x: Int, y: Int, width: Int, height: Int) {
        glPushAttrib(GL_ALL_ATTRIB_BITS)
        glPushMatrix()

        glEnable(GL_TEXTURE_2D)

        glBindTexture(GL_TEXTURE_2D, textureID)

        glTranslatef(x.toFloat(), y.toFloat(), 0.0f)

        glBegin(GL_QUADS)
        glTexCoord2f(0.0f, 0.0f); glVertex2f(0.0f, 0.0f) // Bottom-left corner
        glTexCoord2f(1.0f, 0.0f); glVertex2f(width.toFloat(), 0.0f) // Bottom-right corner
        glTexCoord2f(1.0f, 1.0f); glVertex2f(width.toFloat(), height.toFloat()) // Top-right corner
        glTexCoord2f(0.0f, 1.0f); glVertex2f(0.0f, height.toFloat()) // Top-left corner
        glEnd()

        glDisable(GL_TEXTURE_2D)

        glPopMatrix()
        glPopAttrib()
    }

    fun clickSound() {
        mc.playSound("gui.button.press".asResourceLocation())
    }

    fun showSettingsSound() {
        mc.playSound("random.bow".asResourceLocation())
    }

    protected fun round(v: Float): Float {
        var bigDecimal = BigDecimal(v.toString())
        bigDecimal = bigDecimal.setScale(2, 4)
        return bigDecimal.toFloat()
    }

    protected fun getHoverColor(color: Color, hover: Int, inactiveModule: Boolean = false): Int {
        val r = color.red - hover * 2
        val g = color.green - hover * 2
        val b = color.blue - hover * 2
        val alpha = if (inactiveModule) color.alpha.coerceAtMost(128) else color.alpha

        return Color(max(r, 0), max(g, 0), max(b, 0), alpha).rgb
    }

    fun <T> Value<T>.setAndSaveValueOnButtonRelease(new: T) {
        set(new, false)

        with(WaitTickUtils) {
            if (!hasScheduled(this)) {
                conditionalSchedule(this, 10) {
                    (sliderValueHeld == null).also { if (it) saveConfig(valuesConfig) }
                }
            }
        }
    }
}