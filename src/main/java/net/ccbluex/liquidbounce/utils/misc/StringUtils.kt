/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.misc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class StringUtils {

    private static final Map<String,String> pinyinMap=new HashMap<>();
    private static final HashMap<String,String> airCache = new HashMap<>();

    public static String toCompleteString(final String[] args, final int start) {
        return toCompleteString(args, start, " ");
    }

    public static String toCompleteString(final String[] args, final int start, final String join) {
        if(args.length <= start) return "";

        return String.join(join, Arrays.copyOfRange(args, start, args.length));
    }

    public static String replace(final String string, final String searchChars, String replaceChars) {
        if(string.isEmpty() || searchChars.isEmpty() || searchChars.equals(replaceChars))
            return string;

        if(replaceChars == null)
            replaceChars = "";

        final int stringLength = string.length();
        final int searchCharsLength = searchChars.length();
        final StringBuilder stringBuilder = new StringBuilder(string);

        for(int i = 0; i < stringLength; i++) {
            final int start = stringBuilder.indexOf(searchChars, i);

            if(start == -1) {
                if(i == 0)
                    return string;

                return stringBuilder.toString();
            }

            stringBuilder.replace(start, start + searchCharsLength, replaceChars);
        }

        return stringBuilder.toString();
    }

    public static String injectAirString(String str) {
        if(airCache.containsKey(str)) return airCache.get(str);

        StringBuilder stringBuilder = new StringBuilder();

        boolean hasAdded = false;
        for(char c : str.toCharArray()) {
            stringBuilder.append(c);
            if (!hasAdded) stringBuilder.append('\uF8FF');
            hasAdded = true;
        }

        String result = stringBuilder.toString();
        airCache.put(str, result);

        return result;
    }

}
