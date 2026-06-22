/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import kotlinx.coroutines.delay
import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.ccbluex.liquidbounce.event.async.loopSequence
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextBoolean
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextFloat
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextInt
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.randomString

object Spammer : Module("Spammer", Category.OTHER, Category.SubCategory.MISCELLANEOUS, subjective = true) {

    private val delay by intRange("Delay", 500..1000, 0..5000)
        .describe("Random millisecond delay range between messages.")

    private val message by text("Message", "$CLIENT_NAME Client | fdpinfo.github(.io) | opZywl on GitHub")
        .describe("The chat message to spam.")

    private val custom by boolean("Custom", false)
        .describe("Use placeholder substitution in the message.")

    private val converter by choices("Converter", arrayOf("None", "Leet", "RandomCase", "RandomSpace"), "None")
        .describe("Text transform applied to each sent message.")

    val onUpdate = loopSequence {
        mc.thePlayer?.sendChatMessage(
            convert(
                if (custom) replace(message)
                else message + " >" + randomString(nextInt(5, 11)) + "<"
            )
        )

        delay(delay.random().toLong())
    }

    private fun convert(text: String): String =
        when (converter) {
            "Leet" -> text.map { char ->
                when (char) {
                    'o', 'O' -> '0'
                    'l', 'L' -> '1'
                    'e', 'E' -> '3'
                    'a', 'A' -> '4'
                    't', 'T' -> '7'
                    's', 'S' -> 'Z'
                    else -> char
                }
            }.joinToString("")

            "RandomCase" -> text.map { char ->
                if (nextBoolean()) char.uppercaseChar() else char.lowercaseChar()
            }.joinToString("")

            "RandomSpace" -> buildString(text.length * 2) {
                for (char in text) {
                    append(char)
                    if (nextBoolean()) {
                        append(' ')
                    }
                }
            }

            else -> text
        }

    private fun replace(text: String): String {
        var replacedStr = text

        replaceMap.forEach { (key, valueFunc) ->
            replacedStr = replacedStr.replace(key, valueFunc)
        }

        return replacedStr
    }

    private inline fun String.replace(oldValue: String, newValueProvider: () -> Any): String {
        var index = 0
        val newString = StringBuilder(this)
        while (true) {
            index = newString.indexOf(oldValue, startIndex = index)
            if (index == -1) {
                break
            }

            // You have to replace them one by one, otherwise all parameters like %s would be set to the same random string.
            val newValue = newValueProvider().toString()
            newString.replace(index, index + oldValue.length, newValue)

            index += newValue.length
        }
        return newString.toString()
    }

    private fun randomPlayer() =
        mc.netHandler.playerInfoMap
            .map { playerInfo -> playerInfo.gameProfile.name }
            .filter { name -> name != mc.thePlayer.name }
            .randomOrNull() ?: "none"

    private val replaceMap = mapOf(
        "%f" to { nextFloat().toString() },
        "%i" to { nextInt(0, 10000).toString() },
        "%ss" to { randomString(nextInt(1, 6)) },
        "%s" to { randomString(nextInt(1, 10)) },
        "%ls" to { randomString(nextInt(1, 17)) },
        "%p" to { randomPlayer() }
    )
}