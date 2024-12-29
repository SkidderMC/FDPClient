package net.ccbluex.liquidbounce.ui.font.fontmanager.impl

import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.ui.font.fontmanager.api.FontFamily
import net.ccbluex.liquidbounce.ui.font.fontmanager.api.FontManager
import net.ccbluex.liquidbounce.ui.font.fontmanager.api.FontType
import net.ccbluex.liquidbounce.ui.font.fontmanager.util.SneakyThrowing
import java.awt.Font
import java.awt.FontFormatException
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.EnumMap

/**
 * A simple FontManager implementation in Kotlin that reads `.ttf` fonts
 * @author opZywl
 */
class SimpleFontManager private constructor() : FontManager {

    private val fonts = FontRegistry()

    override fun fontFamily(fontType: FontType): FontFamily {
        return fonts.fontFamily(fontType)
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
     * Internal [EnumMap] that associates each [FontType] with its [FontFamily].
     */
    private class FontRegistry : EnumMap<FontType, FontFamily>(FontType::class.java) {

        /**
         * Retrieves a [FontFamily] for the specified [fontType], loading
         * the TTF file locally if it's not already cached.
         */
        fun fontFamily(fontType: FontType): FontFamily {
            return computeIfAbsent(fontType) {
                try {
                    SimpleFontFamily.create(fontType, readFontFromLocal(fontType))
                } catch (e: IOException) {
                    throw SneakyThrowing.sneakyThrow(e)
                }
            }
        }

        /**
         * Reads a `.ttf` file from [FileManager.fontsDir] corresponding to the given [fontType].
         */
        @Throws(IOException::class)
        private fun readFontFromLocal(fontType: FontType): Font {
            val fontFile = File(FileManager.fontsDir, fontType.fileName())
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
