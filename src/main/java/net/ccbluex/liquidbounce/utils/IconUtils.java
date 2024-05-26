/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */

package net.ccbluex.liquidbounce.utils;

import net.minecraft.util.Util;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.Display;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;


public class IconUtils {
    
    public void setIcon(){
        if(Util.getOSType() == Util.EnumOS.OSX) {
            // set mac icon
            setDockIcon();
        } else {
            // set windows and linux icons
            setWindowIcon();
        }
    }

    private void setDockIcon(){
        // https://github.com/GlassMods/MinecraftSnippets/tree/main/MacOS-Dock-Icon-Java8
        InputStream icon = IconUtils.class.getResourceAsStream("/assets/minecraft/fdpclient/mac.png");
        if (icon != null) {
            try {
                Class<?> Application = Class.forName("com.apple.eawt.Application");
                Application.getMethod("setDockIconImage", Image.class).invoke(Application.getMethod("getApplication").invoke(null), ImageIO.read(icon));
            } catch (Exception e) { System.err.println("[ IconUtils ] Error setting dock icon: " + e.getMessage()); }
            IOUtils.closeQuietly(icon);
        } else { System.err.println("[ IconUtils ] Icon file could not be found"); }
    }

    private void setWindowIcon() {
            InputStream icon64 = null;
            InputStream icon32 = null;
            try {
                icon64 = IconUtils.class.getResourceAsStream("/assets/minecraft/fdpclient/64.png");
                icon32 = IconUtils.class.getResourceAsStream("/assets/minecraft/fdpclient/32.png");
                if (icon64 != null && icon32 != null) {
                    Display.setIcon(new ByteBuffer[]{this.readImageToBuffer(icon64), this.readImageToBuffer(icon32)}); // add 16 and 128 at some point
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(icon64);
                IOUtils.closeQuietly(icon32);
            }
    }

    // this code is aids because its skidded directly from Minecraft.class
    public ByteBuffer readImageToBuffer(InputStream imageStream) throws IOException {
        BufferedImage bufferedimage = ImageIO.read(imageStream);
        int[] aint = bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), null, 0, bufferedimage.getWidth());
        ByteBuffer bytebuffer = ByteBuffer.allocate(4 * aint.length);
        int var6 = aint.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            int i = aint[var7];
            bytebuffer.putInt(i << 8 | i >> 24 & 255);
        }

        bytebuffer.flip();
        return bytebuffer;
    }

}
