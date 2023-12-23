package net.ccbluex.liquidbounce.handler.protocol.api;

import net.raphimc.vialegacy.protocols.release.protocol1_8to1_7_6_10.providers.GameProfileFetcher;

import java.util.function.Supplier;

/**
 * This interface is used to access platform specific fields.
 */
public interface VFPlatform {

    /**
     * @return the native version of the platform
     */
    int getGameVersion();

    /**
     * @return if the client is in singleplayer
     */
    Supplier<Boolean> isSingleplayer();

    /**
     * Sends the joinServer API request to authentication servers.
     *
     * @param serverId the server id of the server
     */
    void joinServer(final String serverId) throws Throwable;

    /**
     * @return the game profile fetcher of the platform for ViaLegacy
     */
    GameProfileFetcher getGameProfileFetcher();

}