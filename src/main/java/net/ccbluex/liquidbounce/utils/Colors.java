package net.ccbluex.liquidbounce.utils;

import java.awt.*;
import java.text.NumberFormat;

public class Colors {
    public static int getColor(int color,int a){
        Color color1=new Color(color);
        return new Color(color1.getRed(),color1.getGreen(),color1.getBlue(),a).getRGB();
    }

    public static int getColor(int brightness) {
        return getColor(brightness, brightness, brightness, 255);
    }


    public static int getColor(int red, int green, int blue, int alpha) {
        int color = 0;
        color |= alpha << 24;
        color |= red << 16;
        color |= green << 8;
        color |= blue;
        return color;
    }

}
