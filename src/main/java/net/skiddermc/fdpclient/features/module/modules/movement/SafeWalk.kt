/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.features.module.modules.movement

import net.skiddermc.fdpclient.event.EventTarget
import net.skiddermc.fdpclient.event.MoveEvent
import net.skiddermc.fdpclient.features.module.Module
import net.skiddermc.fdpclient.features.module.ModuleCategory
import net.skiddermc.fdpclient.features.module.ModuleInfo
import net.skiddermc.fdpclient.value.BoolValue

@ModuleInfo(name = "SafeWalk", category = ModuleCategory.MOVEMENT)
class SafeWalk : Module() {

    private val airSafeValue = BoolValue("AirSafe", false)
    private val onlyVoidValue = BoolValue("OnlyPredictVoid", false)

    @EventTarget
    fun onMove(event: MoveEvent) {
        if (onlyVoidValue.get() && !checkVoid()) {
            return
        } else {
            if (airSafeValue.get() || mc.thePlayer.onGround) {
                event.isSafeWalk = true
            }
        }
    }

    private fun checkVoid(): Boolean {
        var i = (-(mc.thePlayer.posY-1.4857625)).toInt()
        var dangerous = true
		while (i <= 0) {
			dangerous = mc.theWorld.getCollisionBoxes(mc.thePlayer.entityBoundingBox.offset(mc.thePlayer.motionX * 1.4, i.toDouble(), mc.thePlayer.motionZ * 1.4)).isEmpty()
			i++
			if (!dangerous) break
		}
        return dangerous
    }
}
