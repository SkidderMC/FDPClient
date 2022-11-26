package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.Slight;

import net.ccbluex.liquidbounce.features.value.FloatValue;
import net.ccbluex.liquidbounce.features.value.IntegerValue;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer;
import net.ccbluex.liquidbounce.utils.render.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import java.awt.*;

public class UISlider {

    FloatValue value;
    IntegerValue valuee;
    public float x;
    public float y;
    public int x2;
    public int y2;
    private boolean isDraging;
    private boolean clickNotDraging;
    public boolean showValue;
    public int tX;
    public int tY;
    public int dragX;
    public int dragY;
    float ani = 0.0F;
    float aniH;

    public UISlider(FloatValue value) {
        this.value = value;
    }

    public UISlider(IntegerValue valuee) {
        this.valuee = valuee;
    }

    public static Color rainbow(long time, float count, float fade) {
        float hue = ((float) time + (1.0E-9F + count) * 4.0E8F) / 1.75000003E10F * 3.0F;
        long color = Long.parseLong(Integer.toHexString(Integer.valueOf(Color.HSBtoRGB(hue, 0.5F, 1.0F)).intValue()), 16);
        Color c = new Color((int) color);

        return new Color((float) c.getRed() / 255.0F * fade, (float) c.getGreen() / 255.0F * fade, (float) c.getBlue() / 255.0F * fade, (float) c.getAlpha() / 255.0F);
    }

    public void draw(float x, float y) {
        boolean countMod = false;
        int[] counter = new int[1];
        int rainbowCol = rainbow(System.nanoTime() * 3L, (float) counter[0], 1.0F).getRGB();
        Color col = new Color(rainbowCol);
        int Ranbow = (new Color(0, col.getGreen() / 3 + 40, col.getGreen() / 2 + 100)).getRGB();
        int Ranbow1 = (new Color(0, col.getGreen() / 3 + 20, col.getGreen() / 2 + 100)).getRGB();
        GameFontRenderer tahoma20 = Fonts.font35;
        float n = (((Float) this.value.getValue()).floatValue() - this.value.getMinimum()) / (this.value.getMaximum() - this.value.getMinimum());

        tahoma20.drawString(this.value.getName(), x - 75.0F, y - 3.0F, Colors.WHITE.c);
        Fonts.font35.drawString(this.value.getValue() + "", x + 250.0F - (float) Fonts.font35.getStringWidth(this.value.getValue() + ""), y - 1.0F, (new Color(Colors.GREY.c)).brighter().brighter().getRGB());
        if (this.ani == 0.0F) {
            this.ani = (float) ((double) (x + 250.0F) - (370.0D - 370.0D * (double) n));
        }

        this.ani = (float) RenderUtil.getAnimationState((double) this.ani, (double) ((float) ((double) (x + 250.0F) - (370.0D - 370.0D * (double) n))), (double) ((float) Math.max(10.0D, Math.abs((double) this.ani - ((double) (x + 120.0F) - (370.0D - 370.0D * (double) n))) * 30.0D * 0.3D)));
        if (this.showValue) {
            ;
        }

        RenderUtil.drawRect(x - 75.0F, y + 3.0F + 11.0F, x + 250.0F, y + 4.0F + 11.0F, (new Color(60, 60, 60)).getRGB());
        RenderUtil.drawGradientRect2((double) (x - 75.0F), (double) (y + 3.0F + 11.0F), (double) this.ani, (double) (y + 4.0F + 11.0F), Ranbow, (new Color(4555775)).getRGB());
    }

    public void draww(float x, float y) {
        boolean countMod = false;
        int[] counter = new int[1];
        int rainbowCol = rainbow(System.nanoTime() * 3L, (float) counter[0], 1.0F).getRGB();
        Color col = new Color(rainbowCol);
        int Ranbow = (new Color(0, col.getGreen() / 3 + 40, col.getGreen() / 2 + 100)).getRGB();
        int Ranbow1 = (new Color(0, col.getGreen() / 3 + 20, col.getGreen() / 2 + 100)).getRGB();
        GameFontRenderer tahoma20 = Fonts.font35;
        int n = (((Integer) this.valuee.getValue()).intValue() - this.valuee.getMinimum()) / (this.valuee.getMaximum() - this.valuee.getMinimum());

        tahoma20.drawString(this.valuee.getName(), x - 75.0F, y - 3.0F, Colors.WHITE.c);
        Fonts.font35.drawString(this.valuee.getValue() + "", x + 250.0F - (float) Fonts.font35.getStringWidth(this.valuee.getValue() + ""), y - 1.0F, (new Color(Colors.GREY.c)).brighter().brighter().getRGB());
        if (this.ani == 0.0F) {
            this.ani = (float) ((double) (x + 250.0F) - (370.0D - 370.0D * (double) n));
        }

        this.ani = (float) RenderUtil.getAnimationState((double) this.ani, (double) ((float) ((double) (x + 250.0F) - (370.0D - 370.0D * (double) n))), (double) ((float) Math.max(10.0D, Math.abs((double) this.ani - ((double) (x + 120.0F) - (370.0D - 370.0D * (double) n))) * 30.0D * 0.3D)));
        if (this.showValue) {
            ;
        }

        RenderUtil.drawRect(x - 75.0F, y + 3.0F + 11.0F, x + 250.0F, y + 4.0F + 11.0F, (new Color(60, 60, 60)).getRGB());
        RenderUtil.drawGradientRect2((double) (x - 75.0F), (double) (y + 3.0F + 11.0F), (double) this.ani, (double) (y + 4.0F + 11.0F), Ranbow, (new Color(4555775)).getRGB());
    }

    public void drawAll(float x, float y, int tx, int ty) {
        this.x = x;
        this.y = y;

        new ScaledResolution(Minecraft.getMinecraft());

        if (this.isHovered(tx, ty, this.x - 100.0F, this.y - 3.0F + 11.0F, this.x - 10.0F, this.y + 10.0F + 11.0F)) {
            this.showValue = true;
        } else {
            this.showValue = false;
        }

        if (Mouse.isButtonDown(0)) {
            if (!this.isHovered(tx, ty, this.x - 75.0F, this.y - 3.0F + 11.0F, this.x + 250.0F, this.y + 10.0F + 11.0F) && !this.isDraging) {
                this.clickNotDraging = true;
            } else {
                this.isDraging = true;
            }

            if (this.isDraging && !this.clickNotDraging) {
                float n = ((float) tx - this.x + 120.0F) / 370.0F;

                if ((double) n < 0.0D) {
                    n = 0.0F;
                }

                if ((double) n > 1.0D) {
                    n = 1.0F;
                }

                float n2 = (float) Math.round(this.value.getMaximum() - this.value.getMinimum()) * n + this.value.getMinimum() * 100.0F / 100.0F;

                this.value.set((Number) Float.valueOf(n2));
            }
        } else {
            this.isDraging = false;
            this.clickNotDraging = false;
        }

        this.tX = tx;
        this.tY = ty;
        this.draw(x, y);
    }

    public void drawAlll(float x, float y, int tx, int ty) {
        this.x = x;
        this.y = y;

        new ScaledResolution(Minecraft.getMinecraft());

        if (this.isHovered(tx, ty, this.x - 100.0F, this.y - 3.0F + 11.0F, this.x - 10.0F, this.y + 10.0F + 11.0F)) {
            this.showValue = true;
        } else {
            this.showValue = false;
        }

        if (Mouse.isButtonDown(0)) {
            if (!this.isHovered(tx, ty, this.x - 75.0F, this.y - 3.0F + 11.0F, this.x + 250.0F, this.y + 10.0F + 11.0F) && !this.isDraging) {
                this.clickNotDraging = true;
            } else {
                this.isDraging = true;
            }

            if (this.isDraging && !this.clickNotDraging) {
                int n = (tx - this.x2 + 120) / 370;

                if (n < 0) {
                    n = 0;
                }

                if (n > 1) {
                    n = 1;
                }

                int n2 = (int) ((double) (Math.round((float) (this.valuee.getMaximum() - this.valuee.getMinimum())) * n) + (double) this.valuee.getMinimum() * 1.0D / 1.0D);

                this.valuee.set((Number) Integer.valueOf(n2));
            }
        } else {
            this.isDraging = false;
            this.clickNotDraging = false;
        }

        this.tX = tx;
        this.tY = ty;
        this.draww(x, y);
    }

    public boolean isHovered(int mouseX, int mouseY, float x, float y, float x2, float y2) {
        return (float) mouseX >= x && (float) mouseX <= x2 && (float) mouseY >= y && (float) mouseY <= y2;
    }
}
