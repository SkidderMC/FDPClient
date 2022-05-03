package net.ccbluex.liquidbounce.utils;

import java.awt.*;

public class Colors {
    public static int getColor(int color,int a){
        Color color1=new Color(color);
        return new Color(color1.getRed(),color1.getGreen(),color1.getBlue(),a).getRGB();
    }
}
