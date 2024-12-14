/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.FDPClient.clickGui
import net.ccbluex.liquidbounce.config.*
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
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
    private val style by
        object : ListValue("Style", arrayOf("Black", "Zywl", "FDP"), "FDP") {
            override fun onChanged(oldValue: String, newValue: String) = updateStyle()
        }
    var scale by float("Scale", 0.8f, 0.5f..1.5f)
    val maxElements by int("MaxElements", 15, 1..30)
    val fadeSpeed by float("FadeSpeed", 1f, 0.5f..4f)
    val scrolls by boolean("Scrolls", true)
    val spacedModules by boolean("SpacedModules", false)
    val panelsForcedInBoundaries by boolean("PanelsForcedInBoundaries", false)


    val backback by boolean("Background Accent", true)
    val scrollMode by choices("Scroll Mode", arrayOf("Screen Height", "Value"), "Value")
    val colormode by choices("Setting Accent", arrayOf("White", "Color"), "Color")
    val clickHeight by int("Tab Height", 250, 100.. 500)

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

    @EventTarget(ignoreCondition = true)
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S2EPacketCloseWindow && mc.currentScreen is ClickGui)
            event.cancelEvent()
    }
}