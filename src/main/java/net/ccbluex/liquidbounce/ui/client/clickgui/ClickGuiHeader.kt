/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui

import net.ccbluex.liquidbounce.FDPClient.CLIENT_GITHUB
import net.ccbluex.liquidbounce.features.module.modules.client.SpotifyModule
import net.ccbluex.liquidbounce.ui.client.clickgui.sidegui.SideGui
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil
import net.ccbluex.liquidbounce.ui.client.gui.GuiUpdate
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.keybind.KeyBindManager
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.fontmanager.GuiFontManager
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.io.MiscUtils
import net.ccbluex.liquidbounce.utils.render.*
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Mouse
import java.awt.Color
import kotlin.math.abs

/**
 * Shared top navigation used by every ClickGUI style: two clean tabs ("ClickGUI" and "Settings").
 * "Settings" opens a modal listing the client quick-actions (Spotify, HUD Designer, theme, links...)
 * each with an icon and a short description, so the actions are not a cluttered icon row.
 */
object ClickGuiHeader : MinecraftInstance {

    private data class Item(val name: String, val glyph: String, val desc: String)

    private val ITEMS = listOf(
        Item("HUD Designer", "E", "Drag and arrange the in-game HUD"),
        Item("Theme & Colors", "T", "Client color theme, background and wallpaper"),
        Item("Spotify", "S", "Open the in-game Spotify player"),
        Item("Font Manager", "F", "Add and pick client fonts"),
        Item("Keybinds", "K", "Bind modules to keys"),
        Item("Check Update", "U", "Look for a newer client build"),
        Item("GitHub", "G", "Open the FDPClient repository"),
        Item("Discord", "D", "Join the community Discord"),
        Item("Support", "H", "Report a bug or ask for help")
    )

    private var settingsOpen = false

    /** True while the Settings modal is open, so the click-GUI behind it stops scrolling. */
    val isOpen: Boolean get() = settingsOpen

    private class Box(val x: Float, val y: Float, val w: Float, val h: Float, val onClick: () -> Unit, val settingsTab: Boolean = false)

    private val tabBoxes = mutableListOf<Box>()
    private val itemBoxes = mutableListOf<Box>()
    private var modal = floatArrayOf(0f, 0f, 0f, 0f)

    private var navX = 0f
    private var navY = 0f
    private var navW = 0f
    private var navH = 0f
    private var posInit = false

    private var wasDown = false
    private var armed = false
    private var movedWhileDown = false
    private var pressX = 0
    private var pressY = 0
    private var grabDX = 0f
    private var grabDY = 0f
    private var pressedSettingsTab: Boolean? = null

    fun close() {
        settingsOpen = false
    }

    private fun action(name: String, sideGui: SideGui, parent: GuiScreen): () -> Unit = when (name) {
        "HUD Designer" -> { { mc.displayGuiScreen(GuiHudDesigner()) } }
        "Theme & Colors" -> { { sideGui.openCategory("Color") } }
        "Spotify" -> { { SpotifyModule.openPlayerScreen() } }
        "Font Manager" -> { { mc.displayGuiScreen(GuiFontManager(parent)) } }
        "Keybinds" -> { { mc.displayGuiScreen(KeyBindManager) } }
        "Check Update" -> { { mc.displayGuiScreen(GuiUpdate()) } }
        "GitHub" -> { { MiscUtils.showURL(CLIENT_GITHUB) } }
        "Discord" -> { { MiscUtils.showURL("https://discord.com/invite/3XRFGeqEYD") } }
        "Support" -> { { MiscUtils.showURL("https://github.com/opZywl/fdpclient/issues") } }
        else -> { {} }
    }

    fun draw(sideGui: SideGui, parent: GuiScreen, mouseX: Int, mouseY: Int) {
        val sr = ScaledResolution(mc)
        if (settingsOpen) drawModal(sr, sideGui, parent, mouseX, mouseY)
        drawTabs(sr, mouseX, mouseY)
    }

    private fun drawTabs(sr: ScaledResolution, mouseX: Int, mouseY: Int) {
        val labels = listOf("ClickGUI" to false, "Settings" to true)
        val height = 22f
        val gap = 6f
        val padX = 18f
        val widths = labels.map { Fonts.Nl_18.stringWidth(it.first) + padX * 2 }
        val total = widths.sum() + gap

        navW = total
        navH = height
        if (!posInit) {
            navX = (sr.scaledWidth - total) / 2f
            navY = 6f
            posInit = true
        }

        updateDrag(sr, mouseX, mouseY)

        navX = navX.coerceIn(0f, (sr.scaledWidth - navW).coerceAtLeast(0f))
        navY = navY.coerceIn(0f, (sr.scaledHeight - navH).coerceAtLeast(0f))

        tabBoxes.clear()
        val accent = ClientThemesUtils.getColor()
        var x = navX
        val y = navY

        labels.forEachIndexed { i, (label, isSettings) ->
            val w = widths[i]
            val active = if (isSettings) settingsOpen else !settingsOpen
            val hovered = RenderHelper.isHovering(x, y, w, height, mouseX, mouseY)
            val bg = when {
                active -> accent
                hovered -> Color(36, 38, 44)
                else -> Color(22, 23, 26)
            }
            RoundedUtil.drawRound(x, y, w, height, 5f, bg)
            Fonts.Nl_18.drawString(label, x + (w - Fonts.Nl_18.stringWidth(label)) / 2f, y + 7f, -1)
            tabBoxes.add(Box(x, y, w, height, { }, isSettings))
            x += w + gap
        }
    }

    private fun updateDrag(sr: ScaledResolution, mouseX: Int, mouseY: Int) {
        val down = Mouse.isButtonDown(0)
        val onNav = mouseX >= navX && mouseX <= navX + navW && mouseY >= navY && mouseY <= navY + navH

        if (down && !wasDown && onNav) {
            armed = true
            movedWhileDown = false
            pressX = mouseX
            pressY = mouseY
            grabDX = mouseX - navX
            grabDY = mouseY - navY
            pressedSettingsTab = tabBoxes.firstOrNull { inside(it, mouseX, mouseY) }?.settingsTab
        }

        if (down && armed) {
            if (!movedWhileDown && (abs(mouseX - pressX) > 3 || abs(mouseY - pressY) > 3)) {
                movedWhileDown = true
            }
            if (movedWhileDown) {
                navX = mouseX - grabDX
                navY = mouseY - grabDY
            }
        }

        if (!down && wasDown) {
            if (armed && !movedWhileDown) {
                pressedSettingsTab?.let { settingsOpen = it }
            }
            armed = false
            movedWhileDown = false
            pressedSettingsTab = null
        }

        wasDown = down
    }

    private fun drawModal(sr: ScaledResolution, sideGui: SideGui, parent: GuiScreen, mouseX: Int, mouseY: Int) {
        RenderPrimitives.drawRect(0f, 0f, sr.scaledWidth.toFloat(), sr.scaledHeight.toFloat(), Color(0, 0, 0, 140).rgb)

        val pw = 340f
        val rowH = 40f
        val top = 40f
        val ph = top + ITEMS.size * rowH + 12f
        val px = (sr.scaledWidth - pw) / 2f
        val py = (sr.scaledHeight - ph) / 2f
        modal = floatArrayOf(px, py, pw, ph)

        RoundedUtil.drawRound(px, py, pw, ph, 7f, Color(16, 17, 22, 250))
        RoundedUtil.drawRound(px, py, pw, 1.2f, 0f, ClientThemesUtils.getColor())
        Fonts.Nl_18.drawString("Settings", px + 14f, py + 14f, -1)

        itemBoxes.clear()
        val accent = ClientThemesUtils.getColor()
        var ry = py + top
        for (item in ITEMS) {
            val rowW = pw - 16f
            val rx = px + 8f
            val hovered = RenderHelper.isHovering(rx, ry, rowW, rowH - 4f, mouseX, mouseY)
            if (hovered) RoundedUtil.drawRound(rx, ry, rowW, rowH - 4f, 4f, Color(255, 255, 255, 20))

            RoundedUtil.drawRound(rx + 6f, ry + 6f, 24f, 24f, 5f, accent)
            val gf = Fonts.nlHeaderIcon_24
            gf.drawString(item.glyph, rx + 6f + (24f - gf.stringWidth(item.glyph)) / 2f, ry + 6f + 8f, -1)

            Fonts.Nl_18.drawString(item.name, rx + 38f, ry + 7f, -1)
            Fonts.Nl_16.drawString(item.desc, rx + 38f, ry + 21f, Color(168, 172, 182).rgb)

            itemBoxes.add(Box(rx, ry, rowW, rowH - 4f, action(item.name, sideGui, parent)))
            ry += rowH
        }
    }

    /** Returns true when the click was consumed (a tab, an item, or anywhere while the modal is open). */
    fun handleClick(mouseX: Int, mouseY: Int): Boolean {
        if (tabBoxes.any { inside(it, mouseX, mouseY) }) {
            return true
        }

        if (settingsOpen) {
            itemBoxes.firstOrNull { inside(it, mouseX, mouseY) }?.let {
                it.onClick()
                settingsOpen = false
                return true
            }
            if (!RenderHelper.isHovering(modal[0], modal[1], modal[2], modal[3], mouseX, mouseY)) {
                settingsOpen = false
            }
            return true
        }
        return false
    }

    private fun inside(b: Box, mouseX: Int, mouseY: Int) = RenderHelper.isHovering(b.x, b.y, b.w, b.h, mouseX, mouseY)
}
