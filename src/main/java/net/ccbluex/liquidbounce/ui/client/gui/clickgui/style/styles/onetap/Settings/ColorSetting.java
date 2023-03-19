package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.onetap.Settings;

import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.onetap.Downward;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.onetap.ModuleRender;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.onetap.OtcClickGUi;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.features.value.ColorValue;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ColorSetting extends Downward<ColorValue> {
    public ColorValue colorValue;
    private float hue;
    private float saturation;
    private float brightness;
    private float alpha;
    private boolean colorSelectorDragging;
    private boolean hueSelectorDragging;
    private boolean alphaSelectorDragging;
    private float modulex;
    private float moduley;
    private float colory;
    
    public ColorSetting(final ColorValue s, final float x, final float y, final int width, final int height, final ModuleRender moduleRender) {
        super(s, x, y, width, height, moduleRender);
        this.colorValue = s;
        this.updateValue(s.get());
    }
    
    @Override
    public void draw(final int mouseX, final int mouseY) {
        this.modulex = OtcClickGUi.getMainx();
        this.moduley = OtcClickGUi.getMainy();
        this.colory = this.pos.y + this.getScrollY();
        final float x2 = this.modulex + 5.0f + this.pos.x + 115.0f;
        final float y2 = this.moduley + 17.0f + this.colory + 10.0f;
        final float width = 11.0f;
        final float height = 5.0f;
        Fonts.fontTahoma.drawString(this.colorValue.getName(), this.modulex + 5.0f + this.pos.x + 4.0f, this.moduley + 17.0f + this.colory + 12.0f, new Color(200, 200, 200).getRGB());
        final int black = RenderUtils.getColor(0);
        RenderUtils.drawRect(x2 - 0.5f, y2 - 0.5f, x2 + width + 0.5f, y2 + height + 0.5f, black);
        final int guiAlpha = 255;
        final int color = this.colorValue.get();
        final int colorAlpha = color >> 24 & 0xFF;
        final int minAlpha = Math.min(guiAlpha, colorAlpha);
        if (colorAlpha < 255) {
            RenderUtils.drawCheckeredBackground(x2, y2, x2 + width, y2 + height);
        }
        final int newColor = new Color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, minAlpha).getRGB();
        RenderUtils.drawGradientRect(x2, y2, x2 + width, y2 + height, newColor, RenderUtils.darker(newColor, 0.6f));
        if (this.colorValue.isExpanded()) {
            GL11.glTranslated(0.0, 0.0, 3.0);
            final float expandedX = this.getExpandedX();
            final float expandedY = this.getExpandedY();
            final float expandedWidth = this.getExpandedWidth();
            final float expandedHeight = this.getExpandedHeight();
            RenderUtils.drawBorderedRect(expandedX, expandedY, expandedX + expandedWidth, expandedY + expandedHeight + 70.0f, 1.0f, new Color(0, 0, 0, 0).getRGB(), new Color(85, 90, 96).getRGB());
            final float colorPickerSize = expandedWidth - 9.0f - 8.0f;
            final float colorPickerLeft = expandedX + 3.0f;
            final float colorPickerTop = expandedY + 3.0f;
            final float colorPickerRight = colorPickerLeft + colorPickerSize;
            final float colorPickerBottom = colorPickerTop + colorPickerSize;
            final int selectorWhiteOverlayColor = new Color(255, 255, 255, Math.min(guiAlpha, 180)).getRGB();
            if (mouseX <= colorPickerLeft || mouseY <= colorPickerTop || mouseX >= colorPickerRight || mouseY >= colorPickerBottom) {
                this.colorSelectorDragging = false;
            }
            RenderUtils.drawRect(colorPickerLeft - 0.5f, colorPickerTop - 0.5f, colorPickerRight + 0.5f, colorPickerBottom + 0.5f, RenderUtils.getColor(0));
            this.drawColorPickerRect(colorPickerLeft, colorPickerTop, colorPickerRight, colorPickerBottom);
            float hueSliderLeft = this.saturation * (colorPickerRight - colorPickerLeft);
            float alphaSliderTop = (1.0f - this.brightness) * (colorPickerBottom - colorPickerTop);
            if (this.colorSelectorDragging) {
                final float hueSliderRight = colorPickerRight - colorPickerLeft;
                final float alphaSliderBottom = mouseX - colorPickerLeft;
                this.saturation = alphaSliderBottom / hueSliderRight;
                hueSliderLeft = alphaSliderBottom;
                final float hueSliderYDif = colorPickerBottom - colorPickerTop;
                final float hueSelectorY = mouseY - colorPickerTop;
                this.brightness = 1.0f - hueSelectorY / hueSliderYDif;
                alphaSliderTop = hueSelectorY;
                this.updateColor(Color.HSBtoRGB(this.hue, this.saturation, this.brightness), false);
            }
            float hueSliderRight = colorPickerLeft + hueSliderLeft - 0.5f;
            float alphaSliderBottom = colorPickerTop + alphaSliderTop - 0.5f;
            float hueSliderYDif = colorPickerLeft + hueSliderLeft + 0.5f;
            float hueSelectorY = colorPickerTop + alphaSliderTop + 0.5f;
            RenderUtils.drawRect(hueSliderRight - 0.5f, alphaSliderBottom - 0.5f, hueSliderRight, hueSelectorY + 0.5f, black);
            RenderUtils.drawRect(hueSliderYDif, alphaSliderBottom - 0.5f, hueSliderYDif + 0.5f, hueSelectorY + 0.5f, black);
            RenderUtils.drawRect(hueSliderRight, alphaSliderBottom - 0.5f, hueSliderYDif, alphaSliderBottom, black);
            RenderUtils.drawRect(hueSliderRight, hueSelectorY, hueSliderYDif, hueSelectorY + 0.5f, black);
            RenderUtils.drawRect(hueSliderRight, alphaSliderBottom, hueSliderYDif, hueSelectorY, selectorWhiteOverlayColor);
            hueSliderLeft = colorPickerRight + 3.0f;
            hueSliderRight = hueSliderLeft + 8.0f;
            if (mouseX <= hueSliderLeft || mouseY <= colorPickerTop || mouseX >= hueSliderRight || mouseY >= colorPickerBottom) {
                this.hueSelectorDragging = false;
            }
            hueSliderYDif = colorPickerBottom - colorPickerTop;
            hueSelectorY = (1.0f - this.hue) * hueSliderYDif;
            if (this.hueSelectorDragging) {
                final float inc = mouseY - colorPickerTop;
                this.hue = 1.0f - inc / hueSliderYDif;
                hueSelectorY = inc;
                this.updateColor(Color.HSBtoRGB(this.hue, this.saturation, this.brightness), false);
            }
            RenderUtils.drawRect(hueSliderLeft - 0.5f, colorPickerTop - 0.5f, hueSliderRight + 0.5f, colorPickerBottom + 0.5f, black);
            float hsHeight = colorPickerBottom - colorPickerTop;
            float alphaSelectorX = hsHeight / 5.0f;
            float asLeft = colorPickerTop;
            for (int i2 = 0; i2 < 5.0f; ++i2) {
                final boolean last = i2 == 4.0f;
                RenderUtils.drawGradientRect(hueSliderLeft, asLeft, hueSliderRight, asLeft + alphaSelectorX, RenderUtils.getColor(Color.HSBtoRGB(1.0f - 0.2f * i2, 1.0f, 1.0f)), RenderUtils.getColor(Color.HSBtoRGB(1.0f - 0.2f * (i2 + 1), 1.0f, 1.0f)));
                if (!last) {
                    asLeft += alphaSelectorX;
                }
            }
            final float hsTop = colorPickerTop + hueSelectorY - 0.5f;
            float asRight = colorPickerTop + hueSelectorY + 0.5f;
            RenderUtils.drawRect(hueSliderLeft - 0.5f, hsTop - 0.5f, hueSliderLeft, asRight + 0.5f, black);
            RenderUtils.drawRect(hueSliderRight, hsTop - 0.5f, hueSliderRight + 0.5f, asRight + 0.5f, black);
            RenderUtils.drawRect(hueSliderLeft, hsTop - 0.5f, hueSliderRight, hsTop, black);
            RenderUtils.drawRect(hueSliderLeft, asRight, hueSliderRight, asRight + 0.5f, black);
            RenderUtils.drawRect(hueSliderLeft, hsTop, hueSliderRight, asRight, selectorWhiteOverlayColor);
            alphaSliderTop = colorPickerBottom + 3.0f;
            alphaSliderBottom = alphaSliderTop + 8.0f;
            if (mouseX <= colorPickerLeft || mouseY <= alphaSliderTop || mouseX >= colorPickerRight || mouseY >= alphaSliderBottom) {
                this.alphaSelectorDragging = false;
            }
            final int z2 = Color.HSBtoRGB(this.hue, this.saturation, this.brightness);
            final int r2 = z2 >> 16 & 0xFF;
            final int g2 = z2 >> 8 & 0xFF;
            final int b2 = z2 & 0xFF;
            hsHeight = colorPickerRight - colorPickerLeft;
            alphaSelectorX = this.alpha * hsHeight;
            if (this.alphaSelectorDragging) {
                asLeft = mouseX - colorPickerLeft;
                this.alpha = asLeft / hsHeight;
                alphaSelectorX = asLeft;
                this.updateColor(new Color(r2, g2, b2, (int)(this.alpha * 255.0f)).getRGB(), true);
            }
            RenderUtils.drawRect(colorPickerLeft - 0.5f, alphaSliderTop - 0.5f, colorPickerRight + 0.5f, alphaSliderBottom + 0.5f, black);
            RenderUtils.drawCheckeredBackground(colorPickerLeft, alphaSliderTop, colorPickerRight, alphaSliderBottom);
            RenderUtils.drawGradientRect(colorPickerLeft, alphaSliderTop, colorPickerRight, alphaSliderBottom, true, new Color(r2, g2, b2, 0).getRGB(), new Color(r2, g2, b2, Math.min(guiAlpha, 255)).getRGB());
            asLeft = colorPickerLeft + alphaSelectorX - 0.5f;
            asRight = colorPickerLeft + alphaSelectorX + 0.5f;
            RenderUtils.drawRect(asLeft - 0.5f, alphaSliderTop, asRight + 0.5f, alphaSliderBottom, black);
            RenderUtils.drawRect(asLeft, alphaSliderTop, asRight, alphaSliderBottom, selectorWhiteOverlayColor);
            GL11.glTranslated(0.0, 0.0, -3.0);
        }
    }
    
    public boolean isHovered(final int mouseX, final int mouseY) {
        return mouseX >= this.modulex + 5.0f + this.pos.x + 115.0f && mouseX <= this.modulex + 5.0f + this.pos.x + 115.0f + 13.0f && mouseY >= this.moduley + 17.0f + this.colory + 10.0f && mouseY <= this.moduley + 17.0f + this.colory + 10.0f - 0.5 + 8.0;
    }
    
    @Override
    public void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) {
        if (mouseButton == 1 && this.isHovered(mouseX, mouseY)) {
            this.colorValue.setExpanded(!this.colorValue.isExpanded());
        }
        if (this.colorValue.isExpanded() && mouseButton == 0) {
            final float expandedX = this.getExpandedX();
            final float expandedY = this.getExpandedY();
            final float expandedWidth = this.getExpandedWidth();
            final float expandedHeight = this.getExpandedHeight();
            final float colorPickerSize = expandedWidth - 9.0f - 8.0f;
            final float colorPickerLeft = expandedX + 3.0f;
            final float colorPickerTop = expandedY + 3.0f;
            final float colorPickerRight = colorPickerLeft + colorPickerSize;
            final float colorPickerBottom = colorPickerTop + colorPickerSize;
            final float alphaSliderTop = colorPickerBottom + 3.0f;
            final float alphaSliderBottom = alphaSliderTop + 8.0f;
            final float hueSliderLeft = colorPickerRight + 3.0f;
            final float hueSliderRight = hueSliderLeft + 8.0f;
            this.colorSelectorDragging = (!this.colorSelectorDragging && mouseX > colorPickerLeft && mouseY > colorPickerTop && mouseX < colorPickerRight && mouseY < colorPickerBottom);
            this.alphaSelectorDragging = (!this.alphaSelectorDragging && mouseX > colorPickerLeft && mouseY > alphaSliderTop && mouseX < colorPickerRight && mouseY < alphaSliderBottom);
            this.hueSelectorDragging = (!this.hueSelectorDragging && mouseX > hueSliderLeft && mouseY > colorPickerTop && mouseX < hueSliderRight && mouseY < colorPickerBottom);
        }
    }
    
    @Override
    public void mouseReleased(final int mouseX, final int mouseY, final int state) {
        if (this.hueSelectorDragging) {
            this.hueSelectorDragging = false;
        }
        else if (this.colorSelectorDragging) {
            this.colorSelectorDragging = false;
        }
        else if (this.alphaSelectorDragging) {
            this.alphaSelectorDragging = false;
        }
    }

    public void updateColor(int hex, boolean hasAlpha) {
        if (hasAlpha) {
            colorValue.set(hex);
        } else {
            colorValue.set(new Color(hex >> 16 & 0xFF, hex >> 8 & 0xFF, hex & 0xFF, (int)(this.alpha * 255.0f)).getRGB());
        }
    }
    
    private void drawColorPickerRect(final float left, final float top, final float right, final float bottom) {
        final int hueBasedColor = RenderUtils.getColor(Color.HSBtoRGB(this.hue, 1.0f, 1.0f));
        RenderUtils.drawGradientRect(left, top, right, bottom, true, RenderUtils.getColor(16777215), hueBasedColor);
        RenderUtils.drawGradientRect(left, top, right, bottom, 0, RenderUtils.getColor(0));
    }
    
    public void updateValue(final int value) {
        final float[] hsb = this.getHSBFromColor(value);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
        this.alpha = (value >> 24 & 0xFF) / 255.0f;
    }
    
    private float[] getHSBFromColor(final int hex) {
        final int r2 = hex >> 16 & 0xFF;
        final int g2 = hex >> 8 & 0xFF;
        final int b2 = hex & 0xFF;
        return Color.RGBtoHSB(r2, g2, b2, null);
    }
    
    public float getExpandedX() {
        return this.modulex + 5.0f + this.pos.x + 115.0f + 11.0f - 80.333336f;
    }
    
    public float getExpandedY() {
        return this.moduley + 17.0f + this.colory + 10.0f + 5.0f;
    }
    
    public float getExpandedWidth() {
        final float right = this.modulex + 5.0f + this.pos.x + 115.0f + 11.0f;
        return right - this.getExpandedX();
    }
    
    public float getExpandedHeight() {
        return 11.0f;
    }
}
