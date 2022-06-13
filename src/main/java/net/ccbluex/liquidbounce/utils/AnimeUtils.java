/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils;

import java.math.*;

public final class AnimeUtils {
   public static double animate(double target, double current, double speed) {
      if (current == target) return current;

      boolean larger = target > current;
      if (speed < 0.0D) {
         speed = 0.0D;
      } else if (speed > 1.0D) {
         speed = 1.0D;
      }

      double dif = Math.max(target, current) - Math.min(target, current);
      double factor = dif * speed;
      if (factor < 0.1D) {
         factor = 0.1D;
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
      if (factor < 0.1D) {
         factor = 0.1D;
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
}
