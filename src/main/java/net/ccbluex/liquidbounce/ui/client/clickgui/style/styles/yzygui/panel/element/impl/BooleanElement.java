/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.impl;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.category.yzyCategory;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.PanelElement;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.utils.RenderUtils;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.Panel;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.Value;

import java.awt.*;

/**
 * @author opZywl - Elements
 */
public final class BooleanElement extends PanelElement {

    private final ModuleElement element;
    private final BoolValue setting;

    public BooleanElement(final ModuleElement element, final Value<?> setting,
                          final Panel parent,
                          final int x, final int y,
                          final int width, final int height) {
        super(parent, x, y, width, height);

        this.element = element;
        this.setting = (BoolValue) setting;
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        RenderUtils.rectangle(
                x, y,
                width, height,
                setting.get()
                        ? yzyCategory.of(element.getModule().getCategory()).getColor()
                        : new Color(26, 26, 26)
        );

        FDPClient.INSTANCE.getCustomFontManager().get("lato-bold-15")
                .drawString(
                        setting.getName(),
                        x + 1,
                        y + (height / 4.0f) + 0.5f,
                        -1
                );
    }

    @Override
    public void mouseClicked(final int mouseX, final int mouseY, final int button) {
        if (this.isHovering(mouseX, mouseY)) {
            setting.toggle();
        }
    }

    @Override
    public void mouseReleased(final int mouseX, final int mouseY, final int state) {
    }

    @Override
    public void keyTyped(final char character, final int code) {
    }

}
