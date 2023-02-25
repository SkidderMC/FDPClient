package net.ccbluex.liquidbounce.features.module.modules.render;

import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.FloatValue;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.FloatValue;

@ModuleInfo(name = "PlayerEdit", description = "Edit the player.", category = ModuleCategory.RENDER)
public class PlayerEdit extends Module {
    public static BoolValue editPlayerSizeValue = new BoolValue("EditPlayerSize", false);
    public static FloatValue playerSizeValue = new FloatValue("PlayerSize", 0.5f,0.01f,5f, editPlayerSizeValue::get);
    public static BoolValue rotatePlayer = new BoolValue("RotatePlayer", false);
    public static BoolValue baby = new BoolValue("Baby", false);
    public static BoolValue onlyMe = new BoolValue("OnlyMe", false, baby::get);
    public static BoolValue onlyOther = new BoolValue("OnlyOther", false, baby::get);
}
