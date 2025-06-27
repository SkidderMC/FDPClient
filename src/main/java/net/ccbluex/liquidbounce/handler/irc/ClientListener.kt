/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.irc

import moe.lasoleil.axochat4j.packet.AxochatPacket

interface ClientListener {

    /**
     * Handle connect to web socket
     */
    fun onConnect()

    /**
     * Handle connect to web socket
     */
    fun onConnected()

    /**
     * Handle handshake
     */
    fun onHandshake(success: Boolean)

    /**
     * Handle disconnect
     */
    fun onDisconnect()

    /**
     * Handle logon to web socket with minecraft account
     */
    fun onLogon()

    /**
     * Handle incoming packets
     */
    fun onPacket(packet: AxochatPacket)

    /**
     * Handle error
     */
    fun onError(cause: Throwable)

}