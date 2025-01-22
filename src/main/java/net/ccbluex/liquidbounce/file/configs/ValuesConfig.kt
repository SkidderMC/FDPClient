/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.file.configs

import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.FDPClient.commandManager
import net.ccbluex.liquidbounce.FDPClient.moduleManager
import net.ccbluex.liquidbounce.handler.cape.CapeService
import net.ccbluex.liquidbounce.features.module.modules.client.IRCModule.jwtToken
import net.ccbluex.liquidbounce.file.FileConfig
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.file.FileManager.PRETTY_GSON
import net.ccbluex.liquidbounce.file.configs.models.ClientConfiguration
import net.ccbluex.liquidbounce.features.module.modules.client.TargetModule.animalValue
import net.ccbluex.liquidbounce.features.module.modules.client.TargetModule.deadValue
import net.ccbluex.liquidbounce.features.module.modules.client.TargetModule.invisibleValue
import net.ccbluex.liquidbounce.features.module.modules.client.TargetModule.mobValue
import net.ccbluex.liquidbounce.features.module.modules.client.TargetModule.playerValue
import net.ccbluex.liquidbounce.handler.payload.ClientFixes
import net.ccbluex.liquidbounce.utils.io.readJson
import java.io.File
import java.io.IOException

class ValuesConfig(file: File) : FileConfig(file) {

    /**
     * Load config from file
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    override fun loadConfig() {
        val json = file.readJson() as? JsonObject ?: return

        val prevVersion = json["ClientVersion"]?.asString ?: "unknown"
        // Compare versions
        if (prevVersion != FDPClient.clientVersionText) {
            // Backup old version config before loading the new one
            FileManager.backupAllConfigs(prevVersion, FDPClient.clientVersionText)
        }

        for ((key, value) in json.entrySet()) {
            when {
                key.equals("CommandPrefix", true) -> {
                    commandManager.prefix = value.asString
                }

                // Here we revert to the old "targets" key
                key.equals("targets", true) -> {
                    val jsonValue = value as JsonObject
                    if (jsonValue.has("TargetPlayer")) playerValue = jsonValue["TargetPlayer"].asBoolean
                    if (jsonValue.has("TargetMobs")) mobValue = jsonValue["TargetMobs"].asBoolean
                    if (jsonValue.has("TargetAnimals")) animalValue = jsonValue["TargetAnimals"].asBoolean
                    if (jsonValue.has("TargetInvisible")) invisibleValue = jsonValue["TargetInvisible"].asBoolean
                    if (jsonValue.has("TargetDead")) deadValue = jsonValue["TargetDead"].asBoolean
                }

                // ClientFixes (AntiForge, etc.)
                key.equals(ClientFixes.name, true) -> {
                    ClientFixes.fromJson(value)
                }

                // Liquid chat
                key.equals("liquidchat", true) -> {
                    val jsonValue = value as JsonObject
                    if (jsonValue.has("token")) jwtToken = jsonValue["token"].asString
                }

                // Donator Cape
                key.equals("DonatorCape", true) -> {
                    val jsonValue = value as JsonObject
                    if (jsonValue.has("TransferCode")) {
                        CapeService.knownToken = jsonValue["TransferCode"].asString
                    }
                }

                // Client Configuration
                key.equals(ClientConfiguration.name, true) -> {
                    ClientConfiguration.fromJson(value)
                }

                // Deprecated - old background key
                key.equals("background", true) -> {
                    val jsonValue = value as JsonObject
                    if (jsonValue.has("Enabled")) ClientConfiguration.customBackground = jsonValue["Enabled"].asBoolean
                    if (jsonValue.has("Particles")) ClientConfiguration.particles = jsonValue["Particles"].asBoolean
                }

                else -> {
                    // Modules
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
        val jsonObject = JsonObject().apply {
            addProperty("CommandPrefix", commandManager.prefix)
            addProperty("ClientVersion", FDPClient.clientVersionText)
        }

        // Revert to old "targets" approach
        val jsonTargets = JsonObject().apply {
            addProperty("TargetPlayer", playerValue)
            addProperty("TargetMobs", mobValue)
            addProperty("TargetAnimals", animalValue)
            addProperty("TargetInvisible", invisibleValue)
            addProperty("TargetDead", deadValue)
        }
        jsonObject.add("targets", jsonTargets)

        // ClientFixes
        jsonObject.add(ClientFixes.name, ClientFixes.toJson())

        // Liquid chat
        val liquidChatObject = JsonObject().apply {
            addProperty("token", jwtToken)
        }
        jsonObject.add("liquidchat", liquidChatObject)

        // Donator Cape
        val capeObject = JsonObject().apply {
            addProperty("TransferCode", CapeService.knownToken)
        }
        jsonObject.add("DonatorCape", capeObject)

        // Client Configuration
        jsonObject.add(ClientConfiguration.name, ClientConfiguration.toJson())

        // Modules
        for (module in moduleManager) {
            if (module.values.isEmpty()) continue

            val jsonModule = JsonObject()
            for (value in module.values) {
                jsonModule.add(value.name, value.toJson())
            }
            jsonObject.add(module.name, jsonModule)
        }

        file.writeText(PRETTY_GSON.toJson(jsonObject))
    }
}
