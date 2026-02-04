/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.spotify

/**
 * Represents a simplified Spotify track.
 */
data class SpotifyTrack(
    val id: String,
    val title: String,
    val artists: String,
    val album: String,
    val coverUrl: String?,
    val durationMs: Int,
)

/**
 * Represents the state of the current Spotify playback session.
 */
data class SpotifyState(
    val track: SpotifyTrack?,
    val isPlaying: Boolean,
    val progressMs: Int,
    val shuffleEnabled: Boolean,
    val repeatMode: SpotifyRepeatMode,
    val volumePercent: Int? = null,
    val updatedAt: Long = System.currentTimeMillis(),
)

enum class SpotifyRepeatMode(val apiValue: String) {
    OFF("off"),
    ALL("context"),
    ONE("track");

    companion object {
        fun fromApi(value: String?): SpotifyRepeatMode {
            if (value == null) {
                return OFF
            }
            return SpotifyRepeatMode.entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) } ?: OFF
        }
    }
}

/**
 * Summarizes a Spotify playlist entry.
 */
data class SpotifyPlaylistSummary(
    val id: String,
    val name: String,
    val description: String?,
    val owner: String?,
    val trackCount: Int,
    val imageUrl: String?,
    val uri: String?,
    val isLikedSongs: Boolean = false,
)

/**
 * Represents a page of Spotify tracks returned by collection endpoints.
 */
data class SpotifyTrackPage(
    val tracks: List<SpotifyTrack>,
    val total: Int,
)

/**
 * OAuth credentials that are required for the Spotify Web API.
 */
data class SpotifyCredentials(
    val clientId: String?,
    val clientSecret: String?,
    val refreshToken: String?,
    val flow: SpotifyAuthFlow = SpotifyAuthFlow.CONFIDENTIAL_CLIENT,
) {
    fun isValid(): Boolean {
        if (clientId.isNullOrBlank() || refreshToken.isNullOrBlank()) {
            return false
        }
        return if (flow == SpotifyAuthFlow.CONFIDENTIAL_CLIENT) {
            !clientSecret.isNullOrBlank()
        } else {
            true
        }
    }
}

enum class SpotifyAuthFlow {
    CONFIDENTIAL_CLIENT,
    PKCE,
}

/**
 * Cached access token and expiry information.
 */
data class SpotifyAccessToken(
    val value: String,
    val expiresAtMillis: Long,
    val refreshToken: String? = null,
)

/**
 * Connection state used by the HUD/GUI to provide feedback to the user.
 */
enum class SpotifyConnectionState(val displayName: String) {
    DISCONNECTED("Disconnected"),
    CONNECTING("Connecting"),
    CONNECTED("Connected"),
    ERROR("Error"),
}