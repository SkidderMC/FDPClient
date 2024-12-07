/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlockName
import net.ccbluex.liquidbounce.utils.block.BlockUtils.searchBlocks
import net.ccbluex.liquidbounce.utils.extensions.SharedScopes
import net.ccbluex.liquidbounce.utils.extensions.block
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils.draw2D
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBlockBox
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.value.block
import net.ccbluex.liquidbounce.value.boolean
import net.ccbluex.liquidbounce.value.choices
import net.ccbluex.liquidbounce.value.int
import net.minecraft.block.Block
import net.minecraft.init.Blocks.air
import net.minecraft.util.BlockPos
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap

object BlockESP : Module("BlockESP", Category.VISUAL, hideModule = false) {
    private val mode by choices("Mode", arrayOf("Box", "2D"), "Box")
    private val block by block("Block", 168)
    private val radius by int("Radius", 40, 5..120)
    private val blockLimit by int("BlockLimit", 256, 0..2056)

    private val colorRainbow by boolean("Rainbow", false)
    private val colorRed by int("R", 255, 0..255) { !colorRainbow }
    private val colorGreen by int("G", 179, 0..255) { !colorRainbow }
    private val colorBlue by int("B", 72, 0..255) { !colorRainbow }

    private val searchTimer = MSTimer()
    private val posList = ConcurrentHashMap.newKeySet<BlockPos>()
    private var searchJob: Job? = null

    override fun onDisable() {
        searchJob?.cancel()
        posList.clear()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (searchTimer.hasTimePassed(1000) && (searchJob?.isActive != true)) {
            val radius = radius
            val selectedBlock = Block.getBlockById(block)
            val blockLimit = blockLimit

            if (selectedBlock == null || selectedBlock == air)
                return

            searchJob = SharedScopes.Default.launch {
                posList.removeIf {
                    it.block != selectedBlock
                }

                posList += searchBlocks(radius, setOf(selectedBlock), blockLimit).keys

                searchTimer.reset()
            }
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val color = if (colorRainbow) rainbow() else Color(colorRed, colorGreen, colorBlue)
        when (mode) {
            "Box" -> posList.forEach { drawBlockBox(it, color, true) }
            "2D" -> posList.forEach { draw2D(it, color.rgb, Color.BLACK.rgb) }
        }
    }

    override val tag
        get() = getBlockName(block)
}