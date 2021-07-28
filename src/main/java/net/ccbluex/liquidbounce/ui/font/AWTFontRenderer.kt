/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.font

import net.ccbluex.liquidbounce.LiquidBounce
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Generate new bitmap based font renderer
 */
class AWTFontRenderer(val font: Font, initialize: Boolean = true) {

    private val fontHeight: Int

    private val chars=HashMap<Char, FontChar>()

    private val fontMetrics: FontMetrics
    private val cacheDir=File(LiquidBounce.fileManager.cacheDir,getFontDirName())

    val height: Int
        get() = (fontHeight - 8) / 2

    init {
        if(!cacheDir.exists())
            cacheDir.mkdir()

        val graphics = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).graphics as Graphics2D
        putHints(graphics)
        graphics.font = font
        fontMetrics=graphics.fontMetrics
        fontHeight = if (fontMetrics.height <= 0){ font.size }else{ fontMetrics.height + 3 }

        if(initialize){
            // 先把英文渲染好,其他的被动渲染
            loadChar(' ')
            prepareCharImages('0','9')
            prepareCharImages('a','z')
            prepareCharImages('A','Z')
        }
    }

    /**
     * Allows you to draw a string with the target font
     *
     * @param text  to render
     * @param x     location for target position
     * @param y     location for target position
     * @param color of the text
     */
    fun drawString(text: String, x: Double, y: Double, color: Int) {
        val scale = 0.25

        GlStateManager.pushMatrix()
        GlStateManager.scale(scale, scale, scale)
        GL11.glTranslated(x * 2F, y * 2.0 - 2.0, 0.0)

        val red = (color shr 16 and 0xff) / 255F
        val green = (color shr 8 and 0xff) / 255F
        val blue = (color and 0xff) / 255F
        val alpha = (color shr 24 and 0xff) / 255F

        GlStateManager.color(red, green, blue, alpha)

        for(char in text.toCharArray()){
            val singleWidth=drawChar(char, 0f, 0f)
            GL11.glTranslatef(singleWidth-8f,0f,0f)
        }

        GlStateManager.popMatrix()
    }

    private fun getFontDirName() = "${font.fontName.replace(" ","_").toLowerCase()}${if(font.isBold){"-bold"}else{""}}${if(font.isItalic){"-italic"}else{""}}-${font.size}"

    /**
     * Draw char from texture to display
     *
     * @param char target font char to render
     * @param x        target position x to render
     * @param y        target position y to render
     */
    private fun drawChar(char: Char, x: Float, y: Float): Int {
        val fontChar=chars[char] ?: loadChar(char)
        Minecraft.getMinecraft().textureManager.bindTexture(fontChar.resourceLocation)
        Gui.drawModalRectWithCustomSizedTexture(x.toInt(), y.toInt(), 0f, 0f, fontChar.width, fontHeight, fontChar.width.toFloat(), fontHeight.toFloat())
        return fontChar.width
    }

    /**
     * Calculate the string width of a text
     *
     * @param text for width calculation
     * @return the width of the text
     */
    fun getStringWidth(text: String): Int {
        var width=0

        for(char in text.toCharArray()){
            val fontChar=chars[char] ?: loadChar(char)
            width+=fontChar.width-8
        }

        return width/2
    }

    private fun putHints(graphics: Graphics2D){
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    }

    /**
     * 获取char对应的缓存文件
     */
    private fun getCharCacheFile(char: Char) = File(cacheDir,"char-${char.toInt()}.png")

    /**
     * @return 通过Char获取的ResourceLocation
     */
    private fun getResourceLocationByChar(char: Char) = ResourceLocation("fdp/font/${getFontDirName()}/char-${char.toInt()}")

    /**
     * 渲染字符图片
     */
    private fun renderCharImage(char: Char): FontChar {
        var charWidth = fontMetrics.charWidth(char) + 8
        if (charWidth <= 0)
            charWidth = 7

        val fontImage = BufferedImage(charWidth, fontHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics = fontImage.graphics as Graphics2D
        putHints(graphics)
        graphics.font = font
        graphics.color = Color.WHITE
        graphics.drawString(char.toString(), 3, 1 + fontMetrics.ascent)

        return FontChar(char, getResourceLocationByChar(char), fontImage)
    }

    /**
     * @param char 字符
     * 初始化单个字符图片
     */
    private fun loadChar(char: Char): FontChar {
        val fc=loadCharImageFromCache(char)
        chars[char] = fc
        return fc
    }

    /**
     * 从本地缓存读取字符图片
     * 没有则渲染
     */
    private fun loadCharImageFromCache(char: Char): FontChar {
        val charImageFile=getCharCacheFile(char)
        return if(charImageFile.exists()){
            FontChar(char, getResourceLocationByChar(char), ImageIO.read(charImageFile))
        }else{
            saveFontCharToCache(renderCharImage(char))
        }
    }

    /**
     * 将渲染好的FontChar保存至缓存
     * @return 传入的FontChar
     */
    private fun saveFontCharToCache(fontChar: FontChar): FontChar {
        ImageIO.write(fontChar.bufImg,"png",getCharCacheFile(fontChar.char))
        return fontChar
    }

    /**
     * @param start 开始字符
     * @param stop 结束字符
     * 如果需要初始化单个直接loadChar(char)就行
     * 预初始化字符图片
     */
    private fun prepareCharImages(start: Char, stop: Char){
        val startAscii=start.toInt().coerceAtMost(stop.toInt())
        val stopAscii=stop.toInt().coerceAtLeast(start.toInt())

        for (ascii in startAscii until stopAscii){
            loadChar(ascii.toChar())
        }
    }
}