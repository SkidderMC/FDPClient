/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.potion.Potion

@ModuleInfo(name = "Sprint", category = ModuleCategory.MOVEMENT, defaultOn = true)
class Sprint : Module() {
    val allDirectionsValue = BoolValue("AllDirections", true)
    private val allDirectionsBypassValue = ListValue("AllDirectionsBypass", arrayOf("Rotate", "Toggle", "Minemora", "Spoof", "LimitSpeed", "None"), "None").displayable { allDirectionsValue.get() }
    private val blindnessValue = BoolValue("Blindness", true)
    val foodValue = BoolValue("Food", true)
    val checkServerSide = BoolValue("CheckServerSide", false)
    val checkServerSideGround = BoolValue("CheckServerSideOnlyGround", false).displayable { checkServerSide.get() }
    private val noPacket = BoolValue("NoPacket", false)
    private val allDirectionsLimitSpeedGround = BoolValue("AllDirectionsLimitSpeedOnlyGround", true)
    private val allDirectionsLimitSpeedValue = FloatValue("AllDirectionsLimitSpeed", 0.7f, 0.5f, 1f).displayable { allDirectionsBypassValue.displayable && allDirectionsBypassValue.equals("LimitSpeed") }

    private var spoofStat = false
        set(value) {
            if (field != value) {
                if (value) {
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING))
                } else {
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                }
                field = value
            }
        }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.isSprinting = true

        if (!MovementUtils.isMoving() || mc.thePlayer.isSneaking || blindnessValue.get() &&
                mc.thePlayer.isPotionActive(Potion.blindness) || foodValue.get() &&
                !(mc.thePlayer.foodStats.foodLevel > 6.0f || mc.thePlayer.capabilities.allowFlying) ||
                (checkServerSide.get() && (mc.thePlayer.onGround || !checkServerSideGround.get()) &&
                !allDirectionsValue.get() && RotationUtils.targetRotation != null &&
                RotationUtils.getRotationDifference(Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)) > 30)) {
            mc.thePlayer.isSprinting = false
            return
        }

        if (allDirectionsValue.get()) {
            mc.thePlayer.isSprinting = true
            if (RotationUtils.getRotationDifference(Rotation((MovementUtils.direction * 180f / Math.PI).toFloat(), mc.thePlayer.rotationPitch)) > 30) {
                when (allDirectionsBypassValue.get().lowercase()) {
                    "rotate" -> RotationUtils.setTargetRotation(Rotation((MovementUtils.direction * 180f / Math.PI).toFloat(), mc.thePlayer.rotationPitch), 10)
                    "toggle" -> {
                        mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING))
                        mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                    }
                    "minemora" -> mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.0000013, mc.thePlayer.posZ)
                    "limitspeed" -> {
                        if (!allDirectionsLimitSpeedGround.get() || mc.thePlayer.onGround) {
                            MovementUtils.limitSpeedByPercent(allDirectionsLimitSpeedValue.get())
                        }
                    }
                    "spoof" -> spoofStat = true
                }
            } else {
                when (allDirectionsBypassValue.get().lowercase()) {
                    "spoof" -> spoofStat = false
                }
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (noPacket.get() && packet is C0BPacketEntityAction && (packet.action == C0BPacketEntityAction.Action.START_SPRINTING || packet.action == C0BPacketEntityAction.Action.STOP_SPRINTING)) {
            event.cancelEvent()
        }
    }
}
