/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.spotify

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.future.future
import kotlinx.coroutines.suspendCancellableCoroutine
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyAccessToken
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyAuthFlow
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyDefaults
import net.ccbluex.liquidbounce.ui.client.spotify.SpotifyService
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.kotlin.SharedScopes
import java.awt.Desktop
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Centralizes the Spotify Web API access so the service can be shared across
 * modules and provides helpers to complete the OAuth flow through the browser.
 */
object SpotifyIntegration : MinecraftInstance {

    private val authorizationScopes = SpotifyDefaults.authorizationScopes
    private val callbackPath = ensureLeadingSlash(SpotifyDefaults.authorizationRedirectPath)
    private val callbackPort = SpotifyDefaults.authorizationRedirectPort
    private val redirectUri = "http://127.0.0.1:$callbackPort$callbackPath"

    val service: SpotifyService = SpotifyService()

    init {
        LOGGER.info("[Spotify] Spotify integration handler initialized (redirectUri=$redirectUri)")
    }

    fun authorizeInBrowser(clientId: String, clientSecret: String?, flow: SpotifyAuthFlow): CompletableFuture<SpotifyAccessToken> {
        LOGGER.info("[Spotify][Browser] Beginning OAuth flow (clientId=${mask(clientId)}, flow=$flow)")
        return SharedScopes.IO.future {
            val state = UUID.randomUUID().toString()
            val pkce = if (flow == SpotifyAuthFlow.PKCE) generatePkceChallenge() else null
            val authorizationUrl = buildAuthorizeUrl(clientId, redirectUri, state, pkce?.challenge)
            openBrowser(authorizationUrl)
            val code = awaitAuthorizationCode(state)
            LOGGER.info("[Spotify][Browser] Authorization code received, exchanging for tokens")
            service.exchangeAuthorizationCode(clientId, clientSecret, code, redirectUri, pkce?.verifier)
        }
    }

    fun openDashboard() {
        openLink(SpotifyDefaults.dashboardUrl, "Spotify dashboard")
    }

    fun openGuide() {
        openLink(SpotifyDefaults.authorizationGuideUrl, "Spotify authorization guide")
    }

    private fun openLink(url: String, label: String) {
        runCatching {
            if (url.isBlank()) {
                throw IllegalStateException("$label URL is empty")
            }
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI(url))
            } else {
                chat("§eCopy this $label URL into your browser: $url")
            }
        }.onFailure {
            LOGGER.warn("[Spotify] Failed to open $label URL", it)
            chat("§cUnable to open $label: ${it.message}")
        }
    }

    private fun openBrowser(url: String) {
        LOGGER.info("[Spotify][Browser] Opening authorization page: $url")
        runCatching {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI(url))
            } else {
                chat("§eOpen the following Spotify authorization URL manually: $url")
            }
        }.onFailure {
            LOGGER.warn("[Spotify][Browser] Failed to open default browser", it)
            chat("§cUnable to open browser: ${it.message}. Open this URL manually: $url")
        }
    }

    private fun buildAuthorizeUrl(clientId: String, redirectUri: String, state: String, pkceChallenge: String?): String {
        val encodedRedirect = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.name())
        val scopeParam = URLEncoder.encode(authorizationScopes.trim().replace(ONE_OR_MORE_SPACES, " "), StandardCharsets.UTF_8.name())
        val builder = StringBuilder("https://accounts.spotify.com/authorize?")
        builder.append("response_type=code")
        builder.append("&client_id=").append(URLEncoder.encode(clientId, StandardCharsets.UTF_8.name()))
        builder.append("&redirect_uri=").append(encodedRedirect)
        builder.append("&scope=").append(scopeParam)
        builder.append("&state=").append(state)
        builder.append("&show_dialog=true")
        if (!pkceChallenge.isNullOrBlank()) {
            builder.append("&code_challenge=")
                .append(URLEncoder.encode(pkceChallenge, StandardCharsets.UTF_8.name()))
            builder.append("&code_challenge_method=S256")
        }
        return builder.toString()
    }

    private suspend fun awaitAuthorizationCode(expectedState: String): String = suspendCancellableCoroutine { cont ->
        val completed = AtomicBoolean(false)
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", callbackPort), 0)
        val handler = HttpHandler { exchange ->
            handleExchange(exchange, expectedState, completed, cont)
        }
        server.createContext(callbackPath, handler)
        server.start()
        cont.invokeOnCancellation {
            runCatching { server.stop(0) }
        }
    }

    private fun handleExchange(
        exchange: HttpExchange,
        expectedState: String,
        completed: AtomicBoolean,
        cont: kotlin.coroutines.Continuation<String>,
    ) {
        try {
            val params = parseQuery(exchange.requestURI.rawQuery.orEmpty())
            val state = params["state"]
            val code = params["code"]
            val error = params["error"]
            val response = buildBrowserResponse(error == null && !code.isNullOrBlank())
            exchange.sendResponseHeaders(200, response.size.toLong())
            exchange.responseBody.use { out: OutputStream ->
                out.write(response)
            }
            if (!completed.compareAndSet(false, true)) {
                return
            }
            when {
                error != null -> cont.resumeWithException(IllegalStateException("Spotify authorization failed: $error"))
                state != expectedState -> cont.resumeWithException(IllegalStateException("Spotify authorization state mismatch"))
                code.isNullOrBlank() -> cont.resumeWithException(IllegalStateException("Spotify authorization did not include a code"))
                else -> cont.resume(code)
            }
        } catch (ex: CancellationException) {
            cont.resumeWithException(ex)
        } catch (ex: Throwable) {
            if (completed.compareAndSet(false, true)) {
                cont.resumeWithException(ex)
            }
        } finally {
            runCatching { exchange.httpContext.server.stop(0) }
        }
    }

    private fun parseQuery(query: String): Map<String, String> {
        if (query.isBlank()) return emptyMap()
        return query.split('&').mapNotNull { segment ->
            if (segment.isBlank()) return@mapNotNull null
            val parts = segment.split('=', limit = 2)
            val key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8.name())
            val value = if (parts.size > 1) URLDecoder.decode(parts[1], StandardCharsets.UTF_8.name()) else ""
            key to value
        }.toMap()
    }

    private fun buildBrowserResponse(success: Boolean): ByteArray {
        val title = if (success) "Authorization complete" else "Authorization failed"
        val body = if (success) {
            "<p>You can return to Minecraft. The Spotify authorization was successful.</p>"
        } else {
            "<p>The Spotify authorization token could not be captured. Please try again.</p>"
        }
        val response = """
            <html>
              <head><title>$title</title></head>
              <body style=\"font-family:sans-serif;background:#0d1117;color:#f0f6fc;text-align:center;\">
                <h2>$title</h2>
                $body
              </body>
            </html>
        """.trimIndent()
        return response.toByteArray(StandardCharsets.UTF_8)
    }

    private fun ensureLeadingSlash(path: String): String = if (path.startsWith("/")) path else "/$path"

    private fun mask(value: String): String = when {
        value.isEmpty() -> "<empty>"
        value.length <= 4 -> "***"
        value.length <= 8 -> value.take(2) + "***"
        else -> value.take(4) + "***" + value.takeLast(2)
    }

    private fun generatePkceChallenge(): PkceChallenge {
        val verifier = buildString(PKCE_VERIFIER_LENGTH) {
            repeat(PKCE_VERIFIER_LENGTH) {
                append(PKCE_CHARSET[secureRandom.nextInt(PKCE_CHARSET.size)])
            }
        }
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(verifier.toByteArray(StandardCharsets.US_ASCII))
        val challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
        return PkceChallenge(verifier, challenge)
    }

    private data class PkceChallenge(val verifier: String, val challenge: String)

    private val ONE_OR_MORE_SPACES = Regex("\\s+")
    private val secureRandom = SecureRandom()
    private const val PKCE_VERIFIER_LENGTH = 64
    private val PKCE_CHARSET = (('a'..'z') + ('A'..'Z') + ('0'..'9') + listOf('-', '.', '_', '~')).toCharArray()
}