/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.impl;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.category.yzyCategory;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.font.renderer.FontRenderer;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.PanelElement;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.Panel;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.FloatValue;
import net.ccbluex.liquidbounce.value.IntegerValue;
import net.ccbluex.liquidbounce.value.ListValue;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: opZywl - Elements
 */
public final class ModuleElement extends PanelElement {

    public static final int MODULE_HEIGHT = 14;

    private final List<PanelElement> elements = new ArrayList<>();
    private final Module module;
    private boolean extended, binding;

    public ModuleElement(final Module module, final Panel parent,
                         final int x, final int y,
                         final int width, final int height) {
        super(parent, x, y, width, height);

        this.module = module;

        module.getValues().forEach(value -> {
            PanelElement element = null;

            if (value instanceof BoolValue) {
                element = new BooleanElement(this, value, parent, x + 4, y, width - 8, 12);
            } else if (value instanceof FloatValue) {
                element = new FloatElement(this, value, parent, x + 4, y, width - 4, 12);
            } else if (value instanceof IntegerValue) {
                element = new IntegerElement(this, value, parent, x + 4, y, width - 4, 12);
            } else if (value instanceof ListValue) {
                element = new ListElement(this, value, parent, x + 4, y, width - 8, 12);
            }

            if (element != null) {
                elements.add(element);
            }
        });

        this.update();
    }

    private void update() {
        int elementY = y + height;

        for (final PanelElement element : elements) {
            element.setX(x + 4);
            element.setY(elementY);

            elementY += element.getHeight();
        }
    }

    public float getExtendedHeight() {
        float height = 0.0f;

        if (extended) {
            for (final PanelElement element : elements) {
                height += element.getHeight();
            }

            height += 2;
        }

        return height;
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        this.update();

        final FontRenderer font = FDPClient.INSTANCE.getCustomFontManager().get("lato-bold-15");
        int moduleHeight = height;

        if (extended) {
            for (final PanelElement element : elements) {
                moduleHeight += element.getHeight();
            }

            moduleHeight += 2;
        }

        Color moduleColor = new Color(37, 37, 37);
        if (module.getState()) {
            yzyCategory category = yzyCategory.Companion.of(module.getCategory());
            if (category != null) {
                moduleColor = category.getColor();
            }
        }

        RenderUtils.INSTANCE.yzyRectangle(
                x + 0.5f, y,
                width - 1.0f, moduleHeight,
                extended ? new Color(26, 26, 26) : moduleColor
        );

        String text = module.getName().toLowerCase();

        if (Keyboard.isKeyDown(Keyboard.KEY_TAB) && module.getKeyBind() != Keyboard.KEY_GRAVE) {
            text += " [" + Keyboard.getKeyName(module.getKeyBind()).toUpperCase() + "]";
        } else if (binding) {
            text = "binding...";
        }

        font.drawString(
                text,
                x + width - font.getWidth(text) - 3,
                y + (height / 4.0f) + 0.5f,
                extended && module.getState() ? moduleColor.getRGB() : new Color(0xD2D2D2).getRGB()
        );

        if (extended) {
            elements.forEach(element -> element.drawScreen(mouseX, mouseY, partialTicks));
        }
    }

    @Override
    public void mouseClicked(final int mouseX, final int mouseY, final int button) {
        if (this.isHovering(mouseX, mouseY)) {
            if (button == 0) {
                if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    this.binding = !binding;
                } else {
                    module.toggle();
                }
            } else if (button == 1) {
                if (!module.getValues().isEmpty()) {
                    this.extended = !extended;
                }
            }
        }

        if (extended) {
            elements.forEach(element -> element.mouseClicked(mouseX, mouseY, button));
        }
    }

    @Override
    public void mouseReleased(final int mouseX, final int mouseY, final int state) {
        if (extended) {
            elements.forEach(element -> element.mouseReleased(mouseX, mouseY, state));
        }
    }

    @Override
    public void keyTyped(final char character, int code) {
        if (binding) {
            if (code == Keyboard.KEY_BACK) {
                code = Keyboard.KEY_NONE;
            }

            module.setKeyBind(code);

            this.binding = false;
        }

        if (extended) {
            int finalCode = code;

            elements.forEach(element -> element.keyTyped(character, finalCode));
        }
    }

    public List<PanelElement> getElements() {
        return elements;
    }

    public Module getModule() {
        return module;
    }

    public boolean isExtended() {
        return extended;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
    }

    public boolean isBinding() {
        return binding;
    }

    public void setBinding(boolean binding) {
        this.binding = binding;
    }
}
