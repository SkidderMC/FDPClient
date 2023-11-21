package cc.paimonmc.viamcp.gui;

import cc.paimonmc.viamcp.ViaMCP;
import cc.paimonmc.viamcp.protocols.ProtocolCollection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;

import java.util.Arrays;
import java.util.Collections;

public class AsyncVersionSlider extends GuiButton {
    private float dragValue = (float) (ProtocolCollection.values().length - Arrays.asList(ProtocolCollection.values()).indexOf(ProtocolCollection.getProtocolCollectionById(ViaMCP.PROTOCOL_VERSION))) / ProtocolCollection.values().length;

    private final ProtocolCollection[] values;
    private float sliderValue;
    public boolean dragging;

    public AsyncVersionSlider(final int buttonId, final int x, final int y, final int widthIn, final int heightIn) {
        super(buttonId, x, y, Math.max(widthIn, 110), heightIn, "");
        this.values = ProtocolCollection.values();
        Collections.reverse(Arrays.asList(values));
        this.sliderValue = dragValue;
        this.displayString = values[(int) (this.sliderValue * (values.length - 1))].getVersion().getName();
    }

    public void drawButton(final Minecraft mc, final int mouseX, final int mouseY) {
        super.drawButton(mc, mouseX, mouseY);
    }

    /**
     * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if it IS hovering over
     * this button.
     */
    public int getHoverState(final boolean mouseOver) {
        return 0;
    }

    /**
     * Fired when the mouse button is dragged. Equivalent of MouseListener.mouseDragged(MouseEvent e).
     */
    public void mouseDragged(final Minecraft mc, final int mouseX, final int mouseY) {
        if (this.visible) {
            if (this.dragging) {
                this.sliderValue = (float) (mouseX - (this.xPosition + 4)) / (float) (this.width - 8);
                this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0F, 1.0F);
                this.dragValue = sliderValue;
                this.displayString = values[(int) (this.sliderValue * (values.length - 1))].getVersion().getName();
                ViaMCP.getInstance().setVersion(values[(int) (this.sliderValue * (values.length - 1))].getVersion().getVersion());
            }

            mc.getTextureManager().bindTexture(buttonTextures);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(this.xPosition + (int) (this.sliderValue * (float) (this.width - 8)), this.yPosition, 0, 66, 4, 20);
            this.drawTexturedModalRect(this.xPosition + (int) (this.sliderValue * (float) (this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
        }
    }

    /**
     * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
     * e).
     */
    public boolean mousePressed(final Minecraft mc, final int mouseX, final int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            this.sliderValue = (float) (mouseX - (this.xPosition + 4)) / (float) (this.width - 8);
            this.sliderValue = MathHelper.clamp_float(this.sliderValue, 0.0F, 1.0F);
            this.dragValue = sliderValue;
            this.displayString = values[(int) (this.sliderValue * (values.length - 1))].getVersion().getName();
            ViaMCP.getInstance().setVersion(values[(int) (this.sliderValue * (values.length - 1))].getVersion().getVersion());
            this.dragging = true;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent e).
     */
    public void mouseReleased(final int mouseX, final int mouseY) {
        this.dragging = false;
    }

    public void setVersion(final int protocol) {
        this.dragValue = (float) (ProtocolCollection.values().length - Arrays.asList(ProtocolCollection.values()).indexOf(ProtocolCollection.getProtocolCollectionById(protocol))) / ProtocolCollection.values().length;
        this.sliderValue = this.dragValue;
        this.displayString = values[(int) (this.sliderValue * (values.length - 1))].getVersion().getName();
    }
}
