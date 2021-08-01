package net.ccbluex.liquidbounce.utils

import java.util.regex.Matcher
import java.util.regex.Pattern

object RegexUtils {
    @JvmStatic
    fun match(matcher: Matcher):Array<String>{
        val result=mutableListOf<String>()

        while (matcher.find()){
            result.add(matcher.group())
        }

        return result.toTypedArray()
    }

    @JvmStatic
    fun match(text: String, pattern: Pattern):Array<String>{
        return match(pattern.matcher(text))
    }

    @JvmStatic
    fun match(text: String, pattern: String):Array<String>{
        return match(text, Pattern.compile(pattern))
    }
}