package net.ccbluex.liquidbounce.features.module.modules.client.irc

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
    fun onPacket(packet: AxochatPacket.S2C)

    /**
     * Handle error
     */
    fun onError(cause: Throwable)

}