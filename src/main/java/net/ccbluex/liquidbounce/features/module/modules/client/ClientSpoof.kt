/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.client.button.*
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.client.gui.GuiButton

@ModuleInfo(name = "ClientSpoof", category = ModuleCategory.CLIENT, defaultOn = true)
object ClientSpoof : Module() {

    val modeValue = ListValue("Payloads", arrayOf("Vanilla", "Forge", "Lunar", "LabyMod", "CheatBreaker", "PvPLounge"), "LabyMod")
    private val buttonValue = ListValue("Button", arrayOf("Better", "RGBRounded", "Wolfram", "Rounded", "Hyperium", "RGB", "Badlion", "Flat", "FLine", "Rise", "Vanilla"), "Hyperium")
    val render = BoolValue("Render", true)

    fun getButtonRenderer(button: GuiButton): AbstractButtonRenderer? {
        return when (ClientSpoof.buttonValue.get().lowercase()) {
            "better" -> BetterButtonRenderer(button)
            "rounded" -> RoundedButtonRenderer(button)
            "fline" -> FLineButtonRenderer(button)
            "rise" -> RiseButtonRenderer(button)
            "hyperium" -> HyperiumButtonRenderer(button)
            "rgb" -> RGBButtonRenderer(button)
            "badlion" -> BadlionTwoButtonRenderer(button)
            "rgbrounded" -> RGBRoundedButtonRenderer(button)
            "wolfram" -> WolframButtonRenderer(button)
            else -> null // vanilla or unknown
        }
    }
}