/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.ghost

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.item.ItemBlock

@ModuleInfo(name = "FastPlace", category = ModuleCategory.GHOST)
object FastPlace : Module() {

    val speedValue = IntegerValue("Speed", 0, 0, 4)
    private val blockonlyValue = BoolValue("BlocksOnly", false)
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!blockonlyValue.get() || mc.thePlayer.heldItem.item is ItemBlock) {
            mc.rightClickDelayTimer = speedValue.get()
        }
    }
}
