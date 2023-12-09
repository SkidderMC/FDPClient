/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.clickgui.fonts.logo;

import net.ccbluex.liquidbounce.ui.clickgui.fonts.api.FontManager;
import net.ccbluex.liquidbounce.ui.clickgui.fonts.impl.SimpleFontManager;

public class info {
    public static String Name = "FDPCLIENT";

    public static String version = "";
    public static String username;
    private static info INSTANCE;
    public static info getInstance() {
        if (INSTANCE == null) INSTANCE = new info();
        return INSTANCE;
    }
    public static final FontManager fontManager = SimpleFontManager.create();
    public static FontManager getFontManager() {
        return fontManager;
    }
}