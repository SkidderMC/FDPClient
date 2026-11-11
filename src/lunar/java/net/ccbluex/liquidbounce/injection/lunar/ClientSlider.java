/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft.
 * https://github.com/SkidderMC/FDPClient/
 * Lunar compatibility by opZywl
 * Reference: https://lucas-lima.xyz
 */
package net.ccbluex.liquidbounce.injection.lunar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import java.util.function.Consumer;

/** The configuration slider uses the vanilla GUI API on both hosts. */
public final class ClientSlider extends GuiButton {
    public String dispString;
    private final String suffix;
    private final double minimum;
    private final double maximum;
    private final boolean decimals;
    private final boolean showValue;
    private final Consumer<ClientSlider> changed;
    private double value;
    private boolean dragging;

    public ClientSlider(int id, int x, int y, int width, int height, String prefix, String suffix,
                        double minimum, double maximum, double value, boolean decimals,
                        boolean showValue, Consumer<ClientSlider> changed) {
        super(id, x, y, width, height, "");
        this.dispString = prefix;
        this.suffix = suffix;
        this.minimum = minimum;
        this.maximum = maximum;
        this.value = Math.max(minimum, Math.min(maximum, value));
        this.decimals = decimals;
        this.showValue = showValue;
        this.changed = changed;
        updateSlider();
    }

    public int getValueInt() { return (int) Math.round(value); }

    public void updateSlider() {
        displayString = dispString + (showValue ? (decimals ? Double.toString(value) : Integer.toString(getValueInt())) : "") + suffix;
    }

    @Override
    protected int getHoverState(boolean hovered) { return 0; }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (!super.mousePressed(mc, mouseX, mouseY)) return false;
        dragging = true;
        move(mouseX);
        return true;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) { dragging = false; }

    @Override
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
        if (!visible) return;
        if (dragging) move(mouseX);
        double progress = maximum == minimum ? 0 : (value - minimum) / (maximum - minimum);
        int handle = xPosition + (int) (progress * (width - 8));
        drawTexturedModalRect(handle, yPosition, 0, 66, 4, 20);
        drawTexturedModalRect(handle + 4, yPosition, 196, 66, 4, 20);
    }

    private void move(int x) {
        double ratio = Math.max(0, Math.min(1, (x - (xPosition + 4.0)) / Math.max(1, width - 8)));
        double next = minimum + ratio * (maximum - minimum);
        if (!decimals) next = Math.round(next);
        if (next == value) return;
        value = next;
        updateSlider();
        if (changed != null) changed.accept(this);
    }
}
