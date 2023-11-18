/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.ClickGui
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.*
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.Slight.SlightUI
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.dropdown.DropdownGUI
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.light.LightClickGUI
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.newVer.NewUi
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.novoline.ClickyUI
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.onetap.OtcClickGUi
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.tenacity.TenacityClickGUI
import net.ccbluex.liquidbounce.ui.client.gui.options.modernuiLaunchOption
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.minecraft.network.play.server.S2EPacketCloseWindow
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.util.*

@ModuleInfo(name = "ClickGUI", category = ModuleCategory.CLIENT, keyBind = Keyboard.KEY_RSHIFT, canEnable = false)
object ClickGUIModule : Module() {

    val styleValue: ListValue = object : ListValue(
        "Style",
        arrayOf(
            "Classic",
            "OneTap",
            "Light",
            "Novoline",
            "Astolfo",
            "LB+",
            "Jello",
            "LiquidBounce",
            "Tenacity5",
            "Slight",
            "Bjur",
            "Null",
            "Slowly",
            "Black",
            "White"
        ),
        "Black"
    ) {
        override fun onChanged(oldValue: String, newValue: String) {
            updateStyle()
        }
    }

    val backback = BoolValue("Background Accent", true)
    val scrollMode = ListValue("Scroll Mode", arrayOf("Screen Height", "Value"), "Value")
    val colormode = ListValue("Setting Accent", arrayOf("White", "Color"), "Color")
    val clickHeight = IntegerValue("Tab Height", 250, 100, 500)
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
    private var slight = SlightUI()
    private var dropdown = DropdownGUI()
    private var tena = TenacityClickGUI()
    private var bjur = BjurStyle()

    override fun onEnable() {
        when {
            styleValue.get().contains("Novoline") -> mc.displayGuiScreen(novoline)
            styleValue.get().contains("OneTap") -> mc.displayGuiScreen(otcGui)
            styleValue.get().contains("Light") -> mc.displayGuiScreen(lightClickGUI)
            styleValue.get().equals("Classic", ignoreCase = true) -> mc.displayGuiScreen(dropdown)
            styleValue.get().equals("Tenacity", ignoreCase = true) -> mc.displayGuiScreen(tena)
            styleValue.get().equals("LB+", ignoreCase = true) -> mc.displayGuiScreen(NewUi.getInstance())
            styleValue.get().equals("Bjur", ignoreCase = true) -> mc.displayGuiScreen(bjur)
            styleValue.get().contains("Slight") -> mc.displayGuiScreen(slight)
            else -> {
                updateStyle()
                mc.displayGuiScreen(modernuiLaunchOption.clickGui)
            }
        }
        this.state = false
    }
    private fun updateStyle() {
        when (styleValue.get().lowercase(Locale.getDefault())) {
            "liquidbounce" -> modernuiLaunchOption.clickGui.style = LiquidBounceStyle()
            "null" -> modernuiLaunchOption.clickGui.style = NullStyle()
            "slowly" -> modernuiLaunchOption.clickGui.style = SlowlyStyle()
            "black", "white" -> modernuiLaunchOption.clickGui.style = if (styleValue.get() == "White") WhiteStyle() else BlackStyle()
            "jello" -> modernuiLaunchOption.clickGui.style = JelloStyle()
            "tenacity5" -> modernuiLaunchOption.clickGui.style = TenacityStyle()
            "astolfo" -> modernuiLaunchOption.clickGui.style = AstolfoStyle()
        }
    }

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