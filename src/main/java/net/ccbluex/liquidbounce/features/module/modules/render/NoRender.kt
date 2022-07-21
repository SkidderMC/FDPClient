/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.item.EntityArmorStand

@ModuleInfo(name = "NoRender", category = ModuleCategory.RENDER)
class NoRender : Module() {

	val nameTagsValue = BoolValue("NameTags", true)
    private val itemsValue = BoolValue("Items", true)
    private val playersValue = BoolValue("Players", true)
    private val mobsValue = BoolValue("Mobs", true)
    private val animalsValue = BoolValue("Animals", true)
    val armorStandValue = BoolValue("ArmorStand", true)
    val allValue = BoolValue("All", true)
    private val autoResetValue = BoolValue("AutoReset", true)
    private val maxRenderRange = FloatValue("MaxRenderRange", 4F, 0F, 16F)

    @EventTarget
    fun onMotion(event: MotionEvent) {
    	for (en in mc.theWorld.loadedEntityList) {
    		val entity = en!! as Entity
    		if (shouldStopRender(entity))
    			entity.renderDistanceWeight = 0.0
            else if (autoResetValue.get())
                entity.renderDistanceWeight = 1.0
    	}
    }

	fun shouldStopRender(entity: Entity): Boolean {
		return (allValue.get()
                ||(itemsValue.get() && entity is EntityItem)
    			|| (playersValue.get() && entity is EntityPlayer)
    			|| (mobsValue.get() && EntityUtils.isMob(entity))
    			|| (animalsValue.get() && EntityUtils.isAnimal(entity))
                || (armorStandValue.get() && entity is EntityArmorStand))
    			&& entity != mc.thePlayer!!
				&& (mc.thePlayer!!.getDistanceToEntityBox(entity).toFloat() > maxRenderRange.get())
	}

 	override fun onDisable() {
 		for (en in mc.theWorld.loadedEntityList) {
 			val entity = en!! as Entity
 			if (entity != mc.thePlayer!! && entity.renderDistanceWeight <= 0.0)
 				entity.renderDistanceWeight = 1.0
 		}
 	}

}