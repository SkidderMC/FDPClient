/*
 * Decompiled with CFR 0.136.
 */
package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.novoline;


import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import org.lwjgl.input.Mouse;

import java.awt.*;

public class ColorValueButton
        extends ValueButton {
    private final float[] hue = {0.0f};
    private int position;
    private int color = new Color(125, 125, 125).getRGB();

    public ColorValueButton(int x, int y) {
        super(null, x, y);
        custom = true;
        position = -1111;
    }

    public void render(int mouseX, int mouseY) {
        float[] huee = {hue[0]};
        RenderUtils.drawRect(x - 10, y - 4, x + 80, y + 11, new Color(0, 0, 0, 100).getRGB());
        for (int i = x - 7; i < x + 79; ++i) {
            Color color = Color.getHSBColor(huee[0] / 255.0f, 0.7f, 1.0f);
            if (mouseX > i - 1 && mouseX < i + 1 && mouseY > y - 6 && mouseY < y + 12 && Mouse.isButtonDown(0)) {
                this.color = color.getRGB();
                position = i;
            }
            if (this.color == color.getRGB()) {
                position = i;
            }
            RenderUtils.drawRect(i - 1, y, i, y + 8, color.getRGB());
            huee[0] = huee[0] + 4.0f;
            if (!(huee[0] > 255.0f)) continue;
            huee[0] = huee[0] - 255.0f;
        }
        RenderUtils.drawRect(position, y, position + 1, y + 8, -1);
        if (hue[0] > 255.0f) {
            hue[0] = hue[0] - 255.0f;
        }
    }

    public void key(char typedChar, int keyCode) {
    }

    public void click(int mouseX, int mouseY, int button) {
    }
}
