/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.block

import net.minecraft.block.state.IBlockState
import net.minecraft.util.BlockPos
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/** Thread-safe base for reusable block indexes fed by [ChunkScanner]. */
abstract class AbstractBlockLocationTracker : BlockScanSubscriber {
    private val lock = ReentrantReadWriteLock()
    private val statesByPosition = HashMap<BlockPos, IBlockState>()
    private val positionsByChunk = HashMap<Long, MutableSet<BlockPos>>()

    @Volatile
    var tracking: Boolean = false
        private set

    protected abstract fun accepts(position: BlockPos, state: IBlockState): Boolean

    fun start() {
        if (tracking) return
        tracking = true
        ChunkScanner.subscribe(this)
    }

    fun stop() {
        if (!tracking) return
        tracking = false
        ChunkScanner.unsubscribe(this)
    }

    override fun recordBlock(position: BlockPos, state: IBlockState) {
        if (!tracking) return
        val immutablePosition = BlockPos(position.x, position.y, position.z)
        lock.write {
            if (accepts(immutablePosition, state)) {
                statesByPosition[immutablePosition] = state
                positionsByChunk.getOrPut(chunkKey(immutablePosition.x shr 4, immutablePosition.z shr 4)) {
                    HashSet()
                }.add(immutablePosition)
            } else {
                removePosition(immutablePosition)
            }
        }
    }

    override fun clearChunk(chunkX: Int, chunkZ: Int) {
        lock.write {
            positionsByChunk.remove(chunkKey(chunkX, chunkZ))?.forEach(statesByPosition::remove)
        }
    }

    override fun clearAll() {
        lock.write {
            statesByPosition.clear()
            positionsByChunk.clear()
        }
    }

    fun stateAt(position: BlockPos): IBlockState? = lock.read { statesByPosition[position] }

    fun snapshot(): Map<BlockPos, IBlockState> = lock.read { HashMap(statesByPosition) }

    fun positions(): Set<BlockPos> = lock.read { HashSet(statesByPosition.keys) }

    fun size(): Int = lock.read { statesByPosition.size }

    private fun removePosition(position: BlockPos) {
        if (statesByPosition.remove(position) == null) return
        val key = chunkKey(position.x shr 4, position.z shr 4)
        positionsByChunk[key]?.let { positions ->
            positions.remove(position)
            if (positions.isEmpty()) positionsByChunk.remove(key)
        }
    }

    private fun chunkKey(x: Int, z: Int): Long = x.toLong() and 0xFFFFFFFFL or (z.toLong() shl 32)
}
