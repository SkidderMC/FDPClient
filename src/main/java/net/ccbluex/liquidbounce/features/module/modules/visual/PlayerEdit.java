package net.ccbluex.liquidbounce.features.module.modules.visual;

import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.FloatValue;
import net.ccbluex.liquidbounce.value.IntegerValue;
import net.ccbluex.liquidbounce.value.ListValue;

@ModuleInfo(name = "PlayerEdit", description = "Edit the player.", category = ModuleCategory.VISUAL)
public class PlayerEdit extends Module {

    public static BoolValue editPlayerSizeValue = new BoolValue("EditPlayerSize", false);
    public static FloatValue playerSizeValue = new FloatValue("PlayerSize", 0.5f,0.01f,5f);
    public static BoolValue rotatePlayer = new BoolValue("RotatePlayer", false);
    public static BoolValue customModel = new BoolValue("CustomModel", false);
    public static ListValue mode = new ListValue("Mode", new String[]{"Freddy", "Rabbit", "Amogus"}, "Freddy");
    public static IntegerValue bodyColorR = new IntegerValue("BodyR", 255,0,255, () -> mode.get().equalsIgnoreCase("Amogus"));
    public static IntegerValue bodyColorG = new IntegerValue("BodyG", 255,0,255, () -> mode.get().equalsIgnoreCase("Amogus"));
    public static IntegerValue bodyColorB = new IntegerValue("BodyB", 255,0,255, () -> mode.get().equalsIgnoreCase("Amogus"));
    public static IntegerValue eyeColorR = new IntegerValue("EyeR", 255,0,255, () -> mode.get().equalsIgnoreCase("Amogus"));
    public static IntegerValue eyeColorG = new IntegerValue("EyeG", 255,0,255, () -> mode.get().equalsIgnoreCase("Amogus"));
    public static IntegerValue eyeColorB = new IntegerValue("EyeB", 255,0,255, () -> mode.get().equalsIgnoreCase("Amogus"));
    public static IntegerValue legsColorR = new IntegerValue("LegsR", 255,0,255, () -> mode.get().equalsIgnoreCase("Amogus"));
    public static IntegerValue legsColorG = new IntegerValue("LegsG", 255,0,255, () -> mode.get().equalsIgnoreCase("Amogus"));
    public static IntegerValue legsColorB = new IntegerValue("LegsB", 255,0,255, () -> mode.get().equalsIgnoreCase("Amogus"));
    public static BoolValue baby = new BoolValue("Baby", false);
    public static BoolValue onlyMe = new BoolValue("OnlyMe", false);
    public static BoolValue onlyOther = new BoolValue("OnlyOther", false);
}
