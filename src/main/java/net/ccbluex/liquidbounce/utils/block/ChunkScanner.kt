/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.block

import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.minecraft.block.state.IBlockState
import net.minecraft.util.BlockPos
import net.minecraft.world.chunk.Chunk
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList

interface BlockScanSubscriber {
    fun recordBlock(position: BlockPos, state: IBlockState)
    fun clearChunk(chunkX: Int, chunkZ: Int)
    fun clearAll()
}

/** Incrementally distributes chunk contents and block changes without a full scan in one frame. */
object ChunkScanner : MinecraftInstance, Listenable {
    private data class BlockChange(val position: BlockPos, val state: IBlockState)

    private class ChunkWork(val chunk: Chunk) {
        var cursor = 0
    }

    private val subscribers = CopyOnWriteArrayList<BlockScanSubscriber>()
    private val blockChanges = ConcurrentLinkedQueue<BlockChange>()
    private val chunkWork = ConcurrentLinkedQueue<ChunkWork>()
    private val queuedChunks = ConcurrentHashMap.newKeySet<Long>()

    @Volatile
    var blockBudgetPerTick: Int = 4_096
        set(value) {
            require(value in 256..65_536) { "Chunk scan budget must be between 256 and 65536" }
            field = value
        }

    init {
        handler<GameTickEvent>(always = true, priority = Byte.MIN_VALUE) { processTick() }
        handler<WorldEvent>(always = true) { clear() }
    }

    fun subscribe(subscriber: BlockScanSubscriber) {
        if (!subscribers.addIfAbsent(subscriber)) return
        enqueueLoadedChunks()
    }

    fun unsubscribe(subscriber: BlockScanSubscriber) {
        subscribers.remove(subscriber)
        subscriber.clearAll()
        if (subscribers.isEmpty()) clearQueues()
    }

    @JvmStatic
    fun chunkLoaded(chunk: Chunk) {
        if (subscribers.isEmpty()) return
        val key = chunkKey(chunk.xPosition, chunk.zPosition)
        if (queuedChunks.add(key)) chunkWork.add(ChunkWork(chunk))
    }

    @JvmStatic
    fun blockChanged(position: BlockPos, state: IBlockState) {
        if (subscribers.isEmpty()) return
        blockChanges.add(BlockChange(BlockPos(position.x, position.y, position.z), state))
    }

    @JvmStatic
    fun chunkUnloaded(chunkX: Int, chunkZ: Int) {
        queuedChunks.remove(chunkKey(chunkX, chunkZ))
        chunkWork.removeIf { it.chunk.xPosition == chunkX && it.chunk.zPosition == chunkZ }
        subscribers.forEach { it.clearChunk(chunkX, chunkZ) }
    }

    fun clear() {
        clearQueues()
        subscribers.forEach(BlockScanSubscriber::clearAll)
    }

    private fun processTick() {
        if (subscribers.isEmpty()) {
            clearQueues()
            return
        }

        var remaining = blockBudgetPerTick
        while (remaining > 0) {
            val change = blockChanges.poll() ?: break
            subscribers.forEach { it.recordBlock(change.position, change.state) }
            remaining--
        }

        while (remaining > 0) {
            val work = chunkWork.peek() ?: break
            val consumed = scan(work, remaining)
            remaining -= consumed
            if (work.cursor >= BLOCKS_PER_CHUNK) {
                chunkWork.poll()
                queuedChunks.remove(chunkKey(work.chunk.xPosition, work.chunk.zPosition))
            }
            if (consumed == 0 && work.cursor < BLOCKS_PER_CHUNK) break
        }
    }

    private fun scan(work: ChunkWork, budget: Int): Int {
        var consumed = 0
        val chunk = work.chunk
        val sections = chunk.blockStorageArray

        while (work.cursor < BLOCKS_PER_CHUNK && consumed < budget) {
            val y = work.cursor ushr 8
            val section = y ushr 4
            if (sections[section] == null) {
                work.cursor = (section + 1) * BLOCKS_PER_SECTION
                continue
            }

            val localX = work.cursor and 15
            val localZ = work.cursor ushr 4 and 15
            val position = BlockPos((chunk.xPosition shl 4) + localX, y, (chunk.zPosition shl 4) + localZ)
            val state = chunk.getBlockState(position)
            subscribers.forEach { it.recordBlock(position, state) }
            work.cursor++
            consumed++
        }
        return consumed
    }

    private fun enqueueLoadedChunks() {
        val world = mc.theWorld ?: return
        val player = mc.thePlayer ?: return
        val provider = world.chunkProvider
        val radius = mc.gameSettings.renderDistanceChunks.coerceIn(2, 16)
        val centerX = player.chunkCoordX
        val centerZ = player.chunkCoordZ

        for (x in centerX - radius..centerX + radius) {
            for (z in centerZ - radius..centerZ + radius) {
                if (provider.chunkExists(x, z)) chunkLoaded(provider.provideChunk(x, z))
            }
        }
    }

    private fun clearQueues() {
        blockChanges.clear()
        chunkWork.clear()
        queuedChunks.clear()
    }

    private fun chunkKey(x: Int, z: Int): Long = x.toLong() and 0xFFFFFFFFL or (z.toLong() shl 32)

    private const val BLOCKS_PER_SECTION = 16 * 16 * 16
    private const val BLOCKS_PER_CHUNK = 16 * 256 * 16
}
