package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C0BPacketEntityAction

@ModuleInfo(name = "SuperKnockback", category = ModuleCategory.COMBAT)
class SuperKnockback : Module() {
    private val hurtTimeValue = IntegerValue("HurtTime", 10, 0, 10)
    private val modeValue = ListValue("Mode", arrayOf("WTap", "Resprint", "Packet"), "Packet")
    private val packetAmount = IntegerValue("PacketAmount", 1,1,5).displayable { modeValue.equals("Packet") }
    private val wtapMode = ListValue("WTapMode", arrayOf("Spam","WTap","STap","ShiftTap"), "WTap").displayable { modeValue.equals("WTap") }
    private val wtapDistance = FloatValue("WTapDistance", 3.2f, 2.9f, 3.5f).displayable { modeValue.equals("WTap") }
    private val wtapShift = BoolValue("WTapShift", false).displayable { !modeValue.equals("Spam") && modeValue.equals("WTap")}
    private val onlyMoveValue = BoolValue("OnlyMove", false)
    private val onlyGroundValue = BoolValue("OnlyGround", false)
    private val delayValue = IntegerValue("Delay", 0, 0, 500)

    val timer = MSTimer()
    private val hitTimer = MSTimer()
    private var tapped = false

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (event.targetEntity is EntityLivingBase) {
            if (hitTimer.hasTimePassed(490.toLong())) {
                hitTimer.reset()
                tapped = false   
            }
            if (event.targetEntity.hurtTime > hurtTimeValue.get() || !timer.hasTimePassed(delayValue.get().toLong())  ||
                (!MovementUtils.isMoving() && onlyMoveValue.get()) || (!mc.thePlayer.onGround && onlyGroundValue.get())) {
                return
            }
            when (modeValue.get().lowercase()) {

                "resprint" -> {
                    if (mc.thePlayer.isSprinting) {
                        mc.thePlayer.isSprinting = false
                    }
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                    mc.thePlayer.serverSprintState = true
                }
                "packet" -> {
                    if (mc.thePlayer.isSprinting) {
                        mc.thePlayer.isSprinting = true
                    }
                    repeat(packetAmount.get()) {
                        mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING))
                        mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                    }
                    mc.thePlayer.serverSprintState = true
                }
            }
            timer.reset()
        }
    }
    
    fun onUpdate() {
        val entity = LiquidBounce.combatManager.target
        if (entity != null) {
            if (entity.hurtTime > hurtTimeValue.get() || !timer.hasTimePassed(delayValue.get().toLong()) && !modeValue.equals("WTap") || (!mc.thePlayer.onGround && onlyGroundValue.get())) {
                return
            }
            
            when (modeValue.get().lowercase()) {
                
                "wtap" -> {
                    when (wtapMode.get().lowercase()) {
                        "wtap" -> {
                           if (!tapped) {
                                mc.gameSettings.keyBindForward.pressed = false
                                if (wtapShift.get()) {
                                    mc.gameSettings.keyBindSneak.pressed = true
                                }
                                if (mc.thePlayer.getDistanceToEntity(entity) > wtapDistance.get()) {
                                    if (Math.random() > 0.6) {
                                        tapped = true
                                    }
                                }
                            } else {
                               mc.gameSettings.keyBindForward.pressed = true
                               if (wtapShift.get()) {
                                    mc.gameSettings.keyBindSneak.pressed = false
                               }
                            }
                        }
                        
                        "stap" -> {
                           if (!tapped) {
                                mc.gameSettings.keyBindForward.pressed = false
                                mc.gameSettings.keyBindBack.pressed = true
                                if (wtapShift.get()) {
                                    mc.gameSettings.keyBindSneak.pressed = true
                                }
                                if (mc.thePlayer.getDistanceToEntity(entity) > wtapDistance.get()) {
                                    if (Math.random() > 0.6) {
                                        tapped = true
                                    }
                                }
                            } else {
                               mc.gameSettings.keyBindForward.pressed = true
                               mc.gameSettings.keyBindBack.pressed = false
                               if (wtapShift.get()) {
                                    mc.gameSettings.keyBindSneak.pressed = false
                               }
                            }
                        }
                        
                        "shifttap" -> {
                           if (!tapped) {
                                mc.gameSettings.keyBindSneak.pressed = true
                                if (mc.thePlayer.getDistanceToEntity(entity) > wtapDistance.get()) {
                                    if (Math.random() > 0.6) {
                                        tapped = true
                                    }
                                }
                            } else {
                                mc.gameSettings.keyBindSneak.pressed = false
                            }
                        }
                        
                        "spam" -> {
                            if (mc.thePlayer.getDistanceToEntity(entity) > wtapDistance.get()) {
                                mc.gameSettings.keyBindForward.pressed = true
                            } else {
                                mc.gameSettings.keyBindForward.pressed = false
                            }
                        }
                        
                        
                    }
                }
                
            }
        }
        
    }
    override val tag: String
        get() = modeValue.get()
}
