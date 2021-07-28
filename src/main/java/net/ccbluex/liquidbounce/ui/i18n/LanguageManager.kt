package net.ccbluex.liquidbounce.ui.i18n

import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.RegexUtils
import java.util.regex.Pattern

object LanguageManager {
    val key="%"
    val defaultLocale="en_us"

    private var language=Language(defaultLocale)

    // regex is slow, so we need to cache match results
    private val pattern=Pattern.compile("$key[A-Za-z0-9\u002E]*$key")

    private val cachedStrings=HashMap<String, String>()

    fun replace(text: String):String{
        if(!text.contains(key))
            return text

        if(cachedStrings.containsKey(text))
            return cachedStrings[text]!!

        val matcher=pattern.matcher(text)
        var result=text
        RegexUtils.match(matcher).forEach {
            result=result.replace(it,get(it.substring(1,it.length-1)))
        }

        return result
    }

    fun get(key: String):String{
        return language.get(key)
    }

    fun switchLanguage(languageStr: String){
        val languageCode=languageStr.toLowerCase()

        if(LanguageManager::class.java.classLoader.getResourceAsStream("assets/minecraft/fdpclient/locale/$languageCode.lang")==null) {
            ClientUtils.logWarn("Language $languageCode not exist!")
            language=Language(defaultLocale)
            return
        }

        ClientUtils.logInfo("Loading language $languageCode")
        language=Language(languageCode, defaultLocale)
    }
}