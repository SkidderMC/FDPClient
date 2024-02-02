/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 *
 * This util sets a mac os dock icon
 * please include this header with all copies of this class
 * https://github.com/GlassMods
 */
package net.ccbluex.liquidbounce.utils;

import net.minecraft.util.Util;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

public class MacOS {
    public static void icon(){
        if(Util.getOSType() != Util.EnumOS.OSX) { return; } // redundant check but you never know with fdp :skull:
        InputStream iconStream = MacOS.class.getResourceAsStream("/assets/minecraft/fdpclient/misc/Darwin.png");
        if (iconStream != null) {
            try {
                BufferedImage icon = ImageIO.read(iconStream);
                Class<?> applicationClass = Class.forName("com.apple.eawt.Application");
                Object applicationInstance = applicationClass.getMethod("getApplication").invoke(null);
                applicationClass.getMethod("setDockIconImage", Image.class).invoke(applicationInstance, icon);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | IOException e) {
                System.err.println("[ MAC OS ] Error setting icon: " + e.getMessage());
            }
        } else {
            System.err.println("[ MAC OS ] Icon not found");
        }
    }
}
