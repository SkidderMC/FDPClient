/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui.multiplayer

import kotlinx.coroutines.launch
import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.client.gui.GuiClientConfiguration
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils
import net.ccbluex.liquidbounce.utils.kotlin.SharedScopes
import net.ccbluex.liquidbounce.utils.render.*
import net.ccbluex.liquidbounce.utils.ui.AbstractScreen
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.multiplayer.GuiConnecting
import net.minecraft.client.multiplayer.ServerAddress
import net.minecraft.client.multiplayer.ServerData
import net.minecraft.client.multiplayer.ServerList
import net.minecraft.network.EnumConnectionState
import net.minecraft.network.NetworkManager
import net.minecraft.network.ServerStatusResponse
import net.minecraft.network.handshake.client.C00Handshake
import net.minecraft.network.status.INetHandlerStatusClient
import net.minecraft.network.status.client.C00PacketServerQuery
import net.minecraft.network.status.client.C01PacketPing
import net.minecraft.network.status.server.S00PacketServerInfo
import net.minecraft.network.status.server.S01PacketPong
import net.minecraft.util.ChatComponentText
import net.minecraft.client.resources.DefaultPlayerSkin
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ChatAllowedCharacters
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.IChatComponent
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Session
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import java.awt.Color
import java.net.InetAddress
import java.net.UnknownHostException
import java.net.URL
import java.net.URLEncoder
import java.util.Base64
import java.util.Collections
import java.util.UUID
import javax.imageio.ImageIO
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom multiplayer / server-list screen sharing the FDPClient main-menu background.
 * Bottom actions use native GuiButtons (identical to the main menu / alt manager); the rest
 * uses the light GL rounded-rect primitive (no shaders) to stay clean and lag-free.
 */
class GuiServerSelect(private val prevGui: GuiScreen) : AbstractScreen() {

    // ServerList must be created in initGui (GuiScreen.mc is null during construction)
    private lateinit var serverList: ServerList
    private val pingManagers = Collections.synchronizedList(ArrayList<PendingPing>())
    private var pingGeneration = 0

    private var searchText = ""
    private var searchFocused = false
    private var onlineOnly = false

    private var scroll = 0f
    private var maxScroll = 0f
    private var selectedIndex = -1

    private val iconCache = HashMap<String, ResourceLocation>()
    private val clientPath = CLIENT_NAME.lowercase()
    private val logo = ResourceLocation("$clientPath/texture/mainmenu/clickgui.png")
    private val unknownServer = ResourceLocation("textures/misc/unknown_server.png")

    private var skinName = ""
    private var skin: ResourceLocation? = null
    private var skinIsHeadTexture = false
    private var skinLoadAttemptName = ""
    private val fades = HashMap<String, Float>()

    private enum class Dialog { NONE, ADD, EDIT, DIRECT }
    private var dialog = Dialog.NONE
    private var dialogAnim = 0f
    private var dialogName = ""
    private var dialogIp = ""
    private var dialogIndex = -1
    private var dialogField = 0

    private val margin = 16f
    private val cardHeight = 54f
    private val cardGap = 8f
    private val barH = 30f
    private val btnH = 20
    private val toggleW = 36f
    private val toggleH = 16f
    private val configButtonW = 62
    private val configButtonH = 20

    private val accent: Color get() = ClientThemesUtils.getColor()
    private val panelBg = Color(0, 0, 0, 120)
    private val textColor = Color(255, 255, 255).rgb
    private val dimColor = Color(195, 198, 205).rgb

    private val barY get() = margin + 50f
    private val listTop get() = barY + barH + 10f
    private val listBottom get() = height - margin - btnH - 12f
    private val cardX get() = margin + cardGap
    private val cardW get() = width - margin * 2f - cardGap * 2f

    private enum class UiIcon { ADD, DIRECT, REFRESH, BACK, SEARCH, SHUFFLE, EDIT, PLAY, DELETE }

    private data class PendingPing(
        val data: ServerData,
        val manager: NetworkManager,
        val generation: Int,
        val startedAt: Long
    )

    private companion object {
        private const val PING_TIMEOUT_MS = 8000L
    }

    override fun initGui() {
        if (!::serverList.isInitialized) serverList = ServerList(mc)
        serverList.loadServerList()

        buttonList.clear()
        +GuiButton(4, configButtonX().toInt(), (barY + (barH - configButtonH) / 2f).toInt(), configButtonW, configButtonH, "Client")

        val by = height - margin.toInt() - btnH
        val bw = 84
        +GuiButton(0, margin.toInt(), by, bw, btnH, "Add")
        +GuiButton(1, margin.toInt() + bw + 6, by, bw, btnH, "Direct")
        +GuiButton(2, margin.toInt() + (bw + 6) * 2, by, bw, btnH, "Refresh")
        +GuiButton(3, width - margin.toInt() - bw, by, bw, btnH, "Back")

        refreshPings()
    }

    override fun actionPerformed(button: GuiButton) {
        if (dialog != Dialog.NONE) return
        when (button.id) {
            0 -> openDialog(Dialog.ADD)
            1 -> openDialog(Dialog.DIRECT)
            2 -> refreshPings()
            3 -> mc.displayGuiScreen(prevGui)
            4 -> mc.displayGuiScreen(GuiClientConfiguration(this))
        }
    }

    // ---------- helpers ----------
    private fun rr(x: Float, y: Float, w: Float, h: Float, radius: Float, color: Color) {
        RenderUtils.drawRoundedRect(x, y, x + w, y + h, radius, color.rgb)
        resetGuiState()
    }

    /**
     * Forces the clean 2D-GUI GL state the custom font renderers require. drawImage/drawHead and the
     * background shader leave depth/alpha/blend/stencil dirty, which garbles every glyph drawn afterwards
     * (the symptom "only correct while a button is hovered" is the hover's stencil pass resetting this state).
     */
    private fun resetGuiState() {
        GL20.glUseProgram(0)
        GL11.glDisable(GL11.GL_STENCIL_TEST)
        GlStateManager.disableDepth()
        GlStateManager.depthMask(true)
        GlStateManager.disableLighting()
        GlStateManager.enableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.enableAlpha()
        GlStateManager.alphaFunc(516, 0.1f)
        GlStateManager.color(1f, 1f, 1f, 1f)
    }

    private fun image(rl: ResourceLocation, x: Float, y: Float, w: Int, h: Int) {
        GlStateManager.color(1f, 1f, 1f, 1f)
        RenderUtils.drawImage(rl, x, y, w, h)
        resetGuiState()
    }

    private fun anim(key: String, active: Boolean, speed: Float = 0.16f): Float {
        var f = fades[key] ?: 0f
        f = (f + (if (active) speed else -speed)).coerceIn(0f, 1f)
        fades[key] = f
        return f
    }

    private fun guiText(text: CharSequence?) = text?.toString()?.replace("\u00C2\u00A7", "\u00A7") ?: ""

    private fun gameTextWidth(font: GameFontRenderer, text: String?) = font.getStringWidth(guiText(text))

    private fun optionToggleX() = margin + (width - margin * 2f) - 12f - toggleW

    private fun optionToggleLabelW() = gameTextWidth(Fonts.fontSemibold35, "Online only").toFloat()

    private fun configButtonX() = optionToggleX() - optionToggleLabelW() - 16f - configButtonW

    private fun drawGameText(font: GameFontRenderer, text: String, x: Float, y: Float, color: Int, shadow: Boolean = false): Int {
        resetGuiState()
        val endX = if (shadow) {
            font.drawStringWithShadow(guiText(text), x, y, color)
        } else {
            font.drawString(guiText(text), x, y, color)
        }
        resetGuiState()
        return endX
    }

    private fun drawUiIcon(icon: UiIcon, x: Float, y: Float, size: Float, color: Int, alphaMul: Float = 1f) {
        resetGuiState()
        val aByte = color ushr 24 and 255
        val alpha = ((if (aByte == 0) 255 else aByte) / 255f * alphaMul).coerceIn(0f, 1f)
        val red = (color ushr 16 and 255) / 255f
        val green = (color ushr 8 and 255) / 255f
        val blue = (color and 255) / 255f

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT or GL11.GL_LINE_BIT or GL11.GL_COLOR_BUFFER_BIT)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glLineWidth((size / 7.5f).coerceIn(1.4f, 2.4f))
        GL11.glColor4f(red, green, blue, alpha)

        fun px(v: Float) = x + size * v
        fun py(v: Float) = y + size * v
        fun line(x1: Float, y1: Float, x2: Float, y2: Float) {
            GL11.glBegin(GL11.GL_LINES)
            GL11.glVertex2f(px(x1), py(y1))
            GL11.glVertex2f(px(x2), py(y2))
            GL11.glEnd()
        }
        fun poly(vararg points: Float) {
            GL11.glBegin(GL11.GL_LINE_STRIP)
            var i = 0
            while (i + 1 < points.size) {
                GL11.glVertex2f(px(points[i]), py(points[i + 1]))
                i += 2
            }
            GL11.glEnd()
        }
        fun arc(cx: Float, cy: Float, radius: Float, start: Int, end: Int) {
            GL11.glBegin(GL11.GL_LINE_STRIP)
            var deg = start
            while (deg <= end) {
                val rad = Math.toRadians(deg.toDouble())
                GL11.glVertex2f(px(cx + cos(rad).toFloat() * radius), py(cy + sin(rad).toFloat() * radius))
                deg += 12
            }
            GL11.glEnd()
        }
        fun triangle(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) {
            GL11.glBegin(GL11.GL_TRIANGLES)
            GL11.glVertex2f(px(x1), py(y1))
            GL11.glVertex2f(px(x2), py(y2))
            GL11.glVertex2f(px(x3), py(y3))
            GL11.glEnd()
        }

        when (icon) {
            UiIcon.ADD -> {
                line(0.5f, 0.18f, 0.5f, 0.82f)
                line(0.18f, 0.5f, 0.82f, 0.5f)
            }
            UiIcon.DIRECT -> {
                poly(0.16f, 0.68f, 0.86f, 0.20f, 0.62f, 0.80f, 0.48f, 0.56f, 0.16f, 0.68f)
                line(0.48f, 0.56f, 0.86f, 0.20f)
            }
            UiIcon.REFRESH -> {
                arc(0.5f, 0.5f, 0.34f, 35, 318)
                line(0.77f, 0.23f, 0.88f, 0.23f)
                line(0.77f, 0.23f, 0.81f, 0.35f)
            }
            UiIcon.BACK -> {
                line(0.18f, 0.5f, 0.84f, 0.5f)
                line(0.18f, 0.5f, 0.42f, 0.25f)
                line(0.18f, 0.5f, 0.42f, 0.75f)
            }
            UiIcon.SEARCH -> {
                arc(0.43f, 0.43f, 0.27f, 0, 360)
                line(0.62f, 0.62f, 0.84f, 0.84f)
            }
            UiIcon.SHUFFLE -> {
                poly(0.14f, 0.35f, 0.36f, 0.35f, 0.64f, 0.64f, 0.84f, 0.64f)
                line(0.73f, 0.52f, 0.84f, 0.64f)
                line(0.73f, 0.76f, 0.84f, 0.64f)
                poly(0.14f, 0.65f, 0.36f, 0.65f, 0.64f, 0.36f, 0.84f, 0.36f)
                line(0.73f, 0.24f, 0.84f, 0.36f)
                line(0.73f, 0.48f, 0.84f, 0.36f)
            }
            UiIcon.EDIT -> {
                poly(0.25f, 0.78f, 0.68f, 0.35f, 0.78f, 0.45f, 0.35f, 0.88f, 0.22f, 0.91f, 0.25f, 0.78f)
                line(0.62f, 0.29f, 0.74f, 0.17f)
                line(0.74f, 0.17f, 0.86f, 0.29f)
                line(0.86f, 0.29f, 0.78f, 0.45f)
            }
            UiIcon.PLAY -> triangle(0.28f, 0.18f, 0.28f, 0.82f, 0.82f, 0.5f)
            UiIcon.DELETE -> {
                line(0.24f, 0.28f, 0.76f, 0.80f)
                line(0.76f, 0.28f, 0.24f, 0.80f)
            }
        }

        GL11.glPopAttrib()
        resetGuiState()
    }

    private fun playerSkin(): ResourceLocation {
        val name = mc.session.username
        if (name != skinName || skin == null) {
            skinName = name
            skinIsHeadTexture = false
            skinLoadAttemptName = ""
            val profile = mc.session.profile
            val fallbackId = profile.id ?: UUID.nameUUIDFromBytes("OfflinePlayer:$name".toByteArray(Charsets.UTF_8))
            skin = DefaultPlayerSkin.getDefaultSkin(fallbackId)
        }
        loadPlayerHead(name)
        return skin ?: DefaultPlayerSkin.getDefaultSkin(UUID.nameUUIDFromBytes("OfflinePlayer:$name".toByteArray(Charsets.UTF_8)))
    }

    private fun loadPlayerHead(name: String) {
        if (skinLoadAttemptName == name || name.isBlank()) return
        skinLoadAttemptName = name
        Thread({
            try {
                val encodedName = URLEncoder.encode(name, "UTF-8")
                val image = URL("https://mc-heads.net/avatar/$encodedName/64").openStream().use { ImageIO.read(it) } ?: return@Thread
                val resourceName = name.replace(Regex("[^a-zA-Z0-9_.-]"), "_").lowercase()
                val rl = ResourceLocation("fdpplayerhead/$resourceName")
                mc.addScheduledTask {
                    mc.textureManager.loadTexture(rl, DynamicTexture(image))
                    if (skinName == name) {
                        skin = rl
                        skinIsHeadTexture = true
                    }
                }
            } catch (ignored: Exception) {
            }
        }, "FDP Player Head Loader").apply { isDaemon = true }.start()
    }

    private fun refreshPings() {
        clearPendingPings()
        serverList.loadServerList()
        selectedIndex = selectedIndex.coerceAtMost(serverList.countServers() - 1)
        iconCache.clear()
        val generation = ++pingGeneration

        // One daemon thread per server (vanilla GuiMultiplayer pattern). The generation guard keeps a
        // late failure from an older refresh from overwriting the current ping pass.
        for (i in 0 until serverList.countServers()) {
            val data = serverList.getServerData(i)
            data.pingToServer = -2L
            data.serverMOTD = "${EnumChatFormatting.GRAY}Pinging..."
            data.populationInfo = ""
            SharedScopes.IO.launch {
                try {
                    pingServer(data, generation)
                } catch (e: Exception) {
                    if (generation == pingGeneration) {
                        data.pingToServer = -1L
                        data.serverMOTD = "${EnumChatFormatting.RED}Can't connect to server"
                    }
                }
            }
        }
    }

    private fun pingServer(data: ServerData, generation: Int) {
        val address = ServerAddress.fromString(data.serverIP) ?: throw UnknownHostException(data.serverIP ?: "")
        val networkManager = NetworkManager.createNetworkManagerAndConnect(
            InetAddress.getByName(address.ip),
            address.port,
            false
        )
        data.serverMOTD = "${EnumChatFormatting.GRAY}Pinging..."
        data.pingToServer = -2L
        data.playerList = null
        pingManagers += PendingPing(data, networkManager, generation, Minecraft.getSystemTime())

        networkManager.netHandler = object : INetHandlerStatusClient {
            private var receivedStatus = false
            private var sentPing = false
            private var pingSentAt = 0L

            override fun handleServerInfo(packetIn: S00PacketServerInfo) {
                if (generation != pingGeneration) {
                    networkManager.closeChannel(ChatComponentText("Stale ping"))
                    return
                }
                if (receivedStatus) {
                    networkManager.closeChannel(ChatComponentText("Received unrequested status"))
                    return
                }

                receivedStatus = true
                applyServerStatus(data, packetIn.response)
                pingSentAt = Minecraft.getSystemTime()
                networkManager.sendPacket(C01PacketPing(pingSentAt))
                sentPing = true
            }

            override fun handlePong(packetIn: S01PacketPong) {
                if (generation == pingGeneration) data.pingToServer = Minecraft.getSystemTime() - pingSentAt
                networkManager.closeChannel(ChatComponentText("Finished"))
            }

            override fun onDisconnect(reason: IChatComponent) {
                if (generation != pingGeneration || sentPing) return
                data.pingToServer = -1L
                data.serverMOTD = "${EnumChatFormatting.DARK_RED}Can't connect to server."
                data.populationInfo = ""
            }
        }

        networkManager.sendPacket(C00Handshake(47, address.ip, address.port, EnumConnectionState.STATUS))
        networkManager.sendPacket(C00PacketServerQuery())
    }

    private fun applyServerStatus(data: ServerData, response: ServerStatusResponse?) {
        if (response == null) {
            data.serverMOTD = ""
            data.populationInfo = "${EnumChatFormatting.DARK_GRAY}???"
            return
        }

        data.serverMOTD = response.serverDescription?.formattedText ?: ""

        response.protocolVersionInfo?.let {
            data.gameVersion = it.name
            data.version = it.protocol
        } ?: run {
            data.gameVersion = "Old"
            data.version = 0
        }

        response.playerCountData?.let { players ->
            data.populationInfo = "${EnumChatFormatting.GRAY}${players.onlinePlayerCount}${EnumChatFormatting.DARK_GRAY}/${EnumChatFormatting.GRAY}${players.maxPlayers}"
            val sampleNames = players.players
                ?.mapNotNull { it?.name }
                ?.filter { it.isNotBlank() }
                .orEmpty()

            if (sampleNames.isNotEmpty()) {
                val list = StringBuilder(sampleNames.joinToString("\n"))
                if (sampleNames.size < players.onlinePlayerCount) {
                    if (list.isNotEmpty()) list.append('\n')
                    list.append("... and ").append(players.onlinePlayerCount - sampleNames.size).append(" more ...")
                }
                data.playerList = list.toString()
            }
        } ?: run {
            data.populationInfo = "${EnumChatFormatting.DARK_GRAY}???"
        }

        response.favicon?.let {
            if (it.startsWith("data:image/png;base64,")) {
                data.base64EncodedIconData = it.substring("data:image/png;base64,".length)
                iconCache.remove(data.serverIP)
            }
        } ?: data.setBase64EncodedIconData(null)
    }

    private fun processPendingPings() {
        val now = Minecraft.getSystemTime()
        synchronized(pingManagers) {
            val iterator = pingManagers.iterator()
            while (iterator.hasNext()) {
                val pending = iterator.next()
                val manager = pending.manager
                if (pending.generation != pingGeneration) {
                    iterator.remove()
                    if (manager.isChannelOpen) manager.closeChannel(ChatComponentText("Stale ping"))
                    continue
                }
                if (manager.isChannelOpen) {
                    if (now - pending.startedAt > PING_TIMEOUT_MS) {
                        pending.data.pingToServer = -1L
                        pending.data.serverMOTD = "${EnumChatFormatting.DARK_RED}Can't connect to server."
                        pending.data.populationInfo = ""
                        iterator.remove()
                        manager.closeChannel(ChatComponentText("Timed out"))
                    } else {
                        manager.processReceivedPackets()
                    }
                } else {
                    iterator.remove()
                    manager.checkDisconnected()
                }
            }
        }
    }

    private fun clearPendingPings() {
        val copy = synchronized(pingManagers) {
            pingManagers.toTypedArray().also {
                pingManagers.clear()
            }
        }
        for (it in copy) {
            val manager = it.manager
            if (manager.isChannelOpen) {
                manager.closeChannel(ChatComponentText("Cancelled"))
            }
        }
    }

    private fun filteredIndices(): List<Int> {
        val q = searchText.lowercase()
        val result = ArrayList<Int>()
        for (i in 0 until serverList.countServers()) {
            val data = serverList.getServerData(i)
            if (onlineOnly && data.pingToServer < 0L) continue
            if (q.isNotEmpty()) {
                if (!(data.serverName ?: "").lowercase().contains(q) && !(data.serverIP ?: "").lowercase().contains(q)) continue
            }
            result.add(i)
        }
        return result
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        processPendingPings()

        assumeNonVolatile = true
        drawBackground(0)
        resetGuiState()

        val open = dialog != Dialog.NONE
        val mx = if (open) -1 else mouseX
        val my = if (open) -1 else mouseY

        // logo
        image(logo, margin, margin, 44, 44)

        drawAccountCard(mx, my)

        // option bar
        val barX = margin
        val barW = width - margin * 2f
        rr(barX, barY, barW, barH, 5f, panelBg)

        val toggleLabel = "Online only"
        val toggleLabelW = optionToggleLabelW()
        val toggleX = optionToggleX()
        drawGameText(Fonts.fontSemibold35, toggleLabel, toggleX - 8f - toggleLabelW, barY + barH / 2f - 4f, textColor, shadow = true)
        drawToggle(toggleX, barY + (barH - toggleH) / 2f, toggleW, toggleH, onlineOnly)

        val searchX = barX + 12f
        drawUiIcon(UiIcon.SEARCH, searchX, barY + (barH - 12f) / 2f, 12f, accent.rgb)
        if (searchText.isEmpty() && !searchFocused) {
            drawGameText(Fonts.fontSemibold35, "Search...", searchX + 20f, barY + barH / 2f - 4f, dimColor, shadow = true)
        } else {
            val caret = if (searchFocused && (System.currentTimeMillis() / 500) % 2 == 0L) "_" else ""
            drawGameText(Fonts.fontSemibold35, searchText + caret, searchX + 20f, barY + barH / 2f - 4f, textColor, shadow = true)
        }

        // list
        val listH = listBottom - listTop
        rr(margin, listTop, width - margin * 2f, listH, 5f, panelBg)

        val indices = filteredIndices()
        val contentHeight = indices.size * (cardHeight + cardGap)
        maxScroll = (contentHeight - listH + cardGap).coerceAtLeast(0f)
        scroll = scroll.coerceIn(0f, maxScroll)

        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        RenderHelper.scissor(margin.toDouble(), listTop.toDouble(), (width - margin * 2f).toDouble(), listH.toDouble())
        var cardY = listTop + cardGap - scroll
        if (indices.isEmpty()) {
            val msg = if (serverList.countServers() == 0) "No servers added yet - click Add or Direct" else "No servers match your filter"
            drawGameText(Fonts.fontSemibold35, msg, margin + (width - margin * 2f - gameTextWidth(Fonts.fontSemibold35, msg)) / 2f, listTop + listH / 2f - 6f, dimColor)
        }
        for (idx in indices) {
            if (cardY + cardHeight >= listTop && cardY <= listBottom) {
                val hovered = !open && RenderHelper.isHovering(cardX, cardY, cardW, cardHeight, mx, my) && my >= listTop && my <= listBottom
                drawServerCard(serverList.getServerData(idx), idx, cardX, cardY, cardW, hovered, mx, my)
            }
            cardY += cardHeight + cardGap
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST)
        resetGuiState()

        if (maxScroll > 0f) {
            val sbH = (listH * (listH / contentHeight)).coerceAtLeast(20f)
            val sbY = listTop + (scroll / maxScroll) * (listH - sbH)
            rr(width - margin - 3f, sbY, 3f, sbH, 1.5f, Color(255, 255, 255, 90))
        }

        // native bottom buttons (drawn by super) - hidden under the modal backdrop when open
        buttonList.forEach { it.enabled = !open }
        resetGuiState()
        super.drawScreen(mouseX, mouseY, partialTicks)
        resetGuiState()

        // icon next to each native button label (grouped left of the centered text)
        val labelFont = Fonts.fontSemibold35
        for (b in buttonList) {
            val icon = when (b.id) {
                0 -> UiIcon.ADD
                1 -> UiIcon.DIRECT
                2 -> UiIcon.REFRESH
                3 -> UiIcon.BACK
                else -> null
            } ?: continue
            val labelW = gameTextWidth(labelFont, b.displayString)
            val iconSize = if (b.id == 2) 10f else 11f
            val gap = if (b.id == 2) 14f else 8f
            val iconX = b.xPosition + b.width / 2f - labelW / 2f - iconSize - gap
            drawUiIcon(icon, iconX, b.yPosition + b.height / 2f - iconSize / 2f, iconSize, 0xE0E0E0)
        }

        // modal on top of everything
        dialogAnim = (dialogAnim + (if (open) 0.18f else -0.22f)).coerceIn(0f, 1f)
        if (dialogAnim > 0.001f) drawDialog(mouseX, mouseY)

        GlStateManager.resetColor()
        assumeNonVolatile = false
    }

    private fun drawServerCard(data: ServerData, index: Int, x: Float, y: Float, w: Float, hovered: Boolean, mouseX: Int, mouseY: Int) {
        val f = anim("card$index", hovered)
        val a = (20 + 18 * f).toInt() + (if (index == selectedIndex) 14 else 0)
        rr(x, y, w, cardHeight, 6f, Color(255, 255, 255, a.coerceAtMost(255)))
        if (index == selectedIndex) rr(x, y, 3f, cardHeight, 1.5f, accent)

        val iconSize = 40f
        val iconX = x + 9f
        val iconY = y + (cardHeight - iconSize) / 2f
        image(resolveIcon(data), iconX, iconY, iconSize.toInt(), iconSize.toInt())

        val ping = data.pingToServer
        if (ping >= 0L) {
            val pingText = "${ping}ms"
            val pingW = gameTextWidth(Fonts.fontSemibold35, pingText) + 8f
            val pc = when {
                ping < 80L -> Color(45, 204, 112); ping < 200L -> Color(241, 196, 15); else -> Color(232, 76, 61)
            }
            val pingY = iconY - 5f
            rr(iconX + iconSize - pingW + 2f, pingY, pingW, 16f, 8f, pc)
            drawGameText(Fonts.fontSemibold35, pingText, iconX + iconSize - pingW + 6f, pingY + 5f, Color.WHITE.rgb, shadow = true)
        }

        val textX = iconX + iconSize + 12f
        val name = data.serverName ?: "Server"
        drawGameText(Fonts.fontSemibold35, name, textX, y + 9f, textColor)
        var tagX = textX + gameTextWidth(Fonts.fontSemibold35, name) + 8f
        if (ping >= 0L) {
            val players = stripColor(data.populationInfo ?: ""); if (players.isNotBlank()) tagX = drawTag(players, tagX, y + 9f)
            val ver = stripColor(data.gameVersion ?: ""); if (ver.isNotBlank()) drawTag(ver, tagX, y + 9f)
        }
        val motd = stripColor((data.serverMOTD ?: "").split("\n").firstOrNull() ?: "")
        drawGameText(Fonts.fontRegular35, trim(motd, w - (textX - x) - 96f, Fonts.fontRegular35), textX, y + 28f, dimColor)

        if (f > 0.01f) {
            val bs = 22f
            val playX = x + w - bs - 8f
            val editX = playX - bs - 6f
            val delX = editX - bs - 6f
            val byy = y + (cardHeight - bs) / 2f
            cardIcon(UiIcon.PLAY, playX, byy, bs, mouseX, mouseY, accent, f)
            cardIcon(UiIcon.EDIT, editX, byy, bs, mouseX, mouseY, accent, f)
            cardIcon(UiIcon.DELETE, delX, byy, bs, mouseX, mouseY, Color(232, 76, 61), f)
        }
    }

    private fun cardIcon(icon: UiIcon, x: Float, y: Float, size: Float, mouseX: Int, mouseY: Int, hoverColor: Color, f: Float) {
        val hov = RenderHelper.isHovering(x, y, size, size, mouseX, mouseY)
        rr(x, y, size, size, 5f, mixAlpha(if (hov) hoverColor else Color(255, 255, 255, 28), f))
        val iconSize = size * 0.52f
        drawUiIcon(icon, x + (size - iconSize) / 2f, y + (size - iconSize) / 2f, iconSize, Color.WHITE.rgb, f)
    }

    private fun mixAlpha(c: Color, f: Float) = Color(c.red, c.green, c.blue, (c.alpha * f).toInt().coerceIn(0, 255))

    private fun mixColor(a: Color, b: Color, f: Float): Color {
        val ff = f.coerceIn(0f, 1f)
        return Color((a.red + (b.red - a.red) * ff).toInt(), (a.green + (b.green - a.green) * ff).toInt(),
            (a.blue + (b.blue - a.blue) * ff).toInt(), (a.alpha + (b.alpha - a.alpha) * ff).toInt())
    }

    private fun drawTag(text: String, x: Float, y: Float): Float {
        val w = gameTextWidth(Fonts.fontSemibold35, text) + 10f
        rr(x, y - 1f, w, 14f, 7f, Color(255, 255, 255, 28))
        drawGameText(Fonts.fontSemibold35, text, x + 5f, y + 3f, textColor)
        return x + w + 6f
    }

    private fun drawToggle(x: Float, y: Float, w: Float, h: Float, on: Boolean) {
        val f = anim("toggle", on, 0.2f)
        rr(x, y, w, h, h / 2f, mixColor(Color(255, 255, 255, 45), accent, f))
        val knob = h - 4f
        rr(x + 2f + (w - knob - 4f) * f, y + 2f, knob, knob, knob / 2f, Color.WHITE)
    }

    private fun drawAccountCard(mouseX: Int, mouseY: Int) {
        val cw = 188f
        val ch = 46f
        val x = width - margin - cw
        val y = margin - 4f
        rr(x, y, cw, ch, 8f, Color(0, 0, 0, 150))

        val avSize = 32f
        val avX = x + 9f
        val avY = y + (ch - avSize) / 2f
        val playerTexture = playerSkin()
        resetGuiState()
        GlStateManager.color(1f, 1f, 1f, 1f)
        if (skinIsHeadTexture) {
            image(playerTexture, avX, avY, avSize.toInt(), avSize.toInt())
        } else {
            RenderUtils.drawHead(playerTexture, avX.toInt(), avY.toInt(), avSize.toInt(), avSize.toInt(), -1)
        }
        resetGuiState()

        val nameX = avX + avSize + 8f
        drawGameText(Fonts.fontSemibold35, mc.session.username, nameX, y + 9f, textColor)
        val premium = (mc.session.token?.length ?: 0) > 5 && !"0".equals(mc.session.token, true)
        drawGameText(Fonts.fontRegular35, if (premium) "Premium" else "Offline", nameX, y + 26f, if (premium) Color(103, 210, 74).rgb else dimColor)

        val isz = 22f
        val penX = x + cw - isz - 8f
        val shufX = penX - isz - 6f
        val iy = y + (ch - isz) / 2f
        rr(shufX, iy, isz, isz, 5f, if (RenderHelper.isHovering(shufX, iy, isz, isz, mouseX, mouseY)) accent else Color(255, 255, 255, 24))
        drawUiIcon(UiIcon.SHUFFLE, shufX + 5f, iy + 5f, 12f, textColor)
        rr(penX, iy, isz, isz, 5f, if (RenderHelper.isHovering(penX, iy, isz, isz, mouseX, mouseY)) accent else Color(255, 255, 255, 24))
        drawUiIcon(UiIcon.EDIT, penX + 5f, iy + 5f, 12f, textColor)
    }

    private fun drawDialog(mouseX: Int, mouseY: Int) {
        val a = dialogAnim
        RenderPrimitives.drawRect(0f, 0f, width.toFloat(), height.toFloat(), Color(0, 0, 0, (150 * a).toInt()).rgb)
        resetGuiState()

        val w = 270f
        val single = dialog == Dialog.DIRECT
        val h = if (single) 112f else 150f
        val cx = width / 2f
        val cy = height / 2f
        GlStateManager.pushMatrix()
        val sc = 0.92f + 0.08f * a
        GlStateManager.translate(cx.toDouble(), cy.toDouble(), 0.0)
        GlStateManager.scale(sc.toDouble(), sc.toDouble(), 1.0)
        GlStateManager.translate(-cx.toDouble(), -cy.toDouble(), 0.0)

        val x = cx - w / 2f
        val y = cy - h / 2f
        rr(x, y, w, h, 8f, Color(20, 21, 26, (248 * a).toInt()))

        val title = when (dialog) {
            Dialog.ADD -> "Add Server"; Dialog.EDIT -> "Edit Server"; Dialog.DIRECT -> "Direct Connect"; else -> ""
        }
        drawGameText(Fonts.fontSemibold35, title, x + 14f, y + 14f, textColor, shadow = true)

        var fy = y + 38f
        if (single) {
            drawField("Server Address", dialogIp, true, x + 14f, fy, w - 28f)
        } else {
            drawField("Name", dialogName, dialogField == 0, x + 14f, fy, w - 28f); fy += 40f
            drawField("Address", dialogIp, dialogField == 1, x + 14f, fy, w - 28f)
        }

        val btnY = y + h - 28f
        val bw = 84f
        val gap = 10f
        val saveX = x + (w - bw * 2f - gap) / 2f
        val saveLabel = if (single) "Connect" else "Save"
        modalButton(saveLabel, saveX, btnY, bw, 20f, true, RenderHelper.isHovering(saveX, btnY, bw, 20f, mouseX, mouseY))
        modalButton("Cancel", saveX + bw + gap, btnY, bw, 20f, false, RenderHelper.isHovering(saveX + bw + gap, btnY, bw, 20f, mouseX, mouseY))
        GlStateManager.popMatrix()
        resetGuiState()
    }

    private fun modalButton(label: String, x: Float, y: Float, w: Float, h: Float, primary: Boolean, hovered: Boolean) {
        val base = if (primary) Color(accent.red, accent.green, accent.blue, 210) else Color(0, 0, 0, 120)
        rr(x, y, w, h, 2.5f, if (hovered) Color(base.red, base.green, base.blue, (base.alpha + 35).coerceAtMost(255)) else base)
        val f = Fonts.fontSemibold35
        drawGameText(f, label, x + (w - gameTextWidth(f, label)) / 2f, y + h / 2f - 4f, 0xE0E0E0, shadow = true)
    }

    private fun drawField(label: String, value: String, focused: Boolean, x: Float, y: Float, w: Float) {
        drawGameText(Fonts.fontRegular35, label, x, y, dimColor, shadow = true)
        rr(x, y + 12f, w, 22f, 5f, Color(255, 255, 255, if (focused) 28 else 18))
        val caret = if (focused && (System.currentTimeMillis() / 500) % 2 == 0L) "_" else ""
        drawGameText(Fonts.fontSemibold35, trim(value, w - 16f, Fonts.fontSemibold35) + caret, x + 8f, y + 19f, textColor, shadow = true)
    }

    private fun resolveIcon(data: ServerData): ResourceLocation {
        val ip = data.serverIP ?: return unknownServer
        iconCache[ip]?.let { return it }
        val base64 = data.base64EncodedIconData ?: return unknownServer
        return try {
            val bytes = Base64.getDecoder().decode(base64.substringAfterLast(','))
            val img = bytes.inputStream().use { ImageIO.read(it) } ?: return unknownServer
            val rl = ResourceLocation("fdpservericon/" + ip.replace(Regex("[^a-z0-9_.-]"), "_").lowercase())
            mc.textureManager.loadTexture(rl, DynamicTexture(img))
            iconCache[ip] = rl
            rl
        } catch (e: Exception) {
            unknownServer
        }
    }

    private fun stripColor(text: String) = guiText(text).replace(Regex("(?i)\\u00A7[0-9A-FK-OR]"), "").trim()

    private fun trim(text: String, maxWidth: Float, font: GameFontRenderer): String {
        val normalized = guiText(text)
        if (gameTextWidth(font, normalized) <= maxWidth) return normalized
        var r = normalized
        while (r.isNotEmpty() && gameTextWidth(font, "$r...") > maxWidth) r = r.dropLast(1)
        return "$r..."
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (dialog != Dialog.NONE) {
            handleDialogClick(mouseX, mouseY)
            return
        }

        val searchX = margin + 12f
        val searchW = (configButtonX() - searchX - 8f).coerceAtLeast(40f)
        searchFocused = RenderHelper.isHovering(searchX, barY, searchW.coerceAtLeast(40f), barH, mouseX, mouseY)

        val toggleLabelW = optionToggleLabelW()
        val toggleX = optionToggleX()
        if (RenderHelper.isHovering(toggleX - toggleLabelW - 8f, barY, toggleW + toggleLabelW + 8f, barH, mouseX, mouseY)) {
            onlineOnly = !onlineOnly
            return
        }

        // account icons / card
        val cw = 188f
        val accX = width - margin - cw
        val accY = margin - 4f
        val accH = 46f
        val isz = 22f
        val penX = accX + cw - isz - 8f
        val shufX = penX - isz - 6f
        val iy = accY + (accH - isz) / 2f
        if (RenderHelper.isHovering(shufX, iy, isz, isz, mouseX, mouseY)) { loginRandom(); return }
        if (RenderHelper.isHovering(penX, iy, isz, isz, mouseX, mouseY)) { mc.displayGuiScreen(GuiAltManager(this)); return }
        if (RenderHelper.isHovering(accX, accY, cw - 56f, accH, mouseX, mouseY)) { mc.displayGuiScreen(GuiAltManager(this)); return }

        // cards
        if (mouseY.toFloat() in listTop..listBottom) {
            var cardY = listTop + cardGap - scroll
            for (idx in filteredIndices()) {
                if (RenderHelper.isHovering(cardX, cardY, cardW, cardHeight, mouseX, mouseY)) {
                    val bs = 22f
                    val playX = cardX + cardW - bs - 8f
                    val editX = playX - bs - 6f
                    val delX = editX - bs - 6f
                    val byy = cardY + (cardHeight - bs) / 2f
                    when {
                        mouseButton == 0 && RenderHelper.isHovering(playX, byy, bs, bs, mouseX, mouseY) -> connect(serverList.getServerData(idx))
                        mouseButton == 0 && RenderHelper.isHovering(editX, byy, bs, bs, mouseX, mouseY) -> openDialog(Dialog.EDIT, idx)
                        mouseButton == 0 && RenderHelper.isHovering(delX, byy, bs, bs, mouseX, mouseY) -> deleteServer(idx)
                        mouseButton == 1 -> openDialog(Dialog.EDIT, idx)
                        mouseButton == 0 -> if (selectedIndex == idx) connect(serverList.getServerData(idx)) else selectedIndex = idx
                    }
                    return
                }
                cardY += cardHeight + cardGap
            }
        }

        // native buttons
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    private fun handleDialogClick(mouseX: Int, mouseY: Int) {
        val w = 270f
        val single = dialog == Dialog.DIRECT
        val h = if (single) 112f else 150f
        val x = (width - w) / 2f
        val y = (height - h) / 2f
        var fy = y + 38f
        if (!single) {
            if (RenderHelper.isHovering(x + 14f, fy + 12f, w - 28f, 22f, mouseX, mouseY)) { dialogField = 0; return }
            fy += 40f
            if (RenderHelper.isHovering(x + 14f, fy + 12f, w - 28f, 22f, mouseX, mouseY)) { dialogField = 1; return }
        } else if (RenderHelper.isHovering(x + 14f, fy + 12f, w - 28f, 22f, mouseX, mouseY)) { dialogField = 1; return }

        val btnY = y + h - 28f
        val bw = 84f
        val gap = 10f
        val saveX = x + (w - bw * 2f - gap) / 2f
        if (RenderHelper.isHovering(saveX, btnY, bw, 20f, mouseX, mouseY)) { confirmDialog(); return }
        if (RenderHelper.isHovering(saveX + bw + gap, btnY, bw, 20f, mouseX, mouseY)) { dialog = Dialog.NONE; return }
        if (!RenderHelper.isHovering(x, y, w, h, mouseX, mouseY)) dialog = Dialog.NONE
    }

    private fun openDialog(mode: Dialog, index: Int = -1) {
        dialog = mode
        dialogIndex = index
        dialogField = if (mode == Dialog.DIRECT) 1 else 0
        searchFocused = false
        when (mode) {
            Dialog.ADD -> { dialogName = "New Server"; dialogIp = "" }
            Dialog.DIRECT -> { dialogName = ""; dialogIp = "" }
            Dialog.EDIT -> { val d = serverList.getServerData(index); dialogName = d.serverName ?: ""; dialogIp = d.serverIP ?: "" }
            else -> {}
        }
    }

    private fun confirmDialog() {
        when (dialog) {
            Dialog.ADD -> if (dialogIp.isNotBlank()) {
                serverList.addServerData(ServerData(dialogName.ifBlank { dialogIp }, dialogIp, false)); serverList.saveServerList(); dialog = Dialog.NONE; refreshPings()
            }
            Dialog.EDIT -> if (dialogIndex in 0 until serverList.countServers()) {
                val d = serverList.getServerData(dialogIndex); d.serverName = dialogName.ifBlank { dialogIp }; d.serverIP = dialogIp; serverList.saveServerList(); dialog = Dialog.NONE; refreshPings()
            }
            Dialog.DIRECT -> if (dialogIp.isNotBlank()) { dialog = Dialog.NONE; connect(ServerData(dialogIp, dialogIp, false)) }
            else -> dialog = Dialog.NONE
        }
    }

    /** Random offline login built manually (avoids elixir CrackedAccount.session, which NPEs at runtime). */
    private fun loginRandom() {
        try {
            val name = RandomUtils.randomUsername()
            val uuid = UUID.nameUUIDFromBytes("OfflinePlayer:$name".toByteArray(Charsets.UTF_8)).toString().replace("-", "")
            mc.session = Session(name, uuid, "0", "mojang")
            skin = null
        } catch (ignored: Exception) {
        }
    }

    private fun connect(data: ServerData) = mc.displayGuiScreen(GuiConnecting(this, mc, data))

    private fun deleteServer(index: Int) {
        if (index in 0 until serverList.countServers()) {
            serverList.removeServerData(index); serverList.saveServerList(); selectedIndex = -1
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (dialog != Dialog.NONE) {
            val ipField = dialog == Dialog.DIRECT || dialogField == 1
            when (keyCode) {
                Keyboard.KEY_ESCAPE -> dialog = Dialog.NONE
                Keyboard.KEY_RETURN -> confirmDialog()
                Keyboard.KEY_TAB -> if (dialog != Dialog.DIRECT) dialogField = 1 - dialogField
                Keyboard.KEY_BACK -> if (ipField) { if (dialogIp.isNotEmpty()) dialogIp = dialogIp.dropLast(1) } else { if (dialogName.isNotEmpty()) dialogName = dialogName.dropLast(1) }
                else -> if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) { if (ipField) dialogIp += typedChar else dialogName += typedChar }
            }
            return
        }
        if (keyCode == Keyboard.KEY_ESCAPE) { mc.displayGuiScreen(prevGui); return }
        if (searchFocused) {
            when (keyCode) {
                Keyboard.KEY_BACK -> if (searchText.isNotEmpty()) searchText = searchText.dropLast(1)
                Keyboard.KEY_RETURN -> searchFocused = false
                else -> if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) searchText += typedChar
            }
            return
        }
        super.keyTyped(typedChar, keyCode)
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        if (dialog != Dialog.NONE) return
        val wheel = Mouse.getEventDWheel()
        if (wheel != 0) scroll = (scroll - wheel / 4f).coerceIn(0f, maxScroll)
    }

    override fun onGuiClosed() {
        clearPendingPings()
        super.onGuiClosed()
    }

    override fun doesGuiPauseGame() = false
}
