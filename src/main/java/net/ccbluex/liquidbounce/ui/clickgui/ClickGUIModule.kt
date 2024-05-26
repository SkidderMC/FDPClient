/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.clickgui

import me.zywl.fdpclient.event.EventTarget
import me.zywl.fdpclient.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.clickgui.style.styles.AstolfoStyle
import net.ccbluex.liquidbounce.ui.clickgui.style.styles.BlackStyle
import net.ccbluex.liquidbounce.ui.clickgui.style.styles.MixedStyle
import net.ccbluex.liquidbounce.ui.clickgui.style.styles.NullStyle
import net.ccbluex.liquidbounce.ui.clickgui.style.styles.classic.DropdownGUI
import net.ccbluex.liquidbounce.ui.clickgui.style.styles.fdpdropdown.FDPDropdownClickGUI
import net.ccbluex.liquidbounce.ui.gui.colortheme.ClientTheme
import net.ccbluex.liquidbounce.ui.gui.options.modernuiLaunchOption
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
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
            "Dropdrown",
//            "LB+",
            "Astolfo",
            "Mixed",
            "Null",
            "Black",
        ),
        "Dropdrown"
    ) {
        override fun onChanged(oldValue: String, newValue: String) {
            updateStyle()
        }
    }

    val scaleValue = FloatValue("Scale", 1f, 0.7f, 2f)
    val maxElementsValue = IntegerValue("MaxElements", 20, 1, 35)
    var clientColor = BoolValue("Client Color", true)
    val colorRedValue = IntegerValue("R", 0, 0, 255).displayable { !clientColor.get() } as IntegerValue
    val colorGreenValue = IntegerValue("G", 160, 0, 255).displayable { !clientColor.get() } as IntegerValue
    val colorBlueValue = IntegerValue("B", 255, 0, 255).displayable { !clientColor.get() } as IntegerValue
    val fastRenderValue = BoolValue("FastRender", false)
    val getClosePrevious = BoolValue("ClosePrevious", false)

    val backback = BoolValue("Background Accent", true)
    val scrollMode = ListValue("Scroll Mode", arrayOf("Screen Height", "Value"), "Value")
    val colormode = ListValue("Setting Accent", arrayOf("White", "Color"), "Color")
    val clickHeight = IntegerValue("Tab Height", 250, 100, 500)

    private var dropdown = DropdownGUI()
    private var dropdownClickGUIFDP = FDPDropdownClickGUI()

    override fun onEnable() {
        when {
            styleValue.get().equals("Classic", ignoreCase = true) -> mc.displayGuiScreen(dropdown)
            styleValue.get().equals("Dropdrown", ignoreCase = true) -> mc.displayGuiScreen(dropdownClickGUIFDP)
//            styleValue.get().equals("LB+", ignoreCase = true) -> mc.displayGuiScreen(NewUi.getInstance())
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
            "black" -> modernuiLaunchOption.clickGui.style = BlackStyle()
            "mixed" -> modernuiLaunchOption.clickGui.style = MixedStyle()
            "astolfo" -> modernuiLaunchOption.clickGui.style = AstolfoStyle()
        }
    }

    @JvmStatic
    fun generateColor(index : Int) : Color {
        return if (clientColor.get()) {
            ClientTheme.getColor(index)
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
