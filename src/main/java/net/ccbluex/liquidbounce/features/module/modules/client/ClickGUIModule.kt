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
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.YzYGui
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.fade
import net.minecraft.network.play.server.S2EPacketCloseWindow
import org.lwjgl.input.Keyboard
import java.awt.Color

object ClickGUIModule : Module("ClickGUI", Category.CLIENT, Keyboard.KEY_RSHIFT, canBeEnabled = false) {
    var lastScale = 0

    private var fdpDropdownGui: FDPDropdownClickGUI? = null
    private var yzyGui: YzYGui? = null

    private val style by choices(
        "Style",
        arrayOf("Black", "Zywl", "FDP"),
        "FDP"
    ).onChanged {
        updateStyle()
        resetGuiInstances()
    }

    private val color by choices(
        "Color", arrayOf("Custom", "Fade", "Theme"), "Theme"
    ) { style == "FDP" }

    private val customColorSetting by color("CustomColor", Color(255, 255, 255)) { color == "Custom" || color == "Fade" }

    var scale by float("Scale", 1.0f, 0.5f..1.5f)
    val maxElements by int("MaxElements", 15, 1..30)
    val fadeSpeed by float("FadeSpeed", 1f, 0.5f..4f)
    val scrolls by boolean("Scrolls", true)
    val spacedModules by boolean("SpacedModules", false)
    val panelsForcedInBoundaries by boolean("PanelsForcedInBoundaries", false)

    val headerColor by boolean("Header Color", true) { style == "FDP" }

    val categoryOutline by boolean("Outline", true) { style == "FDP" }

    val roundedRectRadius by float("RoundedRect-Radius", 0F, 0F..2F)  { style == "FDP" }

    val backback by boolean("Background Accent", true) { style == "FDP" }
    val scrollMode by choices("Scroll Mode", arrayOf("Screen Height", "Value"), "Value")  { style == "FDP" }
    val colormode by choices("Setting Accent", arrayOf("White", "Color"), "Color") { style == "FDP" }
    val clickHeight by int("Tab Height", 250, 100.. 500) { style == "FDP" }

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
                style.equals("FDP", ignoreCase = true) -> {
                    if (fdpDropdownGui == null) {
                        fdpDropdownGui = FDPDropdownClickGUI()
                    } else {
                        fdpDropdownGui?.resetGui()
                    }
                    mc.displayGuiScreen(fdpDropdownGui)
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
        } catch (e: Exception) {
            println("Error during GUI cleanup: ${e.message}")
        }
        fdpDropdownGui = null
        yzyGui = null
    }

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