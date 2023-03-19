package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.onetap;

import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.onetap.Utils.Position;
import net.ccbluex.liquidbounce.features.value.Value;
import net.minecraft.client.gui.Gui;

public abstract class Downward<V extends Value> extends Gui  {
    public V setting;
    private float x;
    private float y;
    private int width;
    private int height;
    public Position pos;
    public ModuleRender moduleRender;
    
    public Downward(final V s, final float x, final float y, final int width, final int height, final ModuleRender moduleRender) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.setting = s;
        this.pos = new Position(this.getX(), this.getY(), (float)this.getWidth(), (float)this.getHeight());
        this.moduleRender = moduleRender;
    }
    
    public abstract void draw(final int p0, final int p1);
    
    public abstract void mouseClicked(final int p0, final int p1, final int p2);
    
    public void keyTyped(final char typedChar, final int keyCode) {
    }
    
    public abstract void mouseReleased(final int p0, final int p1, final int p2);
    
    public void update() {
        this.pos = new Position(this.getX(), this.getY(), (float)this.getWidth(), (float)this.getHeight());
    }
    
    public float getX() {
        return this.x;
    }
    
    public float getY() {
        return this.y;
    }
    
    public int getHeight() {
        return this.height;
    }
    
    public int getWidth() {
        return this.width;
    }
    
    public void setX(final int x) {
        this.x = (float)x;
    }
    
    public void setY(final int y) {
        this.y = (float)y;
    }
    
    public int getScrollY() {
        return this.moduleRender.getScrollY();
    }
}
