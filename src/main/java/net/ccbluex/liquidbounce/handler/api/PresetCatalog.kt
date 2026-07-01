/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.api

import net.ccbluex.liquidbounce.file.gson.decodeJson
import net.ccbluex.liquidbounce.utils.io.AddHeaderInterceptor
import net.ccbluex.liquidbounce.utils.io.DEFAULT_AGENT
import net.ccbluex.liquidbounce.utils.io.get
import okhttp3.OkHttpClient
import java.io.IOException
import java.net.URI
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

data class PresetCatalog(
    val schemaVersion: Int = 1,
    val presets: List<CatalogPreset> = emptyList(),
)

data class CatalogPreset(
    val id: String,
    val name: String = id,
    val description: String = "",
    val url: String,
    val sha256: String,
    val servers: List<String> = emptyList(),
    val anticheats: List<String> = emptyList(),
)

/** HTTPS catalog reader with a mandatory digest for every executable settings script. */
object PresetCatalogService {

    private const val MAX_CATALOG_BYTES = 512 * 1024
    private const val MAX_PRESET_BYTES = 1024 * 1024
    private val sha256Pattern = Regex("^[0-9a-fA-F]{64}$")

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(AddHeaderInterceptor("User-Agent", DEFAULT_AGENT))
            .build()
    }

    fun fetchCatalog(url: String, expectedSha256: String): PresetCatalog {
        requireSecureUrl(url)
        require(sha256Pattern.matches(expectedSha256)) { "Catalog requires a pinned SHA-256" }
        val bytes = fetchBytes(url, MAX_CATALOG_BYTES)
        check(digest(bytes).equals(expectedSha256, ignoreCase = true)) { "Preset catalog failed SHA-256 verification" }
        val catalog = bytes.toString(Charsets.UTF_8).decodeJson<PresetCatalog>()
        require(catalog.schemaVersion == 1) { "Unsupported preset catalog schema ${catalog.schemaVersion}" }
        require(catalog.presets.map { it.id.lowercase() }.distinct().size == catalog.presets.size) {
            "Preset catalog contains duplicate IDs"
        }
        catalog.presets.forEach(::validatePreset)
        return catalog
    }

    fun findForServer(catalog: PresetCatalog, domain: String): CatalogPreset? {
        val normalized = domain.substringBefore(':').trim().lowercase()
        return catalog.presets.firstOrNull { preset ->
            preset.servers.any { pattern -> domainMatches(normalized, pattern) }
        }
    }

    fun downloadVerified(catalogUrl: String, preset: CatalogPreset): String {
        validatePreset(preset)
        val resolved = URI(catalogUrl).resolve(preset.url).toString()
        requireSecureUrl(resolved)
        val bytes = fetchBytes(resolved, MAX_PRESET_BYTES)
        val actual = digest(bytes)
        check(actual.equals(preset.sha256, ignoreCase = true)) {
            "Preset ${preset.id} failed SHA-256 verification"
        }
        return bytes.toString(Charsets.UTF_8)
    }

    internal fun domainMatches(domain: String, pattern: String): Boolean {
        val normalized = pattern.substringBefore(':').trim().lowercase().removePrefix("*.")
        return normalized.isNotEmpty() && (domain == normalized || domain.endsWith(".$normalized"))
    }

    internal fun digest(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes)
        .joinToString("") { "%02x".format(it) }

    private fun validatePreset(preset: CatalogPreset) {
        require(preset.id.matches(Regex("^[a-zA-Z0-9._-]{1,64}$"))) { "Invalid preset ID" }
        require(sha256Pattern.matches(preset.sha256)) { "Preset ${preset.id} has no valid SHA-256" }
        require(preset.url.isNotBlank()) { "Preset ${preset.id} has no URL" }
    }

    private fun requireSecureUrl(url: String) {
        val uri = runCatching { URI(url) }.getOrElse { throw IllegalArgumentException("Invalid preset URL", it) }
        val local = uri.host.equals("localhost", true) || uri.host == "127.0.0.1" || uri.host == "::1"
        require(uri.scheme.equals("https", true) || (local && uri.scheme.equals("http", true))) {
            "Preset catalogs require HTTPS"
        }
    }

    private fun fetchBytes(url: String, maximum: Int): ByteArray = client.get(url).use { response ->
        if (!response.isSuccessful) throw IOException("Preset request failed with HTTP ${response.code}")
        val declared = response.body.contentLength()
        if (declared > maximum) throw IOException("Preset response exceeds $maximum bytes")
        val bytes = response.body.bytes()
        if (bytes.size > maximum) throw IOException("Preset response exceeds $maximum bytes")
        bytes
    }
}
