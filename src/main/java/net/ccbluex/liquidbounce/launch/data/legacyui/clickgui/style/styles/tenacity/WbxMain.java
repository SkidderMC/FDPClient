package net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity;

import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.Module.fonts.api.FontManager;
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.Module.fonts.impl.SimpleFontManager;
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.SideGui.SideGui;

public class WbxMain {
    public static String Name = "FDPClient";
    public static String Rank = "";
    public static String version = "";
    public static String username;
    private final SideGui sideGui = new SideGui();
    private static WbxMain INSTANCE;

    public  SideGui getSideGui() {
        return sideGui;
    }
    public static WbxMain getInstance() {
        try {
            if (INSTANCE == null) INSTANCE = new WbxMain();
            return INSTANCE;
        } catch (Throwable t) {
            //    ClientUtils.getLogger().warn(t);
            throw t;
        }
    }
    public static FontManager fontManager = SimpleFontManager.create();
    public static FontManager getFontManager() {
        return fontManager;
    }
}
