package net.ccbluex.liquidbounce.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HWIDUtils {
	public static String getHWID(){
		try {
			StringBuilder s = new StringBuilder();
			String main = System.getenv("PROCESS_IDENTIFIER") + System.getenv("COMPUTERNAME");
			byte[] bytes = main.getBytes("UTF-8");
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			byte[] md5 = messageDigest.digest(bytes);
			int i = 0;
			for (byte b : md5) {
				s.append(Integer.toHexString((b & 0xFF) | 0x300), 0, 3);
				if (i != md5.length - 1) {
					s.append("");
				}
				i++;
//			System.out.println(calendar.get(Calendar.YEAR));
			}
			return (s.toString()).substring(s.length() - 15, s.length());
		}catch (Exception e){
			e.printStackTrace();
			return "NULL";
		}
	}
}
