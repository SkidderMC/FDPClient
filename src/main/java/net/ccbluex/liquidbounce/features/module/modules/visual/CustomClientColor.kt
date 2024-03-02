/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.IntegerValue
import java.awt.Color

@ModuleInfo(name = "CustomClientColor", category = ModuleCategory.VISUAL)
object CustomClientColor : Module() {
    private val redValue = IntegerValue("Red", 255,0,255)
    private val greenValue = IntegerValue("Green", 255,0,255)
    private val blueValue = IntegerValue("Blue", 255,0,255)

    fun getColor() : Color {
        return Color(redValue.get(), greenValue.get(), blueValue.get())
    }
    fun getColor(alpha : Int) : Color {
        return Color(redValue.get(), greenValue.get(), blueValue.get(), alpha)
    }
}