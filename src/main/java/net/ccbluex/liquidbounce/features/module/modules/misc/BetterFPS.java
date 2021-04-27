/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.misc;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.value.ListValue;

@ModuleInfo(name = "BetterFPS", description = "fps++.", category = ModuleCategory.MISC)
public class BetterFPS extends Module {
    private static final ListValue sinMode = new ListValue("SinMode", new String[] {"Vanilla", "Taylor", "LibGDX", "RivensFull", "RivensHalf", "Rivens"}, "Vanilla");
    private static final ListValue cosMode = new ListValue("CosMode", new String[] {"Vanilla", "Taylor", "LibGDX", "RivensFull", "RivensHalf", "Rivens"}, "Vanilla");

    public static String getSinMode(){
        if (!LiquidBounce.moduleManager.getModule(BetterFPS.class).getState())
            return "Vanilla";
        return sinMode.get();
    }

    public static String getCosMode(){
        if (!LiquidBounce.moduleManager.getModule(BetterFPS.class).getState())
            return "Vanilla";
        return cosMode.get();
    }
}
