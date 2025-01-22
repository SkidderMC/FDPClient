/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.login

import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.event.EventManager.call
import net.ccbluex.liquidbounce.event.SessionUpdateEvent
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.minecraft.util.Session
import java.util.*

fun me.liuli.elixir.compat.Session.intoMinecraftSession() = Session(username, uuid, token, type)

object LoginUtils : MinecraftInstance {

    fun loginSessionId(sessionToken: String): LoginResult {
        val payload = try {
            val base64Payload = sessionToken.split(".")[1]
            String(Base64.getDecoder().decode(base64Payload), Charsets.UTF_8)
        } catch (e: Exception) {
            return LoginResult.FAILED_PARSE_TOKEN
        }

        val sessionObject = try {
            JsonParser().parse(payload).asJsonObject
        } catch (e: Exception) {
            return LoginResult.FAILED_PARSE_TOKEN
        }

        val uuid = sessionObject["profiles"]?.asJsonObject?.get("mc")?.asString ?: return LoginResult.FAILED_PARSE_TOKEN

//        Note: This is replaced with simple check for now.
//        if (!UserUtils.isValidToken(ACCESS_TOKEN)) {
//            return LoginResult.INVALID_ACCOUNT_DATA
//        }
        if (sessionToken.contains(":")) {
            return LoginResult.FAILED_PARSE_TOKEN
        }

        val username = UserUtils.getUsername(uuid) ?: return LoginResult.INVALID_ACCOUNT_DATA

        try {
            mc.session = Session(username, uuid, sessionToken, "microsoft")
        } catch (e: Exception) {
            return LoginResult.INVALID_ACCOUNT_DATA
        }

        call(SessionUpdateEvent)

        return LoginResult.LOGGED
    }

    enum class LoginResult {
        INVALID_ACCOUNT_DATA, LOGGED, FAILED_PARSE_TOKEN
    }

}