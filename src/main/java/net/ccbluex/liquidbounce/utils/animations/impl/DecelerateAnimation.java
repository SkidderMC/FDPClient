package net.ccbluex.liquidbounce.utils.animations.impl;


import net.ccbluex.liquidbounce.utils.animations.Animation;

public class DecelerateAnimation extends Animation {

    public DecelerateAnimation(int ms, double endPoint) {
        super(ms, endPoint);
    }

    protected double getEquation(double x) {
        return 1 - ((x - 1) * (x - 1));
    }
}
