/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.features.module.modules.player

import net.skiddermc.fdpclient.event.EventTarget
import net.skiddermc.fdpclient.event.UpdateEvent
import net.skiddermc.fdpclient.features.module.Module
import net.skiddermc.fdpclient.features.module.ModuleCategory
import net.skiddermc.fdpclient.features.module.ModuleInfo
import net.skiddermc.fdpclient.utils.Rotation
import net.skiddermc.fdpclient.utils.RotationUtils
import net.skiddermc.fdpclient.utils.misc.RandomUtils
import net.skiddermc.fdpclient.value.BoolValue
import net.skiddermc.fdpclient.value.ListValue

@ModuleInfo(name = "AntiAim", category = ModuleCategory.PLAYER)
class AntiAim : Module() {
    private val yawModeValue = ListValue("YawMove", arrayOf("Jitter", "Spin", "Back", "BackJitter"), "Spin")
    private val pitchModeValue = ListValue("PitchMode", arrayOf("Down", "Up", "Jitter", "AnotherJitter"), "Down")
    private val rotateValue = BoolValue("SilentRotate", true)

    private var yaw = 0f
    private var pitch = 0f

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        when (yawModeValue.get().lowercase()) {
            "spin" -> {
                yaw += 20.0f
                if (yaw > 180.0f) {
                    yaw = -180.0f
                } else if (yaw < -180.0f) {
                    yaw = 180.0f
                }
            }
            "jitter" -> {
                yaw = mc.thePlayer.rotationYaw + if (mc.thePlayer.ticksExisted % 2 == 0) 90F else -90F
            }
            "back" -> {
                yaw = mc.thePlayer.rotationYaw + 180f
            }
            "backjitter" -> {
                yaw = mc.thePlayer.rotationYaw + 180f + RandomUtils.nextDouble(-3.0, 3.0).toFloat()
            }
        }

        when (pitchModeValue.get().lowercase()) {
            "up" -> {
                pitch = -90.0f
            }
            "down" -> {
                pitch = 90.0f
            }
            "anotherjitter" -> {
                pitch = 60f + RandomUtils.nextDouble(-3.0, 3.0).toFloat()
            }
            "jitter" -> {
                pitch += 30.0f
                if (pitch > 90.0f) {
                    pitch = -90.0f
                } else if (pitch < -90.0f) {
                    pitch = 90.0f
                }
            }
        }

        if (rotateValue.get()) {
            RotationUtils.setTargetRotation(Rotation(yaw, pitch))
        } else {
            mc.thePlayer.rotationYaw = yaw
            mc.thePlayer.rotationPitch = pitch
        }
    }
}