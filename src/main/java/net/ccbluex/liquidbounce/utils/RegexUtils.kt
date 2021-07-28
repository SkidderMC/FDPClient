package net.ccbluex.liquidbounce.utils

import java.util.regex.Matcher

object RegexUtils {
    @JvmStatic
    fun match(matcher: Matcher):Array<String>{
        val result=mutableListOf<String>()

        while (matcher.find()){
            result.add(matcher.group())
        }

        return result.toTypedArray()
    }
}