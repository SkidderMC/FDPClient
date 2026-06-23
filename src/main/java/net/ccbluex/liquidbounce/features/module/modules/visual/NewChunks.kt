/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.Render3D
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S21PacketChunkData
import net.minecraft.network.play.server.S22PacketMultiBlockChange
import net.minecraft.network.play.server.S23PacketBlockChange
import net.minecraft.network.play.server.S26PacketMapChunkBulk
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.world.ChunkCoordIntPair
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.max

object NewChunks : Module("NewChunks", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY, gameDetecting = false) {

    private val renderDistance by int("RenderDistance", 32, 4..128, "chunks")
        .describe("How far away chunks are rendered.")
    private val renderY by float("RenderY", 0f, -64f..320f)
        .describe("Y height at which the chunk overlay is drawn.")
    private val autoY by boolean("AutoY", false)
        .describe("Automatically place the overlay below you.")

    private val smooth by boolean("Smooth", true)
        .describe("Blend old chunk colors near new chunks.")
    private val persist by boolean("Persist", true)
        .describe("Keep tracked chunks across renders.")

    private val newColor by color("NewColor", Color(0, 255, 0, 80))
        .describe("Color used for newly generated chunks.")
    private val oldColor by color("OldColor", Color(255, 0, 0, 80))
        .describe("Color used for previously loaded chunks.")

    private val chunks = ConcurrentHashMap<ChunkCoordIntPair, Boolean>()

    override fun onDisable() {
        chunks.clear()
    }

    val onWorld = handler<WorldEvent> {
        chunks.clear()
    }

    private fun isFlowing(block: Block) =
        block === Blocks.flowing_water || block === Blocks.flowing_lava

    private fun mark(pos: BlockPos) {
        chunks[ChunkCoordIntPair(pos.x shr 4, pos.z shr 4)] = true
    }

    val onPacket = handler<PacketEvent> { event ->
        when (val packet = event.packet) {
            is S21PacketChunkData -> {
                val pos = ChunkCoordIntPair(packet.chunkX, packet.chunkZ)
                if (packet.extractedSize == 0) {
                    if (!persist) {
                        chunks.remove(pos)
                    }
                } else {
                    chunks.putIfAbsent(pos, false)
                }
            }

            is S26PacketMapChunkBulk -> {
                for (i in 0 until packet.chunkCount) {
                    val pos = ChunkCoordIntPair(packet.getChunkX(i), packet.getChunkZ(i))
                    chunks.putIfAbsent(pos, false)
                }
            }

            is S22PacketMultiBlockChange -> {
                for (update in packet.changedBlocks) {
                    if (isFlowing(update.blockState.block)) {
                        mark(update.pos)
                    }
                }
            }

            is S23PacketBlockChange -> {
                if (isFlowing(packet.blockState.block)) {
                    mark(packet.blockPosition)
                }
            }
        }
    }

    val onRender3D = handler<Render3DEvent> {
        if (chunks.isEmpty()) return@handler

        val player = mc.thePlayer ?: return@handler
        val renderManager = mc.renderManager

        val maxDist = renderDistance.toDouble() * 16.0
        val renderDistSq = maxDist * maxDist

        val drawY = if (autoY) player.posY - 100.0 else renderY.toDouble()

        for ((chunk, isNew) in chunks) {
            val chunkX = chunk.xStart
            val chunkZ = chunk.zStart

            if (player.getDistanceSq(chunkX + 8.0, player.posY, chunkZ + 8.0) > renderDistSq) {
                continue
            }

            var color = if (isNew) newColor else oldColor

            if (smooth && !isNew) {
                var totalWeight = 0.0

                for (dx in -2..2) {
                    for (dz in -2..2) {
                        if (dx == 0 && dz == 0) continue

                        if (chunks[ChunkCoordIntPair(chunk.chunkXPos + dx, chunk.chunkZPos + dz)] == true) {
                            val dist = max(abs(dx), abs(dz))
                            totalWeight += (3.0 - dist) / 2.0
                        }
                    }
                }

                if (totalWeight > 0.0) {
                    val ratio = (totalWeight / 12.0).coerceAtMost(1.0).toFloat()
                    color = ColorUtils.interpolateColor(oldColor, newColor, ratio)
                }
            }

            Render3D.drawAxisAlignedBB(
                AxisAlignedBB.fromBounds(
                    chunkX - renderManager.renderPosX,
                    drawY - renderManager.renderPosY,
                    chunkZ - renderManager.renderPosZ,
                    chunkX + 16.0 - renderManager.renderPosX,
                    drawY + 0.01 - renderManager.renderPosY,
                    chunkZ + 16.0 - renderManager.renderPosZ
                ),
                color
            )
        }
    }
}
