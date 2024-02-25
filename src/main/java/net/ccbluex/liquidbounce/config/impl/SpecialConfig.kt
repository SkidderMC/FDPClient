/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.config.impl

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.config.FileConfig
import net.ccbluex.liquidbounce.config.FileManager
import net.ccbluex.liquidbounce.handler.network.AutoReconnect
import net.ccbluex.liquidbounce.handler.network.ClientFixes
import net.ccbluex.liquidbounce.ui.altmanager.GuiAltManager
import java.io.File
class SpecialConfig(file: File) : FileConfig(file) {
    private var useGlyphFontRenderer = true

    override fun loadConfig(config: String) {
        val json = JsonParser().parse(config).asJsonObject

        FDPClient.commandManager.prefix = '.'
        AutoReconnect.delay = 5000
        ClientFixes.enabled = true
        ClientFixes.blockFML = true
        ClientFixes.blockProxyPacket = true
        ClientFixes.blockPayloadPackets = true
        GuiAltManager.randomAltField.text = "F%nD%nP%n_%s%s%s"
        useGlyphFontRenderer = true

        if (json.has("prefix")) {
            FDPClient.commandManager.prefix = json.get("prefix").asCharacter
        }
        if (json.has("auto-reconnect")) {
            AutoReconnect.delay = json.get("auto-reconnect").asInt
        }
        if (json.has("alt-field")) {
            GuiAltManager.randomAltField.text = json.get("alt-field").asString
        }
        if (json.has("use-glyph-fontrenderer")) {
            useGlyphFontRenderer = json.get("use-glyph-fontrenderer").asBoolean
        }
        if (json.has("client-user")) {
            FDPClient.USER_NAME = json.get("client-user").asString
        }

        if (json.has("anti-forge")) {
            val jsonValue = json.getAsJsonObject("anti-forge")

            if (jsonValue.has("enable")) {
                ClientFixes.enabled = jsonValue.get("enable").asBoolean
            }
            if (jsonValue.has("block-fml")) {
                ClientFixes.blockFML = jsonValue.get("block-fml").asBoolean
            }
            if (jsonValue.has("block-proxy")) {
                ClientFixes.blockProxyPacket = jsonValue.get("block-proxy").asBoolean
            }
            if (jsonValue.has("block-payload")) {
                ClientFixes.blockPayloadPackets = jsonValue.get("block-payload").asBoolean
            }
            if (jsonValue.has("FixResourcePackExploit")) {
                ClientFixes.blockResourcePackExploit = jsonValue.get("FixResourcePackExploit").asBoolean
            }
            if (jsonValue.has("ClientBrand")) {
            ClientFixes.clientBrand = jsonValue.get("ClientBrand").asString
            }
        }
    }

    override fun saveConfig(): String {
        val json = JsonObject()

        json.addProperty("prefix", FDPClient.commandManager.prefix)
        json.addProperty("auto-reconnect", AutoReconnect.delay)
        json.addProperty("alt-field", GuiAltManager.randomAltField.text)
        json.addProperty("client-user", FDPClient.USER_NAME)
        json.addProperty("use-glyph-fontrenderer", useGlyphFontRenderer)

        val antiForgeJson = JsonObject()
        antiForgeJson.addProperty("enable", ClientFixes.enabled)
        antiForgeJson.addProperty("block-fml", ClientFixes.blockFML)
        antiForgeJson.addProperty("block-proxy", ClientFixes.blockProxyPacket)
        antiForgeJson.addProperty("block-payload", ClientFixes.blockPayloadPackets)
        json.add("anti-forge", antiForgeJson)

        return FileManager.PRETTY_GSON.toJson(json)
    }
}