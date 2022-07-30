/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.launch.data.legacyui.clickgui.files.normal;

public class TimerUtil {

    public long lastMS = System.currentTimeMillis();


    public void reset() {
        lastMS = System.currentTimeMillis();
    }


    public boolean hasTimeElapsed(long time, boolean reset) {
        if (System.currentTimeMillis() - lastMS > time) {
            if (reset) reset();
            return true;
        }

        return false;
    }


    public boolean hasTimeElapsed(long time) {
        return System.currentTimeMillis() - lastMS > time;
    }


    public long getTime() {
        return System.currentTimeMillis() - lastMS;
    }

    public void setTime(long time) {
        lastMS = time;
    }

}
