package net.ccbluex.liquidbounce.ui.client.gui.scriptOnline;

import net.ccbluex.liquidbounce.utils.misc.HttpUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class OnlineScriptLoader {
    public static List<String> getScriptsBySubscribe(String url) {
        String decrypt = new String(Base64.getDecoder().decode(HttpUtils.INSTANCE.get(url)), StandardCharsets.UTF_8);
        List<String> scripts = new ArrayList<>();
        for (String line : decrypt.split("#")) {
            String js = HttpUtils.INSTANCE.get(line);
            scripts.add(js);
        }
        return scripts;
    }
}
