/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C0BPacketEntityAction

@ModuleInfo(name = "SuperKnockback", category = ModuleCategory.COMBAT)
class SuperKnockback : Module() {
    private val hurtTimeValue = IntegerValue("HurtTime", 10, 0, 10)
    private val modeValue = ListValue("Mode", arrayOf("ExtraPacket", "WTap", "Packet"), "ExtraPacket")
    private val WtapDelay = IntegerValue("WTapDelay", 6, 1, 10)
    private val onlyMoveValue = BoolValue("OnlyMove", false)
    private val onlyGroundValue = BoolValue("OnlyGround", false)
    private val delayValue = IntegerValue("Delay", 0, 0, 500)
    
    private var ticks = 0

    val timer = MSTimer()

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (event.targetEntity is EntityLivingBase) {
            if (event.targetEntity.hurtTime > hurtTimeValue.get() || !timer.hasTimePassed(delayValue.get().toLong()) ||
                (!MovementUtils.isMoving() && onlyMoveValue.get()) || (!mc.thePlayer.onGround && onlyGroundValue.get())) {
                return
            }
            when (modeValue.get().lowercase()) {
                "extrapacket" -> {
                    if (mc.thePlayer.isSprinting) {
                        mc.thePlayer.isSprinting = true
                    }
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING))
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING))
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                    mc.thePlayer.serverSprintState = true
                }

                "wtap" -> {
                    ticks = 0
                }
                "packet" -> {
                    if (mc.thePlayer.isSprinting) {
                        mc.thePlayer.isSprinting = true
                    }
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING))
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                    mc.thePlayer.serverSprintState = true
                }
            }
            timer.reset()
        }
    }
    
    @EventTarget
    fun onUpdate() {
        ticks ++
        if (modeValue.equals("WTap")) {
            if (ticks <= WtapDelay.get()) {
                mc.gameSettings.keyBindForward.pressed = false
            } else if (ticks == WtapDelay.get() + 1) {
                mc.gameSettings.keyBindForward.pressed = true
            }
        }
    }
    
    @EventTarget
    fun onPreMotion() {
        if (modeValue.equals("WTap")) {
            if (ticks <= WtapDelay.get()) {
                mc.gameSettings.keyBindForward.pressed = false
            } else if (ticks == WtapDelay.get() + 1) {
                mc.gameSettings.keyBindForward.pressed = true
            }
        }
    }
                
    override val tag: String
        get() = modeValue.get()
}
