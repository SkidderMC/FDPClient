/**
 * The abstract class defines several {@code interpolate} methods, which are
 * used to calculate interpolated values. Various built-in implementations of
 * this class are offered. Applications may choose to implement their own
 * {@code Interpolator} to get custom interpolation behavior.
 * <p>
 * A custom {@code Interpolator} has to be defined in terms of a "
 * {@link #curve(double) curve()}".
 * @since JavaFX 2.0
 */
package net.ccbluex.liquidbounce.utils;

public abstract class Interpolator {

    private static final double EPSILON = 1e-12;
    public static final Interpolator LINEAR = new Interpolator() {
        @Override
        protected double curve(double t) {
            return t;
        }

        @Override
        public String toString() {
            return "Interpolator.LINEAR";
        }
    };

    public static final Interpolator EASE_BOTH = new Interpolator() {
        @Override
        protected double curve(double t) {
            // See the SMIL 3.1 specification for details on this calculation
            // acceleration = 0.2, deceleration = 0.2
            return Interpolator.clamp((t < 0.2) ? 3.125 * t * t
                    : (t > 0.8) ? -3.125 * t * t + 6.25 * t - 2.125
                    : 1.25 * t - 0.125);
        }

        @Override
        public String toString() {
            return "Interpolator.EASE_BOTH";
        }
    };
    public double interpolate(double startValue, double endValue,
                              double fraction) {
        return startValue + (endValue - startValue) * curve(fraction);
    }
    private static double clamp(double t) {
        return (t < 0.0) ? 0.0 : (t > 1.0) ? 1.0 : t;
    }
    protected abstract double curve(double t);

}
