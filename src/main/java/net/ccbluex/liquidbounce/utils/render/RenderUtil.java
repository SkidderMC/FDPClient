package net.ccbluex.liquidbounce.utils.render;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static net.minecraft.client.renderer.GlStateManager.disableBlend;
import static net.minecraft.client.renderer.GlStateManager.enableTexture2D;
import static org.lwjgl.opengl.GL11.*;

public enum RenderUtil {
    INSTANCE;

    public static Minecraft mc = Minecraft.getMinecraft();
    private static Frustum frustrum;
    private static final Map<Integer, Boolean> glCapMap = new HashMap<>();
    public static float delta;

    private RenderUtil() {
    }

    public static void pre3D() {
        GL11.glPushMatrix();
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glShadeModel((int)7425);
        GL11.glDisable((int)3553);
        GL11.glEnable((int)2848);
        GL11.glDisable((int)2929);
        GL11.glDisable((int)2896);
        GL11.glDepthMask((boolean)false);
        GL11.glHint((int)3154, (int)4354);
    }

    public static void render(int mode, Runnable render){
        glBegin(mode);
        render.run();
        glEnd();
    }

    public static void setup2DRendering(Runnable f) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_TEXTURE_2D);
        f.run();
        glEnable(GL_TEXTURE_2D);
        GlStateManager.disableBlend();
    }

    public static Double interpolate(double oldValue, double newValue, double interpolationValue){
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }

    public static void setGlCap(final int cap, final boolean state) {
        glCapMap.put(cap, glGetBoolean(cap));
        setGlState(cap, state);
    }


    public static void setGlState(final int cap, final boolean state) {
        if (state)
            glEnable(cap);
        else
            glDisable(cap);
    }

    public static void post3D() {
        GL11.glDepthMask((boolean)true);
        GL11.glEnable((int)2929);
        GL11.glDisable((int)2848);
        GL11.glEnable((int)3553);
        GL11.glDisable((int)3042);
        GL11.glPopMatrix();
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        if (!GL11.glIsEnabled((int)2896)) {
            GL11.glEnable((int)2896);
        }
    }

    public static void drawArrow(double x, double y, int lineWidth, int color, double length) {
        start2D();
        GL11.glPushMatrix();
        GL11.glLineWidth(lineWidth);
        setColor(new Color(color));
        GL11.glBegin(GL_LINE_STRIP);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x + 3, y + length);
        GL11.glVertex2d(x + 3 * 2, y);
        GL11.glEnd();
        GL11.glPopMatrix();
        stop2D();
    }

    public static void resetColor() {
        GlStateManager.color(1, 1, 1, 1);
    }

    public static double animate(double endPoint, double current, double speed) {
        boolean shouldContinueAnimation = endPoint > current;
        if (speed < 0.0D) {
            speed = 0.0D;
        } else if (speed > 1.0D) {
            speed = 1.0D;
        }

        double dif = Math.max(endPoint, current) - Math.min(endPoint, current);
        double factor = dif * speed;
        return current + (shouldContinueAnimation ? factor : -factor);
    }

    public static void setColor(Color c) {
        GL11.glColor4d(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f);
    }

    public static void drawCheck(double x, double y, int lineWidth, int color) {
        start2D();
        GlStateManager.enableBlend();
        GL11.glPushMatrix();
        GL11.glLineWidth((float)lineWidth);
        setColor(new Color(color));
        GL11.glBegin(3);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x + 2.0, y + 2.0);
        GL11.glVertex2d(x + 5.0, y - 2.0);
        GL11.glEnd();
        GL11.glPopMatrix();
        GlStateManager.disableBlend();
        stop2D();
    }

    public static int darker(int color, float factor) {
        int r = (int)((float)(color >> 16 & 255) * factor);
        int g = (int)((float)(color >> 8 & 255) * factor);
        int b = (int)((float)(color & 255) * factor);
        int a = color >> 24 & 255;
        return (r & 255) << 16 | (g & 255) << 8 | b & 255 | (a & 255) << 24;
    }

    public static void enableRender2D() {
        GL11.glEnable(3042);
        GL11.glDisable(2884);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glBlendFunc(770, 771);
        GL11.glLineWidth(1.0f);
    }
    public static void disableRender2D() {
        GL11.glDisable(3042);
        GL11.glEnable(2884);
        GL11.glEnable(3553);
        GL11.glDisable(2848);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }



    public static void drawGradientRect(double left, double top, double right, double bottom, boolean sideways, int startColor, int endColor) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glBegin(GL11.GL_QUADS);
        color(startColor);
        if (sideways) {
            GL11.glVertex2d(left, top);
            GL11.glVertex2d(left, bottom);
            color(endColor);
            GL11.glVertex2d(right, bottom);
            GL11.glVertex2d(right, top);
        } else {
            GL11.glVertex2d(left, top);
            color(endColor);
            GL11.glVertex2d(left, bottom);
            GL11.glVertex2d(right, bottom);
            color(startColor);
            GL11.glVertex2d(right, top);
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    public static void drawRDRect(float left, float top, float width, float height, int color) {
        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, top + height, 0.0D).endVertex();
        worldrenderer.pos(left + width, top + height, 0.0D).endVertex();
        worldrenderer.pos(left + width, top, 0.0D).endVertex();
        worldrenderer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static int reAlpha(int color, float alpha) {
        Color c = new Color(color);
        float r = ((float) 1 / 255) * c.getRed();
        float g = ((float) 1 / 255) * c.getGreen();
        float b = ((float) 1 / 255) * c.getBlue();
        return new Color(r, g, b, alpha).getRGB();
    }

    public static Color blend(final Color color1, final Color color2, final double ratio) {
        final float r = (float)ratio;
        final float ir = 1.0f - r;
        final float[] rgb1 = new float[3];
        final float[] rgb2 = new float[3];
        color1.getColorComponents(rgb1);
        color2.getColorComponents(rgb2);
        final Color color3 = new Color(rgb1[0] * r + rgb2[0] * ir, rgb1[1] * r + rgb2[1] * ir, rgb1[2] * r + rgb2[2] * ir);
        return color3;
    }
    public static void doGlScissor(double mainx, double d, double e, double f) {
        Minecraft mc = Minecraft.getMinecraft();
        int scaleFactor = 1;
        int k = mc.gameSettings.guiScale;
        if (k == 0) {
            k = 1000;
        }
        while (scaleFactor < k && mc.displayWidth / (scaleFactor + 1) >= 320 && mc.displayHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor;
        }
        GL11.glScissor((int) (mainx * scaleFactor), (int) (mc.displayHeight - (d + f) * scaleFactor), (int) (e * scaleFactor), (int) (f * scaleFactor));
    }
    public static boolean isHovering(float x, float y, float width, float height, int mouseX, int mouseY) {
        return (float)mouseX >= x && (float)mouseY >= y && (float)mouseX < x + width && (float)mouseY < y + height;
    }
    public static void scale(float x, float y, float scale, Runnable data) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0.0F);
        GL11.glScalef(scale, scale, 1.0F);
        GL11.glTranslatef(-x, -y, 0.0F);
        data.run();
        GL11.glPopMatrix();
    }
    public static void start2D() {
        glEnable(3042);
        glDisable(3553);
        glBlendFunc(770, 771);
        glEnable(2848);
    }

    public static void stop2D() {
        glEnable(3553);
        glDisable(3042);
        glDisable(2848);
        enableTexture2D();
        disableBlend();
        glColor4f(1, 1, 1, 1);
    }

    public static double interpolate(double newPos, double oldPos) {
        return oldPos + (newPos - oldPos) * (double)Minecraft.getMinecraft().timer.renderPartialTicks;
    }

    public static boolean isInViewFrustrum(Entity entity) {
        return RenderUtil.isInViewFrustrum(entity.getEntityBoundingBox()) || entity.ignoreFrustumCheck;
    }

    public static void drawHorizontalLine(float x, float y, float x1, float thickness, int color) {
        RenderUtil.drawRect2(x, y, x1, y + thickness, color);
    }

    public static void drawRect2(double x, double y, double x2, double y2, int color) {
        RenderUtil.drawRect(x, y, x2, y2, color);
    }

    public static void drawVerticalLine(float x, float y, float y1, float thickness, int color) {
        RenderUtil.drawRect2(x, y, x + thickness, y1, color);
    }


    public static void drawRect(float x, float y, double x1, float y1, Color color) {
        RenderUtil.drawRect(x, y, x1, y1, color.getRGB());
    }
    public static void drawHollowBox(float x, float y, float x1, float y1, float thickness, int color) {
        RenderUtil.drawHorizontalLine(x, y, x1, thickness, color);
        RenderUtil.drawHorizontalLine(x, y1, x1, thickness, color);
        RenderUtil.drawVerticalLine(x, y, y1, thickness, color);
        RenderUtil.drawVerticalLine(x1 - thickness, y, y1, thickness, color);
    }



    public static void rectangle(double left, double top, double right, double bottom, int color) {

        double var5;

        if (left < right) {
            var5 = left;
            left = right;
            right = var5;
        }

        if (top < bottom) {
            var5 = top;
            top = bottom;
            bottom = var5;
        }

        float var11 = (float) (color >> 24 & 255) / 255.0F;
        float var6 = (float) (color >> 16 & 255) / 255.0F;
        float var7 = (float) (color >> 8 & 255) / 255.0F;
        float var8 = (float) (color & 255) / 255.0F;
        WorldRenderer BufferBuilder = Tessellator.getInstance().getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(var6, var7, var8, var11);
        BufferBuilder.begin(7, DefaultVertexFormats.POSITION);
        BufferBuilder.pos(left, bottom, 0.0D).endVertex();
        BufferBuilder.pos(right, bottom, 0.0D).endVertex();
        BufferBuilder.pos(right, top, 0.0D).endVertex();
        BufferBuilder.pos(left, top, 0.0D).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void rectangleBordered(double x, double y, double x1, double y1, double width, int internalColor,
                                         int borderColor) {
        rectangle(x + width, y + width, x1 - width, y1 - width, internalColor);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        rectangle(x + width, y, x1 - width, y + width, borderColor);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        rectangle(x, y, x + width, y1, borderColor);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        rectangle(x1 - width, y, x1, y1, borderColor);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        rectangle(x + width, y1 - width, x1 - width, y1, borderColor);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawBordered(double x2, double y2, double width, double height, double length, int innerColor, int outerColor) {
        Gui.drawRect((int) x2, (int) y2, (int) (x2 + width), (int) y2 + (int) height, innerColor);
        Gui.drawRect((int) (x2 - length), (int) y2, (int) x2, (int) y2 + (int) height, outerColor);
        Gui.drawRect((int) (x2 - length), (int) y2 - (int) length, (int) x2 + (int) width, (int) y2, outerColor);
        Gui.drawRect((int) (x2 + width), (int) y2 - (int) length, (int) x2 + (int) width + (int) length, (int) y2 + (int) height + (int) length, outerColor);
        Gui.drawRect((int) (x2 - length), (int) y2 + (int) height, (int) x2 + (int) width, (int) y2 + (int) height + (int) length, outerColor);
    }

    public static void drawImage(float x, float y, final int width, final int height, final ResourceLocation image, Color color) {
        new ScaledResolution(Minecraft.getMinecraft());
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDepthMask(false);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 1.0f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        Gui.drawModalRectWithCustomSizedTexture((int) x, (int) y, 0.0f, 0.0f, width, height, (float) width, (float) height);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
    }

    public static void drawGradientSideways(double left, double top, double right, double bottom, int col1, int col2) {
        float f = (float) (col1 >> 24 & 255) / 255.0f;
        float f1 = (float) (col1 >> 16 & 255) / 255.0f;
        float f2 = (float) (col1 >> 8 & 255) / 255.0f;
        float f3 = (float) (col1 & 255) / 255.0f;
        float f4 = (float) (col2 >> 24 & 255) / 255.0f;
        float f5 = (float) (col2 >> 16 & 255) / 255.0f;
        float f6 = (float) (col2 >> 8 & 255) / 255.0f;
        float f7 = (float) (col2 & 255) / 255.0f;
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glShadeModel(7425);
        GL11.glPushMatrix();
        GL11.glBegin(7);
        GL11.glColor4f(f1, f2, f3, f);
        GL11.glVertex2d(left, top);
        GL11.glVertex2d(left, bottom);
        GL11.glColor4f(f5, f6, f7, f4);
        GL11.glVertex2d(right, bottom);
        GL11.glVertex2d(right, top);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        GL11.glShadeModel(7424);
    }

    public static void drawScaledCustomSizeModalCircle(int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight) {
        float f = 1.0F / tileWidth;
        float f1 = 1.0F / tileHeight;
        GL11.glColor4f(1, 1, 1, 1);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer bufferbuilder = tessellator.getWorldRenderer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y + height, 0.0D).tex(u * f, (v + (float) vHeight) * f1).endVertex();
        bufferbuilder.pos(x + width, y + height, 0.0D).tex((u + (float) uWidth) * f, (v + (float) vHeight) * f1).endVertex();
        bufferbuilder.pos(x + width, y, 0.0D).tex((u + (float) uWidth) * f, v * f1).endVertex();
        bufferbuilder.pos(x, y, 0.0D).tex(u * f, v * f1).endVertex();
        tessellator.draw();
    }

    public static void drawRoundedRect(float x, float y, float width, float height, float edgeRadius, int color, float borderWidth, int borderColor) {
        if (color == 16777215) color = -65794;
        if (borderColor == 16777215) borderColor = -65794;

        if (edgeRadius < 0.0F) {
            edgeRadius = 0.0F;
        }

        if (edgeRadius > width / 2.0F) {
            edgeRadius = width / 2.0F;
        }

        if (edgeRadius > height / 2.0F) {
            edgeRadius = height / 2.0F;
        }

        drawRDRect(x + edgeRadius, y + edgeRadius, width - edgeRadius * 2.0F, height - edgeRadius * 2.0F, color);
        drawRDRect(x + edgeRadius, y, width - edgeRadius * 2.0F, edgeRadius, color);
        drawRDRect(x + edgeRadius, y + height - edgeRadius, width - edgeRadius * 2.0F, edgeRadius, color);
        drawRDRect(x, y + edgeRadius, edgeRadius, height - edgeRadius * 2.0F, color);
        drawRDRect(x + width - edgeRadius, y + edgeRadius, edgeRadius, height - edgeRadius * 2.0F, color);
        enableRender2D();
        color(color);
        GL11.glBegin(6);
        float centerX = x + edgeRadius;
        float centerY = y + edgeRadius;
        GL11.glVertex2d(centerX, centerY);
        int vertices = (int) Math.min(Math.max(edgeRadius, 10.0F), 90.0F);

        int i;
        double angleRadians;
        for (i = 0; i < vertices + 1; ++i) {
            angleRadians = 6.283185307179586D * (double) (i + 180) / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
        }

        GL11.glEnd();
        GL11.glBegin(6);
        centerX = x + width - edgeRadius;
        centerY = y + edgeRadius;
        GL11.glVertex2d(centerX, centerY);
        vertices = (int) Math.min(Math.max(edgeRadius, 10.0F), 90.0F);

        for (i = 0; i < vertices + 1; ++i) {
            angleRadians = 6.283185307179586D * (double) (i + 90) / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
        }

        GL11.glEnd();
        GL11.glBegin(6);
        centerX = x + edgeRadius;
        centerY = y + height - edgeRadius;
        GL11.glVertex2d(centerX, centerY);
        vertices = (int) Math.min(Math.max(edgeRadius, 10.0F), 90.0F);

        for (i = 0; i < vertices + 1; ++i) {
            angleRadians = 6.283185307179586D * (double) (i + 270) / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
        }

        GL11.glEnd();
        GL11.glBegin(6);

        centerX = x + width - edgeRadius;
        centerY = y + height - edgeRadius;
        GL11.glVertex2d(centerX, centerY);
        vertices = (int) Math.min(Math.max(edgeRadius, 10.0F), 90.0F);

        for (i = 0; i < vertices + 1; ++i) {
            angleRadians = 6.283185307179586D * (double) i / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
        }

        GL11.glEnd();
        color(borderColor);
        GL11.glLineWidth(borderWidth);
        GL11.glBegin(3);
        centerX = x + edgeRadius;
        centerY = y + edgeRadius;
        vertices = (int) Math.min(Math.max(edgeRadius, 10.0F), 90.0F);

        for (i = vertices; i >= 0; --i) {
            angleRadians = 6.283185307179586D * (double) (i + 180) / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
        }

        GL11.glVertex2d((x + edgeRadius), y);
        GL11.glVertex2d((x + width - edgeRadius), y);
        centerX = x + width - edgeRadius;
        centerY = y + edgeRadius;

        for (i = vertices; i >= 0; --i) {
            angleRadians = 6.283185307179586D * (double) (i + 90) / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
        }

        GL11.glVertex2d((x + width), (y + edgeRadius));
        GL11.glVertex2d((x + width), (y + height - edgeRadius));
        centerX = x + width - edgeRadius;
        centerY = y + height - edgeRadius;

        for (i = vertices; i >= 0; --i) {
            angleRadians = 6.283185307179586D * (double) i / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
        }

        GL11.glVertex2d((x + width - edgeRadius), (y + height));
        GL11.glVertex2d((x + edgeRadius), (y + height));

        centerX = x + edgeRadius;
        centerY = y + height - edgeRadius;

        for (i = vertices; i >= 0; --i) {
            angleRadians = 6.283185307179586D * (double) (i + 270) / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
        }

        GL11.glVertex2d(x, y + height - edgeRadius);
        GL11.glVertex2d(x, y + edgeRadius);
        GL11.glEnd();
        disableRender2D();
    }



    public static void drawRect(double left, double top, double right, double bottom, int color) {
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }
        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }
        float f3 = (float)(color >> 24 & 255) / 255.0f;
        float f = (float)(color >> 16 & 255) / 255.0f;
        float f1 = (float)(color >> 8 & 255) / 255.0f;
        float f2 = (float)(color & 255) / 255.0f;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0).endVertex();
        worldrenderer.pos(right, bottom, 0.0).endVertex();
        worldrenderer.pos(right, top, 0.0).endVertex();
        worldrenderer.pos(left, top, 0.0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static boolean isInViewFrustrum(AxisAlignedBB bb) {
        Entity current = Minecraft.getMinecraft().getRenderViewEntity();
        frustrum.setPosition(current.posX, current.posY, current.posZ);
        return frustrum.isBoundingBoxInFrustum(bb);
    }

    public static void drawBorderedRect(final float x, final double d, final float x2, final double e, final float l1,
                                        final int col1, final int col2) {
        Gui.drawRect((int) x, (int) d, (int) x2, (int) e, col2);
        final float f = (col1 >> 24 & 0xFF) / 255.0f;
        final float f2 = (col1 >> 16 & 0xFF) / 255.0f;
        final float f3 = (col1 >> 8 & 0xFF) / 255.0f;
        final float f4 = (col1 & 0xFF) / 255.0f;
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glPushMatrix();
        GL11.glColor4f(f2, f3, f4, f);
        GL11.glLineWidth(l1);
        GL11.glBegin(1);
        GL11.glVertex2d(x, d);
        GL11.glVertex2d(x, e);
        GL11.glVertex2d(x2, e);
        GL11.glVertex2d(x2, d);
        GL11.glVertex2d(x, d);
        GL11.glVertex2d(x2, d);
        GL11.glVertex2d(x, e);
        GL11.glVertex2d(x2, e);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
    }

    public static void drawGradientRect(float left, float top, float right, float bottom, int startColor, int endColor) {
        float f = (float) (startColor >> 24 & 255) / 255.0F;
        float f1 = (float) (startColor >> 16 & 255) / 255.0F;
        float f2 = (float) (startColor >> 8 & 255) / 255.0F;
        float f3 = (float) (startColor & 255) / 255.0F;
        float f4 = (float) (endColor >> 24 & 255) / 255.0F;
        float f5 = (float) (endColor >> 16 & 255) / 255.0F;
        float f6 = (float) (endColor >> 8 & 255) / 255.0F;
        float f7 = (float) (endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(right, top, 0).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(left, top, 0).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(left, bottom, 0).color(f5, f6, f7, f4).endVertex();
        worldrenderer.pos(right, bottom, 0).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static int getColor(int color) {
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        int a = (int) 255;
        return (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF | (a & 0xFF) << 24;
    }
    public static void drawCheckeredBackground(float x, float y, float x2, float y2) {
        RenderUtil.drawRect(x, y, x2, y2, RenderUtil.getColor(16777215));
        for(boolean offset = false; y < y2; ++y) {
            for(float x1 = x + (float)((offset = !offset) ? 1 : 0); x1 < x2; x1 += 2.0F) {
                if (x1 <= x2 - 1.0F) {
                    RenderUtil.drawRect(x1, y, x1 + 1.0F, y + 1.0F, RenderUtil.getColor(8421504));
                }
            }
        }
    }

    public static double interpolatee(double current, double old, double scale) {
        return old + (current - old) * scale;
    }

    public static void pre() {
        GL11.glDisable((int)2929);
        GL11.glDisable((int)3553);
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
    }

    public static void post() {
        GL11.glDisable((int)3042);
        GL11.glEnable((int)3553);
        GL11.glEnable((int)2929);
        GL11.glColor3d((double)1.0, (double)1.0, (double)1.0);
    }

    public static int rainbow(int delay) {
        double rainbowState = Math.ceil((double)(System.currentTimeMillis() + (long)delay) / 20.0);
        return Color.getHSBColor((float)((rainbowState %= 360.0) / 360.0), 0.5f, 0.7f).brighter().getRGB();
    }

    public static Color rainbowEffect(int delay) {
        float hue = (float)(System.nanoTime() + (long)delay) / 2.0E10f % 1.0f;
        Color color = new Color((int)Long.parseLong(Integer.toHexString(Color.HSBtoRGB(hue, 1.0f, 1.0f)), 16));
        return new Color((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
    }

    public static void drawFullscreenImage(ResourceLocation image) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        GL11.glDisable((int)2929);
        GL11.glDepthMask((boolean)false);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glDisable((int)3008);
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0.0f, 0.0f, scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight(), scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
        GL11.glDepthMask((boolean)true);
        GL11.glEnable((int)2929);
        GL11.glEnable((int)3008);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    public static void drawPlayerHead(String playerName, int x, int y, int width, int height) {
        Minecraft.getMinecraft();
        for (EntityPlayer player : Minecraft.getMinecraft().theWorld.playerEntities) {
            if (!playerName.equalsIgnoreCase(player.getName())) continue;
            GameProfile profile = new GameProfile(player.getUniqueID(), player.getName());
            NetworkPlayerInfo networkplayerinfo1 = new NetworkPlayerInfo(profile);
            ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
            GL11.glDisable((int)2929);
            GL11.glEnable((int)3042);
            GL11.glDepthMask((boolean)false);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            Minecraft.getMinecraft().getTextureManager().bindTexture(networkplayerinfo1.getLocationSkin());
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0f, 0.0f, width, height, width, height);
            GL11.glDepthMask((boolean)true);
            GL11.glDisable((int)3042);
            GL11.glEnable((int)2929);
        }
    }

    public static double getAnimationState(double n, final double n2, final double n3) {
        final float n4 = (float)(RenderUtil.delta * n3);
        if (n < n2) {
            if (n + n4 < n2) {
                n += n4;
            }else {
                n = n2;
            }
        }else if (n - n4 > n2) {
            n -= n4;
        }else {
            n = n2;
        }
        return n;
    }

    public static void glColor(Color color) {
        GL11.glColor4f((float)((float)color.getRed() / 255.0f), (float)((float)color.getGreen() / 255.0f), (float)((float)color.getBlue() / 255.0f), (float)((float)color.getAlpha() / 255.0f));
    }

    public static String getShaderCode(InputStreamReader file) {
        String shaderSource = "";
        try {
            String line;
            BufferedReader reader = new BufferedReader(file);
            while ((line = reader.readLine()) != null) {
                shaderSource = String.valueOf(String.valueOf(shaderSource)) + line + "\n";
            }
            reader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return shaderSource.toString();
    }

    public static void drawImage(ResourceLocation image, int x, int y, int width, int height) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        GL11.glDisable((int)2929);
        GL11.glEnable((int)3042);
        GL11.glDepthMask((boolean)false);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0f, 0.0f, width, height, width, height);
        GL11.glDepthMask((boolean)true);
        GL11.glDisable((int)3042);
        GL11.glEnable((int)2929);
    }

    public static void drawOutlinedRect(int x, int y, int width, int height, int lineSize, Color lineColor, Color backgroundColor) {
        RenderUtil.drawRect((float)x, (float)y, (float)width, (float)height, backgroundColor.getRGB());
        RenderUtil.drawRect((float)x, (float)y, (float)width, (float)(y + lineSize), lineColor.getRGB());
        RenderUtil.drawRect((float)x, (float)(height - lineSize), (float)width, (float)height, lineColor.getRGB());
        RenderUtil.drawRect((float)x, (float)(y + lineSize), (float)(x + lineSize), (float)(height - lineSize), lineColor.getRGB());
        RenderUtil.drawRect((float)(width - lineSize), (float)(y + lineSize), (float)width, (float)(height - lineSize), lineColor.getRGB());
    }

    public static void drawImage(ResourceLocation image, int x, int y, int width, int height, Color color) {
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        GL11.glDisable((int)2929);
        GL11.glEnable((int)3042);
        GL11.glDepthMask((boolean)false);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f((float)((float)color.getRed() / 255.0f), (float)((float)color.getBlue() / 255.0f), (float)((float)color.getRed() / 255.0f), (float)1.0f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0f, 0.0f, width, height, width, height);
        GL11.glDepthMask((boolean)true);
        GL11.glDisable((int)3042);
        GL11.glEnable((int)2929);
    }

    public static void doGlScissor(int x, int y, int width, int height) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scale = new ScaledResolution(mc);
        int scaleFactor = 1;
        int k = mc.gameSettings.guiScale;
        if (k == 0) {
            k = 1000;
        }
        while (scaleFactor < k && mc.displayWidth / (scaleFactor + 1) >= 320 && mc.displayHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor;
        }
        GL11.glScalef((float)0.5f, (float)0.5f, (float)0.5f);
        GL11.glScissor((int)(x * scaleFactor), (int)(mc.displayHeight - (y + height) * scaleFactor), (int)(width * scaleFactor), (int)(height * scaleFactor));
        GL11.glScalef((float)2.0f, (float)2.0f, (float)2.0f);
    }

    public static void doGlScissor2(int x, int y, int width, int height) {
        RenderUtil.doGlScissor(x, y, width - x, height - y);
    }

    public static void drawRect(float x1, float y1, float x2, float y2, int color) {
        GL11.glPushMatrix();
        GL11.glEnable((int)3042);
        GL11.glDisable((int)3553);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glEnable((int)2848);
        GL11.glPushMatrix();
        RenderUtil.color(color);
        GL11.glBegin((int)7);
        GL11.glVertex2d((double)x2, (double)y1);
        GL11.glVertex2d((double)x1, (double)y1);
        GL11.glVertex2d((double)x1, (double)y2);
        GL11.glVertex2d((double)x2, (double)y2);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable((int)3553);
        GL11.glDisable((int)3042);
        GL11.glDisable((int)2848);
        GL11.glPopMatrix();
    }

    public static void color(int color) {
        float f = (float)(color >> 24 & 255) / 255.0f;
        float f1 = (float)(color >> 16 & 255) / 255.0f;
        float f2 = (float)(color >> 8 & 255) / 255.0f;
        float f3 = (float)(color & 255) / 255.0f;
        GL11.glColor4f((float)f1, (float)f2, (float)f3, (float)f);
    }

    public static int createShader(String shaderCode, int shaderType) throws Exception {
        int shader = 0;
        try {
            shader = ARBShaderObjects.glCreateShaderObjectARB((int)shaderType);
            if (shader == 0) {
                return 0;
            }
        }
        catch (Exception exc) {
            ARBShaderObjects.glDeleteObjectARB((int)shader);
            throw exc;
        }
        ARBShaderObjects.glShaderSourceARB((int)shader, (CharSequence)shaderCode);
        ARBShaderObjects.glCompileShaderARB((int)shader);
        if (ARBShaderObjects.glGetObjectParameteriARB((int)shader, (int)35713) == 0) {
            throw new RuntimeException("Error creating shader:");
        }
        return shader;
    }

    public void drawCircle(int x, int y, float radius, int color) {
        float alpha = (float)(color >> 24 & 255) / 255.0f;
        float red = (float)(color >> 16 & 255) / 255.0f;
        float green = (float)(color >> 8 & 255) / 255.0f;
        float blue = (float)(color & 255) / 255.0f;
        boolean blend = GL11.glIsEnabled((int)3042);
        boolean line = GL11.glIsEnabled((int)2848);
        boolean texture = GL11.glIsEnabled((int)3553);
        if (!blend) {
            GL11.glEnable((int)3042);
        }
        if (!line) {
            GL11.glEnable((int)2848);
        }
        if (texture) {
            GL11.glDisable((int)3553);
        }
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glColor4f((float)red, (float)green, (float)blue, (float)alpha);
        GL11.glBegin((int)9);
        for (int i = 0; i <= 360; ++i) {
            GL11.glVertex2d((double)((double)x + Math.sin((double)i * 3.141526 / 180.0) * (double)radius), (double)((double)y + Math.cos((double)i * 3.141526 / 180.0) * (double)radius));
        }
        GL11.glEnd();
        if (texture) {
            GL11.glEnable((int)3553);
        }
        if (!line) {
            GL11.glDisable((int)2848);
        }
        if (!blend) {
            GL11.glDisable((int)3042);
        }
    }

    public static void outlineOne() {
        GL11.glPushAttrib((int)1048575);
        GL11.glDisable((int)3008);
        GL11.glDisable((int)3553);
        GL11.glDisable((int)2896);
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glLineWidth((float)4.0f);
        GL11.glEnable((int)2848);
        GL11.glEnable((int)2960);
        GL11.glClear((int)1024);
        GL11.glClearStencil((int)15);
        GL11.glStencilFunc((int)512, (int)1, (int)15);
        GL11.glStencilOp((int)7681, (int)7681, (int)7681);
        GL11.glPolygonMode((int)1032, (int)6913);
    }

    public static void outlineTwo() {
        GL11.glStencilFunc((int)512, (int)0, (int)15);
        GL11.glStencilOp((int)7681, (int)7681, (int)7681);
        GL11.glPolygonMode((int)1032, (int)6914);
    }

    public static void outlineThree() {
        GL11.glStencilFunc((int)514, (int)1, (int)15);
        GL11.glStencilOp((int)7680, (int)7680, (int)7680);
        GL11.glPolygonMode((int)1032, (int)6913);
    }

    public static void outlineFour() {
        GL11.glDepthMask((boolean)false);
        GL11.glDisable((int)2929);
        GL11.glEnable((int)10754);
        GL11.glPolygonOffset((float)1.0f, (float)-2000000.0f);
        GL11.glColor4f((float)0.9529412f, (float)0.6117647f, (float)0.07058824f, (float)1.0f);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f);
    }

    public static void outlineFive() {
        GL11.glPolygonOffset((float)1.0f, (float)2000000.0f);
        GL11.glDisable((int)10754);
        GL11.glEnable((int)2929);
        GL11.glDepthMask((boolean)true);
        GL11.glDisable((int)2960);
        GL11.glDisable((int)2848);
        GL11.glHint((int)3154, (int)4352);
        GL11.glEnable((int)3042);
        GL11.glEnable((int)2896);
        GL11.glEnable((int)3553);
        GL11.glEnable((int)3008);
        GL11.glPopAttrib();
    }

    public static void drawOutlinedBoundingBox(AxisAlignedBB aa) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(3, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(3, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(1, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        tessellator.draw();
    }

    public static void drawBoundingBox(AxisAlignedBB aa) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        tessellator.draw();
    }

    public static void drawOutlinedBlockESP(double x, double y, double z, float red, float green, float blue, float alpha, float lineWidth) {
        GL11.glPushMatrix();
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glDisable((int)3553);
        GL11.glEnable((int)2848);
        GL11.glDisable((int)2929);
        GL11.glDepthMask((boolean)false);
        GL11.glLineWidth((float)lineWidth);
        GL11.glColor4f((float)red, (float)green, (float)blue, (float)alpha);
        RenderUtil.drawOutlinedBoundingBox(new AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0));
        GL11.glDisable((int)2848);
        GL11.glEnable((int)3553);
        GL11.glEnable((int)2929);
        GL11.glDepthMask((boolean)true);
        GL11.glDisable((int)3042);
        GL11.glPopMatrix();
    }

    public static void drawBlockESP(double x, double y, double z, float red, float green, float blue, float alpha, float lineRed, float lineGreen, float lineBlue, float lineAlpha, float lineWidth) {
        GL11.glPushMatrix();
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glDisable((int)2896);
        GL11.glDisable((int)3553);
        GL11.glEnable((int)2848);
        GL11.glDisable((int)2929);
        GL11.glDepthMask((boolean)false);
        GL11.glColor4f((float)red, (float)green, (float)blue, (float)alpha);
        RenderUtil.drawBoundingBox(new AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0));
        GL11.glLineWidth((float)lineWidth);
        GL11.glColor4f((float)lineRed, (float)lineGreen, (float)lineBlue, (float)lineAlpha);
        RenderUtil.drawOutlinedBoundingBox(new AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0));
        GL11.glDisable((int)2848);
        GL11.glEnable((int)3553);
        GL11.glEnable((int)2896);
        GL11.glEnable((int)2929);
        GL11.glDepthMask((boolean)true);
        GL11.glDisable((int)3042);
        GL11.glPopMatrix();
    }

    public static void drawSolidBlockESP(double x, double y, double z, float red, float green, float blue, float alpha) {
        GL11.glPushMatrix();
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glDisable((int)3553);
        GL11.glEnable((int)2848);
        GL11.glDisable((int)2929);
        GL11.glDepthMask((boolean)false);
        GL11.glColor4f((float)red, (float)green, (float)blue, (float)alpha);
        RenderUtil.drawBoundingBox(new AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0));
        GL11.glDisable((int)2848);
        GL11.glEnable((int)3553);
        GL11.glEnable((int)2929);
        GL11.glDepthMask((boolean)true);
        GL11.glDisable((int)3042);
        GL11.glPopMatrix();
    }

    public static void drawOutlinedEntityESP(double x, double y, double z, double width, double height, float red, float green, float blue, float alpha) {
        GL11.glPushMatrix();
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glDisable((int)3553);
        GL11.glEnable((int)2848);
        GL11.glDisable((int)2929);
        GL11.glDepthMask((boolean)false);
        GL11.glColor4f((float)red, (float)green, (float)blue, (float)alpha);
        RenderUtil.drawOutlinedBoundingBox(new AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width));
        GL11.glDisable((int)2848);
        GL11.glEnable((int)3553);
        GL11.glEnable((int)2929);
        GL11.glDepthMask((boolean)true);
        GL11.glDisable((int)3042);
        GL11.glPopMatrix();
    }

    public static void drawSolidEntityESP(double x, double y, double z, double width, double height, float red, float green, float blue, float alpha) {
        GL11.glPushMatrix();
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glDisable((int)3553);
        GL11.glEnable((int)2848);
        GL11.glDisable((int)2929);
        GL11.glDepthMask((boolean)false);
        GL11.glColor4f((float)red, (float)green, (float)blue, (float)alpha);
        RenderUtil.drawBoundingBox(new AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width));
        GL11.glDisable((int)2848);
        GL11.glEnable((int)3553);
        GL11.glEnable((int)2929);
        GL11.glDepthMask((boolean)true);
        GL11.glDisable((int)3042);
        GL11.glPopMatrix();
    }

    public static void drawEntityESP(double x, double y, double z, double width, double height, float red, float green, float blue, float alpha, float lineRed, float lineGreen, float lineBlue, float lineAlpha, float lineWdith) {
        GL11.glPushMatrix();
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glDisable((int)3553);
        GL11.glEnable((int)2848);
        GL11.glDisable((int)2929);
        GL11.glDepthMask((boolean)false);
        GL11.glColor4f((float)red, (float)green, (float)blue, (float)alpha);
        RenderUtil.drawBoundingBox(new AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width));
        GL11.glLineWidth((float)lineWdith);
        GL11.glColor4f((float)lineRed, (float)lineGreen, (float)lineBlue, (float)lineAlpha);
        RenderUtil.drawOutlinedBoundingBox(new AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width));
        GL11.glDisable((int)2848);
        GL11.glEnable((int)3553);
        GL11.glEnable((int)2929);
        GL11.glDepthMask((boolean)true);
        GL11.glDisable((int)3042);
        GL11.glPopMatrix();
    }

    public static void drawTracerLine(double x, double y, double z, float red, float green, float blue, float alpha, float lineWdith) {
        GL11.glPushMatrix();
        GL11.glEnable((int)3042);
        GL11.glEnable((int)2848);
        GL11.glDisable((int)2929);
        GL11.glDisable((int)3553);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glEnable((int)3042);
        GL11.glLineWidth((float)lineWdith);
        GL11.glColor4f((float)red, (float)green, (float)blue, (float)alpha);
        GL11.glBegin((int)2);
        Minecraft.getMinecraft();
        Minecraft.getMinecraft();
        GL11.glVertex3d((double)0.0, (double)(0.0 + (double)mc.thePlayer.getEyeHeight()), (double)0.0);
        GL11.glVertex3d((double)x, (double)y, (double)z);
        GL11.glEnd();
        GL11.glDisable((int)3042);
        GL11.glEnable((int)3553);
        GL11.glEnable((int)2929);
        GL11.glDisable((int)2848);
        GL11.glDisable((int)3042);
        GL11.glPopMatrix();
    }

    public static void drawFilledBox(AxisAlignedBB mask) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(mask.minX, mask.minY, mask.minZ).endVertex();
        worldRenderer.pos(mask.minX, mask.maxY, mask.minZ).endVertex();
        worldRenderer.pos(mask.maxX, mask.minY, mask.minZ).endVertex();
        worldRenderer.pos(mask.maxX, mask.maxY, mask.minZ).endVertex();
        worldRenderer.pos(mask.maxX, mask.minY, mask.maxZ).endVertex();
        worldRenderer.pos(mask.maxX, mask.maxY, mask.maxZ).endVertex();
        worldRenderer.pos(mask.minX, mask.minY, mask.maxZ).endVertex();
        worldRenderer.pos(mask.minX, mask.maxY, mask.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(mask.maxX, mask.maxY, mask.minZ).endVertex();
        worldRenderer.pos(mask.maxX, mask.minY, mask.minZ).endVertex();
        worldRenderer.pos(mask.minX, mask.maxY, mask.minZ).endVertex();
        worldRenderer.pos(mask.minX, mask.minY, mask.minZ).endVertex();
        worldRenderer.pos(mask.minX, mask.maxY, mask.maxZ).endVertex();
        worldRenderer.pos(mask.minX, mask.minY, mask.maxZ).endVertex();
        worldRenderer.pos(mask.maxX, mask.maxY, mask.maxZ).endVertex();
        worldRenderer.pos(mask.maxX, mask.minY, mask.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(mask.minX, mask.maxY, mask.minZ).endVertex();
        worldRenderer.pos(mask.maxX, mask.maxY, mask.minZ).endVertex();
        worldRenderer.pos(mask.maxX, mask.maxY, mask.maxZ).endVertex();
        worldRenderer.pos(mask.minX, mask.maxY, mask.maxZ).endVertex();
        worldRenderer.pos(mask.minX, mask.maxY, mask.minZ).endVertex();
        worldRenderer.pos(mask.minX, mask.maxY, mask.maxZ).endVertex();
        worldRenderer.pos(mask.maxX, mask.maxY, mask.maxZ).endVertex();
        worldRenderer.pos(mask.maxX, mask.maxY, mask.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(mask.minX, mask.minY, mask.minZ).endVertex();
        worldRenderer.pos(mask.maxX, mask.minY, mask.minZ).endVertex();
        worldRenderer.pos(mask.maxX, mask.minY, mask.maxZ).endVertex();
        worldRenderer.pos(mask.minX, mask.minY, mask.maxZ).endVertex();
        worldRenderer.pos(mask.minX, mask.minY, mask.minZ).endVertex();
        worldRenderer.pos(mask.minX, mask.minY, mask.maxZ).endVertex();
        worldRenderer.pos(mask.maxX, mask.minY, mask.maxZ).endVertex();
        worldRenderer.pos(mask.maxX, mask.minY, mask.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(mask.minX, mask.minY, mask.minZ).endVertex();
        worldRenderer.pos(mask.minX, mask.maxY, mask.minZ).endVertex();
        worldRenderer.pos(mask.minX, mask.minY, mask.maxZ).endVertex();
        worldRenderer.pos(mask.minX, mask.maxY, mask.maxZ).endVertex();
        worldRenderer.pos(mask.maxX, mask.minY, mask.maxZ).endVertex();
        worldRenderer.pos(mask.maxX, mask.maxY, mask.maxZ).endVertex();
        worldRenderer.pos(mask.maxX, mask.minY, mask.minZ).endVertex();
        worldRenderer.pos(mask.maxX, mask.maxY, mask.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(mask.minX, mask.maxY, mask.maxZ).endVertex();
        worldRenderer.pos(mask.minX, mask.minY, mask.maxZ).endVertex();
        worldRenderer.pos(mask.minX, mask.maxY, mask.minZ).endVertex();
        worldRenderer.pos(mask.minX, mask.minY, mask.minZ).endVertex();
        worldRenderer.pos(mask.maxX, mask.maxY, mask.minZ).endVertex();
        worldRenderer.pos(mask.maxX, mask.minY, mask.minZ).endVertex();
        worldRenderer.pos(mask.maxX, mask.maxY, mask.maxZ).endVertex();
        worldRenderer.pos(mask.maxX, mask.minY, mask.maxZ).endVertex();
        tessellator.draw();
    }

    public static void checkSetupFBO() {
        Framebuffer fbo = Minecraft.getMinecraft().getFramebuffer();
        if (fbo != null && fbo.depthBuffer > -1) {
            EXTFramebufferObject.glDeleteRenderbuffersEXT((int)fbo.depthBuffer);
            int stencil_depth_buffer_ID = EXTFramebufferObject.glGenRenderbuffersEXT();
            EXTFramebufferObject.glBindRenderbufferEXT((int)36161, (int)stencil_depth_buffer_ID);
            EXTFramebufferObject.glRenderbufferStorageEXT((int)36161, (int)34041, (int)Minecraft.getMinecraft().displayWidth, (int)Minecraft.getMinecraft().displayHeight);
            EXTFramebufferObject.glFramebufferRenderbufferEXT((int)36160, (int)36128, (int)36161, (int)stencil_depth_buffer_ID);
            EXTFramebufferObject.glFramebufferRenderbufferEXT((int)36160, (int)36096, (int)36161, (int)stencil_depth_buffer_ID);
            fbo.depthBuffer = -1;
        }
    }
    public static void drawRoundedRect(float n, float n2, float n3, float n4, final int n5, final int n6) {
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glDepthMask(true);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
        GL11.glScalef(0.5f, 0.5f, 0.5f);
        drawVLine(n *= 2.0f, (n2 *= 2.0f) + 1.0f, (n4 *= 2.0f) - 2.0f, n5);
        drawVLine((n3 *= 2.0f) - 1.0f, n2 + 1.0f, n4 - 2.0f, n5);
        drawHLine(n + 2.0f, n3 - 3.0f, n2, n5);
        drawHLine(n + 2.0f, n3 - 3.0f, n4 - 1.0f, n5);
        drawHLine(n + 1.0f, n + 1.0f, n2 + 1.0f, n5);
        drawHLine(n3 - 2.0f, n3 - 2.0f, n2 + 1.0f, n5);
        drawHLine(n3 - 2.0f, n3 - 2.0f, n4 - 2.0f, n5);
        drawHLine(n + 1.0f, n + 1.0f, n4 - 2.0f, n5);
        drawRect(n + 1.0f, n2 + 1.0f, n3 - 1.0f, n4 - 1.0f, n6);
        GL11.glScalef(2.0f, 2.0f, 2.0f);
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
    }
//    public static void drawRoundedRect(float x, float y, float x2, float y2, float round, int color) {
//        x = (float)((double)x + ((double)(round / 2.0f) + 0.5));
//        y = (float)((double)y + ((double)(round / 2.0f) + 0.5));
//        x2 = (float)((double)x2 - ((double)(round / 2.0f) + 0.5));
//        y2 = (float)((double)y2 - ((double)(round / 2.0f) + 0.5));
//        Gui.drawRect(x, y, x2, y2, color);
//        Gui.circle(x2 - round / 2.0f, y + round / 2.0f, round, color);
//        Gui.circle(x + round / 2.0f, y2 - round / 2.0f, round, color);
//        Gui.circle(x + round / 2.0f, y + round / 2.0f, round, color);
//        Gui.circle(x2 - round / 2.0f, y2 - round / 2.0f, round, color);
//        Gui.drawRect(x - round / 2.0f - 0.5f, y + round / 2.0f, x2, y2 - round / 2.0f, color);
//        Gui.drawRect(x, y + round / 2.0f, x2 + round / 2.0f + 0.5f, y2 - round / 2.0f, color);
//        Gui.drawRect(x + round / 2.0f, y - round / 2.0f - 0.5f, x2 - round / 2.0f, y2 - round / 2.0f, color);
//        Gui.drawRect(x + round / 2.0f, y, x2 - round / 2.0f, y2 + round / 2.0f + 0.5f, color);
//    }

    public static void drawRoundedRect2(float var0, float var1, float var2, float var3, int var4, int var5) {
        RenderUtil.enableGL2D();
        GL11.glScalef((float)0.5f, (float)0.5f, (float)0.5f);
        RenderUtil.drawVLine(var0 *= 2.0f, (var1 *= 2.0f) + 1.0f, (var3 *= 2.0f) - 2.0f, var4);
        RenderUtil.drawVLine((var2 *= 2.0f) - 1.0f, var1 + 1.0f, var3 - 2.0f, var4);
        RenderUtil.drawHLine(var0 + 2.0f, var2 - 3.0f, var1, var4);
        RenderUtil.drawHLine(var0 + 2.0f, var2 - 3.0f, var3 - 1.0f, var4);
        RenderUtil.drawHLine(var0 + 1.0f, var0 + 1.0f, var1 + 1.0f, var4);
        RenderUtil.drawHLine(var2 - 2.0f, var2 - 2.0f, var1 + 1.0f, var4);
        RenderUtil.drawHLine(var2 - 2.0f, var2 - 2.0f, var3 - 2.0f, var4);
        RenderUtil.drawHLine(var0 + 1.0f, var0 + 1.0f, var3 - 2.0f, var4);
        RenderUtil.drawRect(var0 + 1.0f, var1 + 1.0f, var2 - 1.0f, var3 - 1.0f, var5);
        GL11.glScalef((float)2.0f, (float)2.0f, (float)2.0f);
        RenderUtil.disableGL2D();
    }

    public static void drawHLine(float x, float y, float x1, int y1) {
        if (y < x) {
            float var5 = x;
            x = y;
            y = var5;
        }
        RenderUtil.drawRect(x, x1, y + 1.0f, x1 + 1.0f, y1);
    }

    public static void drawVLine(float x, float y, float x1, int y1) {
        if (x1 < y) {
            float var5 = y;
            y = x1;
            x1 = var5;
        }
        RenderUtil.drawRect(x, y + 1.0f, x + 1.0f, x1, y1);
    }

    public static void drawRoundedRectBottem(float x, float y, float x2, float y2, float round, int color) {
        x = (float)((double)x + ((double)(round / 2.0f) + 0.0));
        y = (float)((double)y + ((double)(round / 2.0f) + 0.0));
        x2 = (float)((double)x2 - ((double)(round / 2.0f) + 0.0));
        y2 = (float)((double)y2 - ((double)(round / 2.0f) + 0.0));
        RenderUtil.drawCircle(x + round / 2.0f, y2 - round / 2.0f, round, 180, 270, color);
        RenderUtil.drawCircle(x2 - round / 2.0f, y2 - round / 2.0f, round, 270, 360, color);
        drawRect(x - round / 2.0f, y, x2 + round / 2.0f, y2 - round / 2.0f + 0.0f, color);
        drawRect(x + round / 2.0f, y2 - round / 2.0f + 0.0f, x2 - round / 2.0f, y2 + round / 2.0f + 0.0f, color);
    }

    public static void drawCircle(float x, float y, float radius, int startPi, int endPi, int c) {
        float f = (float)(c >> 24 & 255) / 255.0f;
        float f1 = (float)(c >> 16 & 255) / 255.0f;
        float f2 = (float)(c >> 8 & 255) / 255.0f;
        float f3 = (float)(c & 255) / 255.0f;
        GL11.glColor4f((float)f1, (float)f2, (float)f3, (float)f);
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GL11.glDisable((int)3553);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.alphaFunc(516, 0.001f);
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer render = tess.getWorldRenderer();
        for (double i = (double)startPi; i < (double)endPi; i += 1.0) {
            double cs = i * 3.141592653589793 / 180.0;
            double ps = (i - 1.0) * 3.141592653589793 / 180.0;
            double[] outer = new double[]{Math.cos(cs) * (double)radius, (- Math.sin(cs)) * (double)radius, Math.cos(ps) * (double)radius, (- Math.sin(ps)) * (double)radius};
            render.begin(6, DefaultVertexFormats.POSITION);
            render.pos((double)x + outer[2], (double)y + outer[3], 0.0).endVertex();
            render.pos((double)x + outer[0], (double)y + outer[1], 0.0).endVertex();
            render.pos(x, y, 0.0).endVertex();
            tess.draw();
        }
        GlStateManager.color(0.0f, 0.0f, 0.0f);
        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(516, 0.1f);
        GlStateManager.disableAlpha();
        GL11.glEnable((int)3553);
    }

    public static void drawRect(float left, float top, float right, float bottom, Color color) {
        float tessellator;
        if (left < right) {
            tessellator = left;
            left = right;
            right = tessellator;
        }
        if (top < bottom) {
            tessellator = top;
            top = bottom;
            bottom = tessellator;
        }
        Tessellator tessellator1 = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator1.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0).endVertex();
        worldrenderer.pos(right, bottom, 0.0).endVertex();
        worldrenderer.pos(right, top, 0.0).endVertex();
        worldrenderer.pos(left, top, 0.0).endVertex();
        tessellator1.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawCircle(float xx, float yy, float radius, Color col) {
        int sections = 70;
        double dAngle = 6.283185307179586 / (double)sections;
        GL11.glPushMatrix();
        GL11.glEnable((int)3042);
        GL11.glDisable((int)3553);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glEnable((int)2848);
        GL11.glLineWidth((float)1.0f);
        GL11.glShadeModel((int)7425);
        GL11.glBegin((int)2);
        for (int i = 0; i < sections; ++i) {
            float x = (float)((double)radius * Math.cos((double)i * dAngle));
            float y = (float)((double)radius * Math.sin((double)i * dAngle));
            GL11.glColor4f((float)((float)col.getRed() / 255.0f), (float)((float)col.getGreen() / 255.0f), (float)((float)col.getBlue() / 255.0f), (float)((float)col.getAlpha() / 255.0f));
            GL11.glVertex2f((float)(xx + x), (float)(yy + y));
        }
        GlStateManager.color(0.0f, 0.0f, 0.0f);
        GL11.glEnd();
        GL11.glEnable((int)3553);
        GL11.glDisable((int)3042);
        GL11.glDisable((int)2848);
        GL11.glPopMatrix();
    }

    public static void drawCircle(int x, int y, float radius, int startPi, int endPi, int c) {
        float f = (float)(c >> 24 & 255) / 255.0f;
        float f1 = (float)(c >> 16 & 255) / 255.0f;
        float f2 = (float)(c >> 8 & 255) / 255.0f;
        float f3 = (float)(c & 255) / 255.0f;
        GL11.glColor4f((float)f1, (float)f2, (float)f3, (float)f);
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GL11.glDisable((int)3553);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.alphaFunc(516, 0.001f);
        Tessellator tess = Tessellator.getInstance();
        WorldRenderer render = tess.getWorldRenderer();
        for (double i = (double)startPi; i < (double)endPi; i += 1.0) {
            double cs = i * 3.141592653589793 / 180.0;
            double ps = (i - 1.0) * 3.141592653589793 / 180.0;
            double[] outer = new double[]{Math.cos(cs) * (double)radius, (- Math.sin(cs)) * (double)radius, Math.cos(ps) * (double)radius, (- Math.sin(ps)) * (double)radius};
            render.begin(6, DefaultVertexFormats.POSITION);
            render.pos((double)x + outer[2], (double)y + outer[3], 0.0).endVertex();
            render.pos((double)x + outer[0], (double)y + outer[1], 0.0).endVertex();
            render.pos(x, y, 0.0).endVertex();
            tess.draw();
        }
        GlStateManager.color(0.0f, 0.0f, 0.0f);
        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(516, 0.1f);
        GlStateManager.disableAlpha();
        GL11.glEnable((int)3553);
    }

    public static void drawRoundedRectTop(float x, float y, float x2, float y2, float round, int color) {
        GlStateManager.disableBlend();
        x = (float)((double)x + ((double)(round / 2.0f) + 0.0));
        y = (float)((double)y + ((double)(round / 2.0f) + 0.0));
        x2 = (float)((double)x2 - ((double)(round / 2.0f) + 0.0));
        y2 = (float)((double)y2 - ((double)(round / 2.0f) + 0.0));
        RenderUtil.drawCircle((int)x2 - (int)round / 2, (int)y + (int)round / 2, round, 0, 90, color);
        RenderUtil.drawCircle((int)x + (int)round / 2, (int)y + (int)round / 2, round, 90, 180, color);
        RenderUtil.drawRect(x - round / 2.0f, y + round / 2.0f, x2, y2 + round / 2.0f, color);
        RenderUtil.drawRect(x2 + round / 2.0f - round / 2.0f, y + round / 2.0f, x2 + round / 2.0f, y2 + round / 2.0f, color);
        RenderUtil.drawRect(x + round / 2.0f, y - round / 2.0f, x2 - round / 2.0f, y + round / 2.0f, color);
        GlStateManager.disableBlend();
    }

    public static void enableGL2D() {
        GL11.glDisable((int)2929);
        GL11.glEnable((int)3042);
        GL11.glDisable((int)3553);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glDepthMask((boolean)true);
        GL11.glEnable((int)2848);
        GL11.glHint((int)3154, (int)4354);
        GL11.glHint((int)3155, (int)4354);
    }

    public static void disableGL2D() {
        GL11.glEnable((int)3553);
        GL11.glDisable((int)3042);
        GL11.glEnable((int)2929);
        GL11.glDisable((int)2848);
        GL11.glHint((int)3154, (int)4352);
        GL11.glHint((int)3155, (int)4352);
    }

    public static void startDrawing() {
        GL11.glEnable((int)3042);
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glEnable((int)2848);
        GL11.glDisable((int)3553);
        GL11.glDisable((int)2929);
        Minecraft.getMinecraft().entityRenderer.setupCameraTransform(Minecraft.getMinecraft().timer.renderPartialTicks, 0);
    }

    public static void stopDrawing() {
        GL11.glDisable((int)3042);
        GL11.glEnable((int)3553);
        GL11.glDisable((int)2848);
        GL11.glDisable((int)3042);
        GL11.glEnable((int)2929);
    }

    static {
        frustrum = new Frustum();
    }

    public static boolean isHovering(final int n, final int n2, final float n3, final float n4, final float n5, final float n6) {
        boolean b;
        if (n > n3 && n < n5 && n2 > n4 && n2 < n6) {
            b = true;
        }
        else {
            b = false;
        }
        return b;
    }

    public static void circle(final float n, final float n2, final float n3, final int n4) {
        arc(n, n2, 0.0f, 360.0f, n3, n4);
    }
    
    public static void circle(final float n, final float n2, final float n3, final Color color) {
        arc(n, n2, 0.0f, 360.0f, n3, color);
    }
    
    
    public static void arc(final float n, final float n2, final float n3, final float n4, final float n5, final int n6) {
        arcEllipse(n, n2, n3, n4, n5, n5, n6);
    }
    
    public static void arc(final float n, final float n2, final float n3, final float n4, final float n5, final Color color) {
        arcEllipse(n, n2, n3, n4, n5, n5, color);
    }
    
    public static void arcEllipse(final float n, final float n2, float n3, float n4, final float n5, final float n6, final Color color) {
        GlStateManager.color(0.0f, 0.0f, 0.0f);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
        if (n3 > n4) {
            final float n7 = n4;
            n4 = n3;
            n3 = n7;
        }
        Tessellator.getInstance().getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
        if (color.getAlpha() > 0.5f) {
            GL11.glEnable(2848);
            GL11.glLineWidth(2.0f);
            GL11.glBegin(3);
            float n8 = n4;
            while (n8 >= n3) {
                GL11.glVertex2f(n + (float)Math.cos(n8 * 3.141592653589793 / 180.0) * (n5 * 1.001f), n2 + (float)Math.sin(n8 * 3.141592653589793 / 180.0) * (n6 * 1.001f));
                n8 -= 4.0f;
            }
            GL11.glEnd();
            GL11.glDisable(2848);
        }
        GL11.glBegin(6);
        float n9 = n4;
        while (n9 >= n3) {
            GL11.glVertex2f(n + (float)Math.cos(n9 * 3.141592653589793 / 180.0) * n5, n2 + (float)Math.sin(n9 * 3.141592653589793 / 180.0) * n6);
            n9 -= 4.0f;
        }
        GL11.glEnd();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }


    public static void arcEllipse(final float n, final float n2, float n3, float n4, final float n5, final float n6, final int n7) {
        GlStateManager.color(0.0f, 0.0f, 0.0f);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
        if (n3 > n4) {
            final float n8 = n4;
            n4 = n3;
            n3 = n8;
        }
        final float p_color_3_ = (n7 >> 24 & 0xFF) / 255.0f;
        final float p_color_0_ = (n7 >> 16 & 0xFF) / 255.0f;
        final float p_color_1_ = (n7 >> 8 & 0xFF) / 255.0f;
        final float p_color_2_ = (n7 & 0xFF) / 255.0f;
        Tessellator.getInstance().getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(p_color_0_, p_color_1_, p_color_2_, p_color_3_);
        if (p_color_3_ > 0.5f) {
            GL11.glEnable(2848);
            GL11.glLineWidth(2.0f);
            GL11.glBegin(3);
            float n9 = n4;
            while (n9 >= n3) {
                GL11.glVertex2f(n + (float)Math.cos(n9 * 3.141592653589793 / 180.0) * (n5 * 1.001f), n2 + (float)Math.sin(n9 * 3.141592653589793 / 180.0) * (n6 * 1.001f));
                n9 -= 4.0f;
            }
            GL11.glEnd();
            GL11.glDisable(2848);
        }
        GL11.glBegin(6);
        float n10 = n4;
        while (n10 >= n3) {
            GL11.glVertex2f(n + (float)Math.cos(n10 * 3.141592653589793 / 180.0) * n5, n2 + (float)Math.sin(n10 * 3.141592653589793 / 180.0) * n6);
            n10 -= 4.0f;
        }
        GL11.glEnd();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawArc(float n, float n2, double n3, final int n4, final int n5, final double n6, final int n7) {
        n3 *= 2.0;
        n *= 2.0f;
        n2 *= 2.0f;
        final float n8 = (n4 >> 24 & 0xFF) / 255.0f;
        final float n9 = (n4 >> 16 & 0xFF) / 255.0f;
        final float n10 = (n4 >> 8 & 0xFF) / 255.0f;
        final float n11 = (n4 & 0xFF) / 255.0f;
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glDepthMask(true);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
        GL11.glScalef(0.5f, 0.5f, 0.5f);
        GL11.glLineWidth((float)n7);
        GL11.glEnable(2848);
        GL11.glColor4f(n9, n10, n11, n8);
        GL11.glBegin(3);
        int n12 = n5;
        while (n12 <= n6) {
            GL11.glVertex2d(n + Math.sin(n12 * 3.141592653589793 / 180.0) * n3, n2 + Math.cos(n12 * 3.141592653589793 / 180.0) * n3);
            ++n12;
        }
        GL11.glEnd();
        GL11.glDisable(2848);
        GL11.glScalef(2.0f, 2.0f, 2.0f);
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
    }
}
