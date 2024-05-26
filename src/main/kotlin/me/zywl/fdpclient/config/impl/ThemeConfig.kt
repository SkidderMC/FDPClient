/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package me.zywl.fdpclient.config.impl

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.zywl.fdpclient.config.FileConfig
import me.zywl.fdpclient.config.FileManager
import net.ccbluex.liquidbounce.ui.gui.colortheme.ClientTheme
import java.io.File

class ThemeConfig(file: File) : FileConfig(file) {

    override fun loadConfig(config: String) {
        val json = JsonParser().parse(config).asJsonObject
        if (json.has("Theme")) {
            ClientTheme.ClientColorMode.set(json.get("Theme").asString)
        }
        if (json.has("Fade-Speed")) {
            ClientTheme.fadespeed.set(json.get("Fade-Speed").asInt)
        }
        if (json.has("Fade-Type")) {
            ClientTheme.updown.set(json.get("Fade-Type").asBoolean)
        }
            if (json.has("Text-Static")) {
                ClientTheme.textValue.set(json.get("Text-Static").asBoolean)
            }
    }

    override fun saveConfig(): String {
        val json = JsonObject()
        json.addProperty("Theme", ClientTheme.ClientColorMode.get())
        json.addProperty("Fade-Speed", ClientTheme.fadespeed.get())
        json.addProperty("Fade-Type", ClientTheme.updown.get())
        json.addProperty("Text-Static", ClientTheme.textValue.get())
        return FileManager.PRETTY_GSON.toJson(json)
    }
}