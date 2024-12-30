/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.io

import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

/**
 * HttpUtils based on OkHttp3
 *
 * @author MukjepScarlet
 */
object HttpUtils {

    const val DEFAULT_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/131.0.0.0 Safari/537.36"

    val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .applyBypassHttps()
        .build()

    /**
     * For Java 8 (e.g., 1.8.0_51) that might lack modern TLS support,
     * we force ignoring all certificate checks, enabling all TLS versions/ciphers.
     */
    @JvmStatic
    fun OkHttpClient.Builder.applyBypassHttps(): OkHttpClient.Builder {
        return this
            .sslSocketFactory(createTrustAllSslSocketFactory(), createTrustAllTrustManager())
            .hostnameVerifier { _, _ -> true }
            .connectionSpecs(
                listOf(
                    ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .allEnabledTlsVersions()
                        .allEnabledCipherSuites()
                        .build(),
                    ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                        .allEnabledTlsVersions()
                        .allEnabledCipherSuites()
                        .build(),
                    ConnectionSpec.CLEARTEXT
                )
            )
    }

    /**
     * Creates an SSLSocketFactory that does not validate any certificate.
     */
    @JvmStatic
    private fun createTrustAllSslSocketFactory(): SSLSocketFactory {
        val trustAllCerts = arrayOf(createTrustAllTrustManager())
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())
        return sslContext.socketFactory
    }

    /**
     * Returns a TrustManager that trusts all certificates.
     */
    @JvmStatic
    private fun createTrustAllTrustManager(): X509TrustManager {
        return object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
    }

    /**
     * Constructs a basic Request with the specified method, body, and headers.
     */
    private fun makeRequest(
        url: String,
        method: String,
        agent: String = DEFAULT_AGENT,
        headers: Array<Pair<String, String>> = emptyArray(),
        body: RequestBody? = null
    ): Request {
        val builder = Request.Builder()
            .url(url)
            .method(method, body)
            .header("User-Agent", agent)

        for ((key, value) in headers) {
            builder.addHeader(key, value)
        }

        return builder.build()
    }

    /**
     * Performs a request and returns an InputStream and the HTTP status code.
     */
    fun requestStream(
        url: String,
        method: String,
        agent: String = DEFAULT_AGENT,
        headers: Array<Pair<String, String>> = emptyArray(),
        body: RequestBody? = null
    ): Pair<InputStream, Int> {
        val request = makeRequest(url, method, agent, headers, body)
        val response = httpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            throw IOException("Unexpected code ${response.code}")
        }

        return response.body?.byteStream()!! to response.code
    }

    /**
     * Performs a request and returns the response body as a String and the HTTP status code.
     */
    fun request(
        url: String,
        method: String,
        agent: String = DEFAULT_AGENT,
        headers: Array<Pair<String, String>> = emptyArray(),
        body: RequestBody? = null
    ): Pair<String, Int> {
        val request = makeRequest(url, method, agent, headers, body)
        httpClient.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: ""
            return responseBody to response.code
        }
    }

    /**
     * Performs a GET request.
     */
    fun get(
        url: String,
        agent: String = DEFAULT_AGENT,
        headers: Array<Pair<String, String>> = emptyArray()
    ): Pair<String, Int> {
        return request(url, "GET", agent, headers)
    }

    /**
     * Performs a POST request.
     */
    fun post(
        url: String,
        agent: String = DEFAULT_AGENT,
        headers: Array<Pair<String, String>> = emptyArray(),
        body: RequestBody
    ): Pair<String, Int> {
        return request(url, "POST", agent, headers, body)
    }

    /**
     * Returns only the HTTP status code for a given request.
     */
    fun responseCode(
        url: String,
        method: String,
        agent: String = DEFAULT_AGENT
    ): Int {
        val request = makeRequest(url, method, agent)
        httpClient.newCall(request).execute().use { response ->
            return response.code
        }
    }

    /**
     * Downloads a file from the given URL and saves it to 'file'.
     */
    fun download(
        url: String,
        file: File,
        agent: String = DEFAULT_AGENT,
        headers: Array<Pair<String, String>> = emptyArray()
    ) {
        val request = makeRequest(url, "GET", agent, headers)
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Failed to download file: ${response.code}")
            }
            response.body?.byteStream()?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: throw IOException("Response body is null")
        }
    }
}
