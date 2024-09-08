/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.block.BlockUtils.searchBlocks
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.extensions.isAnimal
import net.ccbluex.liquidbounce.utils.extensions.isMob
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.block.BlockGlass
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer

object NoRender : Module("NoRender", Category.VISUAL, gameDetecting = false, hideModule = false) {

	private val allEntitiesValue by BoolValue("AllEntities", true)
	private val itemsValue by BoolValue("Items", true) { !allEntitiesValue }
	private val playersValue by BoolValue("Players", true)
	private val mobsValue by BoolValue("Mobs", true)
	private val animalsValue by BoolValue("Animals", true) { !allEntitiesValue }
	private val armorStandValue by BoolValue("ArmorStand", true) { !allEntitiesValue }
	private val autoResetValue by BoolValue("AutoReset", true)
	private val maxRenderRange by FloatValue("MaxRenderRange", 4F, 0F..16F)
	private val clearGlassValue by BoolValue("ClearGlass", false)

	// Event to control entity rendering
	@EventTarget
	fun onMotion(event: MotionEvent) {
		mc.theWorld?.loadedEntityList?.forEach { entity ->
			entity?.let {
				if (shouldStopRender(it)) {
					it.renderDistanceWeight = 0.0
				} else if (autoResetValue) {
					it.renderDistanceWeight = 1.0
				}
			}
		}
	}

	// Event to control block rendering (Clear Glass functionality)
	@EventTarget
	fun onRender3D(event: Render3DEvent) {
		mc.thePlayer?.let { _ ->
			val radius = maxRenderRange.toInt()
			val blockList = searchBlocks(radius, null)  // Pass 'null' for targetBlocks

			blockList.forEach { (pos, block) ->
				// Check if the block is glass and ClearGlass is active
				if (clearGlassValue && block is BlockGlass) {
					mc.theWorld.setBlockToAir(pos)
				}
			}
		}
	}

	// Function to determine if an entity should stop rendering
	fun shouldStopRender(entity: Entity): Boolean {
		return ((allEntitiesValue ||
				(itemsValue && entity is EntityItem) ||
				(playersValue && entity is EntityPlayer) ||
				(mobsValue && entity.isMob()) ||
				(animalsValue && entity.isAnimal()) ||
				(armorStandValue && entity is EntityArmorStand)) && entity != mc.thePlayer
				&& (mc.thePlayer?.getDistanceToEntityBox(entity)?.toFloat() ?: 0F) > maxRenderRange)
	}

	// Reset rendering when the module is disabled
	override fun onDisable() {
		mc.theWorld?.loadedEntityList?.forEach { entity ->
			entity?.let {
				if (it != mc.thePlayer && it.renderDistanceWeight <= 0.0) {
					it.renderDistanceWeight = 1.0
				}
			}
		}
	}
}