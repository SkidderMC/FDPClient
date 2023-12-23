/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.clickgui

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.ui.clickgui.style.styles.*
import net.ccbluex.liquidbounce.ui.clickgui.style.styles.classic.DropdownGUI
import net.ccbluex.liquidbounce.ui.clickgui.style.styles.light.LightClickGUI
import net.ccbluex.liquidbounce.ui.clickgui.style.styles.newVer.NewUi
import net.ccbluex.liquidbounce.ui.clickgui.style.styles.novoline.ClickyUI
import net.ccbluex.liquidbounce.ui.clickgui.style.styles.onetap.OtcClickGUi
import net.ccbluex.liquidbounce.ui.client.gui.options.modernuiLaunchOption
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.minecraft.network.play.server.S2EPacketCloseWindow
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.util.*

@ModuleInfo(name = "ClickGUI", category = ModuleCategory.CLIENT, keyBind = Keyboard.KEY_RSHIFT, canEnable = false)
object ClickGUIModule : Module() {

    private val styleValue: ListValue = object : ListValue(
        "Style",
        arrayOf(
            "Classic",
            "OneTap",
            "Light",
            "Novoline",
            "LB+",
            "Astolfo",
            "Mixed",
            "Null",
            "Black",
            "White"
        ),
        "Black"
    ) {
        override fun onChanged(oldValue: String, newValue: String) {
            updateStyle()
        }
    }

    val scaleValue = FloatValue("Scale", 0.70f, 0.7f, 2f)
    val maxElementsValue = IntegerValue("MaxElements", 20, 1, 35)
    val backgroundValue = ListValue("Background", arrayOf("Default", "Gradient", "None"), "None")
    val animationValue = ListValue("Animation", arrayOf("Bread", "Slide", "LiquidBounce", "Zoom", "Ziul", "None"), "Ziul")
    val colorRainbow = BoolValue("Rainbow", true)
    val colorRedValue = IntegerValue("R", 0, 0, 255).displayable { !colorRainbow.get() } as IntegerValue
    val colorGreenValue = IntegerValue("G", 160, 0, 255).displayable { !colorRainbow.get() } as IntegerValue
    val colorBlueValue = IntegerValue("B", 255, 0, 255).displayable { !colorRainbow.get() } as IntegerValue
    val fastRenderValue = BoolValue("FastRender", false)
    val getClosePrevious = BoolValue("ClosePrevious", false)
    val disp = BoolValue("DisplayValue", true)

    private var lightClickGUI = LightClickGUI()
    private var otcGui = OtcClickGUi()
    private var novoline = ClickyUI()
    private var dropdown = DropdownGUI()

    override fun onEnable() {
        when {
            styleValue.get().contains("Novoline") -> mc.displayGuiScreen(novoline)
            styleValue.get().contains("OneTap") -> mc.displayGuiScreen(otcGui)
            styleValue.get().contains("Light") -> mc.displayGuiScreen(lightClickGUI)
            styleValue.get().equals("Classic", ignoreCase = true) -> mc.displayGuiScreen(dropdown)
            styleValue.get().equals("LB+", ignoreCase = true) -> mc.displayGuiScreen(NewUi.getInstance())
            else -> {
                updateStyle()
                mc.displayGuiScreen(modernuiLaunchOption.clickGui)
            }
        }
        this.state = false
    }
    private fun updateStyle() {
        when (styleValue.get().lowercase(Locale.getDefault())) {
            "null" -> modernuiLaunchOption.clickGui.style = NullStyle()
            "black", "white" -> modernuiLaunchOption.clickGui.style = if (styleValue.get() == "White") WhiteStyle() else BlackStyle()
            "mixed" -> modernuiLaunchOption.clickGui.style = MixedStyle()
            "astolfo" -> modernuiLaunchOption.clickGui.style = AstolfoStyle()
        }
    }

    @JvmStatic
    fun generateColor(): Color {
        return if (colorRainbow.get()) {
            rainbow()
        } else {
            Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
        }
    }

    @EventTarget(ignoreCondition = true)
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S2EPacketCloseWindow && mc.currentScreen is ClickGui) {
            event.cancelEvent()
        }
    }
}