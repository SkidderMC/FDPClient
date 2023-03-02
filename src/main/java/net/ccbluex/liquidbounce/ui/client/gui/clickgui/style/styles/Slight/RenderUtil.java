package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.Slight;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public enum RenderUtil {

    ;

    public static int reAlpha(int color, float alpha) {
        Color c = new Color(color);
        float r = 0.003921569F * (float) c.getRed();
        float g = 0.003921569F * (float) c.getGreen();
        float b = 0.003921569F * (float) c.getBlue();

        return (new Color(r, g, b, alpha)).getRGB();
    }

    public static void drawGradientRect2(double left, double top, double right, double bottom, int startColor, int endColor) {
        float sa = (float) (startColor >> 24 & 255) / 255.0F;
        float sr = (float) (startColor >> 16 & 255) / 255.0F;
        float sg = (float) (startColor >> 8 & 255) / 255.0F;
        float sb = (float) (startColor & 255) / 255.0F;
        float ea = (float) (endColor >> 24 & 255) / 255.0F;
        float er = (float) (endColor >> 16 & 255) / 255.0F;
        float eg = (float) (endColor >> 8 & 255) / 255.0F;
        float eb = (float) (endColor & 255) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(left, bottom, 0.0D).color(sr, sg, sb, sa).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).color(er, eg, eb, ea).endVertex();
        worldrenderer.pos(right, top, 0.0D).color(er, eg, eb, ea).endVertex();
        worldrenderer.pos(left, top, 0.0D).color(sr, sg, sb, sa).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawDimRect(double x, double y, double x2, double y2, int col1) {
        drawRect(x, y, x2, y2, col1);
        float f2 = (float) (col1 >> 16 & 255) / 255.0F;
        float f3 = (float) (col1 >> 8 & 255) / 255.0F;
        float f4 = (float) (col1 & 255) / 255.0F;

        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glPushMatrix();
        GL11.glColor4f(f2, f3, f4, 0.2F);
        GL11.glLineWidth(2.0F);
        GL11.glBegin(1);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x, y2);
        GL11.glVertex2d(x2, y2);
        GL11.glVertex2d(x2, y);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x2, y);
        GL11.glVertex2d(x, y2);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
    }

    public static int height() {
        return (new ScaledResolution(Minecraft.getMinecraft())).getScaledHeight();
    }


    public static double getAnimationState(double animation, double finalState, double speed) {
        float add = (float) (0.01D * speed);

        animation = animation < finalState ? (animation + (double) add < finalState ? animation + (double) add : finalState) : (animation - (double) add > finalState ? animation - (double) add : finalState);
        return animation;
    }

    public static void drawImage(ResourceLocation image, int x, int y, int width, int height2) {
        drawImage(image, x, y, width, height2, 1.0F);
    }

    public static void drawImage(ResourceLocation image, int x, int y, int width, int height2, float alpha) {
        new ScaledResolution(Minecraft.getMinecraft());
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDepthMask(false);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha);
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, width, height2, (float) width, (float) height2);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
    }

    public static void doGlScissor(int x, int y, int width, int height2) {
        Minecraft mc = Minecraft.getMinecraft();
        int scaleFactor = 1;
        int k = mc.gameSettings.guiScale;

        if (k == 0) {
            k = 1000;
        }

        while (scaleFactor < k && mc.displayWidth / (scaleFactor + 1) >= 320 && mc.displayHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor;
        }

        GL11.glScissor(x * scaleFactor, mc.displayHeight - (y + height2) * scaleFactor, width * scaleFactor, height2 * scaleFactor);
    }

    public static void drawRect(float left, float top, float right, float bottom, int color) {
        float f3;

        if (left < right) {
            f3 = left;
            left = right;
            right = f3;
        }

        if (top < bottom) {
            f3 = top;
            top = bottom;
            bottom = f3;
        }

        f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer WorldRenderer2 = tessellator.getWorldRenderer();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        WorldRenderer2.begin(7, DefaultVertexFormats.POSITION);
        WorldRenderer2.pos((double) left, (double) bottom, 0.0D).endVertex();
        WorldRenderer2.pos((double) right, (double) bottom, 0.0D).endVertex();
        WorldRenderer2.pos((double) right, (double) top, 0.0D).endVertex();
        WorldRenderer2.pos((double) left, (double) top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void circle(float x, float y, float radius, int fill) {
        arc(x, y, 0.0F, 360.0F, radius, fill);
    }

    public static void circle(float x, float y, float radius, Color fill) {
        arc(x, y, 0.0F, 360.0F, radius, fill);
    }

    public static void arc(float x, float y, float start, float end, float radius, int color) {
        arcEllipse(x, y, start, end, radius, radius, color);
    }

    public static void arc(float x, float y, float start, float end, float radius, Color color) {
        arcEllipse(x, y, start, end, radius, radius, color);
    }

    public static void arcEllipse(float x, float y, float start, float end, float w, float h, int color) {
        GlStateManager.color(0.0F, 0.0F, 0.0F);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
        float temp = 0.0F;

        if (start > end) {
            temp = end;
            end = start;
            start = temp;
        }

        float f = (float) (color >> 24 & 255) / 255.0F;
        float f1 = (float) (color >> 16 & 255) / 255.0F;
        float f2 = (float) (color >> 8 & 255) / 255.0F;
        float f3 = (float) (color & 255) / 255.0F;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f1, f2, f3, f);
        float ldy;
        float ldx;
        float i;

        if (f > 0.5F) {
            GL11.glEnable(2848);
            GL11.glLineWidth(2.0F);
            GL11.glBegin(3);

            for (i = end; i >= start; i -= 4.0F) {
                ldx = (float) Math.cos((double) i * 3.141592653589793D / 180.0D) * w * 1.001F;
                ldy = (float) Math.sin((double) i * 3.141592653589793D / 180.0D) * h * 1.001F;
                GL11.glVertex2f(x + ldx, y + ldy);
            }

            GL11.glEnd();
            GL11.glDisable(2848);
        }

        GL11.glBegin(6);

        for (i = end; i >= start; i -= 4.0F) {
            ldx = (float) Math.cos((double) i * 3.141592653589793D / 180.0D) * w;
            ldy = (float) Math.sin((double) i * 3.141592653589793D / 180.0D) * h;
            GL11.glVertex2f(x + ldx, y + ldy);
        }

        GL11.glEnd();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void arcEllipse(float x, float y, float start, float end, float w, float h, Color color) {
        GlStateManager.color(0.0F, 0.0F, 0.0F);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
        float temp = 0.0F;

        if (start > end) {
            temp = end;
            end = start;
            start = temp;
        }

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color((float) color.getRed() / 255.0F, (float) color.getGreen() / 255.0F, (float) color.getBlue() / 255.0F, (float) color.getAlpha() / 255.0F);
        float ldy;
        float ldx;
        float i;

        if ((float) color.getAlpha() > 0.5F) {
            GL11.glEnable(2848);
            GL11.glLineWidth(2.0F);
            GL11.glBegin(3);

            for (i = end; i >= start; i -= 4.0F) {
                ldx = (float) Math.cos((double) i * 3.141592653589793D / 180.0D) * w * 1.001F;
                ldy = (float) Math.sin((double) i * 3.141592653589793D / 180.0D) * h * 1.001F;
                GL11.glVertex2f(x + ldx, y + ldy);
            }

            GL11.glEnd();
            GL11.glDisable(2848);
        }

        GL11.glBegin(6);

        for (i = end; i >= start; i -= 4.0F) {
            ldx = (float) Math.cos((double) i * 3.141592653589793D / 180.0D) * w;
            ldy = (float) Math.sin((double) i * 3.141592653589793D / 180.0D) * h;
            GL11.glVertex2f(x + ldx, y + ldy);
        }

        GL11.glEnd();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawRect(double d, double e, double g, double h, int color) {
        int f3;

        if (d < g) {
            f3 = (int) d;
            d = g;
            g = (double) f3;
        }

        if (e < h) {
            f3 = (int) e;
            e = h;
            h = (double) f3;
        }

        float f31 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f31);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(d, h, 0.0D).endVertex();
        worldrenderer.pos(g, h, 0.0D).endVertex();
        worldrenderer.pos(g, e, 0.0D).endVertex();
        worldrenderer.pos(d, e, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

}
