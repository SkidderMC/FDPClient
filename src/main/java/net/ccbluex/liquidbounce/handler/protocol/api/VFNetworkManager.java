package net.ccbluex.liquidbounce.handler.protocol.api;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

public interface VFNetworkManager {

    void viaForge$setupPreNettyDecryption();

    /**
     * @return the target version of the connection
     */
    ProtocolVersion viaForge$getTrackedVersion();

    /**
     * Sets the target version of the connection.
     *
     * @param version the target version
     */
    void viaForge$setTrackedVersion(final ProtocolVersion version);

}