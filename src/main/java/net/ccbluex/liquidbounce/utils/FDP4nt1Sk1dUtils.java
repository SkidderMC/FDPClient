package net.ccbluex.liquidbounce.utils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.util.Base64;

public class FDP4nt1Sk1dUtils {
    public static BASE64Encoder encoder = new BASE64Encoder();
    public static BASE64Decoder decoder = new BASE64Decoder();
    //算了还是用base64吧，反正Skidder也看不懂
    public static String decrypt(String ssoToken) {
        try {
            return new String(decoder.decodeBuffer(ssoToken), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Insecure";
    }
    public static String encrypt(String ssoToken) {
        try {
            final byte[] textByte = ssoToken.getBytes("UTF-8");
            return encoder.encode(textByte);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Insecure";
    }
}
