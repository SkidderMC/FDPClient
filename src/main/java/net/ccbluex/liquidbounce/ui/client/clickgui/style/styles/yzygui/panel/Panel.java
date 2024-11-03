/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUIModule;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.yzyGUI;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.category.yzyCategory;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.manager.GUIManager;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element.impl.ModuleElement;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.utils.Pair;
import net.ccbluex.liquidbounce.utils.CPSCounter;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class Panel {

    public static final int PANEL_WIDTH = 100, PANEL_HEIGHT = 15;

    private final List<ModuleElement> elements = new ArrayList<>();
    private final yzyGUI parent;
    private final yzyCategory category;
    private int x, y;
    private int width, height;

    private static int scroll;

    private int dragged;
    private int lastX, lastY;

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean getOpen() {
        return this.open;
    }

    private boolean open;

    private float elementsHeight;

    public int getFade() {
        return (int) fade;
    }

    private float fade;
    private boolean dragging, extended;

    public Panel(final yzyGUI parent, final yzyCategory category,
                 final int x, final int y) {
        this.parent = parent;
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = PANEL_WIDTH;
        this.height = PANEL_HEIGHT;

        int moduleY = height + 1;

        for (final Module module : FDPClient.INSTANCE.getModuleManager().getModuleInCategory(category.getParent())) {
            final ModuleElement element = new ModuleElement(
                    module, this,
                    x + 1, moduleY,
                    width - 2, ModuleElement.MODULE_HEIGHT
            );

            elements.add(element);

            moduleY += element.getHeight() + element.getExtendedHeight();
        }
    }

    public boolean isHovering(final int mouseX, final int mouseY) {
        return CPSCounter.INSTANCE.isHovering(mouseX, mouseY, x, y, x + width, y + height);
    }

    public boolean handleScroll(int mouseX, int mouseY, int wheel) {
        final int maxElements = FDPClient.INSTANCE.getModuleManager().getModule(ClickGUIModule.class).getMaxElements();

        if(mouseX >= getX() && mouseX <= getX() + 100 && mouseY >= getY() && mouseY <= getY() + 19 + elementsHeight) {
            if(wheel < 0 && scroll < elements.size() - maxElements) {
                ++scroll;
                if(scroll < 0)
                    scroll = 0;
            }else if(wheel > 0) {
                --scroll;
                if(scroll < 0)
                    scroll = 0;
            }

            if(wheel < 0) {
                if(dragged < elements.size() - maxElements)
                    ++dragged;
            }else if(wheel > 0 && dragged >= 1) {
                --dragged;
            }

            return true;
        }
        return false;
    }

    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        if (dragging) {
            this.x = mouseX + lastX;
            this.y = mouseY + lastY;
        }

        float panelHeight = height;

        for (final ModuleElement element : elements) {
            if (extended) {
                panelHeight += element.getHeight();
            }

            panelHeight += element.getExtendedHeight();
        }

        RenderUtils.INSTANCE.yzyRectangle(
                x - 0.5f, y - 0.5f,
                width + 1.0f, panelHeight + 3.0f,
                category.getColor()
        );

        RenderUtils.INSTANCE.yzyRectangle(
                x, y,
                width,
                panelHeight + 2.0f,
                new Color(26, 26, 26)
        );

        FDPClient.INSTANCE.getCustomFontManager().get("lato-bold-15")
                .drawStringWithShadow(
                        category.name().toLowerCase(),
                        x + 3,
                        y + (height / 4.0f) + 0.5f,
                        -1
                );

        GlStateManager.pushMatrix();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();

        Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("fdpclient/clickgui/zywl/icons/eye.png"));

        final int size = height - 7;

        RenderUtils.INSTANCE.yzyTexture(
                x + width - size - size - 7,
                y + (height / 4.0f),
                0.0f,
                0.0f,
                size, size, size, size,
                category.getColor()
        );

        Minecraft.getMinecraft().getTextureManager().bindTexture(category.getIcon());

        RenderUtils.INSTANCE.yzyTexture(
                x + width - size - 3,
                y + (height / 4.0f),
                0.0f,
                0.0f,
                size, size, size, size,
                category.getColor()
        );

        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.popMatrix();

        if (extended) {
            int addition = height;

            for (final ModuleElement element : elements) {
                element.setX(x + 1);
                element.setY(y + addition);
                element.drawScreen(mouseX, mouseY, partialTicks);

                addition += element.getHeight();

                if (element.isExtended()) {
                    addition += element.getExtendedHeight();
                }
            }
        }
    }

    public void mouseClicked(final int mouseX, final int mouseY, final int button) {
        final boolean last = extended;

        if (this.isHovering(mouseX, mouseY)) {
            if (button == 0) {
                this.dragging = true;
                this.lastX = x - mouseX;
                this.lastY = y - mouseY;
            } else if (button == 1) {
                this.extended = !extended;
            }
        }

        if (extended) {
            elements.forEach(element -> {
                element.mouseClicked(mouseX, mouseY, button);

                if (!last && extended) {
                    element.setExtended(false);
                }
            });
        }
    }

    public void mouseReleased(final int mouseX, final int mouseY, final int state) {
        this.dragging = false;

        if (extended) {
            elements.forEach(element -> element.mouseReleased(mouseX, mouseY, state));
        }
    }

    public void keyTyped(final char character, final int code) {
        if (extended) {
            elements.forEach(element -> element.keyTyped(character, code));
        }
    }

    void updateFade(final int delta) {
        if(open) {
            if(fade < elementsHeight) fade += 0.4F * delta;
            if(fade > elementsHeight) fade = (int) elementsHeight;
        }else{
            if(fade > 0) fade -= 0.4F * delta;
            if(fade < 0) fade = 0;
        }
    }

    public void onGuiClosed() {
        final GUIManager guiManager = FDPClient.INSTANCE.getGuiManager();
        final Pair<Integer, Integer> positions = new Pair<>(0, 0);

        positions.setKey(x);
        positions.setValue(y);

        guiManager.getPositions().put(category, positions);
        guiManager.getExtendeds().put(category, extended);
    }

    public yzyGUI getParent() {
        return parent;
    }

    public yzyCategory getCategory() {
        return category;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getX() {
        return x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getY() {
        return y;
    }

    public void setLastX(int lastX) {
        this.lastX = lastX;
    }

    public int getLastX() {
        return lastX;
    }

    public void setLastY(int lastY) {
        this.lastY = lastY;
    }

    public int getLastY() {
        return lastY;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public boolean isDragging() {
        return dragging;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
    }

    public boolean isExtended() {
        return extended;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

}
