/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.element;

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.panel.Panel;
import net.ccbluex.liquidbounce.utils.CPSCounter;

/**
 * @author opZywl - Panel Element
 */
public abstract class PanelElement {

    protected final Panel parent;
    protected int x, y;
    protected int width, height;

    public PanelElement(final Panel panel, final int x, final int y, final int width, final int height) {
        this.parent = panel;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Panel getParent() {
        return parent;
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

    public void setWidth(int width) {
        this.width = width;
    }

    public int getWidth() {
        return width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public boolean isHovering(final int mouseX, final int mouseY) {
        return CPSCounter.INSTANCE.isHovering(mouseX, mouseY, x, y, x + width, y + height);
    }

    public abstract void drawScreen(final int mouseX, final int mouseY, final float partialTicks);

    public abstract void mouseClicked(final int mouseX, final int mouseY, final int button);

    public abstract void mouseReleased(final int mouseX, final int mouseY, final int state);

    public abstract void keyTyped(final char character, int code);

}
