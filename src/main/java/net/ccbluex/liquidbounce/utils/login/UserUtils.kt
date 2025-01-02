/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.login

import net.ccbluex.liquidbounce.utils.io.HttpUtils

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

        return HttpUtils.getJson<Profile>(
            "https://api.minecraftservices.com/minecraft/profile/lookup/$uuid"
        )?.name?.also {
            usernameCache[uuid] = it
        }
    }

    /**
     * Get UUID of username
     */
    fun getUUID(username: String): String? {
        usernameCache[username]?.let { return it }

        return HttpUtils.getJson<Profile>(
            "https://api.minecraftservices.com/minecraft/profile/lookup/name/$username"
        )?.id?.also {
            usernameCache[username] = it
        }
    }
}

private class Profile(val id: String, val name: String)