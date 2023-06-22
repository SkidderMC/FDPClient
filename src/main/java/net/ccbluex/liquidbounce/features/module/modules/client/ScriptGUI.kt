/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.ui.client.gui.modernui.GuiScriptLoadMenu

object ScriptGUI : Module("ScriptGUI", category = ModuleCategory.CLIENT, canEnable = false, defaultOn = false) {
    override fun onEnable() {
        mc.displayGuiScreen(GuiScriptLoadMenu())
    }
}
