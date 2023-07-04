/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.network.play.server.S02PacketChat
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

@ModuleInfo(name = "ChatTranslator", category = ModuleCategory.MISC)
object ChatTranslator : Module() {

    private val languageValue = ListValue("Language", arrayOf("Chinese", "English"), "Chinese")
    private val apiValue = ListValue("API", arrayOf("Google", "Bing", "YouDao"), "Bing")

    private val client = HttpClients.createDefault()
    private val cache = HashMap<String, String>()

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.packet is S02PacketChat) {
            val msg = event.packet.chatComponent.formattedText
            if (!cache.contains(msg)) {
                doTranslate(msg)
            } else {
                ClientUtils.displayChatMessage(if (cache.containsKey(msg)) { msg } else { cache[msg]!! })
            }

            event.cancelEvent()
        }
    }

    private fun getLink(msg: String): String {
        val message = msg.replace(" ", "%20")
        return when (apiValue.get().lowercase()) {
            "google" -> "http://translate.google.cn/translate_a/single?client=gtx&dt=t&dj=1&ie=UTF-8&sl=auto&tl=" + (if (languageValue.equals("chinese")) "zh_cn" else "en_us") + "&q=$message"
            "bing" -> "http://api.microsofttranslator.com/v2/Http.svc/Translate?appId=A4D660A48A6A97CCA791C34935E4C02BBB1BEC1C&from=&to=" + (if (languageValue.equals("chinese")) "zh" else "en") + "&text=$message"
            "youdao" -> "http://fanyi.youdao.com/translate?&doctype=json&type=AUTO&i=$message"
            else -> ""
        }
    }

    private fun getResult(data: String): String {
        when (apiValue.get().lowercase()) {
            "google" -> {
                val json = JsonParser().parse(data).asJsonObject
                return json.get("sentences").asJsonArray.get(0).asJsonObject.get("trans").asString
            }
            "bing" -> {
                return data
                    .replace("<string xmlns=\"http://schemas.microsoft.com/2003/10/Serialization/\">", "")
                    .replace("</string>", "")
            }
            "youdao" -> {
                val json = JsonParser().parse(data).asJsonObject
                return json.get("translateResult").asJsonArray.get(0).asJsonArray.get(0).asJsonObject.get("tgt").asString
            }
        }
        return "WRONG VALUE"
    }

    private fun doTranslate(msg: String) {
        Thread {
            try {
                val request = HttpGet(getLink(msg))
                val response = client.execute(request)

                if (response.statusLine.statusCode != 200) {
                    throw IllegalStateException("resp code: " + response.statusLine.statusCode + " != 200")
                }

                val result = getResult(EntityUtils.toString(response.entity))
                cache[msg] = result
                ClientUtils.displayChatMessage(result)
            } catch (e: Exception) {
                e.printStackTrace()
                ClientUtils.displayChatMessage(msg)
            }
        }.start()
    }
}
