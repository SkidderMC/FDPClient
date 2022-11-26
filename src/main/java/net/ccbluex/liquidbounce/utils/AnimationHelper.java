/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils;

import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.value.BoolValue;

public class AnimationHelper {
    public float animationX;
    public int alpha;
    public int getAlpha() {
        return this.alpha;
    }
    public float getAnimationX() {
        return this.animationX;
    }
    public void resetAlpha() {
        this.alpha = 0;
    }
    public AnimationHelper() {
        this.alpha = 0;
    }
    public void updateAlpha(int speed) {
        if(alpha < 255)
            this.alpha += speed;
    }
    public AnimationHelper(BoolValue value) {
        animationX = value.get() ? 5 : -5;
    }
    public AnimationHelper(Module module) {
        animationX = module.getState() ? 5 : -5;
    }

    public static double animate(double target, double current, double speed) {
        boolean larger;
        boolean bl = larger = target > current;
        if (speed < 0.0) {
            speed = 0.0;
        } else if (speed > 1.0) {
            speed = 1.0;
        }
        double dif = Math.max(target, current) - Math.min(target, current);
        double factor = dif * speed;
        if (factor < 0.1) {
            factor = 0.1;
        }
        current = larger ? (current += factor) : (current -= factor);
        return current;
    }
}
