/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.login

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.apache.http.HttpHeaders
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.util.EntityUtils
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object UserUtils {

    private val tokenCache = mutableMapOf<String, Boolean>()
    private val uuidCache = mutableMapOf<String, String?>()
    private val usernameCache = mutableMapOf<String, String?>()

    val client: CloseableHttpClient by lazy {
        HttpClients.custom()
            .setConnectionManager(PoolingHttpClientConnectionManager().apply {
                maxTotal = 200
                defaultMaxPerRoute = 100
            }).build()
    }

    /**
     * Check if token is valid
     *
     * Exam
     * 7a7c4193280a4060971f1e73be3d9bdb
     * 89371141db4f4ec485d68d1f63d01eec
     */
    fun isValidTokenOffline(token: String) = token.length >= 32

    fun isValidToken(token: String): Boolean {
        tokenCache[token]?.let { return it }

        val request = HttpPost("https://authserver.mojang.com/validate").apply {
            setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            val body = JsonObject().apply {
                addProperty("accessToken", token)
            }
            entity = StringEntity(body.toString())
        }

        client.execute(request).use { response ->
            EntityUtils.consumeQuietly(response.entity)
            val isValid = response.statusLine.statusCode == 204
            tokenCache[token] = isValid
            return isValid
        }
    }

    fun getUsername(uuid: String): String? {
        uuidCache[uuid]?.let { return it }

        val request = HttpGet("https://api.minecraftservices.com/minecraft/profile/lookup/$uuid")

        client.execute(request).use { response ->
            if (response.statusLine.statusCode != 200) return null
            return try {
                val jsonObject = JsonParser().parse(EntityUtils.toString(response.entity)).asJsonObject
                val name = jsonObject["name"].asString
                uuidCache[uuid] = name
                name
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Get UUID of username
     */
    fun getUUID(username: String): String {
        usernameCache[username]?.let { return it }

        return try {
            val url = URL("https://api.minecraftservices.com/minecraft/profile/lookup/name/$username")
            (url.openConnection() as HttpsURLConnection).apply {
                connectTimeout = 2000
                readTimeout = 2000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:92.0) Gecko/20100101 Firefox/92.0")
                doOutput = false
            }.inputStream.use {
                val jsonElement = JsonParser().parse(InputStreamReader(it))
                if (jsonElement.isJsonObject) {
                    val id = jsonElement.asJsonObject["id"].asString
                    usernameCache[username] = id
                    id
                } else ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}