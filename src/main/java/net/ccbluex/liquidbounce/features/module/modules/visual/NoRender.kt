/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.block.BlockUtils.searchBlocks
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.extensions.isAnimal
import net.ccbluex.liquidbounce.utils.extensions.isMob
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.BlockPos

/**
 * NoRender is a visual module that allows the player to control the rendering of entities
 * and specific blocks in the game world. It provides functionality to hide or show selected
 * entities and blocks dynamically, enhancing performance or reducing visual clutter.
 *
 * Usage: Toggle the module to hide specific entities and blocks. Adjust settings to control
 * the visibility of certain elements in the game world.
 *
 * @author opZywl
 */
object NoRender : Module("NoRender", Category.VISUAL, gameDetecting = false) {

	private val allEntitiesValue by boolean("AllEntities", true)
	private val itemsValue by boolean("Items", true) { !allEntitiesValue }
	private val playersValue by boolean("Players", true)
	private val mobsValue by boolean("Mobs", true)
	private val animalsValue by boolean("Animals", true) { !allEntitiesValue }
	private val armorStandValue by boolean("ArmorStand", true) { !allEntitiesValue }
	private val autoResetValue by boolean("AutoReset", true)
	private val maxRenderRange by float("MaxRenderRange", 4F, 0F..16F)

	// Option to enable or disable specific block rendering
	private val useSpecificBlock by boolean("Block", true)

	// The specific block selected by its ID
	private val specificBlockValue by block("SpecificBlock", 1)

	// Stores hidden blocks and their original states
	private val hiddenBlocks: MutableMap<BlockPos, IBlockState> = mutableMapOf()

	// Stores the current block being hidden
	private var currentBlock: Block? = null

	// Event to control entity rendering
    val onMotion = handler<MotionEvent> {
		for (en in mc.theWorld.loadedEntityList) {
			val entity = en!!
			if (shouldStopRender(entity))
				entity.renderDistanceWeight = 0.0
			else if (autoResetValue)
				entity.renderDistanceWeight = 1.0
		}
	}

	// Event to control block rendering
    val onRender3D = handler<Render3DEvent> {
		// If the specific block feature is disabled, return without doing anything
		if (!useSpecificBlock) {
			// Ensure that all previously hidden blocks are restored if the option is disabled
			restoreHiddenBlocks()
			return@handler
		}

		mc.thePlayer?.let {
			val radius = 16
			val selectedBlock = Block.getBlockById(specificBlockValue)

			// If there is no change in the selected block, do nothing
			if (currentBlock == selectedBlock) return@handler

			// Restore previously hidden blocks before hiding new ones
			restoreHiddenBlocks()

			// Clear the map for new block selection
			hiddenBlocks.clear()

			// Update the currently selected block
			currentBlock = selectedBlock

			// Search for blocks of the selected type in the player's vicinity
			val blockList = searchBlocks(radius, setOf(selectedBlock))

			// Hide the selected block and save its position and state
			blockList.forEach { (pos, block) ->
				if (block == selectedBlock) {
					hiddenBlocks[pos] = mc.theWorld.getBlockState(pos)
					mc.theWorld.setBlockToAir(pos)
				}
			}
		}
	}

	// Function to restore previously hidden blocks
	private fun restoreHiddenBlocks() {
		hiddenBlocks.forEach { (pos, blockState) ->
			mc.theWorld.setBlockState(pos, blockState)
		}
		hiddenBlocks.clear()
		currentBlock = null
	}

	// Function to determine if an entity should stop rendering
	fun shouldStopRender(entity: Entity): Boolean {
		return (allEntitiesValue
				||(itemsValue && entity is EntityItem)
				|| (playersValue && entity is EntityPlayer)
				|| (mobsValue && entity.isMob())
				|| (animalsValue && entity.isAnimal())
				|| (armorStandValue && entity is EntityArmorStand))
				&& entity != mc.thePlayer!!
				&& (mc.thePlayer!!.getDistanceToEntityBox(entity).toFloat() > maxRenderRange)
	}

	// Resets rendering when the module is disabled
	override fun onDisable() {
		// Restore all hidden blocks when the module is disabled
		restoreHiddenBlocks()

		// Clear the list of hidden blocks
		hiddenBlocks.clear()

		// Reset the selected block
		currentBlock = null

		// Restore entity rendering
		for (en in mc.theWorld.loadedEntityList) {
			val entity = en!!
			if (entity != mc.thePlayer!! && entity.renderDistanceWeight <= 0.0)
				entity.renderDistanceWeight = 1.0
		}
	}

	// Forces re-rendering of blocks when the module is toggled
	override fun onToggle(state: Boolean) {
		mc.renderGlobal.loadRenderers()
	}
}