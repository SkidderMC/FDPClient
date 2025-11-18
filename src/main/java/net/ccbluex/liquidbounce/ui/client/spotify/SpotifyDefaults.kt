/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.spotify

/**
 * Reads default values for the Spotify module from system properties or environment variables.
 * They can be configured via Gradle properties which are then passed as JVM arguments.
 */
object SpotifyDefaults {
    private fun read(propertyKey: String, envKey: String, fallback: String = ""): String {
        return System.getProperty(propertyKey)?.takeIf { it.isNotBlank() }
            ?: System.getenv(envKey)?.takeIf { it.isNotBlank() }
            ?: fallback
    }

    val clientId: String = read("spotify.clientId", "SPOTIFY_CLIENT_ID")
    val clientSecret: String = read("spotify.clientSecret", "SPOTIFY_CLIENT_SECRET")
    val refreshToken: String = read("spotify.refreshToken", "SPOTIFY_REFRESH_TOKEN")
    val quickConnectClientId: String = read(
        "spotify.quickClientId",
        "SPOTIFY_QUICK_CLIENT_ID",
        clientId,
    )
    val pollIntervalSeconds: Int = read("spotify.pollIntervalSeconds", "SPOTIFY_POLL_INTERVAL", "5").toIntOrNull() ?: 5
    val httpTimeoutMillis: Long = read("spotify.httpTimeoutMs", "SPOTIFY_HTTP_TIMEOUT_MS", "12000").toLongOrNull() ?: 12_000L
    val dashboardUrl: String = read(
        "spotify.dashboardUrl",
        "SPOTIFY_DASHBOARD_URL",
        "https://developer.spotify.com/dashboard",
    )
    val authorizationGuideUrl: String = read(
        "spotify.authorizationGuideUrl",
        "SPOTIFY_AUTH_GUIDE_URL",
        "https://developer.spotify.com/documentation/web-api/tutorials/refreshing-tokens",
    )
    val authorizationScopes: String = read(
        "spotify.authorizationScopes",
        "SPOTIFY_AUTH_SCOPES",
        "user-read-currently-playing user-read-playback-state user-modify-playback-state playlist-read-private playlist-read-collaborative user-library-read",
    )
    val authorizationRedirectPort: Int = read(
        "spotify.authorizationRedirectPort",
        "SPOTIFY_AUTH_REDIRECT_PORT",
        "43791",
    ).toIntOrNull() ?: 43_791
    val authorizationRedirectPath: String = read(
        "spotify.authorizationRedirectPath",
        "SPOTIFY_AUTH_REDIRECT_PATH",
        "/spotify-oauth-callback",
    ).ifBlank { "/spotify-oauth-callback" }
}