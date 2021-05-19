package net.ccbluex.liquidbounce.features.special.proxy

import io.netty.bootstrap.ChannelFactory
import io.netty.channel.socket.oio.OioSocketChannel
import java.net.Proxy
import java.net.Socket

class ProxyOioChannelFactory(private val proxy: Proxy) : ChannelFactory<OioSocketChannel?> {
    override fun newChannel(): OioSocketChannel {
        return OioSocketChannel(Socket(proxy))
    }
}
