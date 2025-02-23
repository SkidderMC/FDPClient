/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.impl;

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.Animation;
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.Direction;

public class SmoothStepAnimation extends Animation {

    public SmoothStepAnimation(int ms, double endPoint, Direction direction) {
        super(ms, endPoint, direction);
    }

    protected double getEquation(double x) {
        double x1 = x / (double) getDuration();
        return -2 * Math.pow(x1, 3) + (3 * Math.pow(x1, 2));
    }

}