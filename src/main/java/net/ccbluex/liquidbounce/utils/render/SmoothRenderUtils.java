/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render;

import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public enum SmoothRenderUtils {
    ;

    public static void drawRect(float left, float top, float right, float bottom, final int color) {
        if (left < right) {
            final float f3 = left;
            left = right;
            right = f3;
        }
        if (top < bottom) {
            final float f3 = top;
            top = bottom;
            bottom = f3;
        }
        final float f3 = (color >> 24 & 0xFF) / 255.0f;
        final float f4 = (color >> 16 & 0xFF) / 255.0f;
        final float f5 = (color >> 8 & 0xFF) / 255.0f;
        final float f6 = (color & 0xFF) / 255.0f;
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer WorldRenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f4, f5, f6, f3);
        WorldRenderer.begin(7, DefaultVertexFormats.POSITION);
        WorldRenderer.pos(left, bottom, 0.0).endVertex();
        WorldRenderer.pos(right, bottom, 0.0).endVertex();
        WorldRenderer.pos(right, top, 0.0).endVertex();
        WorldRenderer.pos(left, top, 0.0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void circle(final float x, final float y, final float radius, final int fill) {
        arc(x, y, 0.0f, 360.0f, radius, fill);
    }

    public static void arc(final float x, final float y, final float start, final float end, final float radius, final int color) {
        arcEllipse(x, y, start, end, radius, radius, color);
    }

    public static void arcEllipse(final float x, final float y, float start, float end, final float w, final float h, final int color) {
        GlStateManager.color(0.0f, 0.0f, 0.0f);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
        float temp = 0.0f;
        if (start > end) {
            temp = end;
            end = start;
            start = temp;
        }
        final float var11 = (color >> 24 & 0xFF) / 255.0f;
        final float var12 = (color >> 16 & 0xFF) / 255.0f;
        final float var13 = (color >> 8 & 0xFF) / 255.0f;
        final float var14 = (color & 0xFF) / 255.0f;
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(var12, var13, var14, var11);
        if (var11 > 0.5f) {
            GL11.glEnable(2848);
            GL11.glLineWidth(2.0f);
            GL11.glBegin(3);
            for (float i = end; i >= start; i -= 4.0f) {
                final float ldx = (float)Math.cos(i * 3.141592653589793 / 180.0) * w * 1.001f;
                final float ldy = (float)Math.sin(i * 3.141592653589793 / 180.0) * h * 1.001f;
                GL11.glVertex2f(x + ldx, y + ldy);
            }
            GL11.glEnd();
            GL11.glDisable(2848);
        }
        GL11.glBegin(6);
        for (float i = end; i >= start; i -= 4.0f) {
            final float ldx = (float)Math.cos(i * 3.141592653589793 / 180.0) * w;
            final float ldy = (float)Math.sin(i * 3.141592653589793 / 180.0) * h;
            GL11.glVertex2f(x + ldx, y + ldy);
        }
        GL11.glEnd();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawRect(double d, double e, double g, double h, final int color) {
        if (d < g) {
            final int i = (int)d;
            d = g;
            g = i;
        }
        if (e < h) {
            final int j = (int)e;
            e = h;
            h = j;
        }
        final float f3 = (color >> 24 & 0xFF) / 255.0f;
        final float f4 = (color >> 16 & 0xFF) / 255.0f;
        final float f5 = (color >> 8 & 0xFF) / 255.0f;
        final float f6 = (color & 0xFF) / 255.0f;
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f4, f5, f6, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(d, h, 0.0).endVertex();
        worldrenderer.pos(g, h, 0.0).endVertex();
        worldrenderer.pos(g, e, 0.0).endVertex();
        worldrenderer.pos(d, e, 0.0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawRoundRect(float x, float y, float x2, float y2,float round,int color) {
        round=round/1.2f;
        x += (float)(round / 2.0f + 0.5);
        y += (float)(round / 2.0f + 0.5);
        x2 -= (float)(round / 2.0f + 0.5);
        y2 -= (float)(round / 2.0f + 0.5);
        r2DUtils.drawRect(x, y, x2, y2, color);
        r2DUtils.enableGL2D();
        circle(x2 - round / 2.0f, y + round / 2.0f, round, color);
        circle(x + round / 2.0f, y2 - round / 2.0f, round, color);
        circle(x + round / 2.0f, y + round / 2.0f, round, color);
        circle(x2 - round / 2.0f, y2 - round / 2.0f, round, color);
        r2DUtils.disableGL2D();

        r2DUtils.drawRect((x - round / 2.0f - 0.5f), (y + round / 2.0f), x2, (y2 - round / 2.0f), color);
        drawRect(x, (y + round / 2.0f), (x2 + round / 2.0f + 0.5f), (y2 - round / 2.0f), color);
        drawRect((x + round / 2.0f), (y - round / 2.0f - 0.5f), (x2 - round / 2.0f), (y2 - round / 2.0f), color);
        r2DUtils.drawRect((x + round / 2.0f), y, (x2 - round / 2.0f), (y2 + round / 2.0f + 0.5f), color);
    }

    public static class r2DUtils {
        public static void enableGL2D() {
            GL11.glDisable((int) 2929);
            GL11.glEnable((int) 3042);
            GL11.glDisable((int) 3553);
            GL11.glBlendFunc((int) 770, (int) 771);
            GL11.glDepthMask((boolean) true);
            GL11.glEnable((int) 2848);
            GL11.glHint((int) 3154, (int) 4354);
            GL11.glHint((int) 3155, (int) 4354);
        }

        public static void disableGL2D() {
            GL11.glEnable((int) 3553);
            GL11.glDisable((int) 3042);
            GL11.glEnable((int) 2929);
            GL11.glDisable((int) 2848);
            GL11.glHint((int) 3154, (int) 4352);
            GL11.glHint((int) 3155, (int) 4352);
        }

        public static void glColor(int hex) {
            float alpha = (float) (hex >> 24 & 255) / 255.0f;
            float red = (float) (hex >> 16 & 255) / 255.0f;
            float green = (float) (hex >> 8 & 255) / 255.0f;
            float blue = (float) (hex & 255) / 255.0f;
            GL11.glColor4f((float) red, (float) green, (float) blue, (float) alpha);
        }

        public static void drawRect(float x, float y, float x1, float y1, int color) {
            r2DUtils.enableGL2D();
            glColor(color);
            r2DUtils.drawRect(x, y, x1, y1);
            r2DUtils.disableGL2D();
        }

        public static void drawRect(float x, float y, float x1, float y1) {
            GL11.glBegin((int) 7);
            GL11.glVertex2f((float) x, (float) y1);
            GL11.glVertex2f((float) x1, (float) y1);
            GL11.glVertex2f((float) x1, (float) y);
            GL11.glVertex2f((float) x, (float) y);
            GL11.glEnd();
        }
    }
}
