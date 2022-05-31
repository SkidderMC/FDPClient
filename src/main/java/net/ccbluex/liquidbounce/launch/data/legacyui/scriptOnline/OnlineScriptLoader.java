package net.ccbluex.liquidbounce.launch.data.legacyui.scriptOnline;

import net.ccbluex.liquidbounce.utils.FDP4nt1Sk1dUtils;
import net.ccbluex.liquidbounce.utils.misc.HttpUtils;

import java.util.ArrayList;
import java.util.List;

public class OnlineScriptLoader {
    public static List<String> getScriptsBySubscribe(String url) {
        String decrypt = FDP4nt1Sk1dUtils.decrypt(HttpUtils.INSTANCE.get(url));
        List<String> scripts = new ArrayList<>();
        for (String line : decrypt.split("#")) {
            String js = HttpUtils.INSTANCE.get(line);
            scripts.add(js);
        }
        return scripts;
    }
}
