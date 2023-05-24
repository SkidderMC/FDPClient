package net.ccbluex.liquidbounce.file.configs

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.features.special.*
import net.ccbluex.liquidbounce.file.FileConfig
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.ui.client.GuiBackground
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import java.io.File
import java.net.Proxy

class SpecialConfig(file: File) : FileConfig(file) {
    var useGlyphFontRenderer = true

    override fun loadConfig(config: String) {
        val json = JsonParser().parse(config).asJsonObject

        FDPClient.commandManager.prefix = '.'
        AutoReconnect.delay = 5000
        ClientFixes.enabled = true
        ClientFixes.blockFML = true
        ClientFixes.blockProxyPacket = true
        ClientFixes.blockPayloadPackets = true
        ServerSpoof.enable = false
        ServerSpoof.address = "redesky.com"
        GuiBackground.enabled = true
        GuiBackground.particles = false
        GuiAltManager.randomAltField.text = "F%nD%nP%n_%s%s%s"
        useGlyphFontRenderer = true
        ProxyManager.isEnable = false
        ProxyManager.proxy = "127.0.0.1:10808"
        ProxyManager.proxyType = Proxy.Type.SOCKS

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
            ClientFixes.clientBrand = jsonValue.get("ClientBrand").getAsString();
            }
        }
        if (json.has("serverspoof")) {
            val jsonValue = json.getAsJsonObject("serverspoof")

            if (jsonValue.has("enable")) {
                ServerSpoof.enable = jsonValue.get("enable").asBoolean
            }
            if (jsonValue.has("address")) {
                ServerSpoof.address = jsonValue.get("address").asString
            }
        }
        if (json.has("proxy")) {
            val jsonValue = json.getAsJsonObject("proxy")

            if (jsonValue.has("enable")) {
                ProxyManager.isEnable = jsonValue.get("enable").asBoolean
            }
            if (jsonValue.has("address")) {
                ProxyManager.proxy = jsonValue.get("address").asString
            }
            if (jsonValue.has("type")) {
                ProxyManager.proxyType = Proxy.Type.valueOf(jsonValue.get("type").asString.uppercase())
            }
        }
        if (json.has("background")) {
            val jsonValue = json.getAsJsonObject("background")

            if (jsonValue.has("enable")) {
                GuiBackground.enabled = jsonValue.get("enable").asBoolean
            }
            if (jsonValue.has("particles")) {
                GuiBackground.particles = jsonValue.get("particles").asBoolean
            }
            if (jsonValue.has("gradient")) {
                val name = jsonValue.get("gradient").asString
                GradientBackground.nowGradient = GradientBackground.gradients.find { it.name == name } ?: GradientBackground.gradients.first()
            }
            if (jsonValue.has("gradient-side")) {
                val side = jsonValue.get("gradient-side").asString
                GradientBackground.gradientSide = GradientBackground.gradientSides.find { it.name == side } ?: GradientBackground.gradientSides.first()
            }
            if (jsonValue.has("gradient-animated")) {
                GradientBackground.animated = jsonValue.get("gradient-animated").asBoolean
            }
        }
    }

    override fun saveConfig(): String {
        val json = JsonObject()

        json.addProperty("prefix", FDPClient.commandManager.prefix)
        json.addProperty("auto-reconnect", AutoReconnect.delay)
        json.addProperty("alt-field", GuiAltManager.randomAltField.text)
        json.addProperty("use-glyph-fontrenderer", useGlyphFontRenderer)

        val antiForgeJson = JsonObject()
        antiForgeJson.addProperty("enable", ClientFixes.enabled)
        antiForgeJson.addProperty("block-fml", ClientFixes.blockFML)
        antiForgeJson.addProperty("block-proxy", ClientFixes.blockProxyPacket)
        antiForgeJson.addProperty("block-payload", ClientFixes.blockPayloadPackets)
        json.add("anti-forge", antiForgeJson)

        val serverSpoofJson = JsonObject()
        serverSpoofJson.addProperty("enable", ServerSpoof.enable)
        serverSpoofJson.addProperty("address", ServerSpoof.address)
        json.add("serverspoof", serverSpoofJson)

        val proxyJson = JsonObject()
        proxyJson.addProperty("enable", ProxyManager.isEnable)
        proxyJson.addProperty("address", ProxyManager.proxy)
        proxyJson.addProperty("type", ProxyManager.proxyType.name)
        json.add("proxy", proxyJson)

        val backgroundJson = JsonObject()
        backgroundJson.addProperty("enable", GuiBackground.enabled)
        backgroundJson.addProperty("particles", GuiBackground.particles)
        backgroundJson.addProperty("gradient", GradientBackground.nowGradient.name)
        backgroundJson.addProperty("gradient-side", GradientBackground.gradientSide.name)
        backgroundJson.addProperty("gradient-animated", GradientBackground.animated)
        json.add("background", backgroundJson)

        return FileManager.PRETTY_GSON.toJson(json)
    }
}