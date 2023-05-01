/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.timer;

public class TimerUtil {
    public long lastMS;

    private long getCurrentMS() {
        return System.nanoTime() / 1000000L;
    }

    public boolean hasReached(long delay) {
        return System.currentTimeMillis() - this.lastMS >= delay;
    }

    public void reset() {
        this.lastMS = this.getCurrentMS();
    }

}

