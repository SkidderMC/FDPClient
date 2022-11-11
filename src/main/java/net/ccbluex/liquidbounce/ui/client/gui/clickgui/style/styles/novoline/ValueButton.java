package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.novoline;

import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.features.value.BoolValue;
import net.ccbluex.liquidbounce.features.value.IntegerValue;
import net.ccbluex.liquidbounce.features.value.ListValue;
import net.ccbluex.liquidbounce.features.value.Value;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.Arrays;
import java.util.List;


public class ValueButton {
    public final Value value;
    public String name;
    public boolean custom;
    public boolean change;
    public int x;
    public float y;
    public static int valuebackcolor;


    public ValueButton(Value value, int x, float y) {

        this.value = value;
        this.x = x;
        this.y = y;

        name = "";
        if (this.value instanceof BoolValue) {
            change = (boolean) this.value.get();
        } else if (this.value instanceof ListValue) {
            name = String.valueOf(this.value.get());
        } else if (value instanceof IntegerValue) {
            IntegerValue v = (IntegerValue) value;
            name = name + (v.getDisplayable() ? ((Number) v.get()).intValue() : ((Number) v.get()).doubleValue());
        }
    }

    public void render(int mouseX, int mouseY, Window parent) {
        final GameFontRenderer font = Fonts.font32;
        RenderUtils.drawRect(x - 10, y - 7, x + 80 + parent.allX, y + 11, new Color(40, 40, 40).getRGB());
        if (value instanceof BoolValue) {
            change = (boolean) value.get();
        } else if (value instanceof ListValue) {
            name = String.valueOf(value.get()).toUpperCase();
        } else if (value instanceof IntegerValue) {
            IntegerValue v = (IntegerValue) value;
            name = String.valueOf(((Number) v.get()).doubleValue());
            if (mouseX > x - 9 && mouseX < x + 87 && mouseY > y - 4 && mouseY < y + font.FONT_HEIGHT + 4 && Mouse.isButtonDown(0)) {
                final double min = v.getMinimum();
                final double max = v.getMaximum();
                final double inc = 1;
                final double valAbs = mouseX - (x + 1);
                double perc = valAbs / 68;
                perc = Math.min(Math.max(0, perc), 1);
                final double valRel = (max - min) * perc;
                double val = min + valRel;
                val = Math.round(val * (1 / inc)) / (1 / inc);
                v.set((int) val);
            }
            double number = 86 * (((Number) v.get()).floatValue() - v.getMinimum()) / (v.getMaximum() - v.getMinimum());
            GlStateManager.pushMatrix();
            GlStateManager.translate(-9.0f, 1.0f, 0.0f);
            RenderUtils.drawRect(x + 1, y - 6, (x + 87.0f + parent.allX), y + font.FONT_HEIGHT + 6, new Color(29, 29, 29).getRGB());
            RenderUtils.drawRect(x + 1, y - 6, (x + number + 1.0 + parent.allX), y + font.FONT_HEIGHT + 6, valuebackcolor);
            GlStateManager.popMatrix();
        }
        if (value instanceof BoolValue) {
            final int size = 2;
            if (change) {
                RenderUtils.drawRect(x + 62 + size + parent.allX + 4, y - 4 + size - 1, x + 76 - size + parent.allX + 4, y + 9 - size, new Color(29, 29, 29).getRGB());
                RenderUtils.drawRect(x + 62 + size + parent.allX + 5, y - 4 + size, x + 76 - size + parent.allX + 3, y + 8 - size, new Color(255, 255, 255, 218).getRGB());
            } else {
                RenderUtils.drawRect(x + 62 + size + parent.allX + 4, y - 4 + size - 1, x + 76 - size + parent.allX + 4, y + 9 - size, new Color(29, 29, 29).getRGB());
            }
        }

        if (!(value instanceof IntegerValue)) {
            font.drawStringWithShadow(value.getName(), x - 7 + 2, y, -1);
        }
        if (value instanceof BoolValue) {
            font.drawStringWithShadow(name, x + font.getStringWidth(value.getName()) + 2, y, -1);
        }
        if (value instanceof IntegerValue) {
            font.drawStringWithShadow(value.getName(), x - 7, y - 1, -1);
            font.drawStringWithShadow(name, x + font.getStringWidth(value.getName()) + 2, y - 1, -1);
        }
        if (value instanceof ListValue) {
            font.drawStringWithShadow(name, x + 90 - font.getStringWidth(name) + 2, y, -1);
        }
    }


    public void key(char typedChar, int keyCode) {
    }

    public void click(int mouseX, int mouseY, int button) {
        if (!custom && mouseX > x - 9 && mouseX < x + 87 && mouseY > y - 4 && mouseY < y +Fonts.font35.FONT_HEIGHT + 4) {
            if (value instanceof BoolValue) {
                BoolValue m1 = (BoolValue) value;
                m1.set(!(Boolean) m1.get());
                return;
            }
            if (value instanceof ListValue) {
                ListValue m = (ListValue) value;
                if ((button == 0 || button == 1)) {
                    List<String> options = Arrays.asList(m.getValues());
                    //noinspection SuspiciousMethodCalls
                    int index = options.indexOf(m.get());
                    if (button == 0) {
                        index++;
                    } else {
                        index--;
                    }
                    if (index >= options.size()) {
                        index = 0;
                    } else if (index < 0) {
                        index = options.size() - 1;
                    }
                    this.value.set(m.getValues()[index]);
                }
            }
        }
    }
}
