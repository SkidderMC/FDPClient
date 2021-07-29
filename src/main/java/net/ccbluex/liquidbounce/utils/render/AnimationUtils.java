/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render;

public class AnimationUtils {

    /**
     * In-out-easing function
     * https://github.com/jesusgollonet/processing-penner-easing
     * @param t Current iteration
     * @param d Total iterations
     * @return Eased value
     */
    public static float easeOut(float t, float d) {
        return (t = t / d - 1) * t * t + 1;
    }

    // by AimWhere
    public static float moveTowards(float current, float end, float smoothSpeed, float minSpeed) {
        float movement = (end - current) * smoothSpeed;

        if (movement > 0) {
            movement = Math.max(minSpeed, movement);
            movement = Math.min(end - current, movement);
        } else if (movement < 0) {
            movement = Math.min(-minSpeed, movement);
            movement = Math.max(end - current, movement);
        }
        return current + movement;
    }

    public static float calculateCompensation(float target, float current, long delta, int speed) {
        float diff = current - target;
        if (delta < 1L) {
            delta = 1L;
        }

        double xD;
        if (diff > (float) speed) {
            xD = (double) ((long) speed * delta / 16L) < 0.25D ? 0.5D : (double) ((long) speed * delta / 16L);
            current = (float) ((double) current - xD);
            if (current < target) {
                current = target;
            }
        } else if (diff < (float) (-speed)) {
            xD = (double) ((long) speed * delta / 16L) < 0.25D ? 0.5D : (double) ((long) speed * delta / 16L);
            current = (float) ((double) current + xD);
            if (current > target) {
                current = target;
            }
        } else {
            current = target;
        }

        return current;
    }

    public static float animate(float target, float current, double speed) {
        boolean larger = target > current;
        if (speed < 0.0F) {
            speed = 0.0F;
        } else if (speed > 1.0F) {
            speed = 1.0F;
        }
        float dif = Math.max(target, current) - Math.min(target, current);
        float factor = (float) (dif * speed);
        current = larger ? current + factor : current - factor;
        return current;
    }

    public static double animate(double target, double current, double speed) {
        boolean larger = target > current;
        if (speed < 0.0F) {
            speed = 0.0F;
        } else if (speed > 1.0F) {
            speed = 1.0F;
        }
        double dif = Math.max(target, current) - Math.min(target, current);
        double factor = dif * speed;
        if (factor < 0.1F) {
            factor = 0.1F;
        }
        current = larger ? current + factor : current - factor;
        return current;
    }

    public static float mvoeUD(float current, float end, float minSpeed) {
        return moveUD(current, end, 0.125f, minSpeed);
    }

    public static double getAnimationState(double animation, double finalState, double speed) {
        float add = (float) (0.01 * speed);
        if (animation < finalState) {
            if (animation + add < finalState)
                animation += add;
            else
                animation = finalState;
        } else {
            if (animation - add > finalState)
                animation -= add;
            else
                animation = finalState;
        }
        return animation;
    }

    public static float moveUD(float current, float end, float smoothSpeed, float minSpeed) {
        float movement = (end - current) * smoothSpeed;
        if (movement > 10.0f) {
            movement = Math.max((float) minSpeed, (float) movement);
            movement = Math.min((float) (end - current), (float) movement);
        } else if (movement < 10.0f) {
            movement = Math.min((float) (-minSpeed), (float) movement);
            movement = Math.max((float) (end - current), (float) movement);
        }
        return current + movement;
    }
}
