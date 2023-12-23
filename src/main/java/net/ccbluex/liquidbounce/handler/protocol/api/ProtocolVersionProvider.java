package net.ccbluex.liquidbounce.handler.protocol.api;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.protocols.base.BaseVersionProvider;
import net.ccbluex.liquidbounce.handler.protocol.ProtocolBase;

import java.util.Objects;

public class ProtocolVersionProvider extends BaseVersionProvider {

    @Override
    public int getClosestServerProtocol(UserConnection connection) throws Exception {
        if (connection.isClientSide() && !ProtocolBase.getManager().getPlatform().isSingleplayer().get()) {
            return Objects.requireNonNull(connection.getChannel()).attr(ProtocolBase.VF_NETWORK_MANAGER).get().viaForge$getTrackedVersion().getVersion();
        }
        return super.getClosestServerProtocol(connection);
    }

}