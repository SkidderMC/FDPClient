/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.login

import com.google.gson.JsonParser
import com.mojang.authlib.Agent
import com.mojang.authlib.exceptions.AuthenticationException
import com.mojang.authlib.exceptions.AuthenticationUnavailableException
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.login.UserUtils.getUUID
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.minecraft.util.Session
import java.net.Proxy
import java.util.*

object LoginUtils : MinecraftInstance() {
    @JvmStatic
    fun login(username: String?, password: String?): LoginResult {
        val userAuthentication = YggdrasilAuthenticationService(Proxy.NO_PROXY, "").createUserAuthentication(Agent.MINECRAFT) as YggdrasilUserAuthentication

        userAuthentication.setUsername(username)
        userAuthentication.setPassword(password)

        return try {
            userAuthentication.logIn()
            mc.session = Session(userAuthentication.selectedProfile.name,
                    userAuthentication.selectedProfile.id.toString(), userAuthentication.authenticatedToken, "mojang")
            LoginResult.LOGGED
        } catch (exception: AuthenticationUnavailableException) {
            LoginResult.NO_CONTACT
        } catch (exception: AuthenticationException) {
            val message = exception.message!!
            when {
                message.contains("invalid username or password.", ignoreCase = true) -> LoginResult.INVALID_ACCOUNT_DATA
                message.contains("account migrated", ignoreCase = true) -> LoginResult.MIGRATED
                else -> LoginResult.NO_CONTACT
            }
        } catch (exception: NullPointerException) {
            LoginResult.WRONG_PASSWORD
        }
    }

    @JvmStatic
    fun loginCracked(username: String?) {
        mc.session = Session(username, getUUID(username!!), "-", "legacy")
    }

    @JvmStatic
    fun randomCracked() {
        var name = GuiAltManager.randomAltField.text

        while (name.contains("%n") || name.contains("%s")) {
            if (name.contains("%n")) {
                name = name.replaceFirst("%n", RandomUtils.nextInt(0, 9).toString())
            }

            if (name.contains("%s")) {
                name = name.replaceFirst("%s", RandomUtils.randomString(1))
            }
        }

        loginCracked(name)
    }

    @JvmStatic
    fun loginSessionId(sessionId: String): LoginResult {
        val decodedSessionData = try {
            String(Base64.getDecoder().decode(sessionId.split(".")[1]), Charsets.UTF_8)
        } catch (e: Exception) {
            return LoginResult.FAILED_PARSE_TOKEN
        }

        val sessionObject = try {
            JsonParser().parse(decodedSessionData).asJsonObject
        } catch (e: java.lang.Exception) {
            return LoginResult.FAILED_PARSE_TOKEN
        }
        val uuid = sessionObject.get("spr").asString
        val accessToken = sessionObject.get("yggt").asString

        if (!UserUtils.isValidToken(accessToken)) {
            return LoginResult.INVALID_ACCOUNT_DATA
        }

        val username = UserUtils.getUsername(uuid) ?: return LoginResult.INVALID_ACCOUNT_DATA

        mc.session = Session(username, uuid, accessToken, "mojang")

        return LoginResult.LOGGED
    }

    enum class LoginResult {
        WRONG_PASSWORD, NO_CONTACT, INVALID_ACCOUNT_DATA, MIGRATED, LOGGED, FAILED_PARSE_TOKEN
    }
}