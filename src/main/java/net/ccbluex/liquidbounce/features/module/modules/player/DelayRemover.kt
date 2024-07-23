/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.MovementUtils.updateControls
import net.ccbluex.liquidbounce.value.BoolValue

object DelayRemover : Module("DelayRemover", Category.PLAYER, hideModule = false) {

   // val jumpDelay by BoolValue("NoJumpDelay", false)
  //  val jumpDelayTicks by IntegerValue("JumpDelayTicks", 0, 0.. 4) { jumpDelay }

    val noClickDelay by BoolValue("NoClickDelay", true)

    val blockBreakDelay by BoolValue("NoBlockHitDelay", false)

    val noSlowBreak by BoolValue("NoSlowBreak", false)
    val air by BoolValue("Air", true) { noSlowBreak }
    val water by BoolValue("Water", false) { noSlowBreak }

    val exitGuiValue by BoolValue("NoExitGuiDelay", true)

    private var prevGui = false

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (mc.thePlayer != null && mc.theWorld != null && noClickDelay) {
            mc.leftClickCounter = 0
        }

        if (blockBreakDelay) {
            mc.playerController.blockHitDelay = 0
        }

        if (mc.currentScreen == null && exitGuiValue) {
            if (prevGui) updateControls()
            prevGui = false
        } else {
            prevGui = true
        }
    }

}
