/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.ghost

import me.zywl.fdpclient.event.EventTarget
import me.zywl.fdpclient.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import me.zywl.fdpclient.value.impl.BoolValue
import me.zywl.fdpclient.value.impl.IntegerValue
import net.minecraft.item.ItemBlock

@ModuleInfo(name = "FastPlace", category = ModuleCategory.GHOST)
object FastPlace : Module() {

    val speedValue = IntegerValue("Speed", 0, 0, 4)
    private val blockonlyValue = BoolValue("BlocksOnly", false)
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!blockonlyValue.get() || mc.thePlayer.heldItem.item is ItemBlock) {
            if (mc.thePlayer.ticksExisted % speedValue.get() == 0 || speedValue.get() == 0) {
                mc.rightClickDelayTimer = 1
            }
        }
    }
}
