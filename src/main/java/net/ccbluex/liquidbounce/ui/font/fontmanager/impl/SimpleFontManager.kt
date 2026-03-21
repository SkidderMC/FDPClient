/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.font.fontmanager.impl

import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.ui.font.fontmanager.api.FontFamily
import net.ccbluex.liquidbounce.ui.font.fontmanager.api.FontManager
import net.ccbluex.liquidbounce.ui.font.fontmanager.util.SneakyThrowing
import net.ccbluex.liquidbounce.utils.kotlin.LruCache
import java.awt.Font
import java.awt.FontFormatException
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

/**
 * A simple FontManager implementation in Kotlin that reads `.ttf` fonts
 * @author opZywl
 */
class SimpleFontManager private constructor() : FontManager {

    private val fonts = FontRegistry()

    override fun fontFamily(name: String): FontFamily {
        return fonts.fontFamily(name)
    }

    companion object {
        /**
         * Creates a new instance of [SimpleFontManager].
         */
        @JvmStatic
        fun create(): FontManager {
            return SimpleFontManager()
        }
    }

    /**
     * Internal LRU cache that associates each font name with its [FontFamily].
     * Limits to 20 fonts and disposes evicted fonts to prevent memory leaks.
     */
    private class FontRegistry {

        companion object {
            private const val MAX_CACHED_FONTS = 20
        }

        private val cache = LruCache<String, FontFamily>(MAX_CACHED_FONTS)

        /**
         * Retrieves a [FontFamily] for the specified [fontType], loading
         * the TTF file locally if it's not already cached.
         */
        fun fontFamily(name: String): FontFamily {
            return cache.getOrPut(name) {
                try {
                    SimpleFontFamily.create(name, readFontFromLocal(name))
                } catch (e: IOException) {
                    throw SneakyThrowing.sneakyThrow(e)
                }
            }
        }

        /**
         * Reads a `.ttf` file from [FileManager.fontsDir] corresponding to the given [fontType].
         */
        @Throws(IOException::class)
        private fun readFontFromLocal(name: String): Font {
            val fontFile = File(FileManager.fontsDir, name)
            if (!fontFile.exists()) {
                throw IOException("Couldn't find local font file: ${fontFile.absolutePath}")
            }
            FileInputStream(fontFile).use { fis ->
                return readFont(fis)
            }
        }

        /**
         * Creates a [Font] from the [resource] (TTF data).
         */
        private fun readFont(resource: InputStream): Font {
            return try {
                Font.createFont(Font.TRUETYPE_FONT, resource)
            } catch (e: FontFormatException) {
                throw RuntimeException(
                    "Resource does not contain the required font tables " +
                            "for the specified format.", e
                )
            } catch (e: IOException) {
                throw RuntimeException(
                    "Couldn't completely read font resource.", e
                )
            }
        }
    }
}
