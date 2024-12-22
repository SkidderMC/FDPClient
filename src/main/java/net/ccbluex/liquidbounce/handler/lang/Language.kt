/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.lang

import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.io.decodeJson

fun translationMenu(key: String, vararg args: Any) = LanguageManager.getTranslation("menu.$key", *args)
fun translation(key: String, vararg args: Any) = LanguageManager.getTranslation(key, *args)

object LanguageManager : MinecraftInstance {

    // Current language
    private val language: String
        get() = overrideLanguage.ifBlank { mc.gameSettings.language }
    
    // The game language can be overridden by the user
    var overrideLanguage = ""

    // Common language
    private const val COMMON_UNDERSTOOD_LANGUAGE = "en_US"

    // List of all languages
    val knownLanguages = arrayOf(
        "en_US",
        "pt_BR",
        "pt_PT",
        "zh_CN",
        "zh_TW",
        "bg_BG",
        "ru_RU"
    )
    private val languageMap = hashMapOf<String, Language>()

    /**
     * Load all languages which are pre-defined in [knownLanguages] and stored in assets.
     * If a language is not found, it will be logged as error.
     *
     * Languages are stored in assets/minecraft/liquidbounce/lang and when loaded will be stored in [languageMap]
     */
    fun loadLanguages() {
        for (language in knownLanguages) {
            runCatching {
                languageMap[language] = javaClass.getResourceAsStream("/assets/minecraft/liquidbounce/lang/$language.json")!!
                    .bufferedReader().use { it.decodeJson() }
            }.onSuccess {
                LOGGER.info("Loaded language $language")
            }.onFailure {
                LOGGER.error("Failed to load language $language", it)
            }
        }
    }

    /**
     * Get translation from language
     */
    fun getTranslation(key: String, vararg args: Any)
        = languageMap[language]?.getTranslation(key, *args)
        ?: languageMap[COMMON_UNDERSTOOD_LANGUAGE]?.getTranslation(key, *args)
        ?: key
    
}

class Language(val locale: String, val contributors: List<String>, val translations: Map<String, String>) {

    fun getTranslation(key: String, vararg args: Any) = translations[key]?.format(*args)

    override fun toString() = locale

}