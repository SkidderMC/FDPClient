package net.ccbluex.liquidbounce.ui.i18n

import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

class Language(val locale: String) {

    private val translateMap = HashMap<String, String>()

    init {
        read(locale)
    }

    private fun find(): InputStream {
        val split = locale.replace("-", "_").split("_")

        if(split.size > 1) {
            val str = split[0].lowercase() + "-" + split[1].uppercase()
            LanguageManager::class.java.classLoader.getResourceAsStream("assets/minecraft/fdpclient/translations/${str}/source.properties")?.let {
                return it
            }
        }

        val str = split[0].lowercase()
        LanguageManager::class.java.classLoader.getResourceAsStream("assets/minecraft/fdpclient/translations/${str}/source.properties")?.let {
            return it
        }

        LanguageManager::class.java.classLoader.getResourceAsStream("assets/minecraft/fdpclient/translations/source.properties")?.let {
            return it
        }

        throw IllegalStateException("Can't find language file! Try sync gitsubmodule if this is a custom build!")
    }

    private fun read(locale: String) {
        val prop = Properties()

        prop.load(InputStreamReader(find(), Charsets.UTF_8))

        for ((key, value) in prop.entries) {
            if (key is String && value is String) {
                translateMap[key] = value
            }
        }
    }

    fun get(key: String): String {
        return translateMap[key] ?: key
    }
}