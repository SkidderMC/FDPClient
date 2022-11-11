/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui.clickgui.utils.animations.impl;

import net.ccbluex.liquidbounce.ui.client.gui.clickgui.utils.animations.Animation;
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.utils.animations.Direction;

public class EaseInOutQuad extends Animation {

    public EaseInOutQuad(int ms, double endPoint) {
        super(ms, endPoint);
    }

    public EaseInOutQuad(int ms, double endPoint, Direction direction) {
        super(ms, endPoint, direction);
    }

    protected double getEquation(double x1) {
        double x = x1 / duration;
        return x < 0.5 ? 2 * Math.pow(x, 2) : 1 - Math.pow(-2 * x + 2, 2) / 2;
    }

}
