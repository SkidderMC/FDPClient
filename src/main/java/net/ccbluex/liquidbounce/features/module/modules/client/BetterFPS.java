/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client;

import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.value.ListValue;

@ModuleInfo(name = "BetterFPS", description = "Make math calc faster.", category = ModuleCategory.CLIENT, array = false, canEnable = false)
public class BetterFPS extends Module {
    private static final ListValue sinMode = new ListValue("SinMode", new String[] {"Vanilla", "Taylor", "LibGDX", "RivensFull", "RivensHalf", "Rivens"}, "Vanilla");
    private static final ListValue cosMode = new ListValue("CosMode", new String[] {"Vanilla", "Taylor", "LibGDX", "RivensFull", "RivensHalf", "Rivens"}, "Vanilla");

    public static String getSinMode(){
        return sinMode.get();
    }

    public static String getCosMode(){
        return cosMode.get();
    }
}
