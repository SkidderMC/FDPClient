/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import java.awt.*;

import static net.minecraft.client.renderer.GlStateManager.resetColor;

@Mixin(GuiButtonExt.class)
@SideOnly(Side.CLIENT)
public abstract class MixinGuiButtonExt extends GuiButton {
    @Unique
    private long startTime = -1L;

    @Unique
    private boolean lastHover = false;

    @Unique
    private float progress = xPosition;

    public MixinGuiButtonExt(int p_i1020_1_, int p_i1020_2_, int p_i1020_3_, String p_i1020_4_) {
        super(p_i1020_1_, p_i1020_2_, p_i1020_3_, p_i1020_4_);
    }

    public MixinGuiButtonExt(int p_i46323_1_, int p_i46323_2_, int p_i46323_3_, int p_i46323_4_, int p_i46323_5_, String p_i46323_6_) {
        super(p_i46323_1_, p_i46323_2_, p_i46323_3_, p_i46323_4_, p_i46323_5_, p_i46323_6_);
    }

    /**
     * @author CCBlueX
     */
    @Overwrite
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        final FontRenderer fontRenderer = mc.getLanguageManager().isCurrentLocaleUnicode() ? mc.fontRendererObj : Fonts.fontSemibold35;

        hovered = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;

        float supposedWidth = width;

        if ((Object) this instanceof GuiSlider) {
            supposedWidth *= (float) ((GuiSlider) (Object) this).sliderValue;
            hovered = true;
        }

        if (hovered != lastHover) {
            if (System.currentTimeMillis() - startTime > 200L) {
                startTime = System.currentTimeMillis();
            }
            lastHover = hovered;
        }

        long elapsed = System.currentTimeMillis() - startTime;

        float startingPos = enabled && hovered ? xPosition : progress;
        float endingPos = enabled && hovered ? xPosition + supposedWidth : xPosition;

        progress = (int) (startingPos + (endingPos - startingPos) * MathHelper.clamp_float(elapsed / 200f, 0f, 1f));

        float radius = 2.5F;

        RenderUtils.INSTANCE.withClipping(() -> {
            RenderUtils.INSTANCE.drawRoundedRect(xPosition, yPosition, xPosition + width, yPosition + height, enabled ? new Color(0F, 0F, 0F, 120 / 255f).getRGB() : new Color(0.5F, 0.5F, 0.5F, 0.5F).getRGB(), radius, RenderUtils.RoundedCorners.ALL);
            return null;
        }, () -> {
            if (enabled && progress != xPosition) {
                // Draw blue overlay
                RenderUtils.INSTANCE.drawGradientRect(xPosition, yPosition, progress, yPosition + height, Color.CYAN.darker().getRGB(), Color.BLUE.darker().getRGB(), 0F);
            }
            return null;
        });

        mc.getTextureManager().bindTexture(buttonTextures);
        mouseDragged(mc, mouseX, mouseY);

        fontRenderer.drawStringWithShadow(displayString, (float) (xPosition + width / 2 - fontRenderer.getStringWidth(displayString) / 2), yPosition + (height - 5) / 2F, 14737632);
        resetColor();
    }
}