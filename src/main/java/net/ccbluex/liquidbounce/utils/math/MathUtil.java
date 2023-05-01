package net.ccbluex.liquidbounce.utils.math;

public class MathUtil {

    public static double incValue(final double val, final double inc) {
        final double one = 1.0 / inc;
        return Math.round(val * one) / one;
    }

    public static double roundToHalf(final double d) {
        return Math.round(d * 2.0) / 2.0;
    }
}
