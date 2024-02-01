/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo

import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue

@ModuleInfo(name = "DelayRemover", category = ModuleCategory.PLAYER)
object DelayRemover : Module() {

    val jumpDelay = BoolValue("NoJumpDelay", true)
    val jumpDelayTicks = IntegerValue("JumpDelayTicks", 0, 0, 4)

    val noClickDelay = BoolValue("NoClickDelay", true)

    val blockBreakDelay = BoolValue("NoBlockHitDelay", false)

    val noSlowBreak = BoolValue("NoSlowBreak", false)
    val airValue = BoolValue("Air", true).displayable { noSlowBreak.get() }
    val waterValue = BoolValue("Water", false).displayable { noSlowBreak.get() }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (mc.thePlayer != null && mc.theWorld != null && noClickDelay.get()) {
            mc.leftClickCounter = 0
        }

        if (blockBreakDelay.get()) {
            mc.playerController.blockHitDelay = 0
        }
    }

}
