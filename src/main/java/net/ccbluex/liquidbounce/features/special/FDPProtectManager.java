package net.ccbluex.liquidbounce.features.special;

public class FDPProtectManager {
    private final static FDPProtectManager instance;
    //我急了，写破防了，写了5个小时没写好

    static {
        instance = new FDPProtectManager();
    }
    public String VerifyText="Can't load FDPProtect, You can try restart client (Insecure Version)";
    public static FDPProtectManager getInstance(){
        return instance;
    }
}
