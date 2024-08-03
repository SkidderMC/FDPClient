/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.FDPClient.clickGui
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.BlackStyle
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.server.S2EPacketCloseWindow
import org.lwjgl.input.Keyboard

object ClickGUIModule : Module("ClickGUI", Category.CLIENT, Keyboard.KEY_RSHIFT, canBeEnabled = false) {
    private val style by
        object : ListValue("Style", arrayOf("Black"), "Black") {
            override fun onChanged(oldValue: String, newValue: String) = updateStyle()
        }
    var scale by FloatValue("Scale", 0.8f, 0.5f..1.5f)
    val maxElements by IntegerValue("MaxElements", 15, 1..30)
    val fadeSpeed by FloatValue("FadeSpeed", 1f, 0.5f..4f)
    val scrolls by BoolValue("Scrolls", true)
    val spacedModules by BoolValue("SpacedModules", false)
    val panelsForcedInBoundaries by BoolValue("PanelsForcedInBoundaries", false)


    override fun onEnable() {
        updateStyle()
        mc.displayGuiScreen(clickGui)
    }

    private fun updateStyle() {
        clickGui.style = when (style) {
            "Black" -> BlackStyle
            else -> return
        }
    }

    @EventTarget(ignoreCondition = true)
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S2EPacketCloseWindow && mc.currentScreen is ClickGui)
            event.cancelEvent()
    }
}