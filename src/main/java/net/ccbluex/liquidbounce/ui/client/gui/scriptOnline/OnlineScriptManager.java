package net.ccbluex.liquidbounce.ui.client.gui.scriptOnline;

import net.ccbluex.liquidbounce.script.Script;

public class OnlineScriptManager {
    public static boolean isOnlineScript(Script script) {
        return script.isOnline();
    }
}
