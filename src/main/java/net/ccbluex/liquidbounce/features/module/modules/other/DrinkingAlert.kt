/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.script.api.global.Chat
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemPotion

object DrinkingAlert : Module("DrinkingAlert", Category.OTHER, hideModule = false) {

    private val alertTimer = MSTimer()
    private val drinkers = arrayListOf<EntityLivingBase>()

    override fun onDisable() {
        clearDrag()
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        clearDrag()
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.eventState == EventState.PRE) {
            for (player in mc.theWorld.playerEntities) {
                if (player !in drinkers && player != mc.thePlayer && player.isUsingItem && player.heldItem != null && player.heldItem.item is ItemPotion) {
                    Chat.print("§e" + player.name + "§r is drinking!")
                    drinkers.add(player)
                    alertTimer.reset()
                }
            }
            if (alertTimer.hasTimePassed(3000L) && drinkers.isNotEmpty()) {
                clearDrag()
            }
        }
    }

    private fun clearDrag() {
        drinkers.clear()
        alertTimer.reset()
    }
}