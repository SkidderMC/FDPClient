/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import net.ccbluex.liquidbounce.utils.client.ClientUtils
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

/**
 * HttpUtils based on OkHttp3
 *
 * @author MukjepScarlet
 */
object HttpUtils {

    const val DEFAULT_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"

    val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
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

    @JvmStatic
    private fun createTrustAllSslSocketFactory(): SSLSocketFactory {
        val trustAllCerts = arrayOf(createTrustAllTrustManager())
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())
        return sslContext.socketFactory
    }

    @JvmStatic
    private fun createTrustAllTrustManager(): X509TrustManager {
        return object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
    }

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

    fun requestStream(
        url: String,
        method: String = "GET",
        agent: String = DEFAULT_AGENT,
        headers: Array<Pair<String, String>> = emptyArray(),
        body: RequestBody? = null
    ): Pair<InputStream, Int> {
        val request = makeRequest(url, method, agent, headers, body)
        val response = httpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            throw IOException("Unexpected code ${response.code}")
        }

        return response.body.byteStream() to response.code
    }

    private fun request(
        url: String,
        method: String,
        agent: String = DEFAULT_AGENT,
        headers: Array<Pair<String, String>> = emptyArray(),
        body: RequestBody? = null
    ): Pair<String, Int> {
        val request = makeRequest(url, method, agent, headers, body)
        httpClient.newCall(request).execute().use { response ->
            val responseBody = response.body.string()
            return responseBody to response.code
        }
    }

    fun get(url: String, agent: String = DEFAULT_AGENT, headers: Array<Pair<String, String>> = emptyArray()): Pair<String, Int> {
        return request(url, "GET", agent, headers)
    }

    inline fun <reified T> getJson(url: String): T? {
        return runCatching {
            httpClient.newCall(Request.Builder().url(url).build()).execute().use {
                it.body.charStream().decodeJson<T>()
            }
        }.onFailure {
            ClientUtils.LOGGER.error("[HTTP] Failed to GET JSON from $url", it)
        }.getOrNull()
    }

    fun post(
        url: String,
        agent: String = DEFAULT_AGENT,
        headers: Array<Pair<String, String>> = emptyArray(),
        body: RequestBody
    ): Pair<String, Int> {
        return request(url, "POST", agent, headers, body)
    }

    fun responseCode(url: String, method: String, agent: String = DEFAULT_AGENT): Int {
        val request = makeRequest(url, method, agent)
        httpClient.newCall(request).execute().use { response ->
            return response.code
        }
    }

    object Downloader {

        suspend fun download(
            url: String,
            targetFile: File,
            parallelism: Int = 4,
            chunkSize: Long = 2 * 1024 * 1024
        ) = withContext(Dispatchers.IO) {
            require(parallelism > 0)
            require(chunkSize >= 1024)

            if (parallelism == 1) {
                downloadWholeFile(url, targetFile)
                return@withContext
            }

            val (fileSize, supportsRange) = getFileSizeAndRangeSupport(url)

            if (fileSize <= 0 || !supportsRange) {
                downloadWholeFile(url, targetFile)
                return@withContext
            }

            val maxConcurrency = ((fileSize + chunkSize - 1) / chunkSize).toInt()

            val semaphore = Semaphore(parallelism)

            ClientUtils.LOGGER.info("[HTTP] Starting ${minOf(parallelism, maxConcurrency)} tasks for downloading $url to $targetFile")

            val tempFiles = (0 until maxConcurrency).map { chunkIndex ->
                async {
                    semaphore.withPermit {
                        val start = chunkIndex * chunkSize
                        val end = minOf((chunkIndex + 1) * chunkSize - 1, fileSize - 1)
                        val tempFile = File(targetFile.parent, "chunk_$chunkIndex.tmp")

                        downloadChunk(url, start, end, tempFile)
                        tempFile
                    }
                }
            }.awaitAll()

            mergeChunks(tempFiles, targetFile)
        }

        private fun getFileSizeAndRangeSupport(url: String): Pair<Long, Boolean> {
            val request = Request.Builder()
                .url(url)
                .head()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return Pair(-1, false)

                val contentLength = response.header("Content-Length")?.toLongOrNull() ?: -1
                val acceptRanges = response.header("Accept-Ranges")
                val supportsRange = acceptRanges == "bytes"

                return Pair(contentLength, supportsRange)
            }
        }

        fun downloadWholeFile(url: String, targetFile: File) {
            val request = Request.Builder()
                .url(url)
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Download failed: ${response.code}")

                targetFile.outputStream().use { output ->
                    response.body.byteStream().copyTo(output)
                }
            }
        }

        private fun downloadChunk(url: String, start: Long, end: Long, tempFile: File) {
            val request = Request.Builder()
                .url(url)
                .addHeader("Range", "bytes=$start-$end")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    tempFile.outputStream().use { it.write(response.body.bytes()) }
                } else {
                    throw IOException("Failed to download chunk from $start to $end")
                }
            }
        }

        private fun mergeChunks(tempFiles: List<File>, targetFile: File) {
            RandomAccessFile(targetFile, "rw").use { mergedFile ->
                tempFiles.forEach { tempFile ->
                    tempFile.inputStream().use { input ->
                        mergedFile.channel.transferFrom(input.channel, mergedFile.length(), tempFile.length())
                    }
                    tempFile.delete()
                }
            }
        }
    }

}