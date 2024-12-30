/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.login

import net.ccbluex.liquidbounce.utils.client.ClientUtils
import net.ccbluex.liquidbounce.utils.io.HttpUtils
import net.ccbluex.liquidbounce.utils.io.parseJson

object UserUtils {

    private val uuidCache = hashMapOf<String, String>()
    private val usernameCache = hashMapOf<String, String>()

    /**
     * Check if token is valid
     *
     * Exam
     * 7a7c4193280a4060971f1e73be3d9bdb
     * 89371141db4f4ec485d68d1f63d01eec
     */
    fun isValidTokenOffline(token: String) = token.length >= 32

    fun getUsername(uuid: String): String? {
        uuidCache[uuid]?.let { return it }

        return try {
            val (text, code) = HttpUtils.get("https://api.minecraftservices.com/minecraft/profile/lookup/$uuid")

            if (code != 200) {
                ClientUtils.LOGGER.warn("Failed to get username of UUID $uuid, response code=$code")
                return null
            }

            val jsonObject = text.parseJson().asJsonObject
            val name = jsonObject["name"].asString
            uuidCache[uuid] = name
            name
        } catch (e: Exception) {
            ClientUtils.LOGGER.warn("Failed to get username of UUID $uuid", e)
            null
        }
    }

    /**
     * Get UUID of username
     */
    fun getUUID(username: String): String {
        usernameCache[username]?.let { return it }

        return try {
            val (text, code) = HttpUtils.get("https://api.minecraftservices.com/minecraft/profile/lookup/name/$username")

            if (code != 200) {
                ClientUtils.LOGGER.warn("Failed to get UUID of username $username, response code=$code")
                return ""
            }

            val id = text.parseJson().asJsonObject["id"].asString
            usernameCache[username] = id
            id
        } catch (e: Exception) {
            ClientUtils.LOGGER.warn("Failed to get UUID of username $username", e)
            ""
        }
    }
}