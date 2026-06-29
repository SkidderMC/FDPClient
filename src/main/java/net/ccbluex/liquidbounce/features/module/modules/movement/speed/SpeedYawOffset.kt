/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speed

import net.ccbluex.liquidbounce.config.ToggleableValueGroup
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.rotation.AlwaysRotationSettings
import net.ccbluex.liquidbounce.utils.rotation.Rotation
import net.ccbluex.liquidbounce.utils.rotation.RotationPriority
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.setTargetRotation

/** Reusable yaw-offset strategy for speed modes that benefit from diagonal server movement. */
object SpeedYawOffset : ToggleableValueGroup("YawOffset"), MinecraftInstance {

    private val modeValue = gatedChoices("Mode", arrayOf("Ground", "Air", "Constant"), "Air")
    private val rotations = AlwaysRotationSettings(Speed) { enabled }.withRequestPriority(RotationPriority.LOW)

    init {
        addValue(rotations)
    }

    fun update() {
        if (!enabled) return
        val player = mc.thePlayer ?: return
        if (!player.movementInput.run { moveForward != 0f || moveStrafe != 0f }) return

        val offset = when (modeValue.get()) {
            "Ground" -> if (player.onGround) directionalOffset() else 0f
            "Air" -> if (!player.onGround && player.movementInput.moveForward > 0f && player.movementInput.moveStrafe == 0f) -45f else 0f
            else -> directionalOffset()
        }

        setTargetRotation(Rotation(player.rotationYaw - offset, player.rotationPitch), rotations, rotations.effectiveResetTicks)
    }

    private fun directionalOffset(): Float {
        val input = mc.thePlayer?.movementInput ?: return 0f
        return when {
            input.moveForward > 0f && input.moveStrafe > 0f -> 45f
            input.moveForward > 0f && input.moveStrafe < 0f -> -45f
            input.moveForward < 0f && input.moveStrafe > 0f -> 135f
            input.moveForward < 0f && input.moveStrafe < 0f -> -135f
            input.moveForward < 0f -> 180f
            input.moveStrafe > 0f -> 90f
            input.moveStrafe < 0f -> -90f
            else -> 0f
        }
    }
}
