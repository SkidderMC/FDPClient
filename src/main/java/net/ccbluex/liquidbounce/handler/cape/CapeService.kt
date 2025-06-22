/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.cape

import kotlinx.coroutines.*
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.SessionUpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.file.gson.decodeJson
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.io.*
import net.ccbluex.liquidbounce.utils.kotlin.SharedScopes
import net.ccbluex.liquidbounce.utils.login.UserUtils
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*
import java.util.concurrent.atomic.AtomicLong

/**
 * A more reliable and stress reduced cape service
 *
 * It will frequently update all carriers of capes into a map with the described cape name.
 * This allows to cache already known capes and store them locally and will more quickly load them.
 *
 * We know this might cause sometimes users to not have their capes shown immediately when account switches, but we can reduce the stress
 * on the API and the connection of the user.
 */
object CapeService : Listenable, MinecraftInstance {

    /**
     * The client cape user
     */
    var knownToken = ""
        get() = clientCapeUser?.token ?: field

    @Volatile
    var clientCapeUser: CapeSelfUser? = null
        private set

    /**
     * I would prefer to use CLIENT_API but due to Cloudflare causing issues with SSL and their browser integrity check,
     * we have a separate domain.
     */
    private const val CAPE_API = "http://capes.liquidbounce.net/api/v1/cape"

    /**
     * The API URL to get all cape carriers.
     * Format: [["8f617b6a-bea0-4af5-8e4b-d026d8fa9de8", "marco"], ...]
     */
    private const val CAPE_CARRIERS_URL = "$CAPE_API/carriers"

    private const val SELF_CAPE_URL = "$CAPE_API/self"

    private const val CAPE_NAME_DL_BASE_URL = "$CAPE_API/name/%s"
    private const val REFRESH_DELAY = 300000L // Every 5 minutes should update

    /**
     * Collection of all cape carriers on the API.
     * We start with an empty list, which will be updated by the refreshCapeCarriers function frequently based on the REFRESH_DELAY.
     * A CapeCarrier is a pair of (uuid, cape_name)
     */
    @Volatile
    private var capeCarriers = emptyMap<UUID, String>()
    private val lastUpdate = AtomicLong(0L)
    private var refreshJob: Job? = null

    /**
     * Refresh cape carriers, capture from the API.
     * It will take a list of (uuid, cape_name) tuples.
     */
    fun refreshCapeCarriers(force: Boolean = false, done: (capeCarriers: Map<UUID, String>) -> Unit) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdate.get() > REFRESH_DELAY || force) {
            if (refreshJob?.isActive != true) {
                refreshJob = SharedScopes.IO.launch {
                    runCatching {
                        // Capture data from API and parse JSON
                        HttpClient.get(CAPE_CARRIERS_URL).use {
                            if (!it.isSuccessful) {
                                throw RuntimeException("Failed to get cape carriers. Status code: ${it.code}")
                            }

                            it.body.charStream().readJson().asJsonArray.associate { objInArray ->
                                // Should be a JSON Array. It will fail if not.
                                val arrayInArray = objInArray.asJsonArray
                                // 1. is UUID 2. is name of cape
                                val uuid = arrayInArray[0].asString
                                val name = arrayInArray[1].asString

                                UUID.fromString(uuid) to name
                            }
                        }

                        lastUpdate.set(currentTime)
                        done(capeCarriers)
                    }.onFailure {
                        LOGGER.error("Failed to refresh cape carriers due to error.", it)
                    }
                }
            }
        } else {
            // Call out done immediate because there is no refresh required at the moment
            done(capeCarriers)
        }
    }

    /**
     * Get the download url to cape of UUID
     */
    fun getCapeDownload(uuid: UUID): Pair<String, String>? {
        val clientCapeUser = clientCapeUser

        if (uuid == mc.session.profile.id && clientCapeUser != null) {
            // If the UUID is the same as the current user, we can use the clientCapeUser
            val capeName = clientCapeUser.capeName
            return capeName to String.format(CAPE_NAME_DL_BASE_URL, capeName)
        }

        // Lookup cape carrier by UUID, if UUID is matching
        val capeName = capeCarriers[uuid] ?: return null

        return capeName to String.format(CAPE_NAME_DL_BASE_URL, capeName)
    }

    suspend fun login(token: String) = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(SELF_CAPE_URL)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", token)
            .build()

        HttpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val json = try {
                    response.body.charStream().decodeJson<LoginResponse>()
                } catch (e: Exception) {
                    throw RuntimeException("Failed to decode JSON of self cape. Response: ${response.body.string()}", e)
                }

                clientCapeUser = CapeSelfUser(token, json.enabled, json.uuid, json.cape)
                LOGGER.info("Logged in successfully. Cape: ${json.cape}")
            } else {
                throw RuntimeException("Failed to get self cape. Status code: ${response.code}")
            }
        }
    }

    fun logout() {
        clientCapeUser = null
        knownToken = ""
        LOGGER.info("Logged out successfully.")
    }

    /**
     * Update the cape state of the user
     */
    fun toggleCapeState(done: (enabled: Boolean, success: Boolean, statusCode: Int) -> Unit) {
        val capeUser = clientCapeUser ?: return

        SharedScopes.IO.launch {
            val request = Request.Builder()
                .url(SELF_CAPE_URL)
                .apply {
                    if (capeUser.enabled) delete()
                    else put(RequestBody.EMPTY)
                }
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", capeUser.token)
                .build()

            try {
                val statusCode = HttpClient.newCall(request).execute().use { response ->
                    response.code
                }

                // Refresh cape carriers
                refreshCapeCarriers(force = true) {
                    LOGGER.info("Cape state toggled successfully.")
                }

                capeUser.enabled = !capeUser.enabled

                done(capeUser.enabled, statusCode == 204, statusCode) // HTTP 204 No Content
            } catch (e: Throwable) {
                LOGGER.error("Failed to toggle cape state due to error.", e)
            }
        }
    }

    /**
     * We want to immediately update the owner of the cape and refresh the cape carriers
     */
    private val onNewSession = handler<SessionUpdateEvent>(dispatcher = Dispatchers.IO) {
        // Check if donator cape is actually enabled and has a transfer code, also make sure the account used is premium.
        val capeUser = clientCapeUser ?: return@handler

        if (!UserUtils.isValidTokenOffline(mc.session.token))
            return@handler

        try {
            // Apply cape to new account
            val uuid = mc.session.playerID
            val username = mc.session.username

            val requestBody = "{\"uuid\":${uuid}}".toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(SELF_CAPE_URL)
                .patch(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", capeUser.token)
                .build()

            HttpClient.newCall(request).execute().use { response ->
                if (response.code == 204) { // HTTP 204 No Content
                    capeUser.uuid = uuid
                    LOGGER.info("[Donator Cape] Successfully transferred cape to $uuid ($username)")
                } else {
                    LOGGER.info("[Donator Cape] Failed to transfer cape (${response.code})")
                }
            }

            // Refresh cape carriers
            refreshCapeCarriers(force = true) {
                LOGGER.info("Cape carriers refreshed after session change.")
            }
        } catch (e: Throwable) {
            LOGGER.error("Failed to handle new session due to error.", e)
        }
    }
}

private data class LoginResponse(val cape: String, val enabled: Boolean, val uuid: String)

data class CapeSelfUser(val token: String, var enabled: Boolean, var uuid: String, val capeName: String)