/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.spotify

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.concurrent.TimeUnit

/**
 * Handles the Spotify Web API HTTP calls.
 */
class SpotifyService(
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(SpotifyDefaults.httpTimeoutMillis, TimeUnit.MILLISECONDS)
        .readTimeout(SpotifyDefaults.httpTimeoutMillis, TimeUnit.MILLISECONDS)
        .writeTimeout(SpotifyDefaults.httpTimeoutMillis, TimeUnit.MILLISECONDS)
        .build(),
) {

    suspend fun refreshAccessToken(credentials: SpotifyCredentials): SpotifyAccessToken = withContext(Dispatchers.IO) {
        LOGGER.info(
            "[Spotify][HTTP] POST $TOKEN_URL (clientId=${mask(credentials.clientId)}, refreshToken=${mask(credentials.refreshToken)}, flow=${credentials.flow})"
        )

        val refreshToken = credentials.refreshToken
            ?: throw IOException("Spotify refresh token was null")
        val clientId = credentials.clientId
            ?: throw IOException("Spotify client ID was null")
        val encodedRefresh = URLEncoder.encode(refreshToken, StandardCharsets.UTF_8.name())
        val encodedClientId = URLEncoder.encode(clientId, StandardCharsets.UTF_8.name())
        val payloadBuilder = StringBuilder("grant_type=refresh_token&refresh_token=$encodedRefresh&client_id=$encodedClientId")

        val requestBuilder = Request.Builder()
            .url(TOKEN_URL)
            .header("Content-Type", "application/x-www-form-urlencoded")

        if (credentials.flow == SpotifyAuthFlow.CONFIDENTIAL_CLIENT) {
            val clientSecret = credentials.clientSecret
                ?: throw IOException("Spotify client secret was null for confidential flow")
            val basicAuth = Base64.getEncoder()
                .encodeToString("${clientId}:${clientSecret}".toByteArray(StandardCharsets.UTF_8))
            requestBuilder.header("Authorization", "Basic $basicAuth")
        }

        val request = requestBuilder
            .post(payloadBuilder.toString().toRequestBody(FORM_MEDIA_TYPE))
            .build()

        httpClient.newCall(request).execute().use { response ->
            LOGGER.info("[Spotify][HTTP] Token response status=${response.code} message=${response.message}")

            val body = response.body.string()
            if (!response.isSuccessful) {
                val message = body.ifBlank { "<empty>" }
                LOGGER.warn("[Spotify][HTTP] Token refresh failed body=$message")
                throw IOException("Spotify token refresh failed with HTTP ${'$'}{response.code}: $message")
            }

            if (body.isBlank()) {
                throw IOException("Spotify token response was empty")
            }

            val json = parseJson(body)
            val token = json.get("access_token")?.asString
                ?: throw IOException("Spotify token response did not contain an access token")
            val expiresIn = json.get("expires_in")?.asLong ?: DEFAULT_TOKEN_EXPIRY

            logTokenResponse(json, token)

            val refreshToken = json.get("refresh_token")?.asString
            SpotifyAccessToken(
                value = token,
                expiresAtMillis = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expiresIn - 5),
                refreshToken = refreshToken,
            )
        }
    }

    suspend fun exchangeAuthorizationCode(
        clientId: String,
        clientSecret: String?,
        code: String,
        redirectUri: String,
        codeVerifier: String?,
    ): SpotifyAccessToken = withContext(Dispatchers.IO) {
        LOGGER.info(
            "[Spotify][HTTP] POST $TOKEN_URL (clientId=${mask(clientId)}, grant_type=authorization_code)"
        )

        val encodedCode = URLEncoder.encode(code, StandardCharsets.UTF_8.name())
        val encodedRedirect = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.name())
        val encodedClientId = URLEncoder.encode(clientId, StandardCharsets.UTF_8.name())
        val payloadBuilder = StringBuilder(
            "grant_type=authorization_code&code=$encodedCode&redirect_uri=$encodedRedirect&client_id=$encodedClientId",
        )
        if (!codeVerifier.isNullOrBlank()) {
            payloadBuilder.append("&code_verifier=")
                .append(URLEncoder.encode(codeVerifier, StandardCharsets.UTF_8.name()))
        }

        val requestBuilder = Request.Builder()
            .url(TOKEN_URL)
            .header("Content-Type", "application/x-www-form-urlencoded")

        if (!clientSecret.isNullOrBlank()) {
            val basicAuth = Base64.getEncoder()
                .encodeToString("$clientId:$clientSecret".toByteArray(StandardCharsets.UTF_8))
            requestBuilder.header("Authorization", "Basic $basicAuth")
        }

        val request = requestBuilder
            .post(payloadBuilder.toString().toRequestBody(FORM_MEDIA_TYPE))
            .build()

        httpClient.newCall(request).execute().use { response ->
            LOGGER.info("[Spotify][HTTP] Authorization response status=${response.code} message=${response.message}")

            val body = response.body.string()
            if (!response.isSuccessful) {
                val message = body.ifBlank { "<empty>" }
                LOGGER.warn("[Spotify][HTTP] Authorization exchange failed body=$message")
                throw IOException("Spotify authorization failed with HTTP ${'$'}{response.code}: $message")
            }

            if (body.isBlank()) {
                throw IOException("Spotify authorization response was empty")
            }

            val json = parseJson(body)
            val token = json.get("access_token")?.asString
                ?: throw IOException("Spotify authorization response missing access token")
            val refreshToken = json.get("refresh_token")?.asString
                ?: throw IOException("Spotify authorization response missing refresh token")
            val expiresIn = json.get("expires_in")?.asLong ?: DEFAULT_TOKEN_EXPIRY

            logTokenResponse(json, token)

            SpotifyAccessToken(
                value = token,
                expiresAtMillis = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expiresIn - 5),
                refreshToken = refreshToken,
            )
        }
    }

    suspend fun fetchCurrentlyPlaying(accessToken: String): SpotifyState? = withContext(Dispatchers.IO) {
        LOGGER.info("[Spotify][HTTP] GET $NOW_PLAYING_URL (token=${mask(accessToken)})")

        val request = Request.Builder()
            .url(NOW_PLAYING_URL)
            .header("Authorization", "Bearer $accessToken")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            LOGGER.info("[Spotify][HTTP] Playback response status=${response.code} message=${response.message}")
            if (response.code == 204) {
                LOGGER.info("[Spotify] Spotify API returned 204 - no active playback")
                return@use null
            }

            val body = response.body.string()
            LOGGER.info("[Spotify][HTTP] Playback response body=${body.ifBlank { "<empty>" }}")

            if (!response.isSuccessful) {
                val message = body.ifBlank { "<empty>" }
                throw IOException("Spotify now playing request failed with HTTP ${'$'}{response.code}: $message")
            }

            if (body.isBlank()) {
                LOGGER.info("[Spotify] Playback response was empty")
                return@use null
            }
            val state = parseState(body)
            logPlaybackState(state)
            state
        }
    }

    private fun parseState(body: String): SpotifyState {
        val json = parseJson(body)
        val isPlaying = json.get("is_playing")?.asBoolean ?: false
        val progress = json.get("progress_ms")?.asInt ?: 0

        val item = json.get("item")?.takeIf { it.isJsonObject }?.asJsonObject
            ?: return SpotifyState(null, isPlaying, progress)
        val id = item.get("id")?.asString ?: ""
        val title = item.get("name")?.asString ?: "Unknown"

        val artists = item.get("artists")?.takeIf { it.isJsonArray }?.asJsonArray
            ?.mapNotNull { it.asJsonObject.get("name")?.asString }
            ?.joinToString(", ") ?: "Unknown"

        val albumObj = item.get("album")?.takeIf { it.isJsonObject }?.asJsonObject
        val albumName = albumObj?.get("name")?.asString ?: ""
        val coverUrl = albumObj?.get("images")?.takeIf { it.isJsonArray }?.asJsonArray
            ?.firstOrNull { it.isJsonObject }
            ?.asJsonObject
            ?.get("url")
            ?.asString
        val duration = item.get("duration_ms")?.asInt ?: 0

        return SpotifyState(
            SpotifyTrack(
                id = id,
                title = title,
                artists = artists,
                album = albumName,
                coverUrl = coverUrl,
                durationMs = duration,
            ),
            isPlaying,
            progress,
        )
    }

    private fun parseJson(body: String) = JsonParser().parse(body).asJsonObject

    private fun logTokenResponse(json: JsonObject, token: String) {
        val sanitized = JsonObject()
        for ((key, value) in json.entrySet()) {
            when (key) {
                "access_token" -> sanitized.addProperty(key, mask(token))
                "refresh_token" -> sanitized.addProperty(key, mask(value.asString))
                else -> sanitized.add(key, value)
            }
        }
        LOGGER.info("[Spotify][HTTP] Token response body=$sanitized")
        val expiresIn = json.get("expires_in")?.asLong ?: DEFAULT_TOKEN_EXPIRY
        LOGGER.info("[Spotify] Access token refreshed (expires in ${expiresIn}s)")
    }

    private fun logPlaybackState(state: SpotifyState?) {
        if (state == null) {
            LOGGER.info("[Spotify] No playback data returned by the API")
            return
        }

        val track = state.track
        if (track == null) {
            LOGGER.info("[Spotify] Playback status ${if (state.isPlaying) "is playing" else "paused"} but no track metadata provided")
            return
        }

        val elapsedSeconds = state.progressMs / 1000
        val durationSeconds = track.durationMs.coerceAtLeast(1) / 1000
        LOGGER.info(
            "[Spotify] ${if (state.isPlaying) "Playing" else "Paused"}: ${track.title} - ${track.artists} (${elapsedSeconds}s/${durationSeconds}s)"
        )
    }

    private companion object {
        const val TOKEN_URL = "https://accounts.spotify.com/api/token"
        const val NOW_PLAYING_URL = "https://api.spotify.com/v1/me/player/currently-playing"
        const val DEFAULT_TOKEN_EXPIRY = 3600L
        val FORM_MEDIA_TYPE = "application/x-www-form-urlencoded".toMediaType()

        fun mask(value: String?): String = when {
            value == null -> "<null>"
            value.isEmpty() -> "<empty>"
            value.length <= 4 -> "***"
            else -> value.take(4) + "***"
        }
    }
}