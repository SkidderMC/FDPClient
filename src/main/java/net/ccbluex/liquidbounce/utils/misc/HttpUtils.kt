/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */

package net.ccbluex.liquidbounce.utils.misc

import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * LiquidBounce Hacked Client
 * A minecraft forge injection client using Mixin
 *
 * @game Minecraft
 * @author CCBlueX
 */
object HttpUtils {

    private const val DEFAULT_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36"

    init {
        HttpURLConnection.setFollowRedirects(true)
    }

    fun make(url: String, method: String, data: String,
             agent: String = DEFAULT_AGENT): HttpURLConnection {
        val httpConnection = URL(url).openConnection() as HttpURLConnection

        httpConnection.requestMethod = method
        httpConnection.connectTimeout = 2000
        httpConnection.readTimeout = 10000

        httpConnection.setRequestProperty("User-Agent", agent)

        httpConnection.instanceFollowRedirects = true
        httpConnection.doOutput = true

        if(data.isNotEmpty()){
            val dataOutputStream = DataOutputStream(httpConnection.outputStream)
            dataOutputStream.writeBytes(data)
            dataOutputStream.flush()
        }

        return httpConnection
    }

    @Throws(IOException::class)
    fun request(url: String, method: String, data: String="",
                agent: String = DEFAULT_AGENT): String {
        val connection = make(url, method, data, agent)

        return connection.inputStream.reader().readText()
    }

    @Throws(IOException::class)
    fun requestStream(url: String, method: String,
                      agent: String = DEFAULT_AGENT): InputStream? {
        val connection = make(url, method, agent)

        return connection.inputStream
    }

    @Throws(IOException::class)
    @JvmStatic
    fun get(url: String) = request(url, "GET")

    @Throws(IOException::class)
    @JvmStatic
    fun post(url: String, data: String) = request(url, "POST", data=data)
}