package net.ccbluex.liquidbounce.handler.protocol.api;

import com.viaversion.viaversion.api.connection.UserConnection;
import net.ccbluex.liquidbounce.handler.protocol.ProtocolBase;
import net.raphimc.vialegacy.protocols.release.protocol1_7_2_5to1_6_4.providers.EncryptionProvider;

public class ProtocolEncryptionProvider extends EncryptionProvider {

    @Override
    public void enableDecryption(UserConnection user) {
        user.getChannel().attr(ProtocolBase.VF_NETWORK_MANAGER).getAndRemove().viaForge$setupPreNettyDecryption();
    }

}