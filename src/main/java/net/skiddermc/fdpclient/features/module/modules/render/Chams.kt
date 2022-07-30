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
import net.skiddermc.fdpclient.utils.render.RenderUtils
import net.skiddermc.fdpclient.value.BoolValue
import net.skiddermc.fdpclient.value.IntegerValue
import org.lwjgl.opengl.GL11

@ModuleInfo(name = "Chams", category = ModuleCategory.RENDER)
class Chams : Module() {
    val targetsValue = BoolValue("Targets", true)
    val chestsValue = BoolValue("Chests", true)
    val itemsValue = BoolValue("Items", true)

    private val colorRainbowValue = BoolValue("Rainbow", false)
    private val colorRedValue = IntegerValue("Red", 255, 0, 255).displayable { !colorRainbowValue.get() }
    private val colorGreenValue = IntegerValue("Green", 255, 0, 255).displayable { !colorRainbowValue.get() }
    private val colorBlueValue = IntegerValue("Blue", 255, 0, 255).displayable { !colorRainbowValue.get() }
    private val colorAlphaValue = IntegerValue("Alpha", 200, 0, 255)

    fun setColor() {
        if (colorRainbowValue.get()) {
            RenderUtils.glColor(ColorUtils.rainbowWithAlpha(colorAlphaValue.get()))
        } else {
            GL11.glColor4f(colorRedValue.get() / 255f, colorGreenValue.get() / 255f, colorBlueValue.get() / 255f, colorAlphaValue.get() / 255f)
        }
    }
}