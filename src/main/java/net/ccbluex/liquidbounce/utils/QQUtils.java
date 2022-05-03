package net.ccbluex.liquidbounce.utils;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

import java.util.HashMap;
import java.util.Map;

public class QQUtils {
    public static String QQNumber;
    private static final String QQ_WINDOW_TEXT_PRE = "qqexchangewnd_shortcut_prefix_";
    private static final  User32 user32 = User32.INSTANCE;
    public interface User32 extends StdCallLibrary {
        User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class);
        interface WNDENUMPROC extends StdCallCallback {
            boolean callback(Pointer hWnd, Pointer arg);
        }
        boolean EnumWindows(WNDENUMPROC lpEnumFunc, Pointer arg);

        int GetWindowTextA(Pointer hWnd, byte[] lpString, int nMaxCount);
    }
    public static String getSubString(String text, String left, String right) {
        String result = "";
        int zLen;
        if (left == null || left.isEmpty()) {
            zLen = 0;
        } else {
            zLen = text.indexOf(left);
            if (zLen > -1) {
                zLen += left.length();
            } else {
                zLen = 0;
            }
        }
        int yLen = text.indexOf(right, zLen);
        if (yLen < 0 || right == null || right.isEmpty()) {
            yLen = text.length();
        }
        result = text.substring(zLen, yLen);
        return result;
    }
    public static Map<String,String> getLoginQQList(){
        final Map<String,String> map = new HashMap<>(5);

        user32.EnumWindows(new User32.WNDENUMPROC() {
            public boolean callback(Pointer hWnd, Pointer userData) {
                byte[] windowText = new byte[512];
                user32.GetWindowTextA(hWnd, windowText, 512);
                String wText = Native.toString(windowText);
                if(_filterQQInfo(wText)){
                    map.put(hWnd.toString(), wText.substring(wText.indexOf(QQ_WINDOW_TEXT_PRE) + QQ_WINDOW_TEXT_PRE.length()));
                }
                QQNumber = getSubString(String.valueOf(map),"=","}");
                return true;
            }
        }, null);
        return map;
    }
    private static boolean _filterQQInfo(String windowText){

        if(windowText.startsWith(QQ_WINDOW_TEXT_PRE))
            return true;
        return false;
    }

    public static void getQQ() {
        getLoginQQList();
    }
}

