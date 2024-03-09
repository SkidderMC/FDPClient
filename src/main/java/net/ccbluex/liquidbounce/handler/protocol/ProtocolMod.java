package net.ccbluex.liquidbounce.handler.protocol;

import net.ccbluex.liquidbounce.handler.protocol.api.VFPlatform;
import net.minecraft.realms.RealmsSharedConstants;

public class ProtocolMod implements VFPlatform {

    public static final ProtocolMod PLATFORM = new ProtocolMod();

    @Override
    public int getGameVersion() {
        return RealmsSharedConstants.NETWORK_PROTOCOL_VERSION;
    }
}