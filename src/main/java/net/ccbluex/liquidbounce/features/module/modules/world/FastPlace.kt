/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.minecraft.item.ItemBlock

object FastPlace : Module(name = "FastPlace", category = ModuleCategory.WORLD, defaultOn = false) {
    val speedValue = IntegerValue("Speed", 0, 0, 4)
    val blockonlyValue = BoolValue("BlocksOnly", false)
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!blockonlyValue.get() || mc.thePlayer.heldItem.item is ItemBlock) {
            mc.rightClickDelayTimer = speedValue.get()
        }
    }
}
