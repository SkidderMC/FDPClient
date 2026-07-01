/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.FDPClient.clickGui
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.BlackStyle
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.FDPDropdownClickGUI
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nextgen.NextGenBrowserRuntime
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nextgen.NextGenClickGuiServer
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nextgen.NextGenClickGuiScreen
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.YzYGui
import net.ccbluex.liquidbounce.ui.client.hud.HUD
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Type
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.fade
import net.minecraft.network.play.server.S2EPacketCloseWindow
import org.lwjgl.input.Keyboard
import java.awt.Color

object ClickGUIModule : Module("ClickGUI", Category.CLIENT, Category.SubCategory.CLIENT_GENERAL,  Keyboard.KEY_RSHIFT, canBeEnabled = false) {
    var lastScale = 0

    private var fdpDropdownGui: FDPDropdownClickGUI? = null
    private var yzyGui: YzYGui? = null
    private var neverloseGui: NeverloseGui? = null
    private var nextGenGui: NextGenClickGuiScreen? = null

    private val style by choices(
        "Style",
        arrayOf("Black", "Zywl", "Dropdown", "FDP", "NextGen"),
        "FDP"
    ).onChanged { newStyle ->
        updateStyle()
        resetGuiInstances()
        if (newStyle.equals("NextGen", ignoreCase = true)) {
            warmupNextGenClickGui()
        }
    }
        .describe("Visual style of the ClickGUI.")

    private val color by choices(
        "Color", arrayOf("Custom", "Fade", "Theme"), "Theme"
    ) { style == "Dropdown" }
        .describe("Color source for the dropdown style.")

    private val customColorSetting by color("CustomColor", Color(255, 255, 255)) { color == "Custom" || color == "Fade" }
        .describe("Custom accent color for the ClickGUI.")

    var scale by float("Scale", 1.0f, 0.5f..1.5f)
        .describe("Overall size of the ClickGUI.")
    val maxElements by int("MaxElements", 15, 1..30)
        .describe("Maximum number of modules shown per category before scrolling.")
    val fadeSpeed by float("FadeSpeed", 1f, 0.5f..4f)
        .describe("Speed of the GUI fade/open animations.")
    val scrolls by boolean("Scrolls", true)
        .describe("Allow scrolling through long category lists.")
    val spacedModules by boolean("SpacedModules", false)
        .describe("Add vertical spacing between modules.")
    val panelsForcedInBoundaries by boolean("PanelsForcedInBoundaries", false)
        .describe("Keep dragged panels inside the screen bounds.")

    val headerColor by boolean("Header Color", true) { style == "Dropdown" }
        .describe("Color the dropdown category headers.")

    val categoryOutline by boolean("Outline", true) { style == "Dropdown" }
        .describe("Draw an outline around dropdown categories.")

    val roundedRectRadius by float("RoundedRect-Radius", 0F, 0F..2F)  { style == "Dropdown" }
        .describe("Corner radius of dropdown panels.")

    val backback by boolean("Background Accent", true) { style == "Dropdown" }
        .describe("Use an accent color for the background.")
    val scrollMode by choices("Scroll Mode", arrayOf("Screen Height", "Value"), "Value")  { style == "Dropdown" }
        .describe("How the dropdown tab height is determined.")
    val colormode by choices("Setting Accent", arrayOf("White", "Color"), "Color") { style == "Dropdown" }
        .describe("Accent color used for settings.")
    val clickHeight by int("Tab Height", 250, 100.. 500) { style == "Dropdown" }
        .describe("Fixed height of the dropdown tab.")

    val nextGenInBrowser by boolean("Open In Browser", false) { style == "NextGen" }.onChanged { openInBrowser ->
        val activeScreen = nextGenGui?.takeIf { mc.currentScreen === it }
        if (activeScreen != null) {
            activeScreen.setBrowserMode(openInBrowser)
        } else if (openInBrowser) {
            NextGenBrowserRuntime.releasePersistentBrowser()
        } else {
            warmupNextGenClickGui()
        }
    }
        .describe("Open the menu in an external browser window.")

    private val nextGenVirtualScreens by multiSelect(
        "Virtual Screens",
        arrayOf("Multiplayer", "Singleplayer", "AltManager", "Disconnected", "Inventory"),
        setOf("Multiplayer", "Singleplayer", "AltManager", "Disconnected", "Inventory"),
    ) { style == "NextGen" }
        .describe("Native screens rendered by the active web theme, each with an automatic native fallback.")

    // Momentary action: flips on, kicks off a fresh asset download/retry, then bounces back off so it
    // reads like a button. Stays visible after a failure (even from another style) so the in-game
    // browser can always be recovered, and a failed attempt's reason is shown on the NextGen fallback
    // screen via [NextGenBrowserRuntime.lastErrorLog].
    private val redownloadAssetsValue = boolean("Re-download Assets", false) {
        style == "NextGen" || NextGenBrowserRuntime.state == NextGenBrowserRuntime.State.FAILED
    }
        .describe("Click to fetch any missing in-game browser assets and retry - a fast resume after a failed or stalled download.")
        .onChange { _, triggered ->
            if (triggered) {
                NextGenBrowserRuntime.retry()
                HUD.addNotification(
                    Notification("NextGen ClickGUI", "Resuming in-game browser download...", Type.INFO)
                )
            }
            false
        }

    // Full clean reinstall: purge every cached asset and download from scratch. For the rare case the
    // cache or manifest is corrupt; everyday recovery should use the fast "Re-download Assets" resume above.
    private val cleanReinstallValue = boolean("Clean Reinstall", false) {
        style == "NextGen" || NextGenBrowserRuntime.state == NextGenBrowserRuntime.State.FAILED
    }
        .describe("Click to delete all cached browser assets and download everything again from scratch.")
        .onChange { _, triggered ->
            if (triggered) {
                NextGenBrowserRuntime.retry(redownloadAssets = true)
                HUD.addNotification(
                    Notification("NextGen ClickGUI", "Reinstalling all in-game browser assets...", Type.INFO)
                )
            }
            false
        }

    init {
        group(
            "General",
            "Style", "Scale", "MaxElements", "FadeSpeed",
            "Scrolls", "SpacedModules", "PanelsForcedInBoundaries"
        )
        group("Colors", "Color", "CustomColor")
        group(
            "Dropdown",
            "Header Color", "Outline", "RoundedRect-Radius", "Background Accent",
            "Scroll Mode", "Setting Accent", "Tab Height"
        )
        group("NextGen", "Open In Browser", "Virtual Screens", "Re-download Assets", "Clean Reinstall")
    }

    override fun onEnable() {
        try {
            Keyboard.enableRepeatEvents(true)
            lastScale = mc.gameSettings.guiScale
            mc.gameSettings.guiScale = 2

            when {
                style.equals("Zywl", ignoreCase = true) -> {
                    if (yzyGui == null) {
                        yzyGui = YzYGui(this)
                    } else {
                        yzyGui?.resetGui()
                    }
                    mc.displayGuiScreen(yzyGui)
                    this.state = false
                }
                style.equals("Dropdown", ignoreCase = true) -> {
                    if (fdpDropdownGui == null) {
                        fdpDropdownGui = FDPDropdownClickGUI()
                    } else {
                        fdpDropdownGui?.resetGui()
                    }
                    mc.displayGuiScreen(fdpDropdownGui)
                    this.state = false
                }
                style.equals("FDP", ignoreCase = true) -> {
                    if (neverloseGui == null) {
                        neverloseGui = NeverloseGui()
                    }
                    mc.displayGuiScreen(neverloseGui)
                    this.state = false
                }
                style.equals("NextGen", ignoreCase = true) -> {
                    if (nextGenGui == null) {
                        nextGenGui = NextGenClickGuiScreen()
                    }
                    mc.displayGuiScreen(nextGenGui)
                    this.state = false
                }
                else -> {
                    updateStyle()
                    mc.displayGuiScreen(clickGui)
                    this.state = false
                }
            }
        } catch (e: Exception) {
            println("Error opening ClickGUI: ${e.message}")
            updateStyle()
            mc.displayGuiScreen(clickGui)
            this.state = false
        }
    }

    override fun onDisable() {
        Keyboard.enableRepeatEvents(false)
    }

    private fun resetGuiInstances() {
        try {
            fdpDropdownGui?.onGuiClosed()
            yzyGui?.onGuiClosed()
            nextGenGui?.let { screen ->
                if (mc.currentScreen === screen) {
                    mc.displayGuiScreen(null)
                } else {
                    screen.onGuiClosed()
                }
            }
            neverloseGui = null
            NextGenBrowserRuntime.releasePersistentBrowser()
        } catch (e: Exception) {
            println("Error during GUI cleanup: ${e.message}")
        }
        fdpDropdownGui = null
        yzyGui = null
        nextGenGui = null
    }

    fun warmupNextGenClickGui() {
        if (!style.equals("NextGen", ignoreCase = true) || nextGenInBrowser) {
            return
        }

        NextGenBrowserRuntime.preload(NextGenClickGuiServer.start())
    }

    fun shouldUseVirtualScreen(route: String): Boolean =
        !route.equals("title", ignoreCase = true) &&
            style.equals("NextGen", ignoreCase = true) &&
            nextGenVirtualScreens.any { it.equals(route, ignoreCase = true) }

    private fun updateStyle() {
        clickGui.style = when (style) {
            "Black" -> BlackStyle
            else -> return
        }
    }

    fun generateColor(index: Int): Color {
        return when (color) {
            "Custom" -> {
                customColorSetting
            }
            "Fade" -> {
                fade(customColorSetting, index * 10, 100)
            }
            "Theme" -> {
                ClientThemesUtils.getColor(index)
            }
            else -> ClientThemesUtils.getColor(index)
        }
    }

    val onPacket = handler<PacketEvent>(always = true) { event ->
        if (event.packet is S2EPacketCloseWindow && mc.currentScreen is ClickGui) {
            event.cancelEvent()
        }
    }
}
