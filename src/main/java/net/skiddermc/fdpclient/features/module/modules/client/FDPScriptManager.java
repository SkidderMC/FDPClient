/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.features.module.modules.client;

import net.skiddermc.fdpclient.features.module.Module;
import net.skiddermc.fdpclient.features.module.ModuleCategory;
import net.skiddermc.fdpclient.features.module.ModuleInfo;
import net.skiddermc.fdpclient.launch.data.legacyui.GuiScriptLoadMenu;

@ModuleInfo(name = "FDPScriptManager", category = ModuleCategory.CLIENT)
public class FDPScriptManager extends Module {
    @Override
    public void onEnable() {
        mc.displayGuiScreen(new GuiScriptLoadMenu());
        this.setState(false);
    }
}
