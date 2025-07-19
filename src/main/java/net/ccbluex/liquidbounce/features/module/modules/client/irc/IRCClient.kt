/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client.irc

import moe.lasoleil.axochat4j.client.AxochatClientConnection
import moe.lasoleil.axochat4j.client.OkHttpAxochatClient.newAxochatConnection
import moe.lasoleil.axochat4j.client.WebSocketConnectionEvent
import moe.lasoleil.axochat4j.codec.GsonAxochatClientAdapter
import moe.lasoleil.axochat4j.packet.AxochatPacket
import moe.lasoleil.axochat4j.packet.c2s.C2SBanUserPacket
import moe.lasoleil.axochat4j.packet.c2s.C2SLoginJWTPacket
import moe.lasoleil.axochat4j.packet.c2s.C2SLoginMojangPacket
import moe.lasoleil.axochat4j.packet.c2s.C2SMessagePacket
import moe.lasoleil.axochat4j.packet.c2s.C2SPrivateMessagePacket
import moe.lasoleil.axochat4j.packet.c2s.C2SRequestMojangInfoPacket
import moe.lasoleil.axochat4j.packet.c2s.C2SUnbanUserPacket
import moe.lasoleil.axochat4j.packet.s2c.S2CMojangInfoPacket
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.io.HttpClient
import net.ccbluex.liquidbounce.utils.login.UserUtils
import java.net.URI
import java.util.*

abstract class IRCClient : ClientListener, MinecraftInstance {
    @Volatile
    private var connection: AxochatClientConnection? = null
    var username = ""
    var jwt = false
    var loggedIn = false

    /**
     * Connect websocket
     */
    @Synchronized
    fun connect() {
        if (connection != null) return

        onConnect()

        val config = AxochatClientConnection.Config.builder()
            .packetAdaptor(GsonAxochatClientAdapter.INSTANCE)
            .uri(uri)
            .webSocketHandler { connection, event ->
                when (event) {
                    is WebSocketConnectionEvent.Connected -> {
                        onConnected()
                    }
                    is WebSocketConnectionEvent.Disconnected -> {
                        onDisconnect()
                        this.connection = null
                        username = ""
                        jwt = false
                    }
                    is WebSocketConnectionEvent.ErrorOccurred -> {
                        onError(event.cause)
                    }
                }
            }
            .packetHandler { connection, packet ->
                onPacket0(packet)
            }
            .build()

        connection = HttpClient.newAxochatConnection(config)
    }

    /**
     * Disconnect websocket
     */
    @Synchronized
    fun disconnect() {
        connection?.close(true)
        this.connection = null
        username = ""
        jwt = false
    }

    /**
     * Login to web socket
     */
    @Synchronized
    fun loginMojang() = sendPacket(C2SRequestMojangInfoPacket())

    /**
     * Login to web socket
     */
    @Synchronized
    fun loginJWT(token: String) {
        onLogon()
        sendPacket(C2SLoginJWTPacket(token, true))
        jwt = true
    }

    @Synchronized
    fun isConnected() = connection != null

    /**
     * Handle incoming message of websocket
     */
    private fun onPacket0(packet: AxochatPacket.S2C) {
        if (packet is S2CMojangInfoPacket) {
            onLogon()

            try {
                val sessionHash = packet.sessionHash

                mc.sessionService.joinServer(mc.session.profile, mc.session.token, sessionHash)
                username = mc.session.username
                jwt = false

                sendPacket(C2SLoginMojangPacket(mc.session.username, mc.session.profile.id, true))
            } catch (throwable: Throwable) {
                onError(throwable)
            }
            return
        }

        onPacket(packet)
    }

    /**
     * Send packet to server
     */
    @Synchronized
    fun sendPacket(packet: AxochatPacket.C2S) {
        connection?.send(packet)
    }

    /**
     * Send chat message to server
     */
    @Synchronized
    fun sendMessage(message: String) = sendPacket(C2SMessagePacket(message))

    /**
     * Send private chat message to server
     */
    @Synchronized
    fun sendPrivateMessage(username: String, message: String) = sendPacket(C2SPrivateMessagePacket(username, message))

    /**
     * Ban user from server
     */
    @Synchronized
    fun banUser(target: String) = sendPacket(C2SBanUserPacket(toUUID(target)))

    /**
     * Unban user from server
     */
    @Synchronized
    fun unbanUser(target: String) = sendPacket(C2SUnbanUserPacket(toUUID(target)))

    companion object {

        private val uri = URI("wss://chat.liquidbounce.net:7886/ws")

        /**
         * Convert username or uuid to UUID
         */
        private fun toUUID(target: String): String {
            return try {
                UUID.fromString(target)

                target
            } catch (_: IllegalArgumentException) {
                val incomingUUID = UserUtils.getUUID(target)

                if (incomingUUID.isNullOrBlank()) return ""

                val uuid = StringBuilder(incomingUUID)
                    .insert(20, '-')
                    .insert(16, '-')
                    .insert(12, '-')
                    .insert(8, '-')

                uuid.toString()
            }
        }
    }

}
