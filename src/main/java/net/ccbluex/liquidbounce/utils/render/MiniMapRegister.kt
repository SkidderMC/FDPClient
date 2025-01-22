/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.BlockPos
import net.minecraft.world.chunk.Chunk
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

object MiniMapRegister : MinecraftInstance, Listenable {

    private val chunkTextureMap = HashMap<ChunkLocation, MiniMapTexture>(256)
    private val queuedChunkUpdates = HashSet<Chunk>(256)
    private val queuedChunkDeletions = HashSet<ChunkLocation>(256)
    private var deleteAllChunks = false

    private val lock = ReentrantReadWriteLock()

    fun updateChunk(chunk: Chunk) {
        lock.write {
            queuedChunkUpdates += chunk
        }
    }

    fun getChunkTextureAt(x: Int, z: Int) = lock.read { chunkTextureMap[ChunkLocation(x, z)] }

    val onRender2D = handler<Render2DEvent> {
        updateChunks()
    }

    private fun updateChunks() {
        lock.write {
            if (deleteAllChunks) {
                queuedChunkDeletions.clear()
                queuedChunkUpdates.clear()

                chunkTextureMap.values.forEach { it.delete() }
                chunkTextureMap.clear()

                deleteAllChunks = false
            } else {
                queuedChunkDeletions.forEach {
                    chunkTextureMap.remove(it)?.delete()
                }
                queuedChunkDeletions.clear()
            }

            queuedChunkUpdates.forEach {
                chunkTextureMap.getOrPut(it.location, ::MiniMapTexture).updateChunkData(it)
            }

            queuedChunkUpdates.clear()
        }
    }

    fun getLoadedChunkCount() = lock.read { chunkTextureMap.size }

    fun unloadChunk(x: Int, z: Int) {
        lock.write {
            queuedChunkDeletions += ChunkLocation(x, z)
        }
    }

    fun unloadAllChunks() = lock.write { deleteAllChunks = true }

    class MiniMapTexture {
        val texture = DynamicTexture(16, 16)
        private var deleted = false

        fun updateChunkData(chunk: Chunk) {
            val rgbValues = texture.textureData

            val pos = BlockPos.MutableBlockPos()
            for (x in 0..15) {
                for (z in 0..15) {
                    val bp = pos.set(x, chunk.getHeightValue(x, z) - 1, z)
                    val blockState = chunk.getBlockState(bp)

                    rgbValues[rgbValues.size - 1 - (z shl 4 or x)] = blockState.block.getMapColor(blockState).colorValue or (0xFF shl 24)
                }
            }

            texture.updateDynamicTexture()
        }

        internal fun delete() {
            if (!deleted) {
                texture.deleteGlTexture()
                deleted = true
            }
        }

        protected fun finalize() {
            // We don't need to set deleted to true since the object is deleted after this method call
            if (!deleted)
                texture.deleteGlTexture()
        }
    }

    private val Chunk.location: ChunkLocation
        get() = ChunkLocation(xPosition, zPosition)

    data class ChunkLocation(val x: Int, val z: Int)

}