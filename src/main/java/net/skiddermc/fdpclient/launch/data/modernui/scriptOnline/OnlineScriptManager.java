package net.skiddermc.fdpclient.launch.data.modernui.scriptOnline;

import net.skiddermc.fdpclient.script.Script;

public class OnlineScriptManager {
    public static boolean isOnlineScript(Script script) {
        return script.isOnline();
    }
}
