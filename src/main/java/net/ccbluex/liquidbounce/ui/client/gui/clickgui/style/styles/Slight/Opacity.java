package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.Slight;

import net.ccbluex.liquidbounce.ui.client.gui.AnimationUtil;

public class Opacity {

    private float opacity;
    private long lastMS;

    public Opacity(int opacity) {
        this.opacity = (float) opacity;
        this.lastMS = System.currentTimeMillis();
    }

    public void interpolate(float targetOpacity) {
        long currentMS = System.currentTimeMillis();
        long delta = currentMS - this.lastMS;

        this.lastMS = currentMS;
        this.opacity = AnimationUtil.calculateCompensation(targetOpacity, this.opacity, delta, 20);
    }

    public void interp(float targetOpacity, int speed) {
        long currentMS = System.currentTimeMillis();
        long delta = currentMS - this.lastMS;

        this.lastMS = currentMS;
        this.opacity = AnimationUtil.calculateCompensation(targetOpacity, this.opacity, delta, speed);
    }

    public float getOpacity() {
        return (float) ((int) this.opacity);
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }
}
