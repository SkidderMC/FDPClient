/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.ccbluex.liquidbounce.event.EventManager
import net.ccbluex.liquidbounce.handler.spotify.SpotifyIntegration
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.ui.client.gui.GuiSpotify
import net.ccbluex.liquidbounce.ui.client.gui.GuiSpotifyPlayer
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyAccessToken
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyConnectionChangedEvent
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyConnectionState
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyCredentials
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyAuthFlow
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyDefaults
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyService
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyState
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyStateChangedEvent
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.client.chat
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.EnumMap

/**
 * Standalone Spotify integration that fetches the currently playing track from the Spotify Web API.
 */
object SpotifyModule : Module("Spotify", Category.CLIENT, defaultState = false) {

    private val moduleScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val service: SpotifyService
        get() = SpotifyIntegration.service
    private var workerJob: Job? = null
    private var browserAuthFuture: CompletableFuture<SpotifyAccessToken>? = null
    private val credentialsFile = File(FileManager.dir, "spotify.json")
    private val quickClientId: String = SpotifyDefaults.quickConnectClientId.trim()
    private val supportedAuthModes = SpotifyAuthMode.entries
        .filter { it != SpotifyAuthMode.QUICK || quickClientId.isNotBlank() }
        .toTypedArray()
    private val defaultAuthMode = supportedAuthModes.firstOrNull() ?: SpotifyAuthMode.MANUAL
    private val authModeValue = choices(
        "Mode",
        supportedAuthModes.map { it.storageValue }.toTypedArray(),
        defaultAuthMode.storageValue,
    )
    private val clientIdValue = text("ClientId", SpotifyDefaults.clientId).apply { hide() }
    private val clientSecretValue = text("ClientSecret", SpotifyDefaults.clientSecret).apply { hide() }
    private val refreshTokenValue = text("RefreshToken", SpotifyDefaults.refreshToken).apply { hide() }
    private val quickRefreshTokenValue = text("QuickRefreshToken", "").apply { hide() }
    private val pollIntervalValue = int("PollInterval", SpotifyDefaults.pollIntervalSeconds, 3..60, suffix = "s")
    private val autoReconnectValue = boolean("AutoReconnect", true)
    private val openPlayerValue = boolean("OpenUI", false).apply {
        onChange { _, newValue ->
            if (newValue) {
                mc.addScheduledTask { openPlayerScreen() }
            }
            false
        }
    }
    private val cachedTokens = EnumMap<SpotifyAuthMode, SpotifyAccessToken?>(SpotifyAuthMode::class.java)

    init {
        loadSavedCredentials()
    }

    @Volatile
    var currentState: SpotifyState? = null
        private set

    @Volatile
    var connectionState: SpotifyConnectionState = SpotifyConnectionState.DISCONNECTED
        private set

    @Volatile
    var lastErrorMessage: String? = null
        private set

    val pollIntervalSeconds: Int
        get() = pollIntervalValue.get()

    val autoReconnect: Boolean
        get() = autoReconnectValue.get()

    val authMode: SpotifyAuthMode
        get() = SpotifyAuthMode.fromStorage(authModeValue.get()) ?: defaultAuthMode

    val clientId: String
        get() = clientIdValue.get()

    val clientSecret: String
        get() = clientSecretValue.get()

    val refreshToken: String
        get() = refreshTokenValue.get()

    override fun onEnable() {
        reloadCredentialsFromDisk()
        updateConnection(SpotifyConnectionState.CONNECTING, null)
        startWorker()
        if (!hasCredentials()) {
            val mode = authMode
            if (mode == SpotifyAuthMode.QUICK) {
                chat("§eSpotify quick connect needs a browser authorization. Open the module screen to get started.")
            } else {
                chat("§cSpotify credentials are missing. Open the configuration screen to enter them.")
            }
            mc.displayGuiScreen(GuiSpotify(mc.currentScreen))
        }
    }

    override fun onDisable() {
        workerJob?.cancel()
        workerJob = null
        cachedTokens.clear()
        browserAuthFuture?.cancel(true)
        browserAuthFuture = null
        updateConnection(SpotifyConnectionState.DISCONNECTED, null)
    }

    fun openConfigScreen() {
        reloadCredentialsFromDisk()
        mc.displayGuiScreen(GuiSpotify(mc.currentScreen))
    }

    fun openPlayerScreen() {
        reloadCredentialsFromDisk()
        mc.displayGuiScreen(GuiSpotifyPlayer(mc.currentScreen))
    }

    fun updateCredentials(clientId: String, clientSecret: String, refreshToken: String): Boolean {
        val sanitized = SpotifyCredentials(
            clientId.trim(),
            clientSecret.trim(),
            refreshToken.trim(),
            SpotifyAuthFlow.CONFIDENTIAL_CLIENT,
        )

        LOGGER.info(
            "[Spotify] Received credential update (clientId=${mask(sanitized.clientId)}, refreshToken=${mask(sanitized.refreshToken)})"
        )

        if (!sanitized.isValid()) {
            LOGGER.warn("[Spotify] Ignoring credential update because at least one field is blank")
            return false
        }

        sanitized.clientId?.let { clientIdValue.set(it) }
        sanitized.clientSecret?.let { clientSecretValue.set(it) }
        sanitized.refreshToken?.let { refreshTokenValue.set(it) }
        val saved = persistCredentials()
        cachedTokens[SpotifyAuthMode.MANUAL] = null
        if (state) {
            workerJob?.cancel()
            workerJob = null
            startWorker()
        }
        return saved
    }

    fun setPollInterval(seconds: Int) {
        pollIntervalValue.set(seconds.coerceIn(3, 60))
    }

    fun toggleAutoReconnect(): Boolean {
        autoReconnectValue.toggle()
        return autoReconnectValue.get()
    }

    fun cycleAuthMode(): SpotifyAuthMode {
        val modes = supportedAuthModes
        if (modes.isEmpty()) {
            return SpotifyAuthMode.MANUAL
        }
        val current = authMode
        val currentIndex = modes.indexOf(current).takeIf { it >= 0 } ?: 0
        val next = modes[(currentIndex + 1) % modes.size]
        if (next == current) {
            return current
        }
        authModeValue.set(next.storageValue)
        updateConnection(SpotifyConnectionState.DISCONNECTED, null)
        persistCredentials()
        if (state) {
            workerJob?.cancel()
            workerJob = null
            startWorker()
        }
        return next
    }

    suspend fun acquireAccessToken(forceRefresh: Boolean = false): SpotifyAccessToken? {
        val mode = authMode
        val credentials = resolveCredentials(mode) ?: return null
        return ensureAccessToken(credentials, mode, forceRefresh)
    }

    fun authModeLabel(): String = "Mode: ${authMode.displayName}"

    fun supportsQuickConnect(): Boolean = supportedAuthModes.any { it == SpotifyAuthMode.QUICK }

    fun beginBrowserAuthorization(callback: (BrowserAuthStatus, String) -> Unit): Boolean {
        val mode = authMode
        val clientId = when (mode) {
            SpotifyAuthMode.QUICK -> quickClientId
            SpotifyAuthMode.MANUAL -> clientIdValue.get().trim()
        }
        val clientSecret = when (mode) {
            SpotifyAuthMode.QUICK -> null
            SpotifyAuthMode.MANUAL -> clientSecretValue.get().trim()
        }

        if (clientId.isBlank()) {
            callback(BrowserAuthStatus.ERROR, "Spotify client ID is not configured for ${mode.displayName} mode.")
            return false
        }

        if (mode.flow == SpotifyAuthFlow.CONFIDENTIAL_CLIENT && clientSecret.isNullOrBlank()) {
            callback(BrowserAuthStatus.ERROR, "Enter the client ID and secret before authorizing.")
            return false
        }

        val ongoing = browserAuthFuture
        if (ongoing != null && !ongoing.isDone) {
            callback(BrowserAuthStatus.INFO, "Browser authorization is already running.")
            return false
        }

        callback(BrowserAuthStatus.INFO, "Opening Spotify authorization flow in your browser...")
        val future = SpotifyIntegration.authorizeInBrowser(clientId, clientSecret, mode.flow)
        browserAuthFuture = future
        future.whenComplete { token, throwable ->
            mc.addScheduledTask {
                browserAuthFuture = null
                if (throwable != null) {
                    callback(BrowserAuthStatus.ERROR, "Authorization failed: ${throwable.message}")
                    return@addScheduledTask
                }

                if (token == null || token.refreshToken.isNullOrBlank()) {
                    callback(BrowserAuthStatus.ERROR, "Spotify did not return a refresh token.")
                    return@addScheduledTask
                }

                when (mode) {
                    SpotifyAuthMode.QUICK -> quickRefreshTokenValue.set(token.refreshToken)
                    SpotifyAuthMode.MANUAL -> refreshTokenValue.set(token.refreshToken)
                }
                cachedTokens[mode] = token
                val saved = persistCredentials()
                if (saved) {
                    callback(
                        BrowserAuthStatus.SUCCESS,
                        "Authorization completed for ${mode.displayName}. Credentials saved.",
                    )
                } else {
                    callback(BrowserAuthStatus.ERROR, "Authorization succeeded but saving failed. Check the logs.")
                }

                if (state) {
                    workerJob?.cancel()
                    workerJob = null
                    startWorker()
                }
            }
        }

        return true
    }

    private fun hasCredentials(): Boolean = resolveCredentials() != null

    private fun resolveCredentials(mode: SpotifyAuthMode = authMode): SpotifyCredentials? {
        val resolvedClientId = when (mode) {
            SpotifyAuthMode.QUICK -> quickClientId
            SpotifyAuthMode.MANUAL -> clientIdValue.get().trim()
        }
        if (resolvedClientId.isBlank()) {
            return null
        }

        val resolvedRefreshToken = when (mode) {
            SpotifyAuthMode.QUICK -> quickRefreshTokenValue.get().trim()
            SpotifyAuthMode.MANUAL -> refreshTokenValue.get().trim()
        }
        if (resolvedRefreshToken.isBlank()) {
            return null
        }

        val resolvedSecret = when (mode) {
            SpotifyAuthMode.QUICK -> ""
            SpotifyAuthMode.MANUAL -> clientSecretValue.get().trim()
        }

        val credentials = SpotifyCredentials(
            resolvedClientId,
            resolvedSecret,
            resolvedRefreshToken,
            mode.flow,
        )
        return credentials.takeIf { it.isValid() }
    }

    private fun startWorker() {
        if (workerJob?.isActive == true) {
            return
        }

        workerJob = moduleScope.launch {
            while (this@SpotifyModule.state) {
                val mode = authMode
                val credentials = resolveCredentials(mode)
                if (credentials == null) {
                    handleError("Missing Spotify credentials (${mode.displayName})")
                    delay(RETRY_DELAY_MS)
                    continue
                }

                val token = ensureAccessToken(credentials, mode)
                if (token == null) {
                    delay(RETRY_DELAY_MS)
                    continue
                }

                runCatching { service.fetchCurrentlyPlaying(token.value) }
                    .onFailure { handleError("Failed to fetch playback: ${'$'}{it.message}") }
                    .onSuccess { state ->
                        currentState = state
                        EventManager.call(SpotifyStateChangedEvent(state))
                        updateConnection(SpotifyConnectionState.CONNECTED, null)
                    }

                delay(TimeUnit.SECONDS.toMillis(pollIntervalSeconds.toLong()))
            }
        }
    }

    fun requestPlaybackRefresh() {
        moduleScope.launch {
            val mode = authMode
            val credentials = resolveCredentials(mode) ?: return@launch
            val token = ensureAccessToken(credentials, mode) ?: return@launch
            runCatching { service.fetchCurrentlyPlaying(token.value) }
                .onSuccess { state ->
                    currentState = state
                    EventManager.call(SpotifyStateChangedEvent(state))
                    updateConnection(SpotifyConnectionState.CONNECTED, null)
                }
                .onFailure { handleError("Failed to fetch playback: ${it.message}") }
        }
    }

    private suspend fun ensureAccessToken(
        credentials: SpotifyCredentials,
        mode: SpotifyAuthMode,
        forceRefresh: Boolean = false,
    ): SpotifyAccessToken? {
        val cached = cachedTokens[mode]
        if (!forceRefresh && cached != null && cached.expiresAtMillis > System.currentTimeMillis() + TOKEN_EXPIRY_GRACE_MS) {
            return cached
        }

        return runCatching { service.refreshAccessToken(credentials) }
            .onSuccess {
                cachedTokens[mode] = it
                persistCredentials()
                updateConnection(SpotifyConnectionState.CONNECTED, null)
            }
            .onFailure {
                handleError("Failed to refresh Spotify token: ${'$'}{it.message}")
            }
            .getOrNull()
    }

    private fun handleError(message: String) {
        LOGGER.warn("[Spotify] $message")
        currentState = null
        updateConnection(SpotifyConnectionState.ERROR, message)
        if (!autoReconnect) {
            chat("§cSpotify module disabled: $message")
            state = false
        }
    }

    private fun updateConnection(state: SpotifyConnectionState, error: String?) {
        if (connectionState == state && lastErrorMessage == error) {
            return
        }

        connectionState = state
        lastErrorMessage = error
        EventManager.call(SpotifyConnectionChangedEvent(state, error))
    }

    fun reloadCredentialsFromDisk(): Boolean = loadSavedCredentials()

    fun credentialsFilePath(): String = credentialsFile.absolutePath

    private fun loadSavedCredentials(): Boolean {
        ensureCredentialsDirectory()
        LOGGER.info("[Spotify] Loading credentials from ${credentialsFile.absolutePath}")
        if (!credentialsFile.exists()) {
            cachedTokens.clear()
            LOGGER.info("[Spotify] No saved credentials found at ${credentialsFile.absolutePath}")
            return false
        }

        return runCatching {
            val json = credentialsFile.readText(StandardCharsets.UTF_8)
            if (json.isBlank()) {
                cachedTokens.clear()
                return@runCatching false
            }

            val element = JsonParser().parse(json)
            if (!element.isJsonObject) {
                cachedTokens.clear()
                return@runCatching false
            }

            val obj = element.asJsonObject
            obj.get(CONFIG_KEY_MODE)?.takeIf { it.isJsonPrimitive }?.asString?.let { storedMode ->
                val resolved = SpotifyAuthMode.fromStorage(storedMode)
                if (resolved != null && supportedAuthModes.contains(resolved)) {
                    authModeValue.set(resolved.storageValue)
                } else if (resolved != null) {
                    LOGGER.warn("[Spotify] Stored auth mode ${resolved.displayName} is not supported. Falling back to ${defaultAuthMode.displayName}.")
                    authModeValue.set(defaultAuthMode.storageValue)
                }
            }
            obj.get(CONFIG_KEY_CLIENT_ID)?.takeIf { it.isJsonPrimitive }?.asString?.let { clientIdValue.set(it) }
            obj.get(CONFIG_KEY_CLIENT_SECRET)?.takeIf { it.isJsonPrimitive }?.asString?.let { clientSecretValue.set(it) }
            obj.get(CONFIG_KEY_REFRESH_TOKEN)?.takeIf { it.isJsonPrimitive }?.asString?.let { refreshTokenValue.set(it) }
            obj.get(CONFIG_KEY_QUICK_REFRESH_TOKEN)?.takeIf { it.isJsonPrimitive }?.asString?.let { quickRefreshTokenValue.set(it) }

            cachedTokens[SpotifyAuthMode.MANUAL] = restoreCachedToken(
                obj.get(CONFIG_KEY_ACCESS_TOKEN)?.takeIf { it.isJsonPrimitive }?.asString,
                obj.get(CONFIG_KEY_ACCESS_TOKEN_EXPIRY)?.takeIf { it.isJsonPrimitive }?.asLong ?: 0L,
                "manual",
            )
            cachedTokens[SpotifyAuthMode.QUICK] = restoreCachedToken(
                obj.get(CONFIG_KEY_QUICK_ACCESS_TOKEN)?.takeIf { it.isJsonPrimitive }?.asString,
                obj.get(CONFIG_KEY_QUICK_ACCESS_TOKEN_EXPIRY)?.takeIf { it.isJsonPrimitive }?.asLong ?: 0L,
                "quick",
            )

            LOGGER.info(
                "[Spotify] Loaded credentials from ${credentialsFile.absolutePath} (clientId=${mask(clientIdValue.get())}, refreshToken=${mask(refreshTokenValue.get())})",
            )
            true
        }.onFailure {
            cachedTokens.clear()
            LOGGER.warn("[Spotify] Failed to load saved credentials", it)
        }.getOrDefault(false)
    }

    private fun persistCredentials(): Boolean {
        return runCatching {
            val directory = credentialsFile.parentFile ?: FileManager.dir
            if (!directory.exists() && !directory.mkdirs()) {
                throw IllegalStateException("Unable to create directory: ${directory.absolutePath}")
            }

            val manualToken = cachedTokens[SpotifyAuthMode.MANUAL]
            val quickToken = cachedTokens[SpotifyAuthMode.QUICK]
            LOGGER.info(
                "[Spotify] Persisting credentials to ${credentialsFile.absolutePath} (mode=${authMode.displayName}, manualToken=${maskToken(manualToken)}, quickToken=${maskToken(quickToken)})",
            )

            val payload = JsonObject().apply {
                addProperty(CONFIG_KEY_MODE, authMode.storageValue)
                addProperty(CONFIG_KEY_CLIENT_ID, clientIdValue.get())
                addProperty(CONFIG_KEY_CLIENT_SECRET, clientSecretValue.get())
                addProperty(CONFIG_KEY_REFRESH_TOKEN, refreshTokenValue.get())
                addProperty(CONFIG_KEY_QUICK_REFRESH_TOKEN, quickRefreshTokenValue.get())
                addProperty(CONFIG_KEY_ACCESS_TOKEN, manualToken?.value ?: "")
                addProperty(CONFIG_KEY_ACCESS_TOKEN_EXPIRY, manualToken?.expiresAtMillis ?: 0L)
                addProperty(CONFIG_KEY_QUICK_ACCESS_TOKEN, quickToken?.value ?: "")
                addProperty(CONFIG_KEY_QUICK_ACCESS_TOKEN_EXPIRY, quickToken?.expiresAtMillis ?: 0L)
            }

            FileManager.writeFile(credentialsFile, FileManager.PRETTY_GSON.toJson(payload))
            LOGGER.info(
                "[Spotify] Saved credentials to ${credentialsFile.absolutePath} (${credentialsFile.length()} bytes written)",
            )
        }.onFailure {
            LOGGER.warn("[Spotify] Failed to save credentials", it)
        }.isSuccess
    }

    private fun restoreCachedToken(tokenValue: String?, expiresAt: Long, label: String): SpotifyAccessToken? {
        if (tokenValue.isNullOrBlank()) {
            return null
        }
        return if (expiresAt > System.currentTimeMillis()) {
            LOGGER.info("[Spotify] Restored cached $label access token from disk (expires in ${(expiresAt - System.currentTimeMillis()) / 1000}s)")
            SpotifyAccessToken(tokenValue, expiresAt)
        } else {
            LOGGER.info("[Spotify] Ignoring expired cached $label access token from disk")
            null
        }
    }

    private fun ensureCredentialsDirectory() {
        val directory = credentialsFile.parentFile ?: FileManager.dir
        if (!directory.exists() && directory.mkdirs()) {
            LOGGER.info("[Spotify] Created credentials directory at ${directory.absolutePath}")
        }
    }

    private fun mask(value: String?): String = when {
        value.isNullOrEmpty() -> if (value == null) "<null>" else "<empty>"
        value.length <= 4 -> "***"
        value.length <= 8 -> value.take(2) + "***"
        else -> value.take(4) + "***" + value.takeLast(2)
    }

    private fun maskToken(token: SpotifyAccessToken?): String = token?.value?.let(::mask) ?: "<none>"

    private const val RETRY_DELAY_MS = 5_000L
    private val TOKEN_EXPIRY_GRACE_MS = TimeUnit.SECONDS.toMillis(5)

    private const val CONFIG_KEY_MODE = "mode"
    private const val CONFIG_KEY_CLIENT_ID = "clientId"
    private const val CONFIG_KEY_CLIENT_SECRET = "clientSecret"
    private const val CONFIG_KEY_REFRESH_TOKEN = "refreshToken"
    private const val CONFIG_KEY_QUICK_REFRESH_TOKEN = "quickRefreshToken"
    private const val CONFIG_KEY_ACCESS_TOKEN = "accessToken"
    private const val CONFIG_KEY_ACCESS_TOKEN_EXPIRY = "accessTokenExpiryMillis"
    private const val CONFIG_KEY_QUICK_ACCESS_TOKEN = "quickAccessToken"
    private const val CONFIG_KEY_QUICK_ACCESS_TOKEN_EXPIRY = "quickAccessTokenExpiryMillis"

    enum class BrowserAuthStatus {
        INFO,
        SUCCESS,
        ERROR,
    }

    enum class SpotifyAuthMode(val storageValue: String, val displayName: String, val flow: SpotifyAuthFlow) {
        QUICK("Quick", "Quick Connect", SpotifyAuthFlow.PKCE),
        MANUAL("Manual", "Custom App", SpotifyAuthFlow.CONFIDENTIAL_CLIENT);

        companion object {
            fun fromStorage(value: String?): SpotifyAuthMode? = SpotifyAuthMode.entries.firstOrNull {
                it.storageValue.equals(value, true)
            }
        }
    }
}