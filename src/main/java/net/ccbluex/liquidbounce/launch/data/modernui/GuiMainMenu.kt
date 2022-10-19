/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.launch.data.modernui

import net.ccbluex.liquidbounce.features.module.modules.client.HUD
import net.ccbluex.liquidbounce.launch.data.modernui.mainmenu.*
import net.minecraft.client.gui.*
import net.minecraft.client.settings.GameSettings
import net.minecraft.client.Minecraft;


class GuiMainMenu : GuiScreen(), GuiYesNoCallback {
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        fun isFastRenderEnabled(): Boolean {
            return try {
                val fastRender = GameSettings::class.java.getDeclaredField("ofFastRender")
                fastRender.getBoolean(Minecraft.getMinecraft().gameSettings)
            } catch (var1: Exception) {
                false
            }
        }
        if (isFastRenderEnabled()){
            mc.displayGuiScreen(ClassicGuiMainMenu())
        } else {
            mc.displayGuiScreen(ModernGuiMainMenu())
        }
        drawBackground(1)
    }

}