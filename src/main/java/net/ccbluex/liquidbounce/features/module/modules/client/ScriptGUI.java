/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client;

import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.launch.data.modernui.GuiScriptLoadMenu;

@ModuleInfo(name = "ScriptGUI", category = ModuleCategory.CLIENT, canEnable = false)
public class ScriptGUI extends Module {
    @Override
    public void onEnable() {
        mc.displayGuiScreen(new GuiScriptLoadMenu());
    }
}