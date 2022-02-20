package net.ccbluex.liquidbounce.features.module.modules.client;

import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.value.BoolValue;

@ModuleInfo(name = "Performance", category = ModuleCategory.CLIENT)
public class Performance extends Module {
    public static BoolValue staticParticleColorValue = new BoolValue("StaticParticleColor", false);
    public static BoolValue fastEntityLightningValue = new BoolValue("FastEntityLightning", false);
    public static BoolValue fastBlockLightningValue = new BoolValue("FastBlockLightning", false);
}