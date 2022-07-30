package net.skiddermc.fdpclient.launch.data.legacyui.clickgui.style.styles.novoline;



import net.skiddermc.fdpclient.utils.timer.MSTimer;

import java.awt.*;

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

    public static int moveUDl(final float current, final float end, final float smoothSpeed, final float minSpeed) {
        float movement = (end - current) * smoothSpeed;
        if (movement > 0.0f) {
            movement = Math.max(minSpeed, movement);
            movement = Math.min(end - current, movement);
        } else if (movement < 0.0f) {
            movement = Math.min(-minSpeed, movement);
            movement = Math.max(end - current, movement);
        }
        return (int) (current + movement);
    }

    public static float calculateCompensation(final float target, float current, float f, final float g) {
        final float diff = current - target;
        if (f < 1L) {
            f = 1L;
        }
        if (diff > g) {
            final double xD = (g * f / 16L < 0.25) ? 0.5 : ((double)(g * f / 16L));
            current -= (float)xD;
            if (current < target) {
                current = target;
            }
        }
        else if (diff < -g) {
            final double xD = (g * f / 16L < 0.25) ? 0.5 : ((double)(g * f / 16L));
            current += (float)xD;
            if (current > target) {
                current = target;
            }
        }
        else {
            current = target;
        }
        return current;
    }

    public static float calculateCompensation(final float target, float current, long delta, final int speed) {
        final float diff = current - target;
        if (delta < 1L) {
            delta = 1L;
        }
        if (diff > speed) {
            final double xD = (speed * delta / 16L < 0.25) ? 0.5 : ((double) (speed * delta / 16L));
            current -= (float) xD;
            if (current < target) {
                current = target;
            }
        } else if (diff < -speed) {
            final double xD = (speed * delta / 16L < 0.25) ? 0.5 : ((double) (speed * delta / 16L));
            current += (float) xD;
            if (current > target) {
                current = target;
            }
        } else {
            current = target;
        }
        return current;
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
    public static MSTimer TimerUtils=new MSTimer();
    public static int animatel(float target, float current, float speed) {
        if (TimerUtils.hasTimePassed(4)) {
            boolean larger;
            larger = target > current;
            if (speed < 0.0f) {
                speed = 0.0f;
            } else if (speed > 1.0) {
                speed = 1.0f;
            }
            float dif = Math.max(target, current) - Math.min(target, current);
            float factor = dif * speed;
            if (factor < 0.1f) {
                factor = 0.1f;
            }
            current = larger ? current + factor : current - factor;

            TimerUtils.reset();
        }
        if (Math.abs(current - target) < 0.2) {
            return (int) target;
        } else {
            return (int) current;
        }
    }

    public static float animate(float target, float current, float speed) {
        if (TimerUtils.hasTimePassed(4)) {
            boolean larger;
            larger = target > current;
            if (speed < 0.0f) {
                speed = 0.0f;
            } else if (speed > 1.0) {
                speed = 1.0f;
            }
            float dif = Math.max(target, current) - Math.min(target, current);
            float factor = dif * speed;
            if (factor < 0.1f) {
                factor = 0.1f;
            }
            current = larger ? current + factor : current - factor;

            TimerUtils.reset();
        }
        if (Math.abs(current - target) < 0.2) {
            return target;
        } else {
            return current;
        }
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

    public static Color getColorAnimationState(Color animation, Color finalState, double speed) {
        float add = (float) (0.01 * speed);
        float animationr = animation.getRed();
        float animationg = animation.getGreen();
        float animationb = animation.getBlue();
        float finalStater = finalState.getRed();
        float finalStateg = finalState.getGreen();
        float finalStateb = finalState.getBlue();
        float finalStatea = finalState.getAlpha();
        //r
        if (animationr < finalStater) {
            if (animationr + add < finalStater)
                animationr += add;
            else
                animationr = finalStater;
        } else {
            if (animationr - add > finalStater)
                animationr -= add;
            else
                animationr = finalStater;
        }
        //g
        if (animationg < finalStateg) {
            if (animationg + add < finalStateg)
                animationg += add;
            else
                animationg = finalStateg;
        } else {
            if (animationg - add > finalStateg)
                animationg -= add;
            else
                animationg = finalStateg;
        }
        //b
        if (animationb < finalStateb) {
            if (animationb + add < finalStateb)
                animationb += add;
            else
                animationb = finalStateb;
        } else {
            if (animationb - add > finalStateb)
                animationb -= add;
            else
                animationb = finalStateb;
        }
        animationr /= 255.0f;
        animationg /= 255.0f;
        animationb /= 255.0f;
        finalStatea /= 255.0f;
        if (animationr > 1.0f) animationr = 1.0f;
        if (animationg > 1.0f) animationg = 1.0f;
        if (animationb > 1.0f) animationb = 1.0f;
        if (finalStatea > 1.0f) finalStatea = 1.0f;
        return new Color(animationr, animationg, animationb, finalStatea);
    }

    public static float clamp(float number, float min, float max) {
        return number < min ? min : Math.min(number, max);
    }
}
