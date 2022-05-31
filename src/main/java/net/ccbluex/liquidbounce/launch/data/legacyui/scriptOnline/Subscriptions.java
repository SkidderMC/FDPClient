package net.ccbluex.liquidbounce.launch.data.legacyui.scriptOnline;

import net.ccbluex.liquidbounce.LiquidBounce;

import java.util.ArrayList;
import java.util.List;

public class Subscriptions {
    public static boolean loadingCloud = false;
    public static String tempJs = "";
    public static List<ScriptSubscribe> subscribes = new ArrayList<>();

    public static void addSubscribes(ScriptSubscribe scriptSubscribe) {
        subscribes.add(scriptSubscribe);
        LiquidBounce.fileManager.getSubscriptsConfig().addSubscripts(scriptSubscribe.url, scriptSubscribe.name);
    }
}
