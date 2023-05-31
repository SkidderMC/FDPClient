/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class ImageUtils {
    /**
     * Reads the image to a byte buffer that works with LWJGL.
     * @author func16
     */
    public static ByteBuffer readImageToBuffer(BufferedImage bufferedImage){
        try {
            int[] rgbArray = bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), null, 0, bufferedImage.getWidth());

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * rgbArray.length);
            for (int rgb : rgbArray) {
                byteBuffer.putInt(rgb << 8 | rgb >> 24 & 255);
            }
            byteBuffer.flip();
            return byteBuffer;
        } catch(NoSuchMethodError e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Resize the image to the specified width and height.
     * @author liulihaocai
     */
    public static BufferedImage resizeImage(BufferedImage image, int width,int height) {
        BufferedImage buffImg = new BufferedImage(width,height, BufferedImage.TYPE_4BYTE_ABGR);
        buffImg.getGraphics().drawImage(image.getScaledInstance(width,height, Image.SCALE_SMOOTH), 0, 0, null);
        return buffImg;
    }
}
