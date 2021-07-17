package net.ccbluex.liquidbounce.file.config.sections

import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.special.AntiForge
import net.ccbluex.liquidbounce.features.special.AutoReconnect
import net.ccbluex.liquidbounce.features.special.ServerSpoof
import net.ccbluex.liquidbounce.file.config.ConfigSection
import net.ccbluex.liquidbounce.ui.client.GuiBackground

class SpecialSection : ConfigSection("special") {
    override fun load(json: JsonObject) {
        LiquidBounce.commandManager.prefix='.'
        AutoReconnect.delay=5000
        AntiForge.enabled=true
        AntiForge.blockFML=true
        AntiForge.blockProxyPacket=true
        AntiForge.blockPayloadPackets=true
        ServerSpoof.enable=false
        ServerSpoof.address="redesky.com"
        GuiBackground.enabled=true
        GuiBackground.particles=false

        if(json.has("prefix")){
            LiquidBounce.commandManager.prefix=json.get("prefix").asCharacter
        }
        if(json.has("auto-reconnect")){
            AutoReconnect.delay=json.get("auto-reconnect").asInt
        }
        if(json.has("anti-forge")){
            val jsonValue = json.getAsJsonObject("anti-forge")

            if (jsonValue.has("enable"))
                AntiForge.enabled = jsonValue.get("enable").asBoolean
            if (jsonValue.has("block-fml"))
                AntiForge.blockFML = jsonValue.get("block-fml").asBoolean
            if (jsonValue.has("block-proxy"))
                AntiForge.blockProxyPacket = jsonValue.get("block-proxy").asBoolean
            if (jsonValue.has("block-payload"))
                AntiForge.blockPayloadPackets = jsonValue.get("block-payload").asBoolean
        }
        if(json.has("serverspoof")){
            val jsonValue = json.getAsJsonObject("serverspoof")

            if (jsonValue.has("enable"))
                ServerSpoof.enable = jsonValue.get("enable").asBoolean
            if(jsonValue.has("address"))
                ServerSpoof.address = jsonValue.get("address").asString
        }
        if(json.has("background")){
            val jsonValue = json.getAsJsonObject("background")

            if (jsonValue.has("enable"))
                GuiBackground.enabled=jsonValue.get("enable").asBoolean
            if(jsonValue.has("particles"))
                GuiBackground.particles=jsonValue.get("particles").asBoolean
        }
    }

    override fun save(): JsonObject {
        val json=JsonObject()

        json.addProperty("prefix",LiquidBounce.commandManager.prefix)
        json.addProperty("auto-reconnect",AutoReconnect.delay)

        val antiForgeJson=JsonObject()
        antiForgeJson.addProperty("enable",AntiForge.enabled)
        antiForgeJson.addProperty("block-fml",AntiForge.blockFML)
        antiForgeJson.addProperty("block-proxy",AntiForge.blockProxyPacket)
        antiForgeJson.addProperty("block-payload",AntiForge.blockPayloadPackets)
        json.add("anti-forge",antiForgeJson)

        val serverSpoofJson=JsonObject()
        serverSpoofJson.addProperty("enable",ServerSpoof.enable)
        serverSpoofJson.addProperty("address",ServerSpoof.address)
        json.add("serverspoof",serverSpoofJson)

        val backgroundJson=JsonObject()
        backgroundJson.addProperty("enable",GuiBackground.enabled)
        backgroundJson.addProperty("particles",GuiBackground.particles)
        json.add("background",backgroundJson)

        return json
    }
}