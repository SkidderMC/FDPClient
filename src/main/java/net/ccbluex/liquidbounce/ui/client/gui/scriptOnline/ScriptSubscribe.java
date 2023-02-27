package net.ccbluex.liquidbounce.ui.client.gui.scriptOnline;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.script.Script;

import java.io.File;
import java.util.Objects;

public class ScriptSubscribe {
    public final String url;
    public final String name;
    public boolean state = true;

    public ScriptSubscribe(String url, String name) {
        if (Objects.equals(name, "")) name = url;
        this.url = url;
        this.name = name;
    }

    public void load() {
        Subscriptions.loadingCloud = true;
        for (String script : OnlineScriptLoader.getScriptsBySubscribe(url)) {
            Subscriptions.tempJs = script;
            Script script1 = new Script(new File("CloudLoad"));
            LiquidBounce.scriptManager.getScripts().add(script1);
        }
        Subscriptions.loadingCloud = false;
    }
}
