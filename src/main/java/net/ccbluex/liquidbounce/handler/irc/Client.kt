/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.irc

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBufOutputStream
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollSocketChannel
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.DefaultHttpHeaders
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory
import io.netty.handler.codec.http.websocketx.WebSocketVersion
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
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
import net.ccbluex.liquidbounce.utils.login.UserUtils
import net.minecraft.network.NetworkManager
import java.net.URI
import java.util.*

abstract class Client : ClientListener, MinecraftInstance {

    internal var channel: Channel? = null
    var username = ""
    var jwt = false
    var loggedIn = false

    /**
     * Connect websocket
     */
    fun connect() {
        onConnect()

        val uri = URI("wss://chat.liquidbounce.net:7886/ws")

        val ssl = uri.scheme.equals("wss", true)
        val sslContext = if (ssl) SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE) else null

        val handler = ClientHandler(this, WebSocketClientHandshakerFactory.newHandshaker(
                uri, WebSocketVersion.V13, null,
                true, DefaultHttpHeaders()))

        channel = Bootstrap().apply {
            if (Epoll.isAvailable()) {
                channelFactory(::EpollSocketChannel).group(NetworkManager.CLIENT_EPOLL_EVENTLOOP.value)
            } else {
                channelFactory(::NioSocketChannel).group(NetworkManager.CLIENT_NIO_EVENTLOOP.value)
            }
        }.handler(object : ChannelInitializer<SocketChannel>() {

            /**
             * This method will be called once the [Channel] was registered. After the method returns this instance
             * will be removed from the [ChannelPipeline] of the [Channel].
             *
             * @param ch            the [Channel] which was registered.
             * @throws Exception    is thrown if an error occurs. In that case the [Channel] will be closed.
             */
            override fun initChannel(ch: SocketChannel) {
                val pipeline = ch.pipeline()

                if (sslContext != null) pipeline.addLast(sslContext.newHandler(ch.alloc()))

                pipeline.addLast(HttpClientCodec(), HttpObjectAggregator(8192), handler)
            }
        }).connect(uri.host, uri.port).sync().channel()

        handler.handshakeFuture.sync()

        if (isConnected()) onConnected()
    }

    /**
     * Disconnect websocket
     */
    fun disconnect() {
        channel?.close()
        channel = null
        username = ""
        jwt = false
    }

    /**
     * Login to web socket
     */
    fun loginMojang() = sendPacket(C2SRequestMojangInfoPacket())

    /**
     * Login to web socket
     */
    fun loginJWT(token: String) {
        onLogon()
        sendPacket(C2SLoginJWTPacket(token, true))
        jwt = true
    }

    fun isConnected() = channel?.isOpen ?: false

    /**
     * Handle incoming message of websocket
     */
    internal fun onMessage(message: String) {
        val packet = GsonAxochatClientAdapter.INSTANCE.read(message)

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
    fun sendPacket(packet: AxochatPacket) {
        val channel = channel ?: return

        val buffer = PooledByteBufAllocator.DEFAULT.buffer(256)
        ByteBufOutputStream(buffer).writer(Charsets.UTF_8).use {
            GsonAxochatClientAdapter.INSTANCE.write(it, packet)
        }
        channel.writeAndFlush(TextWebSocketFrame(buffer))
    }

    /**
     * Send chat message to server
     */
    fun sendMessage(message: String) = sendPacket(C2SMessagePacket(message))

    /**
     * Send private chat message to server
     */
    fun sendPrivateMessage(username: String, message: String) = sendPacket(C2SPrivateMessagePacket(username, message))

    /**
     * Ban user from server
     */
    fun banUser(target: String) = sendPacket(C2SBanUserPacket(toUUID(target)))

    /**
     * Unban user from server
     */
    fun unbanUser(target: String) = sendPacket(C2SUnbanUserPacket(toUUID(target)))

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
