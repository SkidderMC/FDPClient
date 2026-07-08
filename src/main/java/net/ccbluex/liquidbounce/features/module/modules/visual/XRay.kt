/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.world.IBlockAccess

object XRay : Module("XRay", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY, gameDetecting = false) {

    private val exposedOnly by boolean("ExposedOnly", false)
        .describe("Only show ores exposed to air.")
    private val fullBright by boolean("FullBright", false)
        .describe("Brighten the world while XRay is active.")
    private val backgroundOpacity by int("BackgroundOpacity", 0, 0..255)
        .describe("Opacity of ordinary blocks. Zero hides them completely.")

    val xrayBlocks = mutableListOf(
        Blocks.coal_ore,
        Blocks.iron_ore,
        Blocks.gold_ore,
        Blocks.redstone_ore,
        Blocks.lapis_ore,
        Blocks.diamond_ore,
        Blocks.emerald_ore,
        Blocks.quartz_ore,
        Blocks.clay,
        Blocks.glowstone,
        Blocks.crafting_table,
        Blocks.torch,
        Blocks.ladder,
        Blocks.tnt,
        Blocks.coal_block,
        Blocks.iron_block,
        Blocks.gold_block,
        Blocks.diamond_block,
        Blocks.emerald_block,
        Blocks.lapis_block,
        Blocks.fire,
        Blocks.mossy_cobblestone,
        Blocks.mob_spawner,
        Blocks.end_portal_frame,
        Blocks.enchanting_table,
        Blocks.bookshelf,
        Blocks.command_block,
        Blocks.lava,
        Blocks.flowing_lava,
        Blocks.water,
        Blocks.flowing_water,
        Blocks.furnace,
        Blocks.lit_furnace
    )

    private var prevGammaLevel = 0f

    override fun onEnable() {
        prevGammaLevel = mc.gameSettings.gammaSetting

        if (fullBright)
            mc.gameSettings.gammaSetting = 100000f
    }

    override fun onToggle(state: Boolean) {
        mc.renderGlobal.loadRenderers()
    }

    override fun onDisable() {
        mc.gameSettings.gammaSetting = prevGammaLevel
    }

    fun shouldRender(world: IBlockAccess, pos: BlockPos, block: Block): Boolean {
        if (block !in xrayBlocks)
            return false

        if (!exposedOnly)
            return true

        return EnumFacing.values().any { !world.getBlockState(pos.offset(it)).block.isOpaqueCube }
    }

    fun modifyShouldSideBeRendered(
        original: Boolean,
        world: IBlockAccess,
        adjacentPos: BlockPos,
        side: EnumFacing,
        block: Block,
    ): Boolean = if (block in xrayBlocks) {
        shouldRender(world, adjacentPos.offset(side.opposite), block)
    } else {
        backgroundOpacity > 0 && original
    }

    fun shouldRenderBackground(block: Block): Boolean =
        handleEvents() && backgroundOpacity > 0 && block !in xrayBlocks

    fun shouldUseTranslucentBackground(block: Block): Boolean =
        shouldRenderBackground(block) && backgroundOpacity < 255

    private val renderingBackground = ThreadLocal.withInitial { false }

    fun beginBackgroundRender(block: Block): Boolean {
        val background = shouldUseTranslucentBackground(block)
        renderingBackground.set(background)
        return background
    }

    fun endBackgroundRender() {
        renderingBackground.set(false)
    }

    fun currentBackgroundAlpha(): Int =
        if (renderingBackground.get()) backgroundOpacity else 255
}
