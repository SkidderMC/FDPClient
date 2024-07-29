/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.extensions.isAnimal
import net.ccbluex.liquidbounce.utils.extensions.isMob
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer

object NoRender : Module("NoRender", Category.VISUAL, gameDetecting = false, hideModule = false) {

    private val allValue by BoolValue("All", true)
	//val nameTagsValue by BoolValue("NameTags", true)
    private val itemsValue by BoolValue("Items", true) { !allValue }
    private val playersValue by BoolValue("Players", true)
    private val mobsValue by BoolValue("Mobs", true)
    private val animalsValue by BoolValue("Animals", true) { !allValue }
    private val armorStandValue by BoolValue("ArmorStand", true) { !allValue }
    private val autoResetValue by BoolValue("AutoReset", true)
    private val maxRenderRange by FloatValue("MaxRenderRange", 4F, 0F.. 16F)

    @EventTarget
    fun onMotion(event: MotionEvent) {
    	for (en in mc.theWorld.loadedEntityList) {
    		val entity = en!!
    		if (shouldStopRender(entity))
    			entity.renderDistanceWeight = 0.0
            else if (autoResetValue)
                entity.renderDistanceWeight = 1.0
    	}
    }

	fun shouldStopRender(entity: Entity): Boolean {
		return (allValue
                ||(itemsValue && entity is EntityItem)
    			|| (playersValue && entity is EntityPlayer)
    			|| (mobsValue && entity.isMob())
    			|| (animalsValue && entity.isAnimal())
                || (armorStandValue && entity is EntityArmorStand))
    			&& entity != mc.thePlayer!!
				&& (mc.thePlayer!!.getDistanceToEntityBox(entity).toFloat() > maxRenderRange)
	}

 	override fun onDisable() {
 		for (en in mc.theWorld.loadedEntityList) {
 			val entity = en!!
 			if (entity != mc.thePlayer!! && entity.renderDistanceWeight <= 0.0)
 				entity.renderDistanceWeight = 1.0
 		}
 	}

}