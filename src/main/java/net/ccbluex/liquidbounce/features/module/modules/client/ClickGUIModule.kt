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
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.yzyGUI
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils
import net.minecraft.network.play.server.S2EPacketCloseWindow
import org.lwjgl.input.Keyboard
import java.awt.Color

object ClickGUIModule : Module("ClickGUI", Category.CLIENT, Keyboard.KEY_RSHIFT, canBeEnabled = false) {
    var lastScale = 0

    private val style by choices(
        "Style",
        arrayOf("Black", "Zywl", "FDP"),
        "FDP"
    ).onChanged {
        updateStyle()
    }

    var scale by float("Scale", 0.8f, 0.5f..1.5f)
    val maxElements by int("MaxElements", 15, 1..30)
    val fadeSpeed by float("FadeSpeed", 1f, 0.5f..4f)
    val scrolls by boolean("Scrolls", true)
    val spacedModules by boolean("SpacedModules", false)
    val panelsForcedInBoundaries by boolean("PanelsForcedInBoundaries", false)

    val categoryOutline by boolean("Header Outline", true) { style == "FDP" }

    val backback by boolean("Background Accent", true) { style == "FDP" }
    val scrollMode by choices("Scroll Mode", arrayOf("Screen Height", "Value"), "Value")  { style == "FDP" }
    val colormode by choices("Setting Accent", arrayOf("White", "Color"), "Color") { style == "FDP" }
    val clickHeight by int("Tab Height", 250, 100.. 500) { style == "FDP" }

    override fun onEnable() {
        lastScale = mc.gameSettings.guiScale
        mc.gameSettings.guiScale = 2

        when {
            style.equals("Zywl", ignoreCase = true) -> {
                mc.displayGuiScreen(
                    yzyGUI(
                        this
                    )
                )
                this.state = false
            }
            style.equals("FDP", ignoreCase = true) -> {
                mc.displayGuiScreen(FDPDropdownClickGUI())
                this.state = false
            }
            else -> {
                updateStyle()
                mc.displayGuiScreen(clickGui)
                this.state = false
            }
        }
    }

    private fun updateStyle() {
        clickGui.style = when (style) {
            "Black" -> BlackStyle
            else -> return
        }
    }

    @JvmStatic
    fun generateColor(index: Int): Color {
        return ClientThemesUtils.getColor(index)
    }

    val onPacket = handler<PacketEvent>(always = true) { event ->
        if (event.packet is S2EPacketCloseWindow && mc.currentScreen is ClickGui) {
            event.cancelEvent()
        }
    }
}