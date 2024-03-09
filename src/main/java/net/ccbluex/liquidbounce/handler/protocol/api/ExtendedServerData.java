package net.ccbluex.liquidbounce.handler.protocol.api;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;

/**
 * This interface is used to store the target version for a specific server in the server list.
 */
public interface ExtendedServerData {

    ProtocolVersion viaForge$getVersion();

    void viaForge$setVersion(final ProtocolVersion version);

}