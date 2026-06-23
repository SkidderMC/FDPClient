/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nextgen

import com.google.gson.JsonObject
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import java.io.File
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.util.concurrent.Executors

object NextGenClickGuiServer {

    private const val RESOURCE_ROOT = "/assets/minecraft/fdpclient/nextgen-clickgui"

    private var server: HttpServer? = null
    private var serverPort = -1

    val port: Int
        get() = serverPort

    val url: String
        get() = "http://localhost:$serverPort/?port=$serverPort&static#/clickgui"

    /** URL of the standalone Spotify player page (served from a sibling resource folder). */
    val spotifyUrl: String
        get() = "http://localhost:$serverPort/spotify-gui/index.html?port=$serverPort"

    @Synchronized
    fun start(): String {
        if (server != null) {
            return url
        }

        val httpServer = HttpServer.create(InetSocketAddress("localhost", 0), 0)
        httpServer.executor = Executors.newCachedThreadPool { runnable ->
            Thread(runnable, "FDP-NextGenClickGUI").apply { isDaemon = true }
        }
        httpServer.createContext("/") { exchange ->
            try {
                addCors(exchange)
                if (exchange.requestMethod.equals("OPTIONS", ignoreCase = true)) {
                    exchange.sendResponseHeaders(204, -1)
                    return@createContext
                }

                if (exchange.requestURI.path.startsWith("/api/v1/")) {
                    handleApi(exchange)
                } else {
                    handleStatic(exchange)
                }
            } catch (throwable: Throwable) {
                LOGGER.error("[NextGenClickGUI] Request failed: ${exchange.requestURI}", throwable)
                sendJson(exchange, JsonObject().apply { addProperty("error", throwable.message ?: "unknown") }, 500)
            } finally {
                exchange.close()
            }
        }
        httpServer.start()

        server = httpServer
        serverPort = httpServer.address.port

        LOGGER.info("[NextGenClickGUI] Server started at $url")
        return url
    }

    private fun handleApi(exchange: HttpExchange) {
        val method = exchange.requestMethod.uppercase()
        val path = exchange.requestURI.path.removePrefix("/api/v1")

        when {
            method == "GET" && path == "/client/modules" ->
                sendJson(exchange, NextGenClickGuiBridge.modules())

            method == "GET" && path.startsWith("/client/module/") ->
                sendJson(exchange, NextGenClickGuiBridge.module(decode(path.removePrefix("/client/module/"))))

            method == "GET" && path == "/client/modules/settings" ->
                sendJson(exchange, NextGenClickGuiBridge.moduleSettings(query(exchange, "name") ?: ""))

            method == "PUT" && path == "/client/modules/settings" -> {
                NextGenClickGuiBridge.applyModuleSettings(query(exchange, "name") ?: "", exchange.bodyText())
                sendNoContent(exchange)
            }

            method == "POST" && path == "/client/modules/toggle" -> {
                NextGenClickGuiBridge.setModuleEnabled(exchange.bodyText())
                sendNoContent(exchange)
            }

            method == "GET" && path == "/client/localStorage/all" ->
                sendJson(exchange, NextGenClickGuiBridge.localStorage())

            method == "PUT" && path == "/client/localStorage/all" -> {
                NextGenClickGuiBridge.saveLocalStorage(exchange.bodyText())
                sendNoContent(exchange)
            }

            method == "GET" && path == "/client/window" ->
                sendJson(exchange, NextGenClickGuiBridge.gameWindow())

            method == "GET" && path.startsWith("/client/theme/") ->
                sendJson(exchange, NextGenClickGuiBridge.theme(decode(path.removePrefix("/client/theme/"))))

            method == "GET" && path == "/client/info" ->
                sendJson(exchange, NextGenClickGuiBridge.clientInfo())

            method == "GET" && path == "/client/global" ->
                sendJson(exchange, NextGenClickGuiBridge.globalSettings())

            method == "PUT" && path == "/client/global" -> {
                NextGenClickGuiBridge.applyGlobalSettings(exchange.bodyText())
                sendNoContent(exchange)
            }

            method == "POST" && path == "/client/action" -> {
                NextGenClickGuiBridge.runAction(exchange.bodyText())
                sendNoContent(exchange)
            }

            method == "GET" && path == "/client/virtualScreen" ->
                sendJson(exchange, NextGenClickGuiBridge.virtualScreen())

            method == "GET" && path == "/client/spotify" ->
                sendJson(exchange, NextGenClickGuiBridge.spotifyNowPlaying())

            method == "POST" && path == "/client/spotify/control" -> {
                NextGenClickGuiBridge.spotifyControl(exchange.bodyText())
                sendNoContent(exchange)
            }

            method == "GET" && path == "/client/spotify/playlists" ->
                sendJson(exchange, NextGenClickGuiBridge.spotifyPlaylists())

            method == "GET" && path == "/client/spotify/playlist" ->
                sendJson(exchange, NextGenClickGuiBridge.spotifyPlaylistTracks(query(exchange, "id") ?: ""))

            method == "POST" && path == "/client/spotify/play" -> {
                NextGenClickGuiBridge.spotifyPlay(exchange.bodyText())
                sendNoContent(exchange)
            }

            method == "POST" && path == "/client/spotify/like" -> {
                NextGenClickGuiBridge.spotifyLike(exchange.bodyText())
                sendNoContent(exchange)
            }

            method == "POST" && path == "/client/typing" ->
                sendNoContent(exchange)

            method == "GET" && path == "/client/input" ->
                sendJson(exchange, NextGenClickGuiBridge.printableKey(query(exchange, "key") ?: "key.keyboard.unknown"))

            else -> sendJson(exchange, JsonObject().apply {
                addProperty("error", "No NextGen ClickGUI endpoint for $method $path")
            }, 404)
        }
    }

    private fun handleStatic(exchange: HttpExchange) {
        val rawPath = decode(exchange.requestURI.path).trimStart('/')
        val requested = sanitize(if (rawPath.isBlank()) "index.html" else rawPath)
        val bytes = readStatic(requested) ?: if (!requested.contains('.')) readStatic("index.html") else null

        if (bytes == null) {
            exchange.sendResponseHeaders(404, -1)
            return
        }

        exchange.responseHeaders.set("Content-Type", mimeType(requested))
        exchange.sendResponseHeaders(200, bytes.size.toLong())
        exchange.responseBody.use { it.write(bytes) }
    }

    private fun readStatic(path: String): ByteArray? {
        devThemeFile(path)?.takeIf(File::isFile)?.let { return it.readBytes() }

        // The standalone Spotify player lives in a sibling resource folder, not under the clickgui app.
        val resourcePath = if (path.startsWith("spotify-gui/")) {
            "/assets/minecraft/fdpclient/$path"
        } else {
            "$RESOURCE_ROOT/$path"
        }
        return NextGenClickGuiServer::class.java
            .getResourceAsStream(resourcePath)
            ?.use { it.readBytes() }
    }

    private fun devThemeFile(path: String): File? {
        val candidates = arrayOf(
            File("nextgen-theme/dist", path),
            File(File(System.getProperty("user.dir")).parentFile ?: File("."), "nextgen-theme/dist/$path")
        )

        return candidates.firstOrNull { it.isFile }
    }

    private fun sanitize(path: String): String =
        path.replace('\\', '/').split('/').filter { it.isNotBlank() && it != ".." }.joinToString("/")

    private fun query(exchange: HttpExchange, key: String): String? {
        return exchange.requestURI.rawQuery
            ?.split('&')
            ?.mapNotNull {
                val parts = it.split('=', limit = 2)
                if (parts.size == 2) decode(parts[0]) to decode(parts[1]) else null
            }
            ?.firstOrNull { it.first == key }
            ?.second
    }

    private fun decode(value: String): String = URLDecoder.decode(value, Charsets.UTF_8.name())

    private fun HttpExchange.bodyText(): String = requestBody.bufferedReader(Charsets.UTF_8).use { it.readText() }

    private fun sendJson(exchange: HttpExchange, body: Any, status: Int = 200) {
        val bytes = FileManager.PRETTY_GSON.toJson(body).toByteArray(Charsets.UTF_8)
        exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
        exchange.sendResponseHeaders(status, bytes.size.toLong())
        exchange.responseBody.use { it.write(bytes) }
    }

    private fun sendNoContent(exchange: HttpExchange) {
        exchange.sendResponseHeaders(204, -1)
    }

    private fun addCors(exchange: HttpExchange) {
        exchange.responseHeaders.add("Access-Control-Allow-Origin", "*")
        exchange.responseHeaders.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        exchange.responseHeaders.add("Access-Control-Allow-Headers", "Content-Type")
    }

    private fun mimeType(path: String): String = when (path.substringAfterLast('.', "").lowercase()) {
        "html" -> "text/html; charset=utf-8"
        "js" -> "application/javascript; charset=utf-8"
        "css" -> "text/css; charset=utf-8"
        "json" -> "application/json; charset=utf-8"
        "svg" -> "image/svg+xml"
        "png" -> "image/png"
        "jpg", "jpeg" -> "image/jpeg"
        "ttf" -> "font/ttf"
        "woff" -> "font/woff"
        "woff2" -> "font/woff2"
        else -> "application/octet-stream"
    }
}
