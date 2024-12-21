/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.font

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.FDPClient.CLIENT_CLOUD
import net.ccbluex.liquidbounce.file.FileManager.fontsDir
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.io.URLRegistryUtils.FONTS
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.io.HttpUtils.download
import net.ccbluex.liquidbounce.utils.io.extractZipTo
import net.ccbluex.liquidbounce.utils.io.jsonArray
import net.ccbluex.liquidbounce.utils.io.readJson
import net.ccbluex.liquidbounce.utils.io.writeJson
import net.minecraft.client.gui.FontRenderer
import java.awt.Font
import java.io.File
import kotlin.system.measureTimeMillis

data class FontInfo(val name: String, val size: Int = -1, val isCustom: Boolean = false)

private val FONT_REGISTRY = LinkedHashMap<FontInfo, FontRenderer>()
object Fonts : MinecraftInstance {

    val minecraftFont: FontRenderer = mc.fontRendererObj

    lateinit var font15: GameFontRenderer

    lateinit var font20: GameFontRenderer

    lateinit var fontTiny: GameFontRenderer

    lateinit var fontSmall: GameFontRenderer

    lateinit var font35: GameFontRenderer

    lateinit var font40: GameFontRenderer

    lateinit var font72: GameFontRenderer

    lateinit var fontBold180: GameFontRenderer

    lateinit var fontSFUI35: GameFontRenderer

    lateinit var fontSFUI40: GameFontRenderer

    lateinit var fontIcons35: GameFontRenderer

    lateinit var fontIconXD85: GameFontRenderer

    lateinit var fontNovoAngularIcon85: GameFontRenderer

    private fun <T : FontRenderer> register(fontInfo: FontInfo, fontRenderer: T): T {
        FONT_REGISTRY[fontInfo] = fontRenderer
        return fontRenderer
    }

    fun loadFonts() {
        LOGGER.info("Loading Fonts.")

        val time = measureTimeMillis {
            downloadFonts()
            register(FontInfo(name = "Minecraft Font"), minecraftFont)
            font35 = register(FontInfo(name = "Roboto Medium", size = 35),
                getFontFromFile("Roboto-Medium.ttf", 35).asGameFontRenderer())
            font40 = register(FontInfo(name = "Roboto Medium", size = 40),
                getFontFromFile("Roboto-Medium.ttf", 40).asGameFontRenderer())
            fontBold180 = register(FontInfo(name = "Roboto Bold", size = 180),
                getFontFromFile("Roboto-Bold.ttf", 180).asGameFontRenderer())
            fontSmall = register(FontInfo(name = "Roboto Medium", size = 30),
                getFontFromFile("Roboto-Medium.ttf", 30).asGameFontRenderer())
            fontTiny = register(FontInfo(name = "Roboto Medium", size = 24),
                getFontFromFile("Roboto-Medium.ttf", 24).asGameFontRenderer())
            fontTiny = register(FontInfo(name = "Roboto Medium", size = 24),
                getFontFromFile("Roboto-Medium.ttf", 24).asGameFontRenderer())
            font15 = register(FontInfo(name = "Roboto Medium", size = 15),
                getFontFromFile("Roboto-Medium.ttf", 20).asGameFontRenderer())
            font20 = register(FontInfo(name = "Roboto Medium", size = 20),
                getFontFromFile("Roboto-Medium.ttf", 15).asGameFontRenderer())
            font35 = register(FontInfo(name = "Roboto Medium", size = 35),
                getFontFromFile("Roboto-Medium.ttf", 35).asGameFontRenderer())
            font40 = register(FontInfo(name = "Roboto Medium", size = 40),
                getFontFromFile("Roboto-Medium.ttf", 40).asGameFontRenderer())
            font72 = register(FontInfo(name = "Roboto Medium", size = 72),
                getFontFromFile("Roboto-Medium.ttf", 72).asGameFontRenderer())
            // SFUI
            fontSFUI35 = register(FontInfo(name = "sfui", size = 35),
                getFontFromFile("sfui.ttf", 35).asGameFontRenderer())
            fontSFUI40 = register(FontInfo(name = "sfui", size = 40),
                getFontFromFile("sfui.ttf", 40).asGameFontRenderer())
            // icons
            fontIcons35 = register(FontInfo(name = "aqua", size = 35),
                getFontFromFile("aquaIcons.ttf", 35).asGameFontRenderer())
            fontIconXD85 = register(FontInfo(name = "iconxd", size = 85),
                getFontFromFile("iconxd.ttf", 85).asGameFontRenderer())
            fontNovoAngularIcon85 = register(FontInfo(name = "novoangular", size = 85),
                getFontFromFile("novoangular.ttf", 85).asGameFontRenderer())

            loadCustomFonts()
        }
        LOGGER.info("Loaded Fonts. (${time}ms)")
    }

    private fun loadCustomFonts() {
        FONT_REGISTRY.keys.removeIf { it.isCustom }

        File(fontsDir, "fonts.json").apply {
            if (exists()) {
                val jsonElement = readJson()

                if (jsonElement !is JsonArray) return@apply

                for (element in jsonElement) {
                    if (element !is JsonObject) return@apply

                    val font = getFontFromFile(element["fontFile"].asString, element["fontSize"].asInt)

                    FONT_REGISTRY[FontInfo(font.name, font.size, isCustom = true)] = GameFontRenderer(font)
                }
            } else {
                createNewFile()
                writeJson(jsonArray())
            }
        }
    }

    private fun downloadFonts() {
        val robotoZipFile = File(fontsDir, "roboto.zip")
        if (!robotoZipFile.exists()) {
            LOGGER.info("Downloading roboto fonts...")
            download("$CLIENT_CLOUD/fonts/Roboto.zip", robotoZipFile)
            LOGGER.info("Extract roboto fonts...")
            robotoZipFile.extractZipTo(fontsDir)
        }

        val fontZipFile = File(fontsDir, "font.zip")
        if (!fontZipFile.exists()) {
            LOGGER.info("Downloading additional fonts...")
            download("${FONTS}/Font.zip", fontZipFile)
        }

        if(fontZipFile.exists()){
            LOGGER.info("Font zip file exists, trying to extract...")
            if(!fontsDir.exists()){
                LOGGER.info("Fonts directory does not exist, trying to create...")
                fontsDir.mkdirs()
            }
            try{
                fontZipFile.extractZipTo(fontsDir){file ->
                    LOGGER.info("Extracted: ${file.absolutePath}")
                }
                val extractedFiles = fontsDir.listFiles { file -> file.isFile && file.name.endsWith(".ttf") }
                if (extractedFiles != null && extractedFiles.isNotEmpty()) {
                    LOGGER.info("Fonts extracted successfully:")
                    extractedFiles.forEach{file ->
                        LOGGER.info(" - ${file.absolutePath}")
                    }
                }else {
                    LOGGER.warn("No .ttf files extracted")
                }
            }catch (e:Exception){
                LOGGER.error("Error during extraction", e)
            }

        }else{
            LOGGER.warn("font not found")
        }

    }

    fun getFontRenderer(name: String, size: Int): FontRenderer {
        return FONT_REGISTRY.entries.firstOrNull { (fontInfo, _) ->
            fontInfo.size == size && fontInfo.name.equals(name, true)
        }?.value ?: minecraftFont
    }

    fun getFontDetails(fontRenderer: FontRenderer): FontInfo? {
        return FONT_REGISTRY.keys.firstOrNull { FONT_REGISTRY[it] == fontRenderer }
    }

    val fonts: List<FontRenderer>
        get() = FONT_REGISTRY.values.toList()

    private fun getFontFromFile(fontName: String, size: Int): Font = try {
        File(fontsDir, fontName).inputStream().use { inputStream ->
            Font.createFont(Font.TRUETYPE_FONT, inputStream).deriveFont(Font.PLAIN, size.toFloat())
        }
    } catch (e: Exception) {
        LOGGER.warn("Exception during loading font[name=${fontName}, size=${size}]", e)
        Font("default", Font.PLAIN, size)
    }

    private fun Font.asGameFontRenderer(): GameFontRenderer {
        return GameFontRenderer(this@asGameFontRenderer)
    }
}