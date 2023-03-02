/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render;

public class StringConversions {
    public static Object castNumber(String newValueText) {
        if (newValueText.contains(".")) {
            if (newValueText.toLowerCase().contains("f")) {
                return (float) Float.parseFloat((String) newValueText);
            }
            return Double.parseDouble(newValueText);
        }
        if (StringConversions.isNumeric(newValueText)) {
            return Integer.parseInt(newValueText);
        }
        return newValueText;
    }

    public static boolean isNumeric(String text) {
        try {
            Integer.parseInt(text);
            return true;
        }
        catch (NumberFormatException numberFormatException) {
            return false;
        }
    }
}
