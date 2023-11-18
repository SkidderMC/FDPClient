package net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.novoline;

public class AnimationUtil {
    public static float fastmax(float a, float b) {
        return (a >= b) ? a : b;
    }

    public static float fastmin(float a, float b) {
        return (a <= b) ? a : b;
    }

    public static float moveUD(final float current, final float end, final float smoothSpeed, final float minSpeed) {
        float movement = (end - current) * smoothSpeed;
        if (movement > 0.0f) {
            movement = fastmax(minSpeed, movement);
            movement = fastmin(end - current, movement);
        } else if (movement < 0.0f) {
            movement = fastmin(-minSpeed, movement);
            movement = fastmax(end - current, movement);
        }
        return current + movement;
    }

    public static double getAnimationState(double animation, double finalState, double speed) {
        float add = (float) (0.01 * speed);
        animation = animation < finalState ? (Math.min(animation + add, finalState)) : (Math.max(animation - add, finalState));
        return animation;
    }

    public static float getAnimationState(float animation, float finalState, float speed) {
        float add = (float) (0.01 * speed);
        animation = animation < finalState ? (Math.min(animation + add, finalState)) : (Math.max(animation - add, finalState));
        return animation;
    }

    public static double animate(final double target, double current, double speed) {
        final boolean larger = (target > current);
        if (speed < 0.0) {
            speed = 0.0;
        } else if (speed > 1.0) {
            speed = 1.0;
        }
        if (target == current) {
            return target;
        }
        final double dif = Math.max(target, current) - Math.min(target, current);
        double factor = Math.max(dif * speed, 1.0);
        if (factor < 0.1) {
            factor = 0.1;
        }
        if (larger) {
            if (current + factor > target) {
                current = target;
            } else {
                current += factor;
            }
        } else if (current - factor < target) {
            current = target;
        } else {
            current -= factor;
        }
        return current;
    }

}
