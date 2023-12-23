package net.ccbluex.liquidbounce.handler.protocol.api;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.MovementTransmitterProvider;

public class ProtocolMovementTransmitterProvider extends MovementTransmitterProvider {

    @Override
    public void sendPlayer(UserConnection userConnection) {
        // We are on the client side, so we can handle the idle packet properly
    }

}