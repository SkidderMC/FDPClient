/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client.mathalgo;

public class TaylorMath {
    private static final float BF_SIN_TO_COS = (float) (Math.PI * 0.5);
    private static final double TAU = Math.PI * 2;

    public float sin(float rad) {
        long n = (long) (rad / TAU);
        double x = rad - n * TAU;

        double x2 = x * x;
        double x3 = x2 * x;
        double x5 = x2 * x3;
        double x7 = x2 * x5;
        double x9 = x2 * x7;
        double x11 = x2 * x9;
        double x13 = x2 * x11;
        double x15 = x2 * x13;
        double x17 = x2 * x15;

        x -= x3 * 0.16666666666666666666666666666667;
        x += x5 * 0.00833333333333333333333333333333;
        x -= x7 * 1.984126984126984126984126984127e-4;
        x += x9 * 2.7557319223985890652557319223986e-6;
        x -= x11 * 2.5052108385441718775052108385442e-8;
        x += x13 * 1.6059043836821614599392377170155e-10;
        x -= x15 * 7.6471637318198164759011319857881e-13;
        x += x17 * 2.8114572543455207631989455830103e-15;
        return (float) x;
    }

    public float cos(float rad) {
        return sin(rad + BF_SIN_TO_COS);
    }
}