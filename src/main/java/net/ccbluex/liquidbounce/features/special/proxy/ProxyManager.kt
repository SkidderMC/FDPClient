package net.ccbluex.liquidbounce.features.special.proxy

import java.net.InetSocketAddress
import java.net.Proxy

object ProxyManager {
    var address=""
    var type=Type.DISABLE

    fun getProxy():Proxy {
        return if(type==Type.DISABLE){
            Proxy.NO_PROXY
        }else{
            val add = address.split(":")
            val port=if(add.size>1){add[1].toInt()}else{8080}
            val addr = InetSocketAddress(add[0], port)
            Proxy(type.type,addr)
        }
    }

    enum class Type(val displayName: String, val type: Proxy.Type) {
        DISABLE("Disable",Proxy.Type.DIRECT),
        SOCKS("Socks",Proxy.Type.SOCKS),
        HTTP("Http",Proxy.Type.HTTP)
    }
}