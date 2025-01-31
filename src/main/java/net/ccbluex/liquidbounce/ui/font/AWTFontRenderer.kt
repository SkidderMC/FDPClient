/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.font

import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.kotlin.LruCache
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.GlStateManager.bindTexture
import net.minecraft.client.renderer.texture.TextureUtil
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.GL11.*
import java.awt.*
import java.awt.image.BufferedImage
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * A memory-optimized bitmap-based font renderer using AWT [Font].
 *
 * @author opZywl
 */
@SideOnly(Side.CLIENT)
class AWTFontRenderer(
    val font: Font,
    startChar: Int = 0,
    stopChar: Int = 255,
    private val loadingScreen: Boolean = false
) : MinecraftInstance {

    companion object : Listenable {
        var assumeNonVolatile: Boolean = false

        /** All active font renderers (for GC tasks). */
        private val activeFontRenderers = mutableListOf<AWTFontRenderer>()

        /**
         * Runs a block with [assumeNonVolatile] = true, then restores it.
         */
        inline fun assumeNonVolatile(block: () -> Unit) {
            assumeNonVolatile = true
            try {
                block()
            } finally {
                assumeNonVolatile = false
            }
        }

        // Garbage collection constants
        private const val GC_TICKS = 600                    // Do GC every 600 frames
        private const val CACHED_FONT_REMOVAL_TIME = 30000L // 30s time-based eviction
        private const val MAX_CACHED_STRINGS = 255          // LRU cache size limit

        private var gcTicks = 0

        /**
         * Should be called each frame or so. Every 600 frames, we run garbage collection
         * on every active font renderer.
         */
        private val onRender2D = handler<Render2DEvent>(priority = Byte.MIN_VALUE) {
            if (++gcTicks > GC_TICKS) {
                activeFontRenderers.forEach { it.collectGarbage() }
                gcTicks = 0
            }
        }
    }

    /**
     * Info about each character's location in the texture.
     */
    private data class CharLocation(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int
    )

    /**
     * Stores a compiled display list for a specific string, plus a "last usage" timestamp.
     */
    private data class CachedFont(
        val displayList: Int,
        var lastUsage: Long,
        var deleted: Boolean = false
    )

    private val charLocations = arrayOfNulls<CharLocation>(stopChar)

    /**
     * We store strings in an LRU-like map with time-based eviction:
     * - If the size exceeds [MAX_CACHED_STRINGS], we remove the eldest entry.
     * - If an entry hasn't been used for 30s, we remove it.
     */
    private val cachedStringFonts = LruCache<String, CachedFont>(MAX_CACHED_STRINGS)

    /**
     * We don't do GC for the cached widths because they are constant for each string
     */
    private val cachedStringWidths = LruCache<String, Int>(MAX_CACHED_STRINGS)

    private var textureID: Int = -1
    private var textureWidth: Int = 0
    private var textureHeight: Int = 0
    private var fontHeight: Int = -1

    /**
     * Typical "font height" to use in layout, derived from [fontHeight].
     * Adjust as needed.
     */
    val height: Int
        get() = (fontHeight - 8) / 2

    init {
        // Generate the large bitmap with all glyphs
        renderBitmap(startChar, stopChar)

        // Register for GC tasks
        activeFontRenderers += this
    }

    /**
     * Draw [text] at ([x], [y]) with [color]. Scales by 0.25 => typical UI text size.
     */
    fun drawString(text: String, x: Double, y: Double, color: Int) {
        // Scale down => 0.25 => then everything is "2 * x" in real coords
        glPushMatrix()
        glScaled(0.25, 0.25, 0.25)

        // Shift to final position
        glTranslated(x * 2.0, y * 2.0 - 2.0, 0.0)

        if (loadingScreen) {
            glBindTexture(GL_TEXTURE_2D, textureID)
        } else {
            bindTexture(textureID)
        }

        // Extract ARGB
        val (alpha, red, green, blue) = ColorUtils.unpackARGBFloatValue(color)
        glColor4f(red, green, blue, alpha)

        // 1) If we've cached this text, just call the display list
        val cached = cachedStringFonts[text]
        if (cached != null) {
            glCallList(cached.displayList)
            cached.lastUsage = System.currentTimeMillis()
            glPopMatrix()
            return
        }

        // 2) Not cached => build it now
        var listID = -1
        if (assumeNonVolatile) {
            listID = glGenLists(1)
            glNewList(listID, GL_COMPILE_AND_EXECUTE)
        }

        glBegin(GL_QUADS)

        var currX = 0f
        var fallbackWidth = 0f // fallback for MC glyphs

        for (char in text) {
            val loc = charLocations.getOrNull(char.code)
            if (loc == null) {
                // Fallback => break quads, draw with MC font
                glEnd()
                GlStateManager.resetColor()

                glPushMatrix()

                // Because we scaled by 0.25 => revert
                val rev = 4.0f
                glScalef(rev, rev, rev)

                // Then scale by (font.size / 32.0)
                val scale = font.size / 32.0f
                glScalef(scale, scale, 1.0f)

                mc.fontRendererObj.posY = 1.0f
                mc.fontRendererObj.posX = (currX / rev) + fallbackWidth

                val fallbackW = mc.fontRendererObj.renderUnicodeChar(char, false).coerceAtLeast(0f)
                fallbackWidth += fallbackW

                if (loadingScreen) {
                    glBindTexture(GL_TEXTURE_2D, textureID)
                } else {
                    bindTexture(textureID)
                }
                glPopMatrix()
                glBegin(GL_QUADS)
            } else {
                drawChar(loc, currX + (fallbackWidth * 4f), 0f)
                currX += (loc.width - 8f)
            }
        }

        glEnd()

        if (assumeNonVolatile && listID >= 0) {
            // Insert into our LRU + time-based map
            cachedStringFonts[text] = CachedFont(listID, System.currentTimeMillis())
            glEndList()
        }

        glPopMatrix()
    }

    /**
     * Returns the pixel-width of [text]. If a character is not in [charLocations],
     * we fallback to MC's font (approx).
     */
    fun getStringWidth(text: String): Int = cachedStringWidths.getOrPut(text) {
        var myWidth = 0
        var fallbackWidth = 0f
        val fallbackScale = font.size / 32f

        for (char in text) {
            val loc = charLocations.getOrNull(char.code)
            if (loc == null) {
                val w = mc.fontRendererObj.getCharWidth(char)
                fallbackWidth += ((w + 8) * fallbackScale).coerceAtLeast(0f)
            } else {
                myWidth += (loc.width - 8)
            }
        }

        (myWidth / 2) + fallbackWidth.roundToInt()
    }

    /**
     * Disposes of this font, removing it from the list and freeing texture memory.
     */
    fun dispose() {
        if (textureID != -1) {
            glDeleteTextures(textureID)
            textureID = -1
        }
        activeFontRenderers.remove(this)
    }

    /** If the user forgets to call [dispose], still free resources. */
    protected fun finalize() {
        dispose()
    }

    private fun drawChar(loc: CharLocation, x: Float, y: Float) {
        val w = loc.width.toFloat()
        val h = loc.height.toFloat()

        val u = loc.x.toFloat() / textureWidth
        val v = loc.y.toFloat() / textureHeight
        val uw = w / textureWidth
        val vh = h / textureHeight

        // 4 corners
        glTexCoord2f(u, v)
        glVertex2f(x, y)

        glTexCoord2f(u, v + vh)
        glVertex2f(x, y + h)

        glTexCoord2f(u + uw, v + vh)
        glVertex2f(x + w, y + h)

        glTexCoord2f(u + uw, v)
        glVertex2f(x + w, y)
    }

    /**
     * Builds the single large texture with [startChar] to [stopChar] glyphs.
     */
    private fun renderBitmap(startChar: Int, stopChar: Int) {
        val fontImages = arrayOfNulls<BufferedImage>(stopChar)

        var rowHeight = 0
        var charX = 0
        var charY = 0

        for (charCode in startChar until stopChar) {
            val charImg = drawCharToImage(charCode.toChar())
            val cw = charImg.width
            val ch = charImg.height

            if (ch > fontHeight) {
                fontHeight = ch
            }

            val loc = CharLocation(charX, charY, cw, ch)
            charLocations[charCode] = loc
            fontImages[charCode] = charImg

            charX += cw
            if (cw > 0 && ch > rowHeight) {
                rowHeight = ch
            }
            // If exceeding ~2k width, break line
            if (charX > 2048) {
                if (charX > textureWidth)
                    textureWidth = charX
                charX = 0
                charY += rowHeight
                rowHeight = 0
            }
        }
        // finalize
        textureWidth = max(textureWidth, charX)
        textureHeight = charY + rowHeight

        // Big final image
        val bigImage = BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB)
        val g = bigImage.createGraphics()
        g.font = font
        g.color = Color(255, 255, 255, 0) // transparent
        g.fillRect(0, 0, textureWidth, textureHeight)
        g.color = Color.WHITE

        // Draw each char subimage
        for (charCode in startChar until stopChar) {
            val subImg = fontImages[charCode] ?: continue
            val loc = charLocations[charCode] ?: continue
            g.drawImage(subImg, loc.x, loc.y, null)
        }

        // Upload to GPU
        textureID = TextureUtil.uploadTextureImageAllocate(TextureUtil.glGenTextures(), bigImage, true, true)
    }

    /**
     * Draws a single char [c] into a small [BufferedImage].
     */
    private fun drawCharToImage(c: Char): BufferedImage {
        // measure to get width/height
        val measureImg = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
        val measureG = measureImg.createGraphics()
        measureG.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        measureG.font = font

        val fm = measureG.fontMetrics
        var w = fm.charWidth(c) + 8
        if (w <= 0) w = 7
        var h = fm.height + 3
        if (h <= 0) h = font.size

        // real
        val charImg = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
        val g2d = charImg.createGraphics()
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g2d.font = font
        g2d.color = Color.WHITE
        g2d.drawString(c.toString(), 3, 1 + fm.ascent)
        return charImg
    }

    /**
     * Removes unused / old strings from the cache. Called by global GC every so often.
     */
    private fun collectGarbage() {
        val now = System.currentTimeMillis()

        with(cachedStringFonts.entries.iterator()) {
            while (hasNext()) {
                val cached = next().value
                if (!cached.deleted && (now - cached.lastUsage) > CACHED_FONT_REMOVAL_TIME) {
                    glDeleteLists(cached.displayList, 1)
                    cached.deleted = true
                    remove()
                }
            }
        }
    }
}