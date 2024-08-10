/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.client.button.*
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.TextValue
import net.minecraft.client.gui.GuiButton
import java.util.*

/**
 * The type Client spoof.
 */
object BrandSpoofer : Module("BrandSpoofer", Category.CLIENT, hideModule = false) {
    /**
     * The Mode value.
     */
    val possibleBrands = ListValue(
        "Mode", arrayOf(
            "Vanilla",
            "OptiFine",
            "Fabric",
            "Feather",
            "LunarClient",
            "LabyMod",
            "CheatBreaker",
            "PvPLounge",
            "Minebuilders",
            "FML",
            "Geyser",
            "Log4j",
            "Custom",
        ), "FDP"
    )

    val customValue = TextValue("Custom-Brand", "WTF") { possibleBrands.get().equals("Custom", true) }

    private val buttonValue = ListValue(
        "Button",
        arrayOf(
            "Dark",
            "Light",
            "Rounded",
            "LiquidBounce",
            "Fline",
            "FDP",
            "PVP",
            "Vanilla"
        ),
        "FDP"
    )

    fun getButtonRenderer(button: GuiButton?): AbstractButtonRenderer? {
        val lowerCaseButtonValue = buttonValue.get().lowercase(Locale.getDefault())
        return when (lowerCaseButtonValue) {
            "rounded" -> RoundedButtonRenderer(button!!)
            "fdp" -> HyperiumButtonRenderer(button!!)
            "pvp" -> PvPClientButtonRenderer(button!!)
            "liquidbounce" -> LiquidButtonRenderer(button!!)
            "light" -> LunarButtonRenderer(button!!)
            "dark" -> BlackoutButtonRenderer(button!!)
            else -> null // vanilla or unknown
        }
    }
}