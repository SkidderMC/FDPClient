/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.io

import io.netty.channel.EventLoopGroup
import io.netty.channel.epoll.Epoll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.ccbluex.liquidbounce.FDPClient.IN_DEV
import net.ccbluex.liquidbounce.FDPClient.clientCommit
import net.ccbluex.liquidbounce.FDPClient.clientVersionText
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.file.gson.decodeJson
import net.ccbluex.liquidbounce.utils.client.ClientUtils
import net.minecraft.network.NetworkManager
import okhttp3.*
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

/**
 * User agent
 * <version> (<commit>, <branch>, <build-type>, <platform>)
 */
val DEFAULT_AGENT =
    "${CLIENT_NAME}/${clientVersionText} (${clientCommit}, ${if (IN_DEV) "dev" else "release"}, ${System.getProperty("os.name")})"

val clientEventLoopGroup: EventLoopGroup get() = if (Epoll.isAvailable()) {
    NetworkManager.CLIENT_EPOLL_EVENTLOOP.value
} else {
    NetworkManager.CLIENT_NIO_EVENTLOOP.value
}

/**
 * Global [OkHttpClient]
 */
val HttpClient: OkHttpClient = OkHttpClient.Builder()
    .dispatcher(Dispatcher(clientEventLoopGroup))
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(25, TimeUnit.SECONDS)
    .writeTimeout(25, TimeUnit.SECONDS)
    .cache(Cache(FileManager.dir.resolve("http-cache"), maxSize = 16L shl 20))
    .applyBypassHttps()
    .addInterceptor(AddHeaderInterceptor("User-Agent", DEFAULT_AGENT))
    .build()

class AddHeaderInterceptor(
    val name: String,
    val value: String,
    val replace: Boolean = false,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        if (replace) builder.removeHeader(name)
        builder.addHeader(name, value)
        return chain.proceed(builder.build())
    }
}

// Requests

fun OkHttpClient.get(url: String) = newCall {
    url(url).get()
}.execute()

fun OkHttpClient.head(url: String) = newCall {
    url(url).head()
}.execute()

fun OkHttpClient.post(url: String, body: RequestBody = RequestBody.EMPTY) = newCall {
    url(url).post(body)
}.execute()

fun OkHttpClient.delete(url: String, body: RequestBody? = RequestBody.EMPTY) = newCall {
    url(url).delete(body)
}.execute()

fun OkHttpClient.put(url: String, body: RequestBody = RequestBody.EMPTY) = newCall {
    url(url).put(body)
}.execute()

fun OkHttpClient.patch(url: String, body: RequestBody = RequestBody.EMPTY) = newCall {
    url(url).patch(body)
}.execute()

fun OkHttpClient.request(url: String, method: String, body: RequestBody? = null) = newCall {
    url(url).method(method, body)
}.execute()

// General

inline fun OkHttpClient.newCall(requestBlock: Request.Builder.() -> Unit): Call =
    this.newCall(Request.Builder().apply(requestBlock).build())

inline fun <reified T> Response.jsonBody(): T? = use {
    runCatching {
        this.body.charStream().decodeJson<T>()
    }.onFailure {
        ClientUtils.LOGGER.error("[HTTP] Failed to parse JSON body (${T::class.java.simpleName})", it)
    }.getOrNull()
}

fun Response.toFile(file: File) = use { response ->
    if (response.isSuccessful) {
        val partFile = File(file.parentFile, file.name + ".part")
        partFile.sink().buffer().use(response.body.source()::readAll)
        file.delete()
        if (!partFile.renameTo(file)) {
            partFile.delete()
            throw IOException("[HTTP] Failed to move ${partFile.name} to $file")
        }
    } else {
        throw IOException("[HTTP] Failed to write Response to File $file, HTTP ${response.code} ${response.message}")
    }
}

private fun createTrustAllTrustManager(): X509TrustManager {
    return object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }
}

private fun createTrustAllSslSocketFactory(): SSLSocketFactory {
    val trustAllCerts = arrayOf(createTrustAllTrustManager())
    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, trustAllCerts, SecureRandom())
    return sslContext.socketFactory
}

/**
 * For Java 8 (e.g., 1.8.0_51) that might lack modern TLS support,
 * we force ignoring all certificate checks, enabling all TLS versions/ciphers.
 */
fun OkHttpClient.Builder.applyBypassHttps() = this
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

object Downloader {

    /**
     * Downloads [url] into [targetFile].
     *
     * Network failures (timeouts, DNS errors, broken streams, non-2xx responses, ...) are caught
     * and logged instead of being propagated, so a download that happens during startup can never
     * crash the client. On failure the target file is left absent/unchanged and the caller is
     * expected to fall back to the bundled resource. Returns `true` when the file was downloaded
     * successfully, `false` otherwise.
     */
    suspend fun download(
        url: String,
        targetFile: File,
        parallelism: Int = 4,
        chunkSize: Long = 2 * 1024 * 1024
    ): Boolean = withContext(Dispatchers.IO) {
        require(parallelism > 0)
        require(chunkSize >= 1024)

        try {
            if (parallelism == 1) {
                downloadWholeFile(url, targetFile)
                return@withContext true
            }

            val (fileSize, supportsRange) = getFileSizeAndRangeSupport(url)

            if (fileSize <= 0 || !supportsRange) {
                downloadWholeFile(url, targetFile)
                return@withContext true
            }

            val maxConcurrency = ((fileSize + chunkSize - 1) / chunkSize).toInt()

            val semaphore = Semaphore(parallelism)

            ClientUtils.LOGGER.info(
                "[HTTP] Starting ${
                    minOf(
                        parallelism,
                        maxConcurrency
                    )
                } tasks for downloading $url to $targetFile"
            )

            val tempFiles = List(maxConcurrency) { chunkIndex ->
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
            true
        } catch (throwable: Throwable) {
            ClientUtils.LOGGER.error(
                "[HTTP] Failed to download $url to $targetFile, falling back to the bundled resource",
                throwable
            )
            false
        }
    }

    private fun getFileSizeAndRangeSupport(url: String): Pair<Long, Boolean> =
        runCatching {
            HttpClient.head(url).use { response ->
                if (!response.isSuccessful) return@runCatching Pair(-1L, false)

                val contentLength = response.header("Content-Length")?.toLongOrNull() ?: -1
                val acceptRanges = response.header("Accept-Ranges")
                val supportsRange = acceptRanges == "bytes"

                Pair(contentLength, supportsRange)
            }
        }.getOrElse { throwable ->
            ClientUtils.LOGGER.warn(
                "[HTTP] Failed to probe size/range support for $url, falling back to whole-file download",
                throwable
            )
            Pair(-1L, false)
        }

    /**
     * Downloads the whole [url] into [targetFile].
     *
     * Any failure is caught and logged so a startup download can never crash the client; the caller
     * is expected to detect the missing file and fall back to the bundled resource. Returns `true`
     * on success, `false` otherwise.
     */
    fun downloadWholeFile(url: String, targetFile: File): Boolean =
        runCatching {
            HttpClient.get(url).toFile(targetFile)
            true
        }.getOrElse { throwable ->
            ClientUtils.LOGGER.error(
                "[HTTP] Failed to download $url to $targetFile, falling back to the bundled resource",
                throwable
            )
            false
        }

    private fun downloadChunk(url: String, start: Long, end: Long, tempFile: File) {
        try {
            HttpClient.newCall {
                url(url).addHeader("Range", "bytes=$start-$end")
            }.execute().toFile(tempFile)
        } catch (e: IOException) {
            throw IOException("Failed to download chunk from $start to $end", e)
        }
    }

    private fun mergeChunks(tempFiles: List<File>, targetFile: File) {
        targetFile.sink().buffer().use { mergedSink ->
            for (tempFile in tempFiles) {
                tempFile.source().buffer().use(mergedSink::writeAll)
                tempFile.delete()
            }
        }
    }
}
