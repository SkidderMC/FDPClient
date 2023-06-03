/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils;

import net.ccbluex.liquidbounce.utils.render.RenderUtils;

public final class AnimationUtils {
    public static double animate(double target, double current, double speed) {
        if (current == target) return current;

        boolean larger = target > current;
        if (speed < 0.0) {
            speed = 0.0;
        } else if (speed > 1.0) {
            speed = 1.0;
        }

        double dif = Math.max(target, current) - Math.min(target, current);
        double factor = dif * speed;
        if (factor < 0.1) {
            factor = 0.1;
        }

        if (larger) {
            current += factor;
            if (current >= target) current = target;
        } else if (target < current) {
            current -= factor;
            if (current <= target) current = target;
        }

        return current;
    }

    public static float animate(float target, float current, float speed) {
        if (current == target) return current;

        boolean larger = target > current;
        if (speed < 0.0F) {
            speed = 0.0F;
        } else if (speed > 1.0F) {
            speed = 1.0F;
        }

        double dif = Math.max(target, (double)current) - Math.min(target, (double)current);
        double factor = dif * (double)speed;
        if (factor < 0.1) {
            factor = 0.1;
        }

        if (larger) {
            current += (float)factor;
            if (current >= target) current = target;
        } else if (target < current) {
            current -= (float)factor;
            if (current <= target) current = target;
        }

        return current;
    }

    public static float lstransition(float now, float desired, double speed) {
        final double dif = Math.abs(desired - now);
        float a = (float) Math.abs((desired - (desired - (Math.abs(desired - now)))) / (100 - (speed * 10)));
        float x = now;

        if (dif > 0) {
            if (now < desired)
                x += a * RenderUtils.deltaTime;
            else if (now > desired)
                x -= a * RenderUtils.deltaTime;
        } else
            x = desired;

        if(Math.abs(desired - x) < 10.0E-3 && x != desired)
            x = desired;

        return x;
    }

    public static double changer(double current, double add, double min, double max) {
        current += add;
        if (current > max) {
            current = max;
        }
        if (current < min) {
            current = min;
        }

        return current;
    }

    public static float changer(float current, float add, float min, float max) {
        current += add;
        if (current > max) {
            current = max;
        }
        if (current < min) {
            current = min;
        }

        return current;
    }
    public static float easeOut(float t, float d) {
        return (t = t / d - 1) * t * t + 1;
    }
}