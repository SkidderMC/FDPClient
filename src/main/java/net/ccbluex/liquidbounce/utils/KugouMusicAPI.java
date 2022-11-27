/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils;

import lombok.Getter;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Objects;

public class KugouMusicAPI {

    @Getter
    private static long total;

    public static KMusic[] search(String keyword, int page) {
        KMusic[] KMusics = new KMusic[0];
        try {
            keyword = URLEncoder.encode(keyword, "UTF-8");
            String searchAPIResult = sendGet("http://mobilecdn.kugou.com/api/v3/search/song?format=json&keyword=%KEYWORD%&page=%PAGE%&pagesize=20&showtype=1".replaceAll("%KEYWORD%", keyword).replaceAll("%PAGE%", String.valueOf(page)));
            JSONObject jsonObject = new JSONObject(searchAPIResult);
            if (
                    jsonObject.getInt("status") == 1 &&
                            jsonObject.getInt("errcode") == 0 &&
                            Objects.equals(jsonObject.getString("error"), "")
            ) {
                jsonObject = jsonObject.getJSONObject("data");
                total = jsonObject.getLong("total");
                int count = 0;
                KMusics = new KMusic[jsonObject.getJSONArray("info").length()];
                for (Object o : jsonObject.getJSONArray("info")) {
                    JSONObject musicJSONObject = (JSONObject) o;
                    KMusics[count] = new KMusic();
                    KMusics[count].hash = musicJSONObject.getString("hash");
                    KMusics[count].album_audio_id = musicJSONObject.getInt("album_audio_id");
                    KMusics[count].filename = musicJSONObject.getString("filename");
                    count++;
                }
            }
            for (KMusic KMusic : KMusics) {
                String infoAPIResult = sendGet("https://www.kugou.com/yy/index.php?r=play/getdata&hash=%HASH%&album_audio_id=%ALBUM_AUDIO_ID%".replaceAll("%HASH%", KMusic.hash).replaceAll("%ALBUM_AUDIO_ID%", String.valueOf(KMusic.album_audio_id)));
                JSONObject musicInfo = new JSONObject(infoAPIResult);
                if (
                        musicInfo.getInt("status") == 1 &&
                                musicInfo.getInt("err_code") == 0
                ) {
                    musicInfo = musicInfo.getJSONObject("data");
                    KMusic.mp3Url = musicInfo.getString("img");
                    KMusic.picUrl = musicInfo.getString("play_url");
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return KMusics;
    }

    public static String sendGet(String fullUrl) {
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        try {
            URL realUrl = new URL(fullUrl);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.setRequestProperty("cookie", "kg_mid=114514; kg_dfid=1919810; kg_dfid_collect=2333"); //酷狗我草尼姆
            connection.connect();
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result.toString();
    }

    public static class KMusic{
        public String filename, picUrl, mp3Url, hash;
        public Integer album_audio_id;
    }

}
