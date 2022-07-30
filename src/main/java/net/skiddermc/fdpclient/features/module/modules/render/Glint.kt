/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.features.module.modules.render

import net.skiddermc.fdpclient.features.module.Module
import net.skiddermc.fdpclient.features.module.ModuleCategory
import net.skiddermc.fdpclient.features.module.ModuleInfo
import net.skiddermc.fdpclient.utils.render.ColorUtils
import net.skiddermc.fdpclient.value.IntegerValue
import net.skiddermc.fdpclient.value.ListValue
import java.awt.Color

@ModuleInfo(name = "Glint", category = ModuleCategory.RENDER)
class Glint : Module() {

    private val modeValue = ListValue("Mode", arrayOf("Rainbow", "AnotherRainbow", "Custom"), "Custom")
    private val redValue = IntegerValue("Red", 255, 0, 255).displayable { modeValue.equals("Custom") }
    private val greenValue = IntegerValue("Green", 0, 0, 255).displayable { modeValue.equals("Custom") }
    private val blueValue = IntegerValue("Blue", 0, 0, 255).displayable { modeValue.equals("Custom") }

    fun getColor(): Color {
        return when (modeValue.get().lowercase()) {
            "rainbow" -> ColorUtils.rainbow()
            "anotherrainbow" -> ColorUtils.skyRainbow(10, 0.9F, 1F, 1.0)
            else -> Color(redValue.get(), greenValue.get(), blueValue.get())
        }
    }
}