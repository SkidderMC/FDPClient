/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.utils;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.awt.*;

public final class RenderUtils {

    public static void texture(final double x, final double y, final float u, final float v, final double width, final double height, final float textureWidth, final float textureHeight, final Color color) {
        final float valueWidth = 1.0F / textureWidth, valueHeight = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();

        GlStateManager.color((float) color.getRed() / 255.0F, (float) color.getGreen() / 255.0F, (float) color.getBlue() / 255.0F, (float) color.getAlpha() / 255.0F);

        renderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        renderer.pos(x, y + height, 0.0D).tex(u * valueWidth, (v + (float) height) * valueHeight).endVertex();
        renderer.pos(x + width, y + height, 0.0D).tex((u + (float) width) * valueWidth, (v + (float) height) * valueHeight).endVertex();
        renderer.pos(x + width, y, 0.0D).tex((u + (float) width) * valueWidth, v * valueHeight).endVertex();
        renderer.pos(x, y, 0.0D).tex(u * valueWidth, v * valueHeight).endVertex();

        tessellator.draw();

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void rectangle(final float x, final float y, final float width, final float height, final Color color) {
        GlStateManager.enableBlend();

        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        WorldRenderer renderer = Tessellator.getInstance().getWorldRenderer();

        GlStateManager.color((float) color.getRed() / 255.0F, (float) color.getGreen() / 255.0F, (float) color.getBlue() / 255.0F, (float) color.getAlpha() / 255.0F);

        renderer.begin(7, DefaultVertexFormats.POSITION);
        renderer.pos(x, y + height, 0.0D).endVertex();
        renderer.pos(x + width, y + height, 0.0D).endVertex();
        renderer.pos(x + width, y, 0.0D).endVertex();
        renderer.pos(x, y, 0.0D).endVertex();

        Tessellator.getInstance().draw();

        GlStateManager.enableTexture2D();

        GlStateManager.disableBlend();

        GlStateManager.bindTexture(0);
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

}
