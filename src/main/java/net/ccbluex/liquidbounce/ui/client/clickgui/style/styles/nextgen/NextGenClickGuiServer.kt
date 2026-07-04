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
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object NextGenClickGuiServer {

    private const val RESOURCE_ROOT = "/assets/minecraft/fdpclient/nextgen-clickgui"

    private var server: HttpServer? = null
    private var executor: ExecutorService? = null
    private var serverPort = -1

    val port: Int
        get() = serverPort

    val url: String
        get() = screenUrl("clickgui")

    fun screenUrl(screen: String): String =
        "http://localhost:$serverPort/?port=$serverPort&wsPort=${UiEventSocket.port}&static#/${screen.trimStart('/')}"

    /** URL of the standalone Spotify player page (served from a sibling resource folder). */
    val spotifyUrl: String
        get() = "http://localhost:$serverPort/spotify-gui/index.html?port=$serverPort&wsPort=${UiEventSocket.port}"

    @Synchronized
    fun start(): String {
        if (server != null) {
            return url
        }

        val httpServer = try {
            UiEventSocket.start()
            HttpServer.create(InetSocketAddress("localhost", 0), 0)
        } catch (throwable: Throwable) {
            UiEventSocket.stop()
            throw throwable
        }
        val requestExecutor = Executors.newCachedThreadPool { runnable ->
            Thread(runnable, "FDP-NextGenClickGUI").apply { isDaemon = true }
        }
        executor = requestExecutor
        httpServer.executor = requestExecutor
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

    @Synchronized
    fun stop() {
        server?.stop(0)
        server = null
        executor?.shutdownNow()
        executor = null
        serverPort = -1
        UiEventSocket.stop()
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

            method == "GET" && path == "/client/spoofer" ->
                sendJson(exchange, NextGenClickGuiBridge.moduleSettings("BrandSpoofer"))

            method == "PUT" && path == "/client/spoofer" -> {
                NextGenClickGuiBridge.applyModuleSettings("BrandSpoofer", exchange.bodyText())
                sendNoContent(exchange)
            }

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

            method == "POST" && path == "/client/virtualScreen" ->
                sendNoContent(exchange)

            method == "GET" && path == "/client/player" ->
                sendJson(exchange, NextGenHudBridge.playerData())

            method == "GET" && path == "/client/player/inventory" ->
                sendJson(exchange, NextGenHudBridge.playerInventory())

            method == "GET" && path == "/client/crosshair" ->
                sendJson(exchange, NextGenHudBridge.crosshair())

            method == "GET" && path == "/client/keybinds" ->
                sendJson(exchange, NextGenHudBridge.keybinds())

            method == "GET" && path == "/client/session" ->
                sendJson(exchange, NextGenHudBridge.session())

            method == "GET" && path == "/client/components" ->
                sendJson(exchange, NextGenHudBridge.components(null))

            method == "GET" && path.startsWith("/client/components/") ->
                sendJson(exchange, NextGenHudBridge.components(decode(path.removePrefix("/client/components/"))))

            method == "GET" && path == "/client/resource/itemTexture" ->
                sendPng(exchange, NextGenHudBridge.itemTexture(query(exchange, "id") ?: "minecraft:air"))

            method == "GET" && path == "/client/resource/effectTexture" ->
                sendPng(exchange, NextGenHudBridge.effectTexture(query(exchange, "id") ?: ""))

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

            method == "POST" && path == "/client/fileDialog" ->
                sendJson(exchange, NextGenClickGuiBridge.openFileDialog(exchange.bodyText()))

            method == "POST" && path == "/client/browsePath" -> {
                NextGenClickGuiBridge.browsePath(exchange.bodyText())
                sendNoContent(exchange)
            }

            method == "POST" && path == "/client/browse" -> {
                NextGenMenuBridge.browse(exchange.bodyText())
                sendNoContent(exchange)
            }

            method == "POST" && path == "/client/exit" -> {
                NextGenMenuBridge.exit()
                sendNoContent(exchange)
            }

            method == "PUT" && path == "/client/screen" -> {
                NextGenMenuBridge.openScreen(exchange.bodyText())
                sendNoContent(exchange)
            }

            method == "DELETE" && path == "/client/screen" -> {
                NextGenMenuBridge.closeScreen()
                sendNoContent(exchange)
            }

            method == "GET" && path == "/client/servers" ->
                sendJson(exchange, NextGenMenuBridge.servers())

            method == "POST" && path == "/client/servers/connect" -> {
                NextGenMenuBridge.connectServer(exchange.bodyText())
                sendNoContent(exchange)
            }

            method == "PUT" && path == "/client/servers/add" -> {
                NextGenMenuBridge.addServer(exchange.bodyText())
                sendNoContent(exchange)
            }

            method == "PUT" && path == "/client/servers/edit" -> {
                NextGenMenuBridge.editServer(exchange.bodyText())
                sendNoContent(exchange)
            }

            method == "DELETE" && path == "/client/servers/remove" -> {
                NextGenMenuBridge.removeServer(exchange.bodyText())
                sendNoContent(exchange)
            }

            method == "POST" && path == "/client/servers/order" -> {
                NextGenMenuBridge.orderServers(exchange.bodyText())
                sendNoContent(exchange)
            }

            method == "GET" && path == "/client/protocols" ->
                sendJson(exchange, NextGenMenuBridge.protocols())

            method == "GET" && path == "/client/protocols/protocol" ->
                sendJson(exchange, NextGenMenuBridge.protocol())

            method == "PUT" && path == "/client/protocols/protocol" -> sendNoContent(exchange)

            method == "GET" && path == "/client/accounts" ->
                sendJson(exchange, NextGenMenuBridge.accounts())

            method == "POST" && path == "/client/accounts/new/cracked" -> {
                NextGenMenuBridge.addCrackedAccount(exchange.bodyText())
                sendNoContent(exchange)
            }

            method == "POST" && (path == "/client/accounts/new/session" ||
                path == "/client/accounts/new/altening" || path.startsWith("/client/accounts/new/microsoft")) -> {
                NextGenMenuBridge.openNativeAltManager()
                sendNoContent(exchange)
            }

            method == "POST" && path == "/client/accounts/order" -> {
                NextGenMenuBridge.orderAccounts(exchange.bodyText())
                sendNoContent(exchange)
            }

            method == "DELETE" && path == "/client/account" -> {
                NextGenMenuBridge.removeAccount(exchange.bodyText())
                sendNoContent(exchange)
            }

            method == "POST" && path == "/client/account/login" -> {
                NextGenMenuBridge.loginAccount(exchange.bodyText())
                sendNoContent(exchange)
            }

            method == "POST" && path == "/client/account/login/cracked" -> {
                NextGenMenuBridge.directCrackedLogin(exchange.bodyText())
                sendNoContent(exchange)
            }

            method == "POST" && path == "/client/account/login/session" -> {
                NextGenMenuBridge.openNativeAltManager()
                sendNoContent(exchange)
            }

            method == "POST" && path == "/client/account/restore" -> {
                NextGenMenuBridge.restoreSession()
                sendNoContent(exchange)
            }

            (method == "PUT" || method == "DELETE") && path == "/client/account/favorite" -> sendNoContent(exchange)

            method == "POST" && path == "/client/account/random-name" ->
                sendJson(exchange, NextGenMenuBridge.randomName())

            method == "GET" && path == "/client/worlds" ->
                sendJson(exchange, NextGenMenuBridge.worlds())

            method == "POST" && path == "/client/worlds/join" -> {
                NextGenMenuBridge.openWorld(exchange.bodyText())
                sendNoContent(exchange)
            }

            method == "POST" && path == "/client/worlds/edit" -> {
                NextGenMenuBridge.editWorld(exchange.bodyText())
                sendNoContent(exchange)
            }

            method == "POST" && path == "/client/worlds/delete" -> {
                NextGenMenuBridge.removeWorld(exchange.bodyText())
                sendNoContent(exchange)
            }

            method == "GET" && path == "/client/proxies" ->
                sendJson(exchange, NextGenMenuBridge.emptyProxies())

            method == "GET" && path == "/client/proxy" ->
                sendJson(exchange, JsonObject().apply { addProperty("error", "No proxy transport is configured") }, 404)

            path.startsWith("/client/prox") -> {
                NextGenMenuBridge.rejectProxyOperation()
                sendNoContent(exchange)
            }

            method == "GET" && path == "/client/update" ->
                sendJson(exchange, NextGenMenuBridge.update())

            method == "POST" && path == "/client/reconnect" -> {
                NextGenMenuBridge.reconnect()
                sendNoContent(exchange)
            }

            method == "POST" && path == "/client/shader" -> sendNoContent(exchange)

            method == "GET" && path == "/client/resource" ->
                sendPng(exchange, NextGenHudBridge.resource(query(exchange, "id") ?: "minecraft:textures/misc/unknown_server.png"))

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
        // Never let the embedded browser cache the UI shell, otherwise a rebuilt theme keeps
        // showing the old markup; hashed asset names already handle their own busting.
        exchange.responseHeaders.set("Cache-Control", "no-cache, no-store, must-revalidate")
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

    private fun sendPng(exchange: HttpExchange, bytes: ByteArray) {
        exchange.responseHeaders.set("Content-Type", "image/png")
        exchange.responseHeaders.set("Cache-Control", "public, max-age=300")
        exchange.sendResponseHeaders(200, bytes.size.toLong())
        exchange.responseBody.use { it.write(bytes) }
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
