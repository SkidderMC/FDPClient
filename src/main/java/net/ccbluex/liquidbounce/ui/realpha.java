package net.ccbluex.liquidbounce.ui;

import java.awt.Color;

public class realpha {

    public static int rainbow(final int n) {
        return Color.getHSBColor((float)(Math.ceil((System.currentTimeMillis() + n) / 10.0) % 360.0 / 360.0), 0.5f, 1.0f).getRGB();
    }

    public static int reAlpha(final int n, final float n2) {
        final Color color = new Color(n);
        return new Color(0.003921569f * color.getRed(), 0.003921569f * color.getGreen(), 0.003921569f * color.getBlue(), n2).getRGB();
    }
}