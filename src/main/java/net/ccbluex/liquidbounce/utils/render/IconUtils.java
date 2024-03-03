/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 *
 * This util sets a mac os dock icon
 * please include this header with all copies of this class
 * https://github.com/GlassMods/MinecraftSnippets/tree/main/MacOS-Dock-Icon-Java8
 */
package net.ccbluex.liquidbounce.utils.render;

import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class IconUtils {

    public static ByteBuffer[] fav() {
        InputStream[] streams = new InputStream[]{
                IconUtils.class.getResourceAsStream("/assets/minecraft/fdpclient/16.png"),
                IconUtils.class.getResourceAsStream("/assets/minecraft/fdpclient/32.png"),
                IconUtils.class.getResourceAsStream("/assets/minecraft/fdpclient/64.png"),
                IconUtils.class.getResourceAsStream("/assets/minecraft/fdpclient/256.png")
        };
        ByteBuffer[] buffer = new ByteBuffer[streams.length];

        try {
            for (int i = 0; i < streams.length; i++) {
                buffer[i] = readImageToBuffer(streams[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            for (InputStream s : streams) {
                IOUtils.closeQuietly(s);
            }
        }

        return buffer;
    }

    public static ByteBuffer readImageToBuffer(InputStream imageStream) throws IOException {
        BufferedImage var2 = ImageIO.read(imageStream);
        int[] var3 = var2.getRGB(0, 0, var2.getWidth(), var2.getHeight(), null, 0, var2.getWidth());
        ByteBuffer var4 = ByteBuffer.allocate(4 * var3.length);
        int[] var5 = var3;
        int var6 = var3.length;

        for (int var7 = 0; var7 < var6; ++var7) {
            int var8 = var5[var7];
            var4.putInt(var8 << 8 | var8 >> 24 & 255);
        }

        var4.flip();
        return var4;
    }

}