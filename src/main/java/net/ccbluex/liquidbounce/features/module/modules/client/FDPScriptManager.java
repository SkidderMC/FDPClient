package net.ccbluex.liquidbounce.features.module.modules.client;

import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.launch.data.legacyui.GuiScriptLoadMenu;

@ModuleInfo(name = "FDPScriptManager", category = ModuleCategory.CLIENT)
public class FDPScriptManager extends Module {
    @Override
    public void onEnable() {
        mc.displayGuiScreen(new GuiScriptLoadMenu());
        this.setState(false);
    }
}
