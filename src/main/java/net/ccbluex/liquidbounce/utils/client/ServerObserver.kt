/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.client

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.C14PacketTabComplete
import net.minecraft.network.play.server.S01PacketJoinGame
import net.minecraft.network.play.server.S03PacketTimeUpdate
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S32PacketConfirmTransaction
import net.minecraft.network.play.server.S3APacketTabComplete
import net.minecraft.network.play.server.S3FPacketCustomPayload
import java.util.ArrayDeque
import java.util.Collections
import java.util.Locale
import java.util.TreeSet

/**
 * Session-scoped source of truth for server telemetry and fingerprinting.
 *
 * Packet consumers should use this object instead of maintaining their own TPS, transaction,
 * payload-channel or setback histories. All state is reset when the world changes.
 */
object ServerObserver : MinecraftInstance, Listenable {

    private const val TPS_SAMPLE_COUNT = 15
    private const val TRANSACTION_SAMPLE_COUNT = 32
    private const val PLUGIN_SCAN_DELAY_TICKS = 40

    private val tpsIntervals = ArrayDeque<Long>(TPS_SAMPLE_COUNT + 1)
    private val transactionSamples = ArrayDeque<Int>(TRANSACTION_SAMPLE_COUNT + 1)
    private val mutablePlugins = TreeSet(String.CASE_INSENSITIVE_ORDER)
    private val mutablePayloadChannels = TreeSet(String.CASE_INSENSITIVE_ORDER)

    private var lastTimeUpdateAt = -1L
    private var awaitingCommandSuggestions = false
    private var pluginScanRequested = false
    private var ticksSinceReset = 0

    @Volatile
    var tps: Double = Double.NaN
        private set

    @Volatile
    var serverBrand: String? = null
        private set

    @Volatile
    var lastLagBackAt: Long = -1L
        private set

    @Volatile
    var lagBackCount: Int = 0
        private set

    val plugins: Set<String>
        get() = synchronized(mutablePlugins) {
            Collections.unmodifiableSet(TreeSet(mutablePlugins))
        }

    val payloadChannels: Set<String>
        get() = synchronized(mutablePayloadChannels) {
            Collections.unmodifiableSet(TreeSet(mutablePayloadChannels))
        }

    val transactions: List<Int>
        get() = synchronized(transactionSamples) { transactionSamples.toList() }

    val ping: Int
        get() {
            val player = mc.thePlayer ?: return -1
            return mc.netHandler?.getPlayerInfo(player.uniqueID)?.responseTime ?: -1
        }

    val hasRecentLagBack: Boolean
        get() = lastLagBackAt > 0L && System.currentTimeMillis() - lastLagBackAt <= 5_000L

    val onPacket = handler<PacketEvent>(always = true) { event ->
        if (event.eventType != EventState.RECEIVE) return@handler

        when (val packet = event.packet) {
            is S01PacketJoinGame -> resetSession()
            is S03PacketTimeUpdate -> captureTimeUpdate()
            is S08PacketPlayerPosLook -> {
                lastLagBackAt = System.currentTimeMillis()
                lagBackCount++
            }
            is S32PacketConfirmTransaction -> captureTransaction(packet.actionNumber.toInt())
            is S3APacketTabComplete -> captureCommandSuggestions(packet)
            is S3FPacketCustomPayload -> capturePayload(packet)
        }
    }

    val onWorld = handler<WorldEvent>(always = true) {
        resetSession()
    }

    val onUpdate = handler<UpdateEvent>(always = true) {
        if (pluginScanRequested || mc.thePlayer == null) return@handler
        if (++ticksSinceReset >= PLUGIN_SCAN_DELAY_TICKS) requestPluginScan()
    }

    /** Requests the protocol-47 command suggestions used to discover namespaced commands. */
    fun requestPluginScan(): Boolean {
        if (mc.thePlayer == null || pluginScanRequested || awaitingCommandSuggestions) return false
        pluginScanRequested = true
        awaitingCommandSuggestions = true
        sendPacket(C14PacketTabComplete("/"))
        return true
    }

    /**
     * Combines address, discovered plugin names, payload channels, brand and transaction cadence.
     * A plugin-derived result wins because it is more explicit than packet timing.
     */
    fun guessAnticheat(address: String? = mc.currentServerData?.serverIP): String? {
        if (address?.substringBefore(':')?.endsWith("hypixel.net", true) == true) return "Watchdog"

        val tokens = buildList {
            addAll(plugins)
            addAll(payloadChannels)
            serverBrand?.let(::add)
        }.map { normalize(it) }

        KNOWN_ANTICHEATS.firstOrNull { known ->
            tokens.any { token -> token.contains(known.token) }
        }?.let { return it.displayName }

        return guessFromTransactions(transactions)
    }

    fun resetSession() {
        synchronized(tpsIntervals) { tpsIntervals.clear() }
        synchronized(transactionSamples) { transactionSamples.clear() }
        synchronized(mutablePlugins) { mutablePlugins.clear() }
        synchronized(mutablePayloadChannels) { mutablePayloadChannels.clear() }
        lastTimeUpdateAt = -1L
        awaitingCommandSuggestions = false
        pluginScanRequested = false
        ticksSinceReset = 0
        tps = Double.NaN
        serverBrand = null
        lastLagBackAt = -1L
        lagBackCount = 0
    }

    private fun captureTimeUpdate() {
        val now = System.currentTimeMillis()
        if (lastTimeUpdateAt > 0L) {
            synchronized(tpsIntervals) {
                tpsIntervals.addLast(now - lastTimeUpdateAt)
                while (tpsIntervals.size > TPS_SAMPLE_COUNT) tpsIntervals.removeFirst()
                val average = tpsIntervals.map(Long::toDouble).average()
                tps = if (average.isFinite() && average > 0.0) {
                    (20_000.0 / average).coerceIn(0.0, 20.0)
                } else Double.NaN
            }
        }
        lastTimeUpdateAt = now
    }

    private fun captureTransaction(actionNumber: Int) {
        synchronized(transactionSamples) {
            transactionSamples.addLast(actionNumber)
            while (transactionSamples.size > TRANSACTION_SAMPLE_COUNT) transactionSamples.removeFirst()
        }
    }

    private fun captureCommandSuggestions(packet: S3APacketTabComplete) {
        if (!awaitingCommandSuggestions) return
        awaitingCommandSuggestions = false
        val matches = runCatching { packet.func_149630_c() }.getOrElse { emptyArray() }
        synchronized(mutablePlugins) {
            matches.forEach { raw ->
                val command = raw.removePrefix("/")
                val separator = command.indexOf(':')
                if (separator > 0) mutablePlugins += command.substring(0, separator)
            }
        }
    }

    private fun capturePayload(packet: S3FPacketCustomPayload) {
        val channel = runCatching { packet.channelName }.getOrNull()?.trim().orEmpty()
        if (channel.isEmpty()) return

        synchronized(mutablePayloadChannels) { mutablePayloadChannels += channel }

        when {
            channel.equals("MC|Brand", true) -> readPayloadString(packet)?.let { serverBrand = it }
            channel.equals("REGISTER", true) -> readPayloadBytes(packet)
                ?.toString(Charsets.UTF_8)
                ?.split('\u0000')
                ?.filter(String::isNotBlank)
                ?.let { channels -> synchronized(mutablePayloadChannels) { mutablePayloadChannels.addAll(channels) } }
        }
    }

    private fun readPayloadString(packet: S3FPacketCustomPayload): String? = runCatching {
        PacketBuffer(packet.bufferData.duplicate()).readStringFromBuffer(32767).takeIf(String::isNotBlank)
    }.getOrNull()

    private fun readPayloadBytes(packet: S3FPacketCustomPayload): ByteArray? = runCatching {
        val duplicate = packet.bufferData.duplicate()
        ByteArray(duplicate.readableBytes()).also(duplicate::readBytes)
    }.getOrNull()

    private fun guessFromTransactions(samples: List<Int>): String? {
        if (samples.size < 5) return null
        val firstFive = samples.take(5)
        val differences = firstFive.zipWithNext { left, right -> right - left }
        val first = firstFive.first()

        return when {
            differences.all { it == differences.first() } -> when (differences.first()) {
                1 -> when (first) {
                    in -23772..-23762 -> "Vulcan"
                    in 95..105, in -20005..-19995 -> "Matrix"
                    in -32773..-32762 -> "Grizzly"
                    else -> "Verus"
                }
                -1 -> when {
                    first in -8287..-8280 -> "Errata"
                    first < -3000 -> "Intave"
                    first in -5..0 -> "Grim"
                    first in -3000..-2995 -> "Karhu"
                    else -> "Polar"
                }
                else -> null
            }
            firstFive[0] == firstFive[1] && firstFive.drop(2).zipWithNext().all { (a, b) -> b - a == 1 } -> "Verus"
            differences[0] >= 100 && differences[1] == -1 && differences.drop(2).all { it == -1 } -> "Polar"
            first < -3000 && firstFive.any { it == 0 } -> "Intave"
            firstFive.take(3) == listOf(-30767, -30766, -25767) &&
                firstFive.drop(3).zipWithNext().all { (a, b) -> b - a == 1 } -> "Old Vulcan"
            else -> "Unknown"
        }
    }

    private fun normalize(value: String) = value.lowercase(Locale.ROOT).filter(Char::isLetterOrDigit)

    private data class KnownAnticheat(val token: String, val displayName: String)

    private val KNOWN_ANTICHEATS = listOf(
        KnownAnticheat("nocheatplus", "NoCheatPlus"),
        KnownAnticheat("grimac", "Grim"),
        KnownAnticheat("vulcan", "Vulcan"),
        KnownAnticheat("matrix", "Matrix"),
        KnownAnticheat("spartan", "Spartan"),
        KnownAnticheat("intave", "Intave"),
        KnownAnticheat("verus", "Verus"),
        KnownAnticheat("polar", "Polar"),
        KnownAnticheat("karhu", "Karhu"),
        KnownAnticheat("themis", "Themis"),
        KnownAnticheat("negativity", "Negativity"),
        KnownAnticheat("anticheatreloaded", "AntiCheatReloaded"),
        KnownAnticheat("aac", "AAC")
    )
}
