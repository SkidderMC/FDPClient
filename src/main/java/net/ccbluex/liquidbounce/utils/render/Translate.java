/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render;

import net.ccbluex.liquidbounce.ui.client.gui.AnimationUtil;
import net.ccbluex.liquidbounce.utils.AnimationUtils;
public final class Translate {
    private float x;
    private float y;
    private boolean first = false;

    public Translate(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void interpolate(float targetX, float targetY, double smoothing) {
        if(first) {
            this.x = AnimationUtil.animate(targetX, this.x, smoothing);
            this.y = AnimationUtil.animate(targetY, this.y, smoothing);
        } else {
            this.x = targetX;
            this.y = targetY;
            first = true;
        }
    }

    public float getY() {
        return this.y;
    }

    private boolean isInitialized;
    private float currentX;
    private float currentY;

    public Translate(float initialY) {
        this.currentX = 0; // Assuming that the initial X should be 0
        this.currentY = initialY;
    }


}

