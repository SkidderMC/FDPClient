package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.*
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import kotlin.math.cos
import kotlin.math.sin

class MedusaLongjump : LongJumpMode("Medusa") {
    private val boostValue = FloatValue("${valuePrefix}Boost", 2.0f, 0.6f, 2.5f)
    private val motionYValue = FloatValue("${valuePrefix}MotionY", 0.625f, 0.8f, 0.42f)
    private val bTimerValue = FloatValue("${valuePrefix}BoostTimer", 0.2f, 0.05f, 0.4f)
    private val onlyDamageValue = BoolValue("${valuePrefix}OnlyDamage", true)
    private val motionResetValue = BoolValue("${valuePrefix}MotionReset", true)
    var canBoost = false
    var boosting = false
    var firstEnable = false
    var skipDetect = false
    var flagReveiced = false
    var coDynamic = "Medusa LongJump Bypass - by Co Dynamic 2022 12 22"
    
    override fun onEnable() {
        mc.timer.timerSpeed = 1.0f
        canBoost = false
        boosting = false
        firstEnable = true
        skipDetect = false
    }
    
    override fun onDisable() {
        MovementUtils.resetMotion(false)
        mc.timer.timerSpeed = 1.0f
    }
    
    override fun onUpdate(event: UpdateEvent) {
        if (canBoost && boosting && mc.timer.timerSpeed < 1.0f && flagReveiced) {
            skipDetect = true
            mc.thePlayer.jump()
            mc.thePlayer.onGround = true
            mc.timer.timerSpeed = 1.0f
            mc.thePlayer.motionY = motionYValue.get().toDouble()
            MovementUtils.strafe(boostValue.get())
            longjump.airTick = 999
            firstEnable = false
            boosting = false
            canBoost = false
            longjump.noTimerModify = false
        }else if (mc.thePlayer.onGround && motionResetValue.get()) {
            MovementUtils.resetMotion(false)
            //more convenient operation, but sometimes will flag a little bit (not so important)
        }
    }
    
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S08PacketPlayerPosLook) {
            flagReveiced = true
        }
        if (packet is C03PacketPlayer && canBoost) {
            packet.onGround = true
        }
        if (packet is S12PacketEntityVelocity) {
            if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer) return
            event.cancelEvent()
        }
    }
    
    override fun onAttemptDisable() {
        if (!firstEnable) {
            longjump.state = false
        } else {
            longjump.airTick = -1
        }
    }
    
    override fun onAttemptJump() {
        if (onlyDamageValue.get() && mc.thePlayer.hurtTime == 0)
            return
        mc.thePlayer.jump()
    }
    
    override fun onJump(event: JumpEvent) {
        if (skipDetect) {
            skipDetect = false
            return
        }
        event.cancelEvent()
        if ((mc.thePlayer.hurtTime > 0 || !onlyDamageValue.get()) && !canBoost && mc.thePlayer.onGround && longjump.airTick < 100) {
            flagReveiced = false
            canBoost = true
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ, false))
            mc.thePlayer.onGround = false
            MovementUtils.resetMotion(true)
            mc.thePlayer.jumpMovementFactor = 0.0f
            longjump.airTick = -1
            mc.timer.timerSpeed = bTimerValue.get()
            longjump.noTimerModify = true
            boosting = true
        }
    }
}
