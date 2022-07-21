package net.ccbluex.liquidbounce.features.module.modules.client;

import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.newVer.NewUi;
import net.ccbluex.liquidbounce.value.BoolValue;

@ModuleInfo(name = "NewGUI", category = ModuleCategory.CLIENT, forceNoSound = true)
public class NewGUI extends Module {
    public static final BoolValue fastRenderValue = new BoolValue("FastRender", false);
    @Override
    public void onEnable() {
        mc.displayGuiScreen(NewUi.getInstance());
    }
}