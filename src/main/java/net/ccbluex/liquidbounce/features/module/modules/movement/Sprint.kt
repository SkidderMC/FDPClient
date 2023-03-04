/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
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
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.potion.Potion

@ModuleInfo(name = "Sprint", category = ModuleCategory.MOVEMENT, defaultOn = true)
class Sprint : Module() {
    val useItemValue = BoolValue("UseItem", false)
    val useItemSwordValue = BoolValue("UseItemOnlySword", false).displayable{ useItemValue.get() }
    val hungryValue = BoolValue("Hungry", true)
    val sneakValue = BoolValue("Sneak", false)
    val collideValue = BoolValue("Collide", false)
    val jumpDirectionsValue = BoolValue("JumpDirections", false)
    val allDirectionsValue = BoolValue("AllDirections", false)
    val allDirectionsBypassValue = ListValue("AllDirectionsBypass", arrayOf("Rotate", "RotateSpoof", "Toggle", "Spoof", "SpamSprint", "NoStopSprint", "Minemora", "LimitSpeed", "None"), "None").displayable { allDirectionsValue.get() }
    private val allDirectionsLimitSpeedGround = BoolValue("AllDirectionsLimitSpeedOnlyGround", true)
    private val allDirectionsLimitSpeedValue = FloatValue("AllDirectionsLimitSpeed", 0.7f, 0.5f, 1f).displayable { allDirectionsBypassValue.displayable && allDirectionsBypassValue.equals("LimitSpeed") }
    private val noPacketValue = BoolValue("NoPackets", false)
    var switchStat = false
    var forceSprint = false
    
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (allDirectionsValue.get()) {
            when(allDirectionsBypassValue.get()) {
                "NoStopSprint" -> {
                    forceSprint = true
                }
                "SpamSprint" -> {
                    forceSprint = true
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                }
                "Spoof" -> {
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                    switchStat = true
                }
            }
            if (RotationUtils.getRotationDifference(Rotation(MovementUtils.movingYaw, mc.thePlayer.rotationPitch), Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)) > 30) {
                when(allDirectionsBypassValue.get()) {
                    "Rotate" -> RotationUtils.setTargetRotation(Rotation(MovementUtils.movingYaw, mc.thePlayer.rotationPitch), 2)
                    "RotateSpoof" -> {
                        switchStat = !switchStat
                        if (switchStat) {
                            RotationUtils.setTargetRotation(Rotation(MovementUtils.movingYaw, mc.thePlayer.rotationPitch))
                        }
                    }
                    "Toggle" -> {
                        mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING))
                        mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                    }
                    "Minemora" -> {
                        if (mc.thePlayer.onGround && RotationUtils.getRotationDifference(Rotation(MovementUtils.movingYaw, mc.thePlayer.rotationPitch)) > 60) {
                            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.0000013, mc.thePlayer.posZ)
                            mc.thePlayer.motionY = 0.0
                        }
                    }
                    "LimitSpeed" -> {
                        if (!allDirectionsLimitSpeedGround.get() || mc.thePlayer.onGround) {
                            MovementUtils.limitSpeedByPercent(allDirectionsLimitSpeedValue.get())
                        }
                    }
                }
            }
        } else {
            switchStat = false
            forceSprint = false
        }
    }
    
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C0BPacketEntityAction) {
            if (allDirectionsValue.get()) {
                when(allDirectionsBypassValue.get()) {
                    "SpamSprint", "NoStopSprint" -> {
                        if (packet.action == C0BPacketEntityAction.Action.STOP_SPRINTING) {
                            event.cancelEvent()
                        }
                    }
                    "Toggle" -> {
                        if (switchStat) {
                            if (packet.action == C0BPacketEntityAction.Action.STOP_SPRINTING) {
                                event.cancelEvent()
                            } else {
                                switchStat = !switchStat
                            }
                        } else {
                            if (packet.action == C0BPacketEntityAction.Action.START_SPRINTING) {
                                event.cancelEvent()
                            } else {
                                switchStat = !switchStat
                            }
                        }
                    }
                    "Spoof" -> {
                        if (switchStat) {
                            if (packet.action == C0BPacketEntityAction.Action.STOP_SPRINTING || packet.action == C0BPacketEntityAction.Action.START_SPRINTING) {
                                event.cancelEvent()
                            }
                        }
                    }
                }
            }
            if (noPacketValue.get() && !event.isCancelled) {
                if (packet.action == C0BPacketEntityAction.Action.STOP_SPRINTING || packet.action == C0BPacketEntityAction.Action.START_SPRINTING) {
                    event.cancelEvent()
                }
            }
        }
    }
}
