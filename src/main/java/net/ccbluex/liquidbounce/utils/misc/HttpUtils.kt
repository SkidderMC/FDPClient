/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */

package net.ccbluex.liquidbounce.utils.misc

import org.apache.commons.io.FileUtils
import java.io.*
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

    const val DEFAULT_AGENT = "Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.7113.93 Safari/537.36 Java/1.8.0_191"

    init {
        HttpURLConnection.setFollowRedirects(true)
    }

    fun make(
        url: String,
        method: String,
        data: String = "",
        agent: String = DEFAULT_AGENT
    ): HttpURLConnection {
        val httpConnection = URL(url).openConnection() as HttpURLConnection

        httpConnection.requestMethod = method
        httpConnection.connectTimeout = 2000
        httpConnection.readTimeout = 10000

        httpConnection.setRequestProperty("User-Agent", agent)

        httpConnection.instanceFollowRedirects = true
        httpConnection.doOutput = true

        if (data.isNotEmpty()) {
            val dataOutputStream = DataOutputStream(httpConnection.outputStream)
            dataOutputStream.writeBytes(data)
            dataOutputStream.flush()
        }

        return httpConnection
    }

    fun request(
        url: String,
        method: String,
        data: String = "",
        agent: String = DEFAULT_AGENT
    ): String {
        val connection = make(url, method, data, agent)

        return connection.inputStream.reader().readText()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun download(url: String, file: File) = FileUtils.copyInputStreamToFile(make(url, "GET").inputStream, file)

    fun get(url: String) = request(url, "GET")

    fun post(url: String, data: String) = request(url, "POST", data = data)

}
