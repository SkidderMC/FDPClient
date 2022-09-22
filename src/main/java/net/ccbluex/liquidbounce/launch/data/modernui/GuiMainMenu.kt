/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.launch.data.modernui

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.launch.data.modernui.mainmenu.*
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.features.module.modules.client.HUD
import net.ccbluex.liquidbounce.ui.client.GuiBackground
import net.minecraft.client.gui.*
import net.minecraft.util.ResourceLocation
import java.awt.Color

class GuiMainMenu : GuiScreen(), GuiYesNoCallback {
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (HUD.mainMenuStyle.equals("Five")){
            mc.displayGuiScreen(ModernGuiMainMenu())
        } else {
            mc.displayGuiScreen(ClassicGuiMainMenu())
        }
        drawBackground(0)
    }

}