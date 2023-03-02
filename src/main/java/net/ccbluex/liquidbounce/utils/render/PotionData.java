/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render;

public class PotionData {
    public int maxTimer = 0;
    public float animationX = 0;
    public final Translate translate;
    public final int level;
    public PotionData(Translate translate, int level) {
        this.translate = translate;
        this.level = level;
    }

    public float getAnimationX() {
        return animationX;
    }

    public int getMaxTimer() {
        return maxTimer;
    }
}