package net.ccbluex.liquidbounce.handler.protocol.api;

import net.raphimc.vialoader.util.VersionEnum;

public interface VFNetworkManager {

    void viaForge$setupPreNettyDecryption();

    /**
     * @return the target version of the connection
     */
    VersionEnum viaForge$getTrackedVersion();

    /**
     * Sets the target version of the connection.
     *
     * @param version the target version
     */
    void viaForge$setTrackedVersion(final VersionEnum version);

}