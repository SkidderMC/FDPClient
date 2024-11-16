/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.impl;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.font.renderer.FontRenderer;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.PanelElement;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.Panel;
import net.ccbluex.liquidbounce.value.ListValue;
import net.ccbluex.liquidbounce.value.Value;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * @author opZywl - Elements
 */
public final class ListElement extends PanelElement {

    private final ModuleElement element;
    private final ListValue setting;

    public ListElement(final ModuleElement element, final Value<?> setting, final Panel parent, final int x, final int y, final int width, final int height) {
        super(parent, x, y, width, height);

        this.element = element;
        this.setting = (ListValue) setting;
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, float partialTicks) {
        final FontRenderer font = FDPClient.INSTANCE.getCustomFontManager().get("lato-bold-15");
        final String value = setting.get();

        font.drawString(setting.getName(), x + 1, y + (height / 4.0f) + 0.5f, -1);

        font.drawString(value, x + width - font.getWidth(value) - 1, y + (height / 4.0f) + 0.5f, new Color(0xD2D2D2).getRGB());
    }

    @Override
    public void mouseClicked(final int mouseX, final int mouseY, final int button) {
        if (this.isHovering(mouseX, mouseY)) {
            this.cycle(button == 0);
        }
    }

    @Override
    public void mouseReleased(final int mouseX, final int mouseY, final int state) {
    }

    @Override
    public void keyTyped(final char character, final int code) {
    }

    public void cycle(final boolean next) {
        final List<String> values = Arrays.asList(setting.getValues());
        int index = values.indexOf(setting.get());

        if (++index >= values.size()) {
            index = 0;
        }

        setting.changeValue(values.get(index));
    }

}
