/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.launch.data.modernui.clickgui.files.normal;

import net.skiddermc.fdpclient.features.module.Module;
import net.skiddermc.fdpclient.features.module.ModuleCategory;
import net.skiddermc.fdpclient.features.module.ModuleManager;

import java.util.List;
import java.util.stream.Collectors;

public class Main{

    public static int categoryCount;

    public static boolean reloadModules;

    public static float allowedClickGuiHeight = 300;

    /**
     * 一个个添加
     */

    public static List<Module> getModulesInCategory(ModuleCategory c, ModuleManager moduleManager) {
        return moduleManager.getModules().stream().filter(m -> m.getCategory() == c).collect(Collectors.toList());
    }

}
