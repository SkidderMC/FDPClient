package net.ccbluex.liquidbounce.handler.protocol.api;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

public interface VFNetworkManager {

    ProtocolVersion viaForge$getTrackedVersion();

    void viaForge$setTrackedVersion(final ProtocolVersion version);

}