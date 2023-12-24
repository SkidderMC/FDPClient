package net.ccbluex.liquidbounce.utils.render;

import org.lwjgl.opengl.GL11;

import java.awt.*;

public class VertexUtils {
    public static void start(int mode) {
        GL11.glBegin(mode);
    }

    public static void add(double x, double y, Color color) {
        GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        GL11.glVertex2d(x, y);
    }

    public static void end() {
        GL11.glEnd();
    }
}