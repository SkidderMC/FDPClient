/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui.clickgui.utils.normal;

import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleManager;

import java.util.List;
import java.util.stream.Collectors;

public class Main{

    public static int categoryCount;

    public static boolean reloadModules;

    public static float allowedClickGuiHeight = 300;
    
    public static List<Module> getModulesInCategory(ModuleCategory c, ModuleManager moduleManager) {
        return moduleManager.getModules().stream().filter(m -> m.getCategory() == c).collect(Collectors.toList());
    }

}
