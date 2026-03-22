/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.client.EntityLookup
import net.ccbluex.liquidbounce.utils.extensions.isLookingOnEntity
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBlockBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityBox
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.isEntityHeightVisible
import net.minecraft.entity.item.EntityFallingBlock
import net.minecraft.util.BlockPos
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap

object ProphuntESP : Module("ProphuntESP", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY, gameDetecting = false) {
    private val mode by choices("Mode", arrayOf("Box", "OtherBox", "Glow"), "OtherBox")
    private val glowSettings = GlowRenderSettings(isSupported = { mode == "Glow" }).also { addValues(it.values) }

    private val color by color("Color", Color(0, 90, 255))

    private val renderFilters = RenderFilterSettings(50, 1..200).also { addValues(it.values) }

    private val blocks = ConcurrentHashMap<BlockPos, Long>()

    private val entities by EntityLookup<EntityFallingBlock>()
        .filter { !renderFilters.onLook || mc.thePlayer.isLookingOnEntity(it, renderFilters.maxAngleDifference.toDouble()) }
        .filter { renderFilters.thruBlocks || isEntityHeightVisible(it) }
        .filter { renderFilters.withinDistance(mc.thePlayer.getDistanceSqToEntity(it)) }

    fun recordBlock(blockPos: BlockPos) {
        blocks[blockPos] = System.currentTimeMillis()
    }

    override fun onDisable() {
        blocks.clear()
    }

    val handleFallingBlocks = handler<Render3DEvent> {
        if (mode != "Box" && mode != "OtherBox") return@handler

        for (entity in entities) {
            drawEntityBox(entity, color, mode == "Box")
        }
    }

    val handleUpdateBlocks = handler<Render3DEvent> {
        val now = System.currentTimeMillis()

        with(blocks.entries.iterator()) {
            while (hasNext()) {
                val (pos, time) = next()

                if (now - time > 2000L) {
                    remove()
                    continue
                }

                drawBlockBox(pos, color, mode == "Box")
            }
        }
    }

    val onRender2D = handler<Render2DEvent> { event ->
        if (mc.theWorld == null || mode != "Glow") return@handler

        renderGlow(event.partialTicks, color, glowSettings) {
            for (entity in entities) {
                try {
                    mc.renderManager.renderEntityStatic(entity, mc.timer.renderPartialTicks, true)
                } catch (ex: Exception) {
                    LOGGER.error("An error occurred while rendering all entities for shader esp", ex)
                }
            }
        }
    }
}
