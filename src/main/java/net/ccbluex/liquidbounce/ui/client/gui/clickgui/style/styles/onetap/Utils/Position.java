package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.onetap.Utils;

import com.google.gson.annotations.Expose;

public class Position
{
    @Expose
    public float x;
    @Expose
    public float y;
    @Expose
    public float width;
    @Expose
    public float height;
    
    public Position(final float x, final float y, final float width, final float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public static Position empty() {
        return new Position(-1.0f, -1.0f, 0.0f, 0.0f);
    }
    
    public boolean isHovered(final int mouseX, final int mouseY) {
        return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
    }
    
    public boolean isHovered(final int mouseX, final int mouseY, final int offsetX, final int offsetY, final int cWidth, final int cHeight) {
        return mouseX >= this.x + offsetX && mouseX <= this.x + offsetX + cWidth && mouseY >= this.y + offsetY && mouseY <= this.y + offsetY + cHeight;
    }
    
    public float[] clicksOff(final int mouseX, final int mouseY) {
        return new float[] { mouseX - this.x, mouseY - this.y };
    }
}
