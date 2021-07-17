/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.potion.Potion

@ModuleInfo(name = "Sprint", description = "Automatically sprints all the time.", category = ModuleCategory.MOVEMENT, defaultOn = true)
class Sprint : Module() {
    val allDirectionsValue = BoolValue("AllDirections", true)
    private val allDirectionsRotateValue = BoolValue("AllDirectionsRotate", true)
    private val blindnessValue = BoolValue("Blindness", true)
    val foodValue = BoolValue("Food", true)
    val checkServerSide = BoolValue("CheckServerSide", false)
    val checkServerSideGround = BoolValue("CheckServerSideOnlyGround", false)

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.isSprinting = true

        if (!MovementUtils.isMoving() || mc.thePlayer.isSneaking || blindnessValue.get()
                && mc.thePlayer.isPotionActive(Potion.blindness) || foodValue.get()
                && !(mc.thePlayer.foodStats.foodLevel > 6.0f || mc.thePlayer.capabilities.allowFlying)
                || (checkServerSide.get() && (mc.thePlayer.onGround || !checkServerSideGround.get())
                && !allDirectionsValue.get() && RotationUtils.targetRotation != null
                && RotationUtils.getRotationDifference(Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)) > 30)) {
            mc.thePlayer.isSprinting = false
            return
        }

        if (allDirectionsValue.get()) {
            mc.thePlayer.isSprinting = true
            if (allDirectionsRotateValue.get() && !mc.gameSettings.keyBindForward.pressed) {
                RotationUtils.setTargetRotation(Rotation((MovementUtils.getDirection() * 180f / Math.PI).toFloat(), mc.thePlayer.rotationPitch))
            }
        }
    }
}