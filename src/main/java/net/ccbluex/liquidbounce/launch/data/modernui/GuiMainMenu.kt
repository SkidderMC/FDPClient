/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.launch.data.modernui

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.launch.data.modernui.mainmenu.*
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.gui.*;
import net.minecraft.util.ResourceLocation
import java.awt.Color

public class GuiMainMenu : GuiScreen(), GuiYesNoCallback {
    fun drawScreen() {
        val sr = ScaledResolution(mc)
        RenderUtils.drawImage(ResourceLocation("fdpclient/background.png"), 0, 0, sr.ScaledWidth, sr.ScaledHeight)
        mc.displayGuiScreen(ModernGuiMainMenu())
    }

}