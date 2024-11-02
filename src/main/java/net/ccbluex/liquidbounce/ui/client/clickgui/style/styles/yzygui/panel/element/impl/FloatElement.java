/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.impl;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.font.renderer.FontRenderer;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.PanelElement;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.utils.RenderUtils;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.Panel;
import net.ccbluex.liquidbounce.value.FloatValue;
import net.ccbluex.liquidbounce.value.Value;
import java.awt.*;
import java.lang.Math;

/**
 * @author opZywl - Elements
 */
public final class FloatElement extends PanelElement {

    private final ModuleElement element;
    private final FloatValue setting;
    private boolean dragging;

    public FloatElement(final ModuleElement element, final Value<?> setting,
                        final Panel parent,
                        final int x, final int y,
                        final int width, final int height) {
        super(parent, x, y, width, height);

        this.element = element;
        this.setting = (FloatValue) setting;
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, float partialTicks) {
        final double min = setting.getMinimum(),
                max = setting.getMaximum(),
                value = setting.get(),
                increment = 1;

        if (dragging) {
            double newValue = Math.round((mouseX - x) * (max - min) / width + min);
            newValue = Math.max(min, Math.min(max, newValue));  // Clamp to range

            setting.set(newValue);
        }

        final double percentage = width / (max - min),
                barWidth = percentage * value - percentage * min;
        final Color categoryColor = parent.getCategory().getColor();

        RenderUtils.rectangle(
                x, y,
                (float) barWidth, height,
                categoryColor
        );

        RenderUtils.rectangle(
                (float) (x + barWidth - 3.0f), y,
                3.0f, height,
                categoryColor.darker()
        );

        final FontRenderer font = FDPClient.INSTANCE.getCustomFontManager().get("lato-bold-15");

        font.drawString(
                setting.getName(),
                x + 1,
                y + (height / 4.0f) + 0.5f,
                -1
        );

        final String roundedValue = String.valueOf(Math.round(setting.get()));

        font.drawString(
                roundedValue,
                x + width - 3 - font.getWidth(roundedValue),
                y + (height / 4.0f) + 0.5f,
                new Color(0xD2D2D2).getRGB()
        );
    }

    @Override
    public void mouseClicked(final int mouseX, final int mouseY, final int button) {
        if (this.isHovering(mouseX, mouseY)) {
            this.dragging = true;
        }
    }

    @Override
    public void mouseReleased(final int mouseX, final int mouseY, final int state) {
        this.dragging = false;
    }

    @Override
    public void keyTyped(final char character, final int code) {
    }

}