/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nextgen

import com.google.gson.JsonObject
import com.google.gson.JsonNull
import net.ccbluex.liquidbounce.event.ClientChange
import net.ccbluex.liquidbounce.event.ClientChangeBus
import net.ccbluex.liquidbounce.event.ClientShutdownEvent
import net.ccbluex.liquidbounce.event.KeyStateEvent
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.file.gson.GsonProfiles
import net.ccbluex.liquidbounce.handler.combat.CombatManager
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.minecraft.client.Minecraft
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.EOFException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/** Local-only, bounded WebSocket publisher for the browser UI. */
object UiEventSocket : Listenable {
    private const val MAX_HEADER_BYTES = 8192
    private const val MAX_FRAME_BYTES = 1 shl 20
    private const val MAX_PENDING_MESSAGES = 256
    private const val SOCKET_TIMEOUT_MS = 15_000
    private const val WEB_SOCKET_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"

    private val clients = ConcurrentHashMap.newKeySet<Client>()
    private var serverSocket: ServerSocket? = null
    private var acceptThread: Thread? = null
    private var updateCounter = 0
    private var lastInventoryHash = 0
    private var lastBlockCounterHash = 0
    private var lastTargetId: Int? = null

    val port: Int
        @Synchronized get() = serverSocket?.localPort ?: -1

    init {
        ClientChangeBus.subscribe(::onChange)
    }

    val onWorld = handler<WorldEvent>(always = true) { event ->
        updateCounter = 0
        lastInventoryHash = 0
        lastBlockCounterHash = 0
        lastTargetId = null
        ClientChangeBus.publish(ClientChange.WorldState(event.worldClient != null))
    }

    val onShutdown = handler<ClientShutdownEvent>(always = true) { stop() }

    val onUpdate = handler<UpdateEvent>(always = true) {
        if (clients.isEmpty()) return@handler

        updateCounter++
        if (updateCounter % 2 == 0 && Minecraft.getMinecraft().thePlayer != null) {
            publish("clientPlayerData", JsonObject().apply {
                add("playerData", NextGenHudBridge.playerData())
            })

            val target = CombatManager.target
            if (target != null) {
                lastTargetId = target.entityId
                publish("targetChange", JsonObject().apply {
                    add("target", NextGenHudBridge.targetData(target))
                })
            } else if (lastTargetId != null) {
                lastTargetId = null
                publish("targetChange", JsonObject().apply { add("target", JsonNull.INSTANCE) })
            }
        }

        if (updateCounter % 5 == 0) {
            val inventory = NextGenHudBridge.playerInventory()
            val inventoryHash = inventory.toString().hashCode()
            if (inventoryHash != lastInventoryHash) {
                lastInventoryHash = inventoryHash
                publish("clientPlayerInventory", JsonObject().apply { add("inventory", inventory) })
            }

            val blockCounter = NextGenHudBridge.blockCounter()
            val blockCounterHash = blockCounter.toString().hashCode()
            if (blockCounterHash != lastBlockCounterHash) {
                lastBlockCounterHash = blockCounterHash
                publish("blockCountChange", blockCounter)
            }
        }

        if (updateCounter % 20 == 0) {
            publish("fps", JsonObject().apply { addProperty("fps", Minecraft.getDebugFPS()) })
        }
    }

    val onKey = handler<KeyStateEvent>(always = true) { event ->
        if (clients.isEmpty()) return@handler
        publish("key", JsonObject().apply {
            addProperty("key", NextGenClickGuiBridge.minecraftKey(event.key))
            addProperty("action", if (event.pressed) 1 else 0)
        })
    }

    @Synchronized
    fun start(): Int {
        serverSocket?.let { return it.localPort }

        val socket = ServerSocket(0, 50, InetAddress.getLoopbackAddress()).apply {
            reuseAddress = true
        }
        serverSocket = socket
        acceptThread = Thread({ acceptLoop(socket) }, "FDP-UI-Events-Accept").apply {
            isDaemon = true
            start()
        }
        return socket.localPort
    }

    @Synchronized
    fun stop() {
        runCatching { serverSocket?.close() }
        serverSocket = null
        acceptThread?.interrupt()
        acceptThread = null
        clients.toList().forEach(Client::close)
        clients.clear()
    }

    fun publish(name: String, event: JsonObject = JsonObject()) {
        if (clients.isEmpty()) return
        val message = GsonProfiles.interop.toJson(JsonObject().apply {
            addProperty("name", name)
            add("event", event)
        })
        clients.forEach { it.offer(message) }
    }

    private fun acceptLoop(server: ServerSocket) {
        while (!server.isClosed) {
            try {
                val socket = server.accept()
                Thread({ connect(socket) }, "FDP-UI-Events-Client").apply {
                    isDaemon = true
                    start()
                }
            } catch (_: SocketTimeoutException) {
                continue
            } catch (throwable: Throwable) {
                if (!server.isClosed) LOGGER.warn("Browser event socket accept failed", throwable)
                break
            }
        }
    }

    private fun connect(socket: Socket) {
        var client: Client? = null
        try {
            socket.tcpNoDelay = true
            socket.soTimeout = SOCKET_TIMEOUT_MS
            val input = BufferedInputStream(socket.getInputStream())
            val output = BufferedOutputStream(socket.getOutputStream())
            performHandshake(input, output)

            client = Client(socket, input, output)
            clients += client
            client.startWriter()
            client.readFrames()
        } catch (_: SocketTimeoutException) {
            // The browser reconnects when a stale connection times out.
        } catch (_: EOFException) {
            // Normal browser close.
        } catch (throwable: Throwable) {
            if (!socket.isClosed) LOGGER.debug("Browser event socket client closed: ${throwable.message}")
        } finally {
            client?.let { clients -= it }
            client?.close() ?: runCatching { socket.close() }
        }
    }

    private fun performHandshake(input: BufferedInputStream, output: BufferedOutputStream) {
        val headerBytes = ArrayList<Byte>(512)
        var matched = 0
        val delimiter = byteArrayOf(13, 10, 13, 10)
        while (headerBytes.size < MAX_HEADER_BYTES && matched < delimiter.size) {
            val next = input.read()
            if (next < 0) throw EOFException()
            val byte = next.toByte()
            headerBytes += byte
            matched = if (byte == delimiter[matched]) matched + 1 else if (byte == delimiter[0]) 1 else 0
        }
        require(matched == delimiter.size) { "WebSocket request header is too large" }

        val lines = headerBytes.toByteArray().toString(StandardCharsets.ISO_8859_1).split("\r\n")
        require(lines.firstOrNull()?.startsWith("GET ") == true) { "Expected a WebSocket GET request" }
        val headers = lines.drop(1).mapNotNull { line ->
            val separator = line.indexOf(':')
            if (separator <= 0) null else line.substring(0, separator).trim().lowercase() to line.substring(separator + 1).trim()
        }.toMap()
        require(headers["upgrade"].equals("websocket", true)) { "Missing WebSocket upgrade" }
        require(headers["connection"]?.contains("upgrade", true) == true) { "Missing connection upgrade" }
        val key = headers["sec-websocket-key"]?.takeIf { it.isNotBlank() }
            ?: error("Missing WebSocket key")
        require(headers["sec-websocket-version"] == "13") { "Unsupported WebSocket version" }

        val accept = Base64.getEncoder().encodeToString(
            MessageDigest.getInstance("SHA-1").digest((key + WEB_SOCKET_GUID).toByteArray(StandardCharsets.US_ASCII))
        )
        output.write(
            ("HTTP/1.1 101 Switching Protocols\r\n" +
                "Upgrade: websocket\r\n" +
                "Connection: Upgrade\r\n" +
                "Sec-WebSocket-Accept: $accept\r\n\r\n").toByteArray(StandardCharsets.US_ASCII)
        )
        output.flush()
    }

    private fun onChange(change: ClientChange) {
        // Nothing to push when no web UI is attached; also avoids building module settings JSON
        // for every value change during config loads when the menu is closed.
        if (clients.isEmpty()) {
            return
        }

        when (change) {
            is ClientChange.ModuleState -> publish("moduleToggle", JsonObject().apply {
                addProperty("moduleName", change.moduleName)
                addProperty("enabled", change.enabled)
                addProperty("hidden", change.hidden)
            })

            is ClientChange.ValueState -> {
                publish("clickGuiValueChange", JsonObject().apply {
                    add("configurable", NextGenClickGuiBridge.moduleSettings(change.ownerName))
                })
                // The module-list cards read each module's bind. The SPA refreshes them on a
                // "valueChanged" event whose value name is "Bind", so emit it for bind changes -
                // that is how a bind set via the .bind command shows up without reopening the menu.
                if (change.valueName == "Bind") {
                    publish("valueChanged", JsonObject().apply {
                        add("value", JsonObject().apply { addProperty("name", change.valueName) })
                    })
                }
            }

            is ClientChange.Configuration -> publish("configurationChanged", JsonObject().apply {
                addProperty("name", change.name)
            })

            is ClientChange.Command -> publish("commandExecuted", JsonObject().apply {
                addProperty("name", change.name)
            })

            is ClientChange.WorldState -> publish("clientState", JsonObject().apply {
                addProperty("inGame", change.connected)
            })
        }
    }

    private class Client(
        private val socket: Socket,
        private val input: BufferedInputStream,
        private val output: BufferedOutputStream,
    ) {
        private val closed = AtomicBoolean(false)
        private val pending = LinkedBlockingQueue<String>(MAX_PENDING_MESSAGES)
        private var writerThread: Thread? = null

        fun startWriter() {
            writerThread = Thread({ writeLoop() }, "FDP-UI-Events-Writer").apply {
                isDaemon = true
                start()
            }
        }

        fun offer(message: String) {
            if (closed.get()) return
            if (!pending.offer(message)) {
                pending.poll()
                pending.offer(message)
            }
        }

        fun readFrames() {
            while (!closed.get()) {
                val first = readByte()
                val second = readByte()
                val finalFrame = first and 0x80 != 0
                val opcode = first and 0x0F
                require(finalFrame) { "Fragmented WebSocket frames are not supported" }

                val masked = second and 0x80 != 0
                var length = (second and 0x7F).toLong()
                if (length == 126L) length = readUnsignedShort().toLong()
                if (length == 127L) length = readLong()
                require(length in 0..MAX_FRAME_BYTES.toLong()) { "WebSocket frame exceeds the size limit" }
                require(masked) { "Browser WebSocket frames must be masked" }

                val mask = ByteArray(4).also(::readFully)
                val payload = ByteArray(length.toInt()).also(::readFully)
                payload.indices.forEach { payload[it] = (payload[it].toInt() xor mask[it and 3].toInt()).toByte() }

                when (opcode) {
                    0x1 -> handleText(payload.toString(StandardCharsets.UTF_8))
                    0x8 -> return
                    0x9 -> writeFrame(0xA, payload)
                    0xA -> Unit
                    else -> error("Unsupported WebSocket opcode $opcode")
                }
            }
        }

        private fun handleText(text: String) {
            if (text.contains("\"name\":\"ping\"") || text.contains("\"name\": \"ping\"")) {
                offer("{\"name\":\"pong\",\"event\":{}}")
            }
        }

        private fun writeLoop() {
            try {
                while (!closed.get()) {
                    val message = pending.poll(1, TimeUnit.SECONDS) ?: continue
                    writeFrame(0x1, message.toByteArray(StandardCharsets.UTF_8))
                }
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            } catch (_: Throwable) {
                close()
            }
        }

        @Synchronized
        private fun writeFrame(opcode: Int, payload: ByteArray) {
            if (closed.get()) return
            output.write(0x80 or opcode)
            when {
                payload.size < 126 -> output.write(payload.size)
                payload.size <= 0xFFFF -> {
                    output.write(126)
                    output.write(payload.size ushr 8)
                    output.write(payload.size)
                }
                else -> {
                    output.write(127)
                    for (shift in 56 downTo 0 step 8) output.write(payload.size.toLong().ushr(shift).toInt())
                }
            }
            output.write(payload)
            output.flush()
        }

        fun close() {
            if (!closed.compareAndSet(false, true)) return
            writerThread?.interrupt()
            pending.clear()
            runCatching { socket.close() }
        }

        private fun readByte(): Int = input.read().takeIf { it >= 0 } ?: throw EOFException()

        private fun readUnsignedShort(): Int = (readByte() shl 8) or readByte()

        private fun readLong(): Long {
            var value = 0L
            repeat(8) { value = (value shl 8) or readByte().toLong() }
            return value
        }

        private fun readFully(bytes: ByteArray) {
            var offset = 0
            while (offset < bytes.size) {
                val read = input.read(bytes, offset, bytes.size - offset)
                if (read < 0) throw EOFException()
                offset += read
            }
        }
    }
}
