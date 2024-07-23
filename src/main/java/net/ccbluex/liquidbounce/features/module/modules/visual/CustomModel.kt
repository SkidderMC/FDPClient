/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual;

import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import me.zywl.fdpclient.value.impl.BoolValue;
import me.zywl.fdpclient.value.impl.IntegerValue;
import me.zywl.fdpclient.value.impl.ListValue;

@ModuleInfo(name = "CustomModel", description = "Custom player.", category = ModuleCategory.VISUAL)
public class CustomModel extends Module {

    public static BoolValue customModel = new BoolValue("CustomModel", false);
    public static BoolValue onlyMe = new BoolValue("OnlyMe", true);
    public static BoolValue onlyOther = new BoolValue("OnlyOther", true);
    public static ListValue mode = new ListValue("Mode", new String[]{"Freddy", "Rabbit", "Amogus"}, "Amogus");
    public static IntegerValue bodyColorR = new IntegerValue("BodyR", 255,0,255, () -> mode.get().equalsIgnoreCase("Amogus"));
    public static IntegerValue bodyColorG = new IntegerValue("BodyG", 0,0,255, () -> mode.get().equalsIgnoreCase("Amogus"));
    public static IntegerValue bodyColorB = new IntegerValue("BodyB", 0,0,255, () -> mode.get().equalsIgnoreCase("Amogus"));
    public static IntegerValue eyeColorR = new IntegerValue("EyeR", 255,0,255, () -> mode.get().equalsIgnoreCase("Amogus"));
    public static IntegerValue eyeColorG = new IntegerValue("EyeG", 255,0,255, () -> mode.get().equalsIgnoreCase("Amogus"));
    public static IntegerValue eyeColorB = new IntegerValue("EyeB", 255,0,255, () -> mode.get().equalsIgnoreCase("Amogus"));
    public static IntegerValue legsColorR = new IntegerValue("LegsR", 255,0,255, () -> mode.get().equalsIgnoreCase("Amogus"));
    public static IntegerValue legsColorG = new IntegerValue("LegsG", 255,0,255, () -> mode.get().equalsIgnoreCase("Amogus"));
    public static IntegerValue legsColorB = new IntegerValue("LegsB", 255,0,255, () -> mode.get().equalsIgnoreCase("Amogus"));
}
