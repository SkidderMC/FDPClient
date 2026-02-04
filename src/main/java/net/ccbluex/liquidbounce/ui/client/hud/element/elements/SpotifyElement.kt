/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.features.module.modules.client.SpotifyModule
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyConnectionState
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyState
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

@ElementInfo(name = "SpotifyDisplay")
class SpotifyElement(
    x: Double = 8.0,
    y: Double = 60.0,
) : Element("SpotifyDisplay", x, y) {

    private val cardWidth by int("CardWidth", 200, 150..320)
    private val cornerRadius by float("CornerRadius", 5f, 0f..8f)
    private val backgroundColor by color("BackgroundColor", Color(8, 8, 8, 190))
    private val accentColor by color("AccentColor", Color(29, 185, 84))
    private val textColor by color("PrimaryText", Color.WHITE)
    private val secondaryTextColor by color("SecondaryText", Color(205, 205, 205))
    private val progressBackgroundColor by color("ProgressBackground", Color(255, 255, 255, 70))
    private val showAlbum by boolean("ShowAlbum", true)
    private val showProgressBar by boolean("ShowProgressBar", true)

    override fun drawElement(): Border {
        val width = cardWidth.toFloat()
        val padding = 6f
        val titleFont = Fonts.fontRegular40
        val infoFont = Fonts.fontSemibold35
        val connectionState = SpotifyModule.connectionState
        val moduleEnabled = SpotifyModule.state
        val playbackState = SpotifyModule.currentState
        val contentWidth = width - padding * 2
        val lines = mutableListOf<Pair<String, Int>>()

        val statusText = when {
            !moduleEnabled -> "Disabled"
            else -> connectionState.displayName
        }
        val statusColor = resolveConnectionColor(connectionState, moduleEnabled)

        var progressRatio = 0f
        var drawProgressBar = false

        if (!moduleEnabled) {
            lines += "Enable the Spotify module to start syncing." to secondaryTextColor.rgb
        } else if (connectionState != SpotifyConnectionState.CONNECTED || playbackState == null) {
            lines += when (connectionState) {
                SpotifyConnectionState.CONNECTING ->
                    "Connecting to Spotify account..." to secondaryTextColor.rgb

                SpotifyConnectionState.ERROR -> {
                    val error = SpotifyModule.lastErrorMessage
                    if (!error.isNullOrBlank()) {
                        ellipsize(error, infoFont, contentWidth) to secondaryTextColor.rgb
                    } else {
                        "Failed to contact Spotify." to secondaryTextColor.rgb
                    }
                }

                SpotifyConnectionState.DISCONNECTED ->
                    "Not connected. Open the Spotify module to begin." to secondaryTextColor.rgb

                SpotifyConnectionState.CONNECTED -> "Waiting for playback data..." to secondaryTextColor.rgb
            }
        } else {
            val track = playbackState.track
            if (track != null) {
                lines += ellipsize(track.title, infoFont, contentWidth) to textColor.rgb
                lines += ellipsize(track.artists, infoFont, contentWidth) to secondaryTextColor.rgb
                if (showAlbum && track.album.isNotBlank()) {
                    lines += ellipsize(track.album, infoFont, contentWidth) to secondaryTextColor.rgb
                }
                val progressMs = computeProgress(playbackState)
                if (track.durationMs > 0) {
                    val line = "${formatTime(progressMs)} / ${formatTime(track.durationMs)}"
                    lines += line to secondaryTextColor.rgb
                    progressRatio = (progressMs.toFloat() / track.durationMs).coerceIn(0f, 1f)
                    drawProgressBar = showProgressBar
                } else {
                    lines += "Elapsed: ${formatTime(progressMs)}" to secondaryTextColor.rgb
                }
                lines += if (playbackState.isPlaying) {
                    "Playing" to accentColor.rgb
                } else {
                    "Paused" to secondaryTextColor.rgb
                }
            } else {
                lines += "No playback detected." to secondaryTextColor.rgb
                lines += "Start Spotify on any device to sync." to secondaryTextColor.rgb
            }
        }

        val lineHeight = infoFont.FONT_HEIGHT + 4f
        val titleHeight = titleFont.FONT_HEIGHT.toFloat()
        var height = padding * 2 + titleHeight
        if (lines.isNotEmpty()) {
            height += 4f + lines.size * lineHeight
        }
        if (drawProgressBar) {
            height += 8f
        }

        RenderUtils.drawRoundedRect(0f, 0f, width, height, cornerRadius, backgroundColor.rgb)

        val iconRadius = titleHeight / 2f - 2f
        val iconCenterY = padding + titleHeight / 2f
        RenderUtils.drawFilledCircle((padding + iconRadius).toInt(), iconCenterY.toInt(), iconRadius, accentColor)

        val textStartX = padding + iconRadius * 2 + 4f
        titleFont.drawString("Spotify", textStartX, padding, textColor.rgb)
        val statusWidth = infoFont.getStringWidth(statusText)
        infoFont.drawString(
            statusText,
            width - padding - statusWidth,
            padding + titleHeight - infoFont.FONT_HEIGHT,
            statusColor,
        )

        var currentY = padding + titleHeight + 4f
        lines.forEach { (text, color) ->
            infoFont.drawString(ellipsize(text, infoFont, contentWidth), padding, currentY, color)
            currentY += lineHeight
        }

        if (drawProgressBar) {
            val barHeight = 4f
            val barX = padding
            val barY = height - padding - barHeight
            RenderUtils.drawRoundedRect(barX, barY, barX + contentWidth, barY + barHeight, barHeight / 2, progressBackgroundColor.rgb)
            if (progressRatio > 0f) {
                RenderUtils.drawRoundedRect(
                    barX,
                    barY,
                    barX + contentWidth * progressRatio,
                    barY + barHeight,
                    barHeight / 2,
                    accentColor.rgb,
                )
            }
        }

        return Border(0f, 0f, width, height)
    }

    private fun computeProgress(state: SpotifyState): Int {
        val elapsed = if (state.isPlaying) (System.currentTimeMillis() - state.updatedAt).toInt() else 0
        val trackDuration = state.track?.durationMs ?: Int.MAX_VALUE
        return min(trackDuration, max(0, state.progressMs + elapsed))
    }

    private fun formatTime(ms: Int): String {
        val clamped = max(0, ms)
        val minutes = clamped / 1000 / 60
        val seconds = (clamped / 1000) % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    private fun ellipsize(text: String, font: GameFontRenderer, maxWidth: Float): String {
        if (font.getStringWidth(text) <= maxWidth) {
            return text
        }
        val ellipsis = "..."
        val ellipsisWidth = font.getStringWidth(ellipsis)
        val targetWidth = max(0f, maxWidth - ellipsisWidth)
        val builder = StringBuilder()
        for (char in text) {
            val candidate = builder.append(char).toString()
            if (font.getStringWidth(candidate) >= targetWidth) {
                builder.setLength(max(0, builder.length - 1))
                break
            }
        }
        return builder.append(ellipsis).toString()
    }

    private fun resolveConnectionColor(state: SpotifyConnectionState, enabled: Boolean): Int {
        return when {
            !enabled -> 0xFF7C7C7C.toInt()
            state == SpotifyConnectionState.CONNECTED -> accentColor.rgb
            state == SpotifyConnectionState.CONNECTING -> 0xFFE09F24.toInt()
            state == SpotifyConnectionState.ERROR -> 0xFFE05757.toInt()
            else -> secondaryTextColor.rgb
        }
    }
}
