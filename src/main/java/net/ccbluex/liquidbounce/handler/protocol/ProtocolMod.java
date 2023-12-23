package net.ccbluex.liquidbounce.handler.protocol;

import net.ccbluex.liquidbounce.handler.protocol.api.ProtocolGameProfileFetcher;
import net.ccbluex.liquidbounce.handler.protocol.api.VFPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.realms.RealmsSharedConstants;
import net.minecraft.util.Session;
import net.raphimc.vialegacy.protocols.release.protocol1_8to1_7_6_10.providers.GameProfileFetcher;

import java.util.function.Supplier;

public class ProtocolMod implements VFPlatform {

    public static  ProtocolMod PLATFORM = new ProtocolMod();

    @Override
    public int getGameVersion() {
        return RealmsSharedConstants.NETWORK_PROTOCOL_VERSION;
    }

    @Override
    public Supplier<Boolean> isSingleplayer() {
        return () -> Minecraft.getMinecraft().isSingleplayer();
    }

    @Override
    public void joinServer(String serverId) throws Throwable {
        final Session session = Minecraft.getMinecraft().getSession();

        Minecraft.getMinecraft().getSessionService().joinServer(session.getProfile(), session.getToken(), serverId);
    }

    @Override
    public GameProfileFetcher getGameProfileFetcher() {
        return new ProtocolGameProfileFetcher();
    }

}