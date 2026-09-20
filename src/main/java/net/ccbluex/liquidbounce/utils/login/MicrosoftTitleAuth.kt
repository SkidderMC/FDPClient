/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.login

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.liuli.elixir.account.MicrosoftAccount
import net.ccbluex.liquidbounce.utils.io.HttpClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class DeviceCodeSession(
    val userCode: String,
    val verificationUri: String,
    internal val deviceCode: String,
    internal val intervalMillis: Long,
    internal val expiresAtMillis: Long
)

/**
 * Signs in through the device code flow of the official Minecraft Java title.
 *
 * The session endpoint answers HTTP 403 to identity tokens that were minted for a third party
 * application, so the title id below is the only one that survives the whole chain.
 */
object MicrosoftTitleAuth {

    const val AUTH_METHOD_NAME = "MICROSOFT_TITLE"

    private const val CLIENT_ID = "00000000402b5328"
    private const val REDIRECT_URI = "https://login.live.com/oauth20_desktop.srf"
    private const val SCOPE = "service::user.auth.xboxlive.com::MBI_SSL"
    private const val RPS_TICKET_RULE = "<access_token>"

    private const val DEVICE_CODE_URL = "https://login.live.com/oauth20_connect.srf"
    private const val TOKEN_URL = "https://login.live.com/oauth20_token.srf"
    private const val DEVICE_CODE_GRANT = "urn:ietf:params:oauth:grant-type:device_code"

    private const val FALLBACK_VERIFICATION_URI = "https://www.microsoft.com/link"
    private const val DEFAULT_INTERVAL_SECONDS = 5L
    private const val DEFAULT_EXPIRY_SECONDS = 900L
    private const val SLOW_DOWN_PENALTY_MILLIS = 5000L

    private val FORM_MEDIA_TYPE = "application/x-www-form-urlencoded".toMediaType()

    @Suppress("UNCHECKED_CAST")
    fun ensureRegistered() {
        val registry = MicrosoftAccount.AuthMethod.registry as MutableMap<String, MicrosoftAccount.AuthMethod>

        registry.getOrPut(AUTH_METHOD_NAME) {
            MicrosoftAccount.AuthMethod(CLIENT_ID, REDIRECT_URI, SCOPE, RPS_TICKET_RULE)
        }
    }

    fun requestDeviceCode(): DeviceCodeSession {
        val json = postForm(
            DEVICE_CODE_URL,
            form("client_id" to CLIENT_ID, "scope" to SCOPE, "response_type" to "device_code")
        )

        json.string("error")?.let { throw IOException("Microsoft rejected the login request: $it") }

        val interval = json.long("interval") ?: DEFAULT_INTERVAL_SECONDS
        val expiresIn = json.long("expires_in") ?: DEFAULT_EXPIRY_SECONDS

        return DeviceCodeSession(
            userCode = json.requireString("user_code", "Microsoft did not return a login code"),
            verificationUri = json.string("verification_uri") ?: FALLBACK_VERIFICATION_URI,
            deviceCode = json.requireString("device_code", "Microsoft did not return a device code"),
            intervalMillis = interval * 1000L,
            expiresAtMillis = System.currentTimeMillis() + expiresIn * 1000L
        )
    }

    fun awaitAccount(session: DeviceCodeSession, isCancelled: () -> Boolean): MicrosoftAccount? {
        var waitMillis = session.intervalMillis

        while (!isCancelled()) {
            if (System.currentTimeMillis() > session.expiresAtMillis) {
                fail("expired_token")
            }

            Thread.sleep(waitMillis)

            if (isCancelled()) {
                return null
            }

            val json = postForm(
                TOKEN_URL,
                form(
                    "client_id" to CLIENT_ID,
                    "grant_type" to DEVICE_CODE_GRANT,
                    "device_code" to session.deviceCode
                )
            )

            when (val error = json.string("error")) {
                null -> return buildAccount(json)
                "authorization_pending" -> Unit
                "slow_down" -> waitMillis += SLOW_DOWN_PENALTY_MILLIS
                else -> fail(error)
            }
        }

        return null
    }

    private fun fail(error: String): Nothing = throw IOException(
        when (error) {
            "authorization_declined" -> "The login was declined in the browser."
            "expired_token" -> "The login code expired, please try again."
            else -> "Microsoft login failed: $error"
        }
    )

    private fun buildAccount(token: JsonObject): MicrosoftAccount {
        val refreshToken = token.requireString("refresh_token", "Microsoft did not return a refresh token")

        ensureRegistered()

        val account = MicrosoftAccount()
        account.fromRawJson(JsonObject().apply {
            addProperty("name", "")
            addProperty("refreshToken", refreshToken)
            addProperty("authMethod", AUTH_METHOD_NAME)
        })
        account.update()

        return account
    }

    private fun postForm(url: String, body: String): JsonObject {
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .post(body.toRequestBody(FORM_MEDIA_TYPE))
            .build()

        HttpClient.newCall(request).execute().use { response ->
            val text = response.body.string()

            if (text.isBlank()) {
                throw IOException("Microsoft returned an empty response (HTTP ${response.code})")
            }

            return JsonParser().parse(text) as? JsonObject
                ?: throw IOException("Microsoft returned an unexpected response (HTTP ${response.code})")
        }
    }

    private fun form(vararg parameters: Pair<String, String>) = parameters.joinToString("&") { (key, value) ->
        "$key=${URLEncoder.encode(value, StandardCharsets.UTF_8.name())}"
    }

    private fun JsonObject.string(key: String) = get(key)?.takeIf { it.isJsonPrimitive }?.asString

    private fun JsonObject.requireString(key: String, message: String) = string(key) ?: throw IOException(message)

    private fun JsonObject.long(key: String) = get(key)?.takeIf { it.isJsonPrimitive }?.asLong
}
