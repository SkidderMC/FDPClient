/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.spotify

import net.ccbluex.liquidbounce.event.Event

/**
 * Fired whenever the Spotify connection state changes.
 */
class SpotifyConnectionChangedEvent(
    val state: SpotifyConnectionState,
    val errorMessage: String? = null,
) : Event()

/**
 * Fired whenever the playback information is updated.
 */
class SpotifyStateChangedEvent(val state: SpotifyState?) : Event()
