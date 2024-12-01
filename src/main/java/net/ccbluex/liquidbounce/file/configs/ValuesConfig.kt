/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.file.configs

import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.FDPClient.commandManager
import net.ccbluex.liquidbounce.FDPClient.moduleManager
import net.ccbluex.liquidbounce.handler.cape.CapeService
import net.ccbluex.liquidbounce.features.module.modules.client.BrandSpoofer.possibleBrands
import net.ccbluex.liquidbounce.features.module.modules.client.IRCModule.jwtToken
import net.ccbluex.liquidbounce.features.module.modules.client.TargetModule.animalValue
import net.ccbluex.liquidbounce.features.module.modules.client.TargetModule.deadValue
import net.ccbluex.liquidbounce.features.module.modules.client.TargetModule.invisibleValue
import net.ccbluex.liquidbounce.features.module.modules.client.TargetModule.mobValue
import net.ccbluex.liquidbounce.features.module.modules.client.TargetModule.playerValue
import net.ccbluex.liquidbounce.handler.other.AutoReconnect.delay
import net.ccbluex.liquidbounce.handler.payload.ClientFixes.blockFML
import net.ccbluex.liquidbounce.handler.payload.ClientFixes.blockPayloadPackets
import net.ccbluex.liquidbounce.handler.payload.ClientFixes.blockProxyPacket
import net.ccbluex.liquidbounce.handler.payload.ClientFixes.blockResourcePackExploit
import net.ccbluex.liquidbounce.handler.payload.ClientFixes.fmlFixesEnabled
import net.ccbluex.liquidbounce.file.FileConfig
import net.ccbluex.liquidbounce.file.FileManager.PRETTY_GSON
import net.ccbluex.liquidbounce.handler.lang.LanguageManager.overrideLanguage
import net.ccbluex.liquidbounce.ui.client.gui.GuiClientConfiguration.Companion.altsLength
import net.ccbluex.liquidbounce.ui.client.gui.GuiClientConfiguration.Companion.enabledClientTitle
import net.ccbluex.liquidbounce.ui.client.gui.GuiClientConfiguration.Companion.enabledCustomBackground
import net.ccbluex.liquidbounce.ui.client.gui.GuiClientConfiguration.Companion.particles
import net.ccbluex.liquidbounce.ui.client.gui.GuiClientConfiguration.Companion.stylisedAlts
import net.ccbluex.liquidbounce.ui.client.gui.GuiClientConfiguration.Companion.unformattedAlts
import java.io.*

class ValuesConfig(file: File) : FileConfig(file) {

    /**
     * Load config from file
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    override fun loadConfig() {
        val jsonElement = JsonParser().parse(file.bufferedReader())
        if (jsonElement is JsonNull) return

        val jsonObject = jsonElement as JsonObject
        for ((key, value) in jsonObject.entrySet()) {
            when {
                key.equals("commandprefix", true) ->
                    commandManager.prefix = value.asCharacter
                key.equals("targets", true) -> {
                    val jsonValue = value as JsonObject
                    if (jsonValue.has("TargetPlayer")) playerValue = jsonValue["TargetPlayer"].asBoolean
                    if (jsonValue.has("TargetMobs")) mobValue = jsonValue["TargetMobs"].asBoolean
                    if (jsonValue.has("TargetAnimals")) animalValue = jsonValue["TargetAnimals"].asBoolean
                    if (jsonValue.has("TargetInvisible")) invisibleValue = jsonValue["TargetInvisible"].asBoolean
                    if (jsonValue.has("TargetDead")) deadValue = jsonValue["TargetDead"].asBoolean
                }
                key.equals("features", true) -> {
                    val jsonValue = value as JsonObject
                    if (jsonValue.has("AntiForge")) fmlFixesEnabled = jsonValue["AntiForge"].asBoolean
                    if (jsonValue.has("AntiForgeFML")) blockFML = jsonValue["AntiForgeFML"].asBoolean
                    if (jsonValue.has("AntiForgeProxy")) blockProxyPacket = jsonValue["AntiForgeProxy"].asBoolean
                    if (jsonValue.has("AntiForgePayloads")) blockPayloadPackets = jsonValue["AntiForgePayloads"].asBoolean
                    if (jsonValue.has("FixResourcePackExploit")) blockResourcePackExploit = jsonValue["FixResourcePackExploit"].asBoolean
                    if (jsonValue.has("ClientBrand")) possibleBrands.set(jsonValue["ClientBrand"].asString)
                    if (jsonValue.has("AutoReconnectDelay")) delay = jsonValue["AutoReconnectDelay"].asInt
                }
                key.equals("liquidchat", true) -> {
                    val jsonValue = value as JsonObject
                    if (jsonValue.has("token")) jwtToken = jsonValue["token"].asString
                }
                key.equals("DonatorCape", true) -> {
                    val jsonValue = value as JsonObject
                    if (jsonValue.has("TransferCode")) {
                        CapeService.knownToken = jsonValue["TransferCode"].asString
                    }
                }
                key.equals("clientConfiguration", true) -> {
                    val jsonValue = value as JsonObject
                    if (jsonValue.has("EnabledClientTitle")) enabledClientTitle = jsonValue["EnabledClientTitle"].asBoolean
                    if (jsonValue.has("EnabledBackground")) enabledCustomBackground = jsonValue["EnabledBackground"].asBoolean
                    if (jsonValue.has("Particles")) particles = jsonValue["Particles"].asBoolean
                    if (jsonValue.has("StylisedAlts")) stylisedAlts = jsonValue["StylisedAlts"].asBoolean
                    if (jsonValue.has("AltsLength")) altsLength = jsonValue["AltsLength"].asInt
                    if (jsonValue.has("CleanAlts")) unformattedAlts = jsonValue["CleanAlts"].asBoolean
                    if (jsonValue.has("OverrideLanguage")) overrideLanguage = jsonValue["OverrideLanguage"].asString
                }
                key.equals("background", true) -> { // Compatibility with old versions
                    val jsonValue = value as JsonObject
                    if (jsonValue.has("Enabled")) enabledCustomBackground = jsonValue["Enabled"].asBoolean
                    if (jsonValue.has("Particles")) particles = jsonValue["Particles"].asBoolean
                }
                else -> {
                    val module = moduleManager[key] ?: continue

                    val jsonModule = value as JsonObject
                    for (moduleValue in module.values) {
                        val element = jsonModule[moduleValue.name]
                        if (element != null) moduleValue.fromJson(element)
                    }
                }
            }
        }
    }

    /**
     * Save config to file
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    override fun saveConfig() {
        val jsonObject = JsonObject()
        jsonObject.run {
            addProperty("CommandPrefix", commandManager.prefix)
        }

        val jsonTargets = JsonObject()
        jsonTargets.run {
            addProperty("TargetPlayer", playerValue)
            addProperty("TargetMobs", mobValue)
            addProperty("TargetAnimals", animalValue)
            addProperty("TargetInvisible", invisibleValue)
            addProperty("TargetDead", deadValue)
        }

        jsonObject.add("targets", jsonTargets)
        val jsonFeatures = JsonObject()
        jsonFeatures.run {
            addProperty("AntiForge", fmlFixesEnabled)
            addProperty("AntiForgeFML", blockFML)
            addProperty("AntiForgeProxy", blockProxyPacket)
            addProperty("AntiForgePayloads", blockPayloadPackets)
            addProperty("FixResourcePackExploit", blockResourcePackExploit)
            addProperty("ClientBrand", possibleBrands.get())
            addProperty("AutoReconnectDelay", delay)
        }
        jsonObject.add("features", jsonFeatures)

        val liquidChatObject = JsonObject()
        liquidChatObject.addProperty("token", jwtToken)
        jsonObject.add("liquidchat", liquidChatObject)

        val capeObject = JsonObject()
        capeObject.addProperty("TransferCode", CapeService.knownToken)
        jsonObject.add("DonatorCape", capeObject)

        val clientObject = JsonObject()
        clientObject.run {
            addProperty("EnabledClientTitle", enabledClientTitle)
            addProperty("EnabledBackground", enabledCustomBackground)
            addProperty("Particles", particles)
            addProperty("StylisedAlts", stylisedAlts)
            addProperty("AltsLength", altsLength)
            addProperty("CleanAlts", unformattedAlts)
            addProperty("OverrideLanguage", overrideLanguage)
        }
        jsonObject.add("clientConfiguration", clientObject)

        for (module in moduleManager.modules) {
            if (module.values.isEmpty()) continue

            val jsonModule = JsonObject()
            for (value in module.values) jsonModule.add(value.name, value.toJson())
            jsonObject.add(module.name, jsonModule)
        }

        file.writeText(PRETTY_GSON.toJson(jsonObject))
    }
}