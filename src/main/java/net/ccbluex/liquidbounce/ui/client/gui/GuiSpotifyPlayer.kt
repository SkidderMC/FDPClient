/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui

import kotlinx.coroutines.launch
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.modules.client.SpotifyModule
import net.ccbluex.liquidbounce.handler.spotify.SpotifyIntegration
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyConnectionChangedEvent
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyConnectionState
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyPlaylistSummary
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyRepeatMode
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyState
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyStateChangedEvent
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyTrack
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyTrackPage
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.io.HttpClient
import net.ccbluex.liquidbounce.utils.io.get
import net.ccbluex.liquidbounce.utils.kotlin.SharedScopes
import net.ccbluex.liquidbounce.utils.ui.AbstractScreen
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import okhttp3.Response
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.io.IOException
import java.util.UUID
import kotlin.math.max

class GuiSpotifyPlayer(private val prevScreen: GuiScreen?) : AbstractScreen(), Listenable {

    private fun spotifyIcon(fileName: String) = ResourceLocation("minecraft", "fdpclient/texture/spotify/$fileName")

    private val iconDefaultPlaylist = spotifyIcon("default_playlist_image.png")
    private val iconGoForward = spotifyIcon("go_forward.png")
    private val iconLiked = spotifyIcon("liked_icon.png")
    private val iconPause = spotifyIcon("pause.png")
    private val iconRepeatOff = spotifyIcon("repeat.png")
    private val iconShuffleOff = spotifyIcon("shuffle.png")
    private val iconEmpty = spotifyIcon("empty.png")
    private val iconHome = spotifyIcon("home.png")
    private val iconLikedSongs = spotifyIcon("liked_songs.png")
    private val iconPlay = spotifyIcon("play.png")
    private val iconRepeatOne = spotifyIcon("repeat_1.png")
    private val iconRepeatAll = spotifyIcon("repeat_enable.png")
    private val iconShuffleOn = spotifyIcon("shuffle_enable.png")
    private val iconBack = spotifyIcon("go_back.png")
    private val iconLike = spotifyIcon("like_icon.png")
    private val iconNext = spotifyIcon("next.png")
    private val iconPrevious = spotifyIcon("previous.png")

    private lateinit var searchField: GuiTextField
    private lateinit var homeButton: SpotifyIconButton
    private lateinit var backButton: SpotifyIconButton
    private lateinit var refreshButton: SpotifyIconButton
    private lateinit var playPauseButton: SpotifyIconButton
    private lateinit var previousButton: SpotifyIconButton
    private lateinit var nextButton: SpotifyIconButton
    private lateinit var shuffleButton: SpotifyIconButton
    private lateinit var repeatButton: SpotifyIconButton

    private var playlists: List<SpotifyPlaylistSummary> = emptyList()
    private var playlistsLoading = false
    private var playlistError: String? = null

    private var selectedPlaylist: SpotifyPlaylistSummary? = null
    private val trackCache = mutableMapOf<String, SpotifyTrackPage>()
    private var displayedTracks: List<SpotifyTrack> = emptyList()
    private var filteredTracks: List<SpotifyTrack> = emptyList()
    private var tracksLoading = false
    private var tracksError: String? = null

    private var playlistScroll = 0f
    private var trackScroll = 0f
    private var searchQuery = ""
    private var selectedTrackIndex = -1
    private var lastTrackClickIndex = -1
    private var lastTrackClickTime = 0L

    private var playbackState: SpotifyState? = SpotifyModule.currentState
    private var connectionState: SpotifyConnectionState = SpotifyModule.connectionState
    private var listening = false
    private var shuffleEnabled = SpotifyModule.currentState?.shuffleEnabled ?: false
    private var repeatMode: SpotifyRepeatMode = SpotifyModule.currentState?.repeatMode ?: SpotifyRepeatMode.OFF

    private val coverCache = mutableMapOf<String, ResourceLocation>()
    private val coverLoading = mutableSetOf<String>()
    private val trackSavedState = mutableMapOf<String, Boolean>()

    private var bannerMessage: String? = null
    private var bannerExpiry = 0L

    private var volumePercent = SpotifyModule.currentState?.volumePercent ?: 50
    private var adjustingVolume = false
    private var volumeDirty = false
    private var volumeSliderRect: PanelArea? = null

    private val stateHandler = handler<SpotifyStateChangedEvent>(always = true) { event ->
        playbackState = event.state
        shuffleEnabled = event.state?.shuffleEnabled ?: false
        repeatMode = event.state?.repeatMode ?: SpotifyRepeatMode.OFF
        event.state?.volumePercent?.let { volumePercent = it }
    }

    private val connectionHandler = handler<SpotifyConnectionChangedEvent>(always = true) { event ->
        connectionState = event.state
    }

    override fun handleEvents(): Boolean = listening

    override fun initGui() {
        super.initGui()
        Keyboard.enableRepeatEvents(true)
        listening = true
        buttonList.clear()
        textFields.clear()

        homeButton = +SpotifyIconButton(
            BUTTON_HOME,
            20,
            28,
            24,
            24,
            iconProvider = { iconHome },
        )
        val searchLeft = homeButton.xPosition + homeButton.width + 6
        val searchWidth = (width - searchLeft - 80).coerceAtLeast(120)
        searchField = textField(401, mc.fontRendererObj, searchLeft, 30, searchWidth, 18)
        searchField.maxStringLength = 80

        backButton = +SpotifyIconButton(
            BUTTON_BACK,
            20,
            height - 42,
            28,
            28,
            iconProvider = { iconBack },
        )
        refreshButton = +SpotifyIconButton(
            BUTTON_REFRESH,
            width - 44,
            28,
            24,
            24,
            iconProvider = { iconGoForward },
        )
        previousButton = +SpotifyIconButton(
            BUTTON_PREVIOUS,
            width / 2 - 90,
            height - 64,
            32,
            32,
            iconProvider = { iconPrevious },
        )
        playPauseButton = +SpotifyIconButton(
            BUTTON_PLAY_PAUSE,
            width / 2 - 32,
            height - 72,
            64,
            44,
            iconProvider = { resolvePlayPauseIcon() },
        )
        nextButton = +SpotifyIconButton(
            BUTTON_NEXT,
            width / 2 + 58,
            height - 64,
            32,
            32,
            iconProvider = { iconNext },
        )
        shuffleButton = +SpotifyIconButton(
            BUTTON_SHUFFLE,
            width / 2 - 150,
            height - 58,
            28,
            28,
            iconProvider = { resolveShuffleIcon() },
        )
        repeatButton = +SpotifyIconButton(
            BUTTON_REPEAT,
            width / 2 + 110,
            height - 58,
            28,
            28,
            iconProvider = { resolveRepeatIcon() },
        )

        if (playlists.isEmpty()) {
            reloadPlaylists(force = true)
        } else {
            updateTrackFilters()
        }
        SpotifyModule.requestPlaybackRefresh()
    }

    override fun onGuiClosed() {
        super.onGuiClosed()
        Keyboard.enableRepeatEvents(false)
        listening = false
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        drawGradientRect(0, 0, width, height, 0xFF111111.toInt(), 0xFF050505.toInt())
        drawCenteredString(mc.fontRendererObj, "Spotify Browser", width / 2, 12, 0xFFFFFFFF.toInt())

        searchField.drawTextBox()
        if (searchField.text.isEmpty() && !searchField.isFocused) {
            mc.fontRendererObj.drawString("Search playlists or tracks", searchField.xPosition + 4, searchField.yPosition + 6, 0xFF777777.toInt())
        }

        drawConnectionBadge()
        drawPlaylists(mouseX, mouseY)
        drawTracks(mouseX, mouseY)
        drawPlaybackBar()
        drawBanner()

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun drawConnectionBadge() {
        val status = connectionState.displayName
        val color = when (connectionState) {
            SpotifyConnectionState.CONNECTED -> 0xFF1DB954.toInt()
            SpotifyConnectionState.CONNECTING -> 0xFFE5A041.toInt()
            SpotifyConnectionState.ERROR -> 0xFFE55959.toInt()
            SpotifyConnectionState.DISCONNECTED -> 0xFFB0B0B0.toInt()
        }
        val text = "Status: $status"
        val textWidth = mc.fontRendererObj.getStringWidth(text)
        mc.fontRendererObj.drawString(text, width - textWidth - 20, 12, color)
    }

    private fun drawPlaylists(mouseX: Int, mouseY: Int) {
        val area = playlistArea()
        drawRect(area.left, area.top, area.right, area.bottom, 0xB0121212.toInt())
        mc.fontRendererObj.drawString("Your Library", area.left, area.top - 12, 0xFFDDDDDD.toInt())

        when {
            playlistsLoading -> {
                drawCenteredString(mc.fontRendererObj, "Loading playlists...", (area.left + area.right) / 2, area.top + 10, 0xFFB0B0B0.toInt())
            }
            playlistError != null -> {
                drawWrappedText(playlistError!!, area, 0xFFE55959.toInt())
            }
            playlists.isEmpty() -> {
                drawCenteredString(mc.fontRendererObj, "Link your Spotify account to load playlists.", (area.left + area.right) / 2, area.top + 10, 0xFFB0B0B0.toInt())
            }
            else -> {
                val rowHeight = PLAYLIST_ROW_HEIGHT
                val viewHeight = area.height()
                val maxScroll = max(0f, playlists.size * rowHeight - viewHeight + 6f)
                playlistScroll = playlistScroll.coerceIn(0f, maxScroll)
                var y = area.top + 4 - playlistScroll
                playlists.forEach { playlist ->
                    if (y > area.bottom) {
                        return@forEach
                    }
                    if (y + rowHeight >= area.top) {
                        val hovered = mouseX in area.left..area.right && mouseY in y.toInt()..(y + rowHeight).toInt()
                        val selected = playlist.id == selectedPlaylist?.id
                        val bgColor = when {
                            selected -> 0x661DB954
                            hovered -> 0x44222222
                            else -> 0x00000000
                        }
                        if (bgColor != 0) {
                            drawRect(area.left + 1, y.toInt(), area.right - 1, (y + rowHeight).toInt(), bgColor)
                        }
                        val trackLabel = if (playlist.trackCount == 1) "1 track" else "${playlist.trackCount} tracks"
                        drawPlaylistArtwork(playlist, area.left + 4, y.toInt() + 4)
                        val textX = area.left + 34
                        mc.fontRendererObj.drawString(playlist.name, textX, y.toInt() + 4, 0xFFF8F8F8.toInt())
                        mc.fontRendererObj.drawString(trackLabel, textX, y.toInt() + 16, 0xFFBEBEBE.toInt())
                    }
                    y += rowHeight
                }
            }
        }
    }

    private fun drawTracks(mouseX: Int, mouseY: Int) {
        val area = trackArea()
        drawRect(area.left, area.top, area.right, area.bottom, 0xB0131313.toInt())
        val playlist = selectedPlaylist
        val title = playlist?.name ?: "Select a playlist"
        mc.fontRendererObj.drawString(title, area.left, area.top - 12, 0xFFDDDDDD.toInt())

        if (!tracksError.isNullOrBlank()) {
            drawWrappedText(tracksError!!, area, 0xFFE55959.toInt())
            return
        }
        if (playlist == null) {
            drawCenteredString(mc.fontRendererObj, "Choose a playlist to load tracks.", (area.left + area.right) / 2, area.top + 10, 0xFFB0B0B0.toInt())
            return
        }
        if (tracksLoading) {
            drawCenteredString(mc.fontRendererObj, "Loading tracks...", (area.left + area.right) / 2, area.top + 10, 0xFFB0B0B0.toInt())
            return
        }
        if (filteredTracks.isEmpty()) {
            val message = if (displayedTracks.isEmpty()) "This playlist has no tracks." else "No tracks match your search."
            drawCenteredString(mc.fontRendererObj, message, (area.left + area.right) / 2, area.top + 10, 0xFFB0B0B0.toInt())
            return
        }

        val rowHeight = TRACK_ROW_HEIGHT
        val viewHeight = area.height()
        val maxScroll = max(0f, filteredTracks.size * rowHeight - viewHeight + 8f)
        trackScroll = trackScroll.coerceIn(0f, maxScroll)

        val artSize = 20
        val numberColumnWidth = 18
        val titleColumnWidth = (area.width() * 0.45f).toInt()
        val artistColumnWidth = (area.width() * 0.28f).toInt()
        val likeColumnLeft = area.right - (LIKE_ICON_SIZE + 8)
        val durationColumnX = likeColumnLeft - 60

        var y = area.top + 4 - trackScroll
        filteredTracks.forEachIndexed { index, track ->
            if (y > area.bottom) {
                return@forEachIndexed
            }
            if (y + rowHeight >= area.top) {
                val hovered = mouseX in area.left..area.right && mouseY in y.toInt()..(y + rowHeight).toInt()
                val isSelected = index == selectedTrackIndex
                val isPlaying = playbackState?.track?.id == track.id
                val bgColor = when {
                    isPlaying -> 0x661DB954
                    isSelected -> 0x55333333
                    hovered -> 0x33202020
                    else -> 0
                }
                if (bgColor != 0) {
                    drawRect(area.left + 1, y.toInt(), area.right - 1, (y + rowHeight).toInt(), bgColor)
                }
                val rowTop = y.toInt()
                val baseY = rowTop + 6
                val numberX = area.left + 6
                mc.fontRendererObj.drawString((index + 1).toString(), numberX, baseY, 0xFFAAAAAA.toInt())
                val artX = numberX + numberColumnWidth
                drawTrackArtwork(track, artX, rowTop + 2, artSize)
                val textX = artX + artSize + 6
                mc.fontRendererObj.drawString(trimToWidth(track.title, titleColumnWidth - 20), textX, baseY, 0xFFF0F0F0.toInt())
                mc.fontRendererObj.drawString(
                    trimToWidth(track.artists, artistColumnWidth - 10),
                    textX + titleColumnWidth,
                    baseY,
                    0xFFB0B0B0.toInt(),
                )
                mc.fontRendererObj.drawString(formatDuration(track.durationMs), durationColumnX, baseY, 0xFFB0B0B0.toInt())
                val saved = isTrackSaved(track)
                val likeIconY = rowTop + ((rowHeight - LIKE_ICON_SIZE) / 2f).toInt()
                val texture = if (saved) iconLiked else iconLike
                drawIcon(texture, likeColumnLeft, likeIconY, LIKE_ICON_SIZE, LIKE_ICON_SIZE, if (saved) 1f else 0.85f)
            }
            y += rowHeight
        }
    }

    private fun drawPlaybackBar() {
        val barTop = height - 95
        val barBottom = height - 32
        drawRect(0, barTop, width, barBottom, 0xFF0F0F0F.toInt())
        val track = playbackState?.track
        if (track == null) {
            volumeSliderRect = null
            drawCenteredString(mc.fontRendererObj, "Start playback to see the current track.", width / 2, barTop + 12, 0xFFB0B0B0.toInt())
            return
        }
        val artSize = 64
        val artX = 25
        val artY = barTop + 6
        drawRemoteArtwork(track.coverUrl, iconEmpty, artX, artY, artSize, artSize, 0.95f)

        val textX = artX + artSize + 10
        mc.fontRendererObj.drawString(track.title, textX, artY + 4, 0xFFFFFFFF.toInt())
        mc.fontRendererObj.drawString(track.artists, textX, artY + 18, 0xFFB0B0B0.toInt())
        mc.fontRendererObj.drawString(track.album, textX, artY + 30, 0xFF8F8F8F.toInt())

        val duration = track.durationMs.coerceAtLeast(1)
        val progress = playbackState?.progressMs ?: 0
        val ratio = progress.toFloat() / duration
        val progressLeft = textX
        val progressRight = max(progressLeft + 80, width - 220)
        val progressTop = artY + artSize + 6
        val progressBottom = progressTop + 6
        drawRect(progressLeft, progressTop, progressRight, progressBottom, 0xFF1E1E1E.toInt())
        drawRect(progressLeft, progressTop, progressLeft + ((progressRight - progressLeft) * ratio).toInt(), progressBottom, 0xFF1DB954.toInt())
        val elapsedText = formatDuration(progress)
        val remainingText = formatDuration(duration - progress)
        mc.fontRendererObj.drawString(elapsedText, progressLeft, progressBottom + 4, 0xFFB0B0B0.toInt())
        val remainingWidth = mc.fontRendererObj.getStringWidth(remainingText)
        mc.fontRendererObj.drawString(remainingText, progressRight - remainingWidth, progressBottom + 4, 0xFFB0B0B0.toInt())
        drawVolumeSlider(artY)
    }

    private fun drawVolumeSlider(artY: Int) {
        val sliderWidth = 140
        val sliderHeight = 6
        val sliderLeft = width - sliderWidth - 40
        val sliderRight = sliderLeft + sliderWidth
        val sliderTop = artY + 10
        val sliderBottom = sliderTop + sliderHeight
        mc.fontRendererObj.drawString("Volume", sliderLeft, sliderTop - 10, 0xFFBEBEBE.toInt())
        drawRect(sliderLeft, sliderTop, sliderRight, sliderBottom, 0xFF1E1E1E.toInt())
        val ratio = volumePercent.coerceIn(0, 100) / 100f
        val fillRight = sliderLeft + (sliderWidth * ratio).toInt()
        drawRect(sliderLeft, sliderTop, fillRight, sliderBottom, 0xFF1DB954.toInt())
        val knobX = fillRight.coerceIn(sliderLeft, sliderRight)
        drawRect(knobX - 3, sliderTop - 3, knobX + 3, sliderBottom + 3, 0xFFFFFFFF.toInt())
        val percentText = "${volumePercent.coerceIn(0, 100)}%"
        val percentWidth = mc.fontRendererObj.getStringWidth(percentText)
        mc.fontRendererObj.drawString(percentText, sliderRight - percentWidth, sliderBottom + 4, 0xFFB0B0B0.toInt())
        volumeSliderRect = PanelArea(sliderLeft, sliderTop - 6, sliderRight, sliderBottom + 10)
    }

    private fun updateVolumeFromMouse(mouseX: Int) {
        val slider = volumeSliderRect ?: return
        val width = (slider.right - slider.left).coerceAtLeast(1)
        val ratio = ((mouseX - slider.left).toFloat() / width).coerceIn(0f, 1f)
        val newVolume = (ratio * 100f).toInt()
        if (newVolume != volumePercent) {
            volumePercent = newVolume
        }
    }

    private fun commitVolumeChange(target: Int) {
        val desired = target.coerceIn(0, 100)
        screenScope.launch {
            val token = SpotifyModule.acquireAccessToken()
            if (token == null) {
                showBanner("Authorize Spotify before controlling playback")
                return@launch
            }
            val result = runCatching { SpotifyIntegration.service.setVolume(token.value, desired) }
            result.onSuccess {
                SpotifyModule.requestPlaybackRefresh()
            }.onFailure {
                showBanner(it.message ?: "Failed to change volume")
            }
        }
    }

    private fun drawBanner() {
        val message = bannerMessage
        if (message.isNullOrBlank()) {
            return
        }
        if (System.currentTimeMillis() > bannerExpiry) {
            bannerMessage = null
            return
        }
        val y = height - 24
        drawRect(width / 2 - 110, y - 4, width / 2 + 110, y + 14, 0xAA000000.toInt())
        drawCenteredString(mc.fontRendererObj, message, width / 2, y, 0xFFFFFFFF.toInt())
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        val wheel = Mouse.getEventDWheel()
        if (wheel == 0) {
            return
        }
        val scaledX = Mouse.getEventX() * width / mc.displayWidth
        val scaledY = height - Mouse.getEventY() * height / mc.displayHeight - 1
        val delta = (wheel / 120f) * 18f
        when {
            playlistArea().contains(scaledX, scaledY) -> adjustPlaylistScroll(-delta)
            trackArea().contains(scaledX, scaledY) -> adjustTrackScroll(-delta)
        }
    }

    private fun adjustPlaylistScroll(delta: Float) {
        val area = playlistArea()
        val maxScroll = max(0f, playlists.size * PLAYLIST_ROW_HEIGHT - area.height() + 6f)
        playlistScroll = (playlistScroll + delta).coerceIn(0f, maxScroll)
    }

    private fun adjustTrackScroll(delta: Float) {
        val area = trackArea()
        val maxScroll = max(0f, filteredTracks.size * TRACK_ROW_HEIGHT - area.height() + 8f)
        trackScroll = (trackScroll + delta).coerceIn(0f, maxScroll)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (searchField.textboxKeyTyped(typedChar, keyCode)) {
            updateSearchQuery(searchField.text)
            return
        }
        super.keyTyped(typedChar, keyCode)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        searchField.mouseClicked(mouseX, mouseY, mouseButton)
        var sliderHandled = false
        if (mouseButton == 0 && volumeSliderRect?.contains(mouseX, mouseY) == true) {
            adjustingVolume = true
            updateVolumeFromMouse(mouseX)
            volumeDirty = true
            sliderHandled = true
        }
        if (mouseButton == 0) {
            when {
                !sliderHandled && playlistArea().contains(mouseX, mouseY) -> handlePlaylistClick(mouseY)
                !sliderHandled && trackArea().contains(mouseX, mouseY) -> handleTrackClick(mouseX, mouseY)
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        if (adjustingVolume && clickedMouseButton == 0) {
            updateVolumeFromMouse(mouseX)
            volumeDirty = true
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (state == 0 && adjustingVolume) {
            adjustingVolume = false
            if (volumeDirty) {
                volumeDirty = false
                commitVolumeChange(volumePercent)
            }
        }
        super.mouseReleased(mouseX, mouseY, state)
    }

    private fun drawPlaylistArtwork(playlist: SpotifyPlaylistSummary, x: Int, y: Int) {
        val size = 24
        if (playlist.isLikedSongs) {
            drawIcon(iconLikedSongs, x, y, size, size)
        } else {
            drawRemoteArtwork(playlist.imageUrl, iconDefaultPlaylist, x, y, size, size, 0.95f)
        }
    }

    private fun drawTrackArtwork(track: SpotifyTrack, x: Int, y: Int, size: Int) {
        drawRemoteArtwork(track.coverUrl, iconEmpty, x, y, size, size, 0.95f)
    }

    private fun drawRemoteArtwork(
        url: String?,
        fallback: ResourceLocation,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        alpha: Float = 1f,
    ) {
        val texture = url?.let { coverCache[it] }
        if (texture != null) {
            drawIcon(texture, x, y, width, height, alpha)
            return
        }
        drawIcon(fallback, x, y, width, height, alpha * 0.85f)
        if (!url.isNullOrBlank()) {
            requestTexture(url)
        }
    }

    private fun requestTexture(url: String) {
        if (coverCache.containsKey(url) || !coverLoading.add(url)) {
            return
        }
        SharedScopes.IO.launch {
            val imageResult = runCatching {
                HttpClient.get(url).use { response ->
                    ensureSuccess(response)
                    response.body.byteStream().use { stream ->
                        javax.imageio.ImageIO.read(stream) ?: throw IOException("Cover art missing")
                    }
                }
            }
            imageResult.onSuccess { image ->
                mc.addScheduledTask {
                    runCatching {
                        val texture = DynamicTexture(image)
                        val location = mc.textureManager.getDynamicTextureLocation(
                            "spotify/" + UUID.randomUUID(),
                            texture,
                        )
                        coverCache[url] = location
                    }.onFailure {
                        LOGGER.warn("[Spotify][GUI] Failed to upload cover art from $url", it)
                    }
                    coverLoading.remove(url)
                }
            }.onFailure {
                LOGGER.warn("[Spotify][GUI] Failed to load cover art from $url", it)
                mc.addScheduledTask { coverLoading.remove(url) }
            }
        }
    }

    private fun handlePlaylistClick(mouseY: Int) {
        val area = playlistArea()
        val relativeY = mouseY - area.top + playlistScroll
        if (relativeY < 0) {
            return
        }
        val index = (relativeY / PLAYLIST_ROW_HEIGHT).toInt()
        if (index in playlists.indices) {
            val playlist = playlists[index]
            if (playlist.id != selectedPlaylist?.id) {
                selectedPlaylist = playlist
                selectedTrackIndex = -1
                trackScroll = 0f
                tracksError = null
                loadTracksFor(playlist, forceReload = false)
            }
        }
    }

    private fun handleTrackClick(mouseX: Int, mouseY: Int) {
        if (filteredTracks.isEmpty()) {
            return
        }
        val area = trackArea()
        val relativeY = mouseY - area.top + trackScroll
        if (relativeY < 0) {
            return
        }
        val index = (relativeY / TRACK_ROW_HEIGHT).toInt()
        if (index !in filteredTracks.indices) {
            return
        }
        val rowTop = (area.top + 4 - trackScroll + index * TRACK_ROW_HEIGHT).toInt()
        val likeLeft = area.right - (LIKE_ICON_SIZE + 8)
        val likeRight = likeLeft + LIKE_ICON_SIZE + 4
        val likeTop = rowTop + ((TRACK_ROW_HEIGHT - LIKE_ICON_SIZE) / 2f).toInt()
        val likeBottom = likeTop + LIKE_ICON_SIZE + 2
        if (mouseX in likeLeft..likeRight && mouseY in likeTop..likeBottom) {
            toggleTrackSave(filteredTracks[index])
            return
        }
        if (index == lastTrackClickIndex && System.currentTimeMillis() - lastTrackClickTime < 300L) {
            playTrack(filteredTracks[index])
        } else {
            selectedTrackIndex = index
            lastTrackClickIndex = index
            lastTrackClickTime = System.currentTimeMillis()
        }
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            BUTTON_HOME -> {
                listening = false
                mc.displayGuiScreen(GuiSpotify(this))
            }
            BUTTON_BACK -> {
                listening = false
                mc.displayGuiScreen(prevScreen)
            }
            BUTTON_REFRESH -> reloadPlaylists(force = true)
            BUTTON_PLAY_PAUSE -> togglePlayback()
            BUTTON_PREVIOUS -> skipTrack(previous = true)
            BUTTON_NEXT -> skipTrack(previous = false)
            BUTTON_SHUFFLE -> toggleShuffle()
            BUTTON_REPEAT -> cycleRepeatMode()
        }
    }

    private fun reloadPlaylists(force: Boolean) {
        playlistsLoading = true
        playlistError = null
        playlistScroll = 0f
        if (force) {
            trackSavedState.clear()
        }
        screenScope.launch {
            val token = SpotifyModule.acquireAccessToken(forceRefresh = force)
            if (token == null) {
                playlistError = "Link your Spotify account and authorize the module."
                playlistsLoading = false
                return@launch
            }
            val result = runCatching {
                SpotifyIntegration.service.fetchUserPlaylists(token.value)
            }
            val likedInfo = runCatching { SpotifyIntegration.service.fetchSavedTracks(token.value, 1, 0) }.getOrNull()
            result.onSuccess { loaded ->
                val likedEntry = SpotifyPlaylistSummary(
                    id = LIKED_SONGS_ID,
                    name = "Liked Songs",
                    description = "Your saved tracks",
                    owner = mc.session?.username,
                    trackCount = likedInfo?.total ?: 0,
                    imageUrl = null,
                    uri = null,
                    isLikedSongs = true,
                )
                playlists = listOf(likedEntry) + loaded
                if (selectedPlaylist == null || force) {
                    selectedPlaylist = playlists.firstOrNull()
                    selectedTrackIndex = -1
                    trackScroll = 0f
                }
                playlistsLoading = false
                val current = selectedPlaylist
                if (current != null) {
                    loadTracksFor(current, forceReload = force)
                }
            }.onFailure {
                LOGGER.warn("[Spotify][GUI] Failed to load playlists", it)
                playlistError = it.message ?: "Unable to load playlists"
                playlists = emptyList()
                playlistsLoading = false
            }
        }
    }

    private fun loadTracksFor(playlist: SpotifyPlaylistSummary, forceReload: Boolean) {
        val cacheKey = playlist.id
        val cached = trackCache[cacheKey]
        if (cached != null && !forceReload) {
            displayedTracks = cached.tracks
            updateTrackFilters()
            return
        }
        tracksLoading = true
        tracksError = null
        displayedTracks = emptyList()
        filteredTracks = emptyList()
        screenScope.launch {
            val token = SpotifyModule.acquireAccessToken(forceRefresh = forceReload)
            if (token == null) {
                tracksError = "Missing Spotify credentials."
                tracksLoading = false
                return@launch
            }
            val pageResult = runCatching {
                if (playlist.isLikedSongs) {
                    SpotifyIntegration.service.fetchSavedTracks(token.value, SAVED_TRACK_LIMIT, 0)
                } else {
                    SpotifyIntegration.service.fetchPlaylistTracks(token.value, playlist.id, PLAYLIST_TRACK_LIMIT, 0)
                }
            }
            val page = pageResult.getOrElse {
                LOGGER.warn("[Spotify][GUI] Failed to load tracks", it)
                tracksError = it.message ?: "Unable to load tracks"
                displayedTracks = emptyList()
                filteredTracks = emptyList()
                tracksLoading = false
                return@launch
            }
            trackCache[cacheKey] = page
            displayedTracks = page.tracks
            if (playlist.isLikedSongs) {
                page.tracks.forEach { trackSavedState[it.id] = true }
            } else if (page.tracks.isNotEmpty()) {
                val savedStates = runCatching {
                    SpotifyIntegration.service.fetchSavedStatuses(token.value, page.tracks.map { it.id })
                }.onFailure {
                    LOGGER.warn("[Spotify][GUI] Failed to resolve saved track states", it)
                }.getOrNull()
                savedStates?.forEach { (id, saved) -> trackSavedState[id] = saved }
            }
            updateTrackFilters()
            tracksLoading = false
        }
    }

    private fun togglePlayback() {
        screenScope.launch {
            val token = SpotifyModule.acquireAccessToken()
            if (token == null) {
                showBanner("Authorize Spotify before controlling playback")
                return@launch
            }
            val playing = playbackState?.isPlaying == true
            val result = runCatching {
                if (playing) {
                    SpotifyIntegration.service.pausePlayback(token.value)
                } else {
                    val playlist = selectedPlaylist
                    val selectedTrack = filteredTracks.getOrNull(selectedTrackIndex)
                    if (playlist != null && !playlist.uri.isNullOrBlank()) {
                        val offsetUri = selectedTrack?.let { buildTrackUri(it.id) }
                        SpotifyIntegration.service.startPlayback(token.value, contextUri = playlist.uri, offsetUri = offsetUri)
                    } else if (selectedTrack != null) {
                        SpotifyIntegration.service.startPlayback(token.value, trackUri = buildTrackUri(selectedTrack.id))
                    } else {
                        SpotifyIntegration.service.startPlayback(token.value)
                    }
                }
            }
            result.onSuccess {
                SpotifyModule.requestPlaybackRefresh()
                showBanner(if (playing) "Paused playback" else "Started playback")
            }.onFailure {
                showBanner(it.message ?: "Failed to control playback")
            }
        }
    }

    private fun skipTrack(previous: Boolean) {
        screenScope.launch {
            val token = SpotifyModule.acquireAccessToken()
            if (token == null) {
                showBanner("Authorize Spotify before controlling playback")
                return@launch
            }
            val result = runCatching {
                if (previous) {
                    SpotifyIntegration.service.skipToPrevious(token.value)
                } else {
                    SpotifyIntegration.service.skipToNext(token.value)
                }
            }
            result.onSuccess {
                SpotifyModule.requestPlaybackRefresh()
                showBanner(if (previous) "Previous track" else "Next track")
            }.onFailure {
                showBanner(it.message ?: "Failed to change track")
            }
        }
    }

    private fun toggleShuffle() {
        screenScope.launch {
            val token = SpotifyModule.acquireAccessToken()
            if (token == null) {
                showBanner("Authorize Spotify before controlling playback")
                return@launch
            }
            val newState = !shuffleEnabled
            val result = runCatching { SpotifyIntegration.service.setShuffleState(token.value, newState) }
            result.onSuccess {
                shuffleEnabled = newState
                SpotifyModule.requestPlaybackRefresh()
                showBanner(if (newState) "Shuffle enabled" else "Shuffle disabled")
            }.onFailure {
                showBanner(it.message ?: "Failed to toggle shuffle")
            }
        }
    }

    private fun cycleRepeatMode() {
        val nextMode = when (repeatMode) {
            SpotifyRepeatMode.OFF -> SpotifyRepeatMode.ALL
            SpotifyRepeatMode.ALL -> SpotifyRepeatMode.ONE
            SpotifyRepeatMode.ONE -> SpotifyRepeatMode.OFF
        }
        screenScope.launch {
            val token = SpotifyModule.acquireAccessToken()
            if (token == null) {
                showBanner("Authorize Spotify before controlling playback")
                return@launch
            }
            val result = runCatching { SpotifyIntegration.service.setRepeatMode(token.value, nextMode) }
            result.onSuccess {
                repeatMode = nextMode
                SpotifyModule.requestPlaybackRefresh()
                val message = when (nextMode) {
                    SpotifyRepeatMode.ALL -> "Repeat all enabled"
                    SpotifyRepeatMode.ONE -> "Repeat track enabled"
                    SpotifyRepeatMode.OFF -> "Repeat disabled"
                }
                showBanner(message)
            }.onFailure {
                showBanner(it.message ?: "Failed to toggle repeat")
            }
        }
    }

    private fun playTrack(track: SpotifyTrack) {
        selectedTrackIndex = filteredTracks.indexOfFirst { it.id == track.id }
        screenScope.launch {
            val token = SpotifyModule.acquireAccessToken()
            if (token == null) {
                showBanner("Authorize Spotify before controlling playback")
                return@launch
            }
            val playlist = selectedPlaylist
            val result = runCatching {
                if (playlist != null && !playlist.uri.isNullOrBlank()) {
                    SpotifyIntegration.service.startPlayback(token.value, contextUri = playlist.uri, offsetUri = buildTrackUri(track.id))
                } else {
                    SpotifyIntegration.service.startPlayback(token.value, trackUri = buildTrackUri(track.id))
                }
            }
            result.onSuccess {
                SpotifyModule.requestPlaybackRefresh()
                showBanner("Playing ${track.title}")
            }.onFailure {
                showBanner(it.message ?: "Failed to start track")
            }
        }
    }

    private fun toggleTrackSave(track: SpotifyTrack) {
        val currentlySaved = isTrackSaved(track)
        screenScope.launch {
            val token = SpotifyModule.acquireAccessToken()
            if (token == null) {
                showBanner("Authorize Spotify before controlling playback")
                return@launch
            }
            val result = runCatching {
                SpotifyIntegration.service.setSavedTracksState(token.value, listOf(track.id), !currentlySaved)
            }
            result.onSuccess {
                trackSavedState[track.id] = !currentlySaved
                if (selectedPlaylist?.isLikedSongs == true && currentlySaved) {
                    loadTracksFor(selectedPlaylist!!, forceReload = true)
                } else {
                    updateTrackFilters()
                }
                showBanner(
                    if (!currentlySaved) "Added ${track.title} to Liked Songs" else "Removed ${track.title} from Liked Songs",
                )
            }.onFailure {
                showBanner(it.message ?: "Failed to update Liked Songs")
            }
        }
    }

    private fun updateSearchQuery(query: String) {
        searchQuery = query
        updateTrackFilters()
    }

    private fun updateTrackFilters() {
        val query = searchQuery.trim().lowercase()
        val source = trackCache[selectedPlaylist?.id]?.tracks ?: displayedTracks
        filteredTracks = if (query.isBlank()) {
            source
        } else {
            source.filter { track ->
                track.title.lowercase().contains(query) || track.artists.lowercase().contains(query)
            }
        }
        if (selectedTrackIndex !in filteredTracks.indices) {
            selectedTrackIndex = -1
        }
        adjustTrackScroll(0f)
    }

    private fun showBanner(message: String) {
        bannerMessage = message
        bannerExpiry = System.currentTimeMillis() + 3500
    }

    private fun playlistArea(): PanelArea {
        val left = 20
        val top = 60
        val right = left + width / 4
        val bottom = height - 110
        return PanelArea(left, top, right, bottom)
    }

    private fun trackArea(): PanelArea {
        val playlistRight = playlistArea().right
        val left = playlistRight + 16
        val top = 60
        val right = width - 20
        val bottom = height - 110
        return PanelArea(left, top, right, bottom)
    }

    private fun formatDuration(durationMs: Int): String {
        val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    private fun isTrackSaved(track: SpotifyTrack): Boolean {
        return trackSavedState[track.id] ?: (selectedPlaylist?.isLikedSongs == true)
    }

    private fun resolvePlayPauseIcon(): ResourceLocation = if (playbackState?.isPlaying == true) iconPause else iconPlay

    private fun resolveShuffleIcon(): ResourceLocation = if (shuffleEnabled) iconShuffleOn else iconShuffleOff

    private fun resolveRepeatIcon(): ResourceLocation = when (repeatMode) {
        SpotifyRepeatMode.ALL -> iconRepeatAll
        SpotifyRepeatMode.ONE -> iconRepeatOne
        SpotifyRepeatMode.OFF -> iconRepeatOff
    }

    private fun trimToWidth(text: String, width: Int): String {
        if (mc.fontRendererObj.getStringWidth(text) <= width) {
            return text
        }
        var trimmed = text
        while (trimmed.isNotEmpty() && mc.fontRendererObj.getStringWidth("$trimmed...") > width) {
            trimmed = trimmed.dropLast(1)
        }
        return if (trimmed.isEmpty()) text else "$trimmed..."
    }

    private fun drawWrappedText(text: String, area: PanelArea, color: Int) {
        val lines = mc.fontRendererObj.listFormattedStringToWidth(text, area.width() - 12)
        var y = area.top + 10
        for (line in lines) {
            if (y > area.bottom - 8) {
                break
            }
            mc.fontRendererObj.drawString(line, area.left + 6, y, color)
            y += 10
        }
    }

    private fun buildTrackUri(id: String): String = if (id.startsWith("spotify:")) id else "spotify:track:$id"

    private fun ensureSuccess(response: Response) {
        if (!response.isSuccessful) {
            throw IOException("HTTP ${response.code} while loading cover art")
        }
    }

    data class PanelArea(val left: Int, val top: Int, val right: Int, val bottom: Int) {
        fun width(): Int = right - left
        fun height(): Int = bottom - top
        fun contains(x: Int, y: Int): Boolean = x in left..right && y in top..bottom
    }

    private fun drawIcon(texture: ResourceLocation, x: Int, y: Int, width: Int, height: Int, alpha: Float = 1f) {
        GlStateManager.enableBlend()
        GlStateManager.color(1f, 1f, 1f, alpha)
        mc.textureManager.bindTexture(texture)
        drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, width, height, width.toFloat(), height.toFloat())
        GlStateManager.color(1f, 1f, 1f, 1f)
        GlStateManager.disableBlend()
    }

    private inner class SpotifyIconButton(
        id: Int,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        private val iconProvider: () -> ResourceLocation,
        private val padding: Int = 3,
    ) : GuiButton(id, x, y, width, height, "") {

        override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
            if (!visible) {
                return
            }
            hovered = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height
            drawRect(xPosition, yPosition, xPosition + width, yPosition + height, 0x44000000)
            val iconX = xPosition + padding
            val iconY = yPosition + padding
            val iconWidth = (width - padding * 2).coerceAtLeast(8)
            val iconHeight = (height - padding * 2).coerceAtLeast(8)
            this@GuiSpotifyPlayer.drawIcon(iconProvider(), iconX, iconY, iconWidth, iconHeight, if (enabled) 1f else 0.4f)
            if (hovered) {
                drawRect(xPosition, yPosition, xPosition + width, yPosition + height, 0x22000000)
            }
        }
    }

    companion object {
        private const val BUTTON_BACK = 600
        private const val BUTTON_REFRESH = 601
        private const val BUTTON_PLAY_PAUSE = 602
        private const val BUTTON_PREVIOUS = 603
        private const val BUTTON_NEXT = 604
        private const val BUTTON_HOME = 605
        private const val BUTTON_SHUFFLE = 606
        private const val BUTTON_REPEAT = 607
        private const val LIKED_SONGS_ID = "liked_songs"
        private const val PLAYLIST_TRACK_LIMIT = 100
        private const val SAVED_TRACK_LIMIT = 50
        private const val PLAYLIST_ROW_HEIGHT = 32f
        private const val TRACK_ROW_HEIGHT = 28f
        private const val LIKE_ICON_SIZE = 16
    }
}