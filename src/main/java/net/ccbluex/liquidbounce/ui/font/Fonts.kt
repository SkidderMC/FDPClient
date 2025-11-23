/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.font

import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.FDPClient.CLIENT_CLOUD
import net.ccbluex.liquidbounce.file.FileManager.fontsDir
import net.ccbluex.liquidbounce.ui.font.fontmanager.impl.SimpleFontRenderer
import net.ccbluex.liquidbounce.ui.font.fontmanager.api.FontRenderer as CustomFontRenderer
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.io.URLRegistryUtils.FONTS
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.io.extractZipTo
import net.ccbluex.liquidbounce.utils.io.*
import net.minecraft.client.gui.FontRenderer
import java.awt.Font
import java.io.File
import kotlin.system.measureTimeMillis

data class FontInfo(val name: String, val size: Int = -1, val isCustom: Boolean = false)

data class CustomFontInfo(val name: String, val fontFile: String, val fontSize: Int)

private val CUSTOM_FONT_REGISTRY = LinkedHashMap<FontInfo, CustomFontRenderer>()

private val FONT_REGISTRY = LinkedHashMap<FontInfo, FontRenderer>()

object Fonts : MinecraftInstance {

    // Legacy wrappers to support older GUI code that expects nested font holders
    class LegacyIconFont(
        val nlfont_18: SimpleFontRenderer,
        val nlfont_20: SimpleFontRenderer,
        val nlfont_24: SimpleFontRenderer,
        val nlfont_28: SimpleFontRenderer
    )

    class LegacyNlFont(
        val Nl_16: SimpleFontRenderer,
        val Nl_18: SimpleFontRenderer,
        val Nl_19: SimpleFontRenderer,
        val Nl_20: SimpleFontRenderer,
        val Nl_22: SimpleFontRenderer? = null
    )

    object NlIcon {
        lateinit var nlfont_20: LegacyIconFont
        lateinit var nlfont_18: LegacyIconFont
        lateinit var nlfont_24: LegacyIconFont
        lateinit var nlfont_28: LegacyIconFont
    }

    object Nl {
        lateinit var Nl_18: LegacyNlFont
        lateinit var Nl_16: LegacyNlFont
        lateinit var Nl_19: LegacyNlFont
        lateinit var Nl_20: LegacyNlFont
        lateinit var Nl_22: LegacyNlFont
    }

    /**
     * Custom Fonts
     */
    private val configFile = File(fontsDir, "fonts.json")
    private var customFontInfoList: List<CustomFontInfo>
        get() = with(configFile) {
            if (exists()) {
                try {
                    // For old versions
                    readJson().asJsonArray.map {
                        it as JsonObject
                        val fontFile = it["fontFile"].asString
                        val fontSize = it["fontSize"].asInt
                        val name = if (it.has("name")) it["name"].asString else fontFile
                        CustomFontInfo(name, fontFile, fontSize)
                    }
                } catch (e: Exception) {
                    LOGGER.error("Failed to load fonts", e)
                    emptyList()
                }
            } else {
                createNewFile()
                writeText("[]") // empty list
                emptyList()
            }
        }
        set(value) = configFile.writeJson(value)

    val minecraftFontInfo = FontInfo(name = "Minecraft Font")
    val minecraftFont: FontRenderer by lazy {
        mc.fontRendererObj
    }

    lateinit var font20: GameFontRenderer

    lateinit var fontSmall: GameFontRenderer

    lateinit var fontExtraBold30: GameFontRenderer
    lateinit var fontExtraBold40: GameFontRenderer
    lateinit var fontSemibold35: GameFontRenderer
    lateinit var fontSemibold40: GameFontRenderer
    lateinit var fontRegular40: GameFontRenderer
    lateinit var fontRegular45: GameFontRenderer
    lateinit var fontRegular35: GameFontRenderer

    lateinit var font72: GameFontRenderer

    lateinit var fontBold180: GameFontRenderer

    lateinit var fontSFUI35: GameFontRenderer

    lateinit var fontSFUI40: GameFontRenderer

    lateinit var fontIconXD85: GameFontRenderer

    lateinit var fontNovoAngularIcon85: GameFontRenderer

    lateinit var ICONFONT_17: SimpleFontRenderer
    lateinit var ICONFONT_20: SimpleFontRenderer
    lateinit var CheckFont_20: SimpleFontRenderer

    // NURSULTAN
    lateinit var Nursultan13: SimpleFontRenderer
    lateinit var Nursultan15: SimpleFontRenderer
    lateinit var Nursultan16: SimpleFontRenderer
    lateinit var Nursultan18: SimpleFontRenderer
    lateinit var Nursultan20: SimpleFontRenderer
    lateinit var Nursultan30: SimpleFontRenderer

    //INTER
    lateinit var InterMedium_13: SimpleFontRenderer
    lateinit var InterMedium_14: SimpleFontRenderer
    lateinit var InterMedium_15: SimpleFontRenderer
    lateinit var InterMedium_16: SimpleFontRenderer
    lateinit var InterMedium_18: SimpleFontRenderer
    lateinit var InterMedium_20: SimpleFontRenderer

    lateinit var InterBold_15: SimpleFontRenderer
    lateinit var InterBold_18: SimpleFontRenderer
    lateinit var InterBold_20: SimpleFontRenderer
    lateinit var InterBold_26: SimpleFontRenderer
    lateinit var InterBold_30: SimpleFontRenderer

    lateinit var InterRegular_15: SimpleFontRenderer
    lateinit var InterRegular_35: SimpleFontRenderer
    lateinit var InterRegular_40: SimpleFontRenderer

    lateinit var fontTahomaSmall: GameFontRenderer

    lateinit var Nl_15: SimpleFontRenderer
    lateinit var Nl_16: SimpleFontRenderer
    lateinit var Nl_18: SimpleFontRenderer
    lateinit var Nl_19: SimpleFontRenderer
    lateinit var Nl_20: SimpleFontRenderer
    lateinit var Nl_22: SimpleFontRenderer

    lateinit var Nl_16_ICON: SimpleFontRenderer
    lateinit var nlfont_18: SimpleFontRenderer
    lateinit var nlfont_20: SimpleFontRenderer
    lateinit var nlfont_24: SimpleFontRenderer
    lateinit var nlfont_28: SimpleFontRenderer

    lateinit var NLBold_18: SimpleFontRenderer
    lateinit var NLBold_32: SimpleFontRenderer
    lateinit var NLBold_35: SimpleFontRenderer
    lateinit var NLBold_28: SimpleFontRenderer

    private fun <T : FontRenderer> register(fontInfo: FontInfo, fontRenderer: T): T {
        FONT_REGISTRY[fontInfo] = fontRenderer
        return fontRenderer
    }

    private fun <T : CustomFontRenderer> registerCustomFont(fontInfo: FontInfo, fontRenderer: T): T {
        CUSTOM_FONT_REGISTRY[fontInfo] = fontRenderer
        return fontRenderer
    }

    fun registerCustomAWTFont(customFontInfo: CustomFontInfo, save: Boolean = true): GameFontRenderer? {
        val font = getFontFromFileOrNull(customFontInfo.fontFile, customFontInfo.fontSize) ?: return null
        val result = register(
            FontInfo(customFontInfo.name, customFontInfo.fontSize, isCustom = true),
            font.asGameFontRenderer()
        )
        if (save) {
            customFontInfoList += customFontInfo
        }
        return result
    }

    fun loadFonts() {
        LOGGER.info("Start to load fonts.")

        val time = measureTimeMillis {
            downloadFonts()
            register(minecraftFontInfo, minecraftFont)

            font20 = register(FontInfo(name = "Roboto Medium", size = 20),
                getFontFromFile("Roboto-Medium.ttf", 20).asGameFontRenderer())

            fontSmall = register(FontInfo(name = "Roboto Medium", size = 30),
                getFontFromFile("Roboto-Medium.ttf", 30).asGameFontRenderer())

            fontSemibold35 = register(
                FontInfo(name = "Outfit Semibold", size = 35),
                getFontFromFile("Outfit-Semibold.ttf", 35).asGameFontRenderer()
            )

            fontRegular35 = register(
                FontInfo(name = "Outfit Regular", size = 35),
                getFontFromFile("Outfit-Regular.ttf", 35).asGameFontRenderer()
            )

            fontRegular40 = register(
                FontInfo(name = "Outfit Regular", size = 40),
                getFontFromFile("Outfit-Regular.ttf", 40).asGameFontRenderer()
            )

            fontSemibold40 = register(
                FontInfo(name = "Outfit Semibold", size = 40),
                getFontFromFile("Outfit-Semibold.ttf", 40).asGameFontRenderer()
            )

            fontRegular45 = register(
                FontInfo(name = "Outfit Regular", size = 45),
                getFontFromFile("Outfit-Regular.ttf", 45).asGameFontRenderer()
            )

            fontSemibold40 = register(
                FontInfo(name = "Outfit Semibold", size = 40),
                getFontFromFile("Outfit-Semibold.ttf", 40).asGameFontRenderer()
            )

            fontExtraBold30 = register(
                FontInfo(name = "Outfit Extrabold", size = 30),
                getFontFromFile("Outfit-Extrabold.ttf", 30).asGameFontRenderer()
            )

            fontExtraBold40 = register(
                FontInfo(name = "Outfit Extrabold", size = 40),
                getFontFromFile("Outfit-Extrabold.ttf", 40).asGameFontRenderer()
            )

            fontBold180 = register(
                FontInfo(name = "Outfit Bold", size = 180),
                getFontFromFile("Outfit-Bold.ttf", 180).asGameFontRenderer()
            )
            font72 = register(FontInfo(name = "Roboto Medium", size = 72),
                getFontFromFile("Roboto-Medium.ttf", 72).asGameFontRenderer())
            fontBold180 = register(FontInfo(name = "Roboto Bold", size = 180),
                getFontFromFile("Roboto-Bold.ttf", 180).asGameFontRenderer())

            // SFUI
            fontSFUI35 = register(FontInfo(name = "sfui", size = 35),
                getFontFromFile("sfui.ttf", 35).asGameFontRenderer())
            fontSFUI40 = register(FontInfo(name = "sfui", size = 40),
                getFontFromFile("sfui.ttf", 40).asGameFontRenderer())
            // icons
            fontIconXD85 = register(FontInfo(name = "iconxd", size = 85),
                getFontFromFile("iconxd.ttf", 85).asGameFontRenderer())
            fontNovoAngularIcon85 = register(FontInfo(name = "novoangular", size = 85),
                getFontFromFile("novoangular.ttf", 85).asGameFontRenderer())

            ICONFONT_20 = registerCustomFont(FontInfo(name = "ICONFONT", size = 20),
                getFontFromFile("stylesicons.ttf", 20).asSimpleFontRenderer())
            ICONFONT_17 = registerCustomFont(FontInfo(name = "ICONFONT", size = 17),
                getFontFromFile("stylesicons.ttf", 17).asSimpleFontRenderer())

            CheckFont_20 = registerCustomFont(FontInfo(name = "Check Font", size = 20),
                getFontFromFile("check.ttf", 20).asSimpleFontRenderer())

            Nursultan13 = registerCustomFont(FontInfo(name = "Nursultan", size = 13),
                getFontFromFile("Nursultan.ttf", 13).asSimpleFontRenderer())
            Nursultan15 = registerCustomFont(FontInfo(name = "Nursultan", size = 15),
                getFontFromFile("Nursultan.ttf", 15).asSimpleFontRenderer())
            Nursultan16 = registerCustomFont(FontInfo(name = "Nursultan", size = 16),
                getFontFromFile("Nursultan.ttf", 16).asSimpleFontRenderer())
            Nursultan18 = registerCustomFont(FontInfo(name = "Nursultan", size = 18),
                getFontFromFile("Nursultan.ttf", 18).asSimpleFontRenderer())
            Nursultan20 = registerCustomFont(FontInfo(name = "Nursultan", size = 20),
                getFontFromFile("Nursultan.ttf", 20).asSimpleFontRenderer())
            Nursultan30 = registerCustomFont(FontInfo(name = "Nursultan", size = 30),
                getFontFromFile("Nursultan.ttf", 30).asSimpleFontRenderer())

            InterMedium_13 = registerCustomFont(FontInfo(name = "InterMedium", size = 13),
                getFontFromFile("Inter_Medium.ttf", 13).asSimpleFontRenderer())
            InterMedium_14 = registerCustomFont(FontInfo(name = "InterMedium", size = 14),
                getFontFromFile("Inter_Medium.ttf", 14).asSimpleFontRenderer())
            InterMedium_15 = registerCustomFont(FontInfo(name = "InterMedium", size = 15),
                getFontFromFile("Inter_Medium.ttf", 15).asSimpleFontRenderer())
            InterMedium_16 = registerCustomFont(FontInfo(name = "InterMedium", size = 16),
                getFontFromFile("Inter_Medium.ttf", 16).asSimpleFontRenderer())
            InterMedium_18 = registerCustomFont(FontInfo(name = "InterMedium", size = 18),
                getFontFromFile("Inter_Medium.ttf", 18).asSimpleFontRenderer())
            InterMedium_20 = registerCustomFont(FontInfo(name = "InterMedium", size = 20),
                getFontFromFile("Inter_Medium.ttf", 20).asSimpleFontRenderer())

            InterBold_15 = registerCustomFont(FontInfo(name = "InterBold", size = 15),
                getFontFromFile("Inter_Bold.ttf", 15).asSimpleFontRenderer())
            InterBold_18 = registerCustomFont(FontInfo(name = "InterBold", size = 18),
                getFontFromFile("Inter_Bold.ttf", 18).asSimpleFontRenderer())
            InterBold_20 = registerCustomFont(FontInfo(name = "InterBold", size = 20),
                getFontFromFile("Inter_Bold.ttf", 20).asSimpleFontRenderer())
            InterBold_26 = registerCustomFont(FontInfo(name = "InterBold", size = 26),
                getFontFromFile("Inter_Bold.ttf", 26).asSimpleFontRenderer())
            InterBold_30 = registerCustomFont(FontInfo(name = "InterBold", size = 30),
                getFontFromFile("Inter_Bold.ttf", 30).asSimpleFontRenderer())

            InterRegular_15 = registerCustomFont(FontInfo(name = "InterRegular", size = 15),
                getFontFromFile("Inter_Regular.ttf", 15).asSimpleFontRenderer())
            InterRegular_35 = registerCustomFont(FontInfo(name = "InterRegular", size = 35),
                getFontFromFile("Inter_Regular.ttf", 35).asSimpleFontRenderer())
            InterRegular_40 = registerCustomFont(FontInfo(name = "InterRegular", size = 40),
                getFontFromFile("Inter_Regular.ttf", 40).asSimpleFontRenderer())

            fontTahomaSmall = register(FontInfo(name = "Tahoma", size = 18),
                getFontFromFile("Tahoma.ttf", 18).asGameFontRenderer())

            //   NL ICON

            Nl_16_ICON = registerCustomFont(FontInfo(name = "nlicon", size = 16),
                getFontFromFile("nlicon.ttf", 16).asSimpleFontRenderer())

            Nl_15 = registerCustomFont(FontInfo(name = "nlfont", size = 15),
                getFontFromFile("nlfont.ttf", 15).asSimpleFontRenderer())
            Nl_16 = registerCustomFont(FontInfo(name = "nlfont", size = 16),
                getFontFromFile("nlfont.ttf", 16).asSimpleFontRenderer())
            Nl_18 = registerCustomFont(FontInfo(name = "nlfont", size = 18),
                getFontFromFile("nlfont.ttf", 18).asSimpleFontRenderer())
            Nl_19 = registerCustomFont(FontInfo(name = "nlfont", size = 19),
                getFontFromFile("nlfont.ttf", 19).asSimpleFontRenderer())
            Nl_20 = registerCustomFont(FontInfo(name = "nlfont", size = 20),
                getFontFromFile("nlfont.ttf", 20).asSimpleFontRenderer())
            Nl_22 = registerCustomFont(FontInfo(name = "nlfont", size = 22),
                getFontFromFile("nlfont.ttf", 22).asSimpleFontRenderer())

            nlfont_18 = registerCustomFont(FontInfo(name = "nlicon", size = 18),
                getFontFromFile("nlicon.ttf", 18).asSimpleFontRenderer())
            nlfont_20 = registerCustomFont(FontInfo(name = "nlicon", size = 20),
                getFontFromFile("nlicon.ttf", 20).asSimpleFontRenderer())
            nlfont_24 = registerCustomFont(FontInfo(name = "nlicon", size = 24),
                getFontFromFile("nlicon.ttf", 24).asSimpleFontRenderer())
            nlfont_28 = registerCustomFont(FontInfo(name = "nlicon", size = 28),
                getFontFromFile("nlicon.ttf", 28).asSimpleFontRenderer())

            // Provide legacy nested holders for nlclickgui
            NlIcon.nlfont_18 = LegacyIconFont(nlfont_18, nlfont_20, nlfont_24, nlfont_28)
            NlIcon.nlfont_20 = LegacyIconFont(nlfont_18, nlfont_20, nlfont_24, nlfont_28)
            NlIcon.nlfont_24 = LegacyIconFont(nlfont_18, nlfont_20, nlfont_24, nlfont_28)
            NlIcon.nlfont_28 = LegacyIconFont(nlfont_18, nlfont_20, nlfont_24, nlfont_28)

            Nl.Nl_16 = LegacyNlFont(Nl_16, Nl_18, Nl_19, Nl_20, Nl_22)
            Nl.Nl_18 = LegacyNlFont(Nl_16, Nl_18, Nl_19, Nl_20, Nl_22)
            Nl.Nl_19 = LegacyNlFont(Nl_16, Nl_18, Nl_19, Nl_20, Nl_22)
            Nl.Nl_20 = LegacyNlFont(Nl_16, Nl_18, Nl_19, Nl_20, Nl_22)
            Nl.Nl_22 = LegacyNlFont(Nl_16, Nl_18, Nl_19, Nl_20, Nl_22)

            NLBold_32 = registerCustomFont(FontInfo(name = "Museo", size = 32),
                getFontFromFile("MuseoSans_900.ttf", 32).asSimpleFontRenderer())
            NLBold_35 = registerCustomFont(FontInfo(name = "Museo", size = 35),
                getFontFromFile("MuseoSans_900.ttf", 35).asSimpleFontRenderer())
            NLBold_18 = registerCustomFont(FontInfo(name = "Museo", size = 18),
                getFontFromFile("MuseoSans_900.ttf", 18).asSimpleFontRenderer())
            NLBold_28 = registerCustomFont(FontInfo(name = "Museo", size = 28),
                getFontFromFile("MuseoSans_900.ttf", 28).asSimpleFontRenderer())

            loadCustomFonts()
        }
        LOGGER.info("Loaded ${FONT_REGISTRY.size} fonts in ${time}ms")
    }

    private fun loadCustomFonts() {
        FONT_REGISTRY.keys.removeIf { it.isCustom }

        customFontInfoList.forEach {
            registerCustomAWTFont(it, save = false)
        }
    }

    fun downloadFonts() {
        fontsDir.mkdirs()
        val outputFile = File(fontsDir, "outfit.zip")
        if (!outputFile.exists()) {
            LOGGER.info("Downloading roboto fonts...")
            Downloader.downloadWholeFile("$CLIENT_CLOUD/fonts/Outfit.zip", outputFile)
            LOGGER.info("Extract roboto fonts...")
            outputFile.extractZipTo(fontsDir)
        }

        val fontZipFile = File(fontsDir, "font.zip")
        if (!fontZipFile.exists()) {
            LOGGER.info("Downloading additional fonts...")
            Downloader.downloadWholeFile("${FONTS}/Font.zip", fontZipFile)
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

    val customFonts: Map<FontInfo, FontRenderer>
        get() = FONT_REGISTRY.filterKeys { it.isCustom }

    fun removeCustomFont(fontInfo: FontInfo): CustomFontInfo? {
        if (!fontInfo.isCustom) {
            return null
        }
        FONT_REGISTRY.remove(fontInfo)
        return customFontInfoList.firstOrNull {
            it.name == fontInfo.name && it.fontSize == fontInfo.size
        }?.also {
            customFontInfoList -= it
        }
    }

    private fun getFontFromFileOrNull(file: String, size: Int): Font? = try {
        File(fontsDir, file).inputStream().use { inputStream ->
            Font.createFont(Font.TRUETYPE_FONT, inputStream).deriveFont(Font.PLAIN, size.toFloat())
        }
    } catch (e: Exception) {
        LOGGER.warn("Exception during loading font[name=${file}, size=${size}]", e)
        null
    }

    private fun getFontFromFile(file: String, size: Int): Font =
        getFontFromFileOrNull(file, size) ?: Font("default", Font.PLAIN, size)

    private fun Font.asGameFontRenderer(): GameFontRenderer {
        return GameFontRenderer(this@asGameFontRenderer)
    }

    private fun Font.asSimpleFontRenderer(): SimpleFontRenderer {
        return SimpleFontRenderer.create(this) as SimpleFontRenderer
    }
}