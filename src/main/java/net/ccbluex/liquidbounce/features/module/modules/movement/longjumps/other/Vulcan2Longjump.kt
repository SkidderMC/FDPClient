package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C0FPacketConfirmTransaction
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity
import kotlin.math.cos
import kotlin.math.sin

class Vulcan2Longjump : LongJumpMode("Vulcan2") {
    private val repeatValue = IntegerValue("${valuePrefix}RepeatTimes", 2, 1, 6)
    private val distanceValue = FloatValue("${valuePrefix}Distance", 7.0f, 2.0f, 8.0f)
    private val onlyDamageValue = BoolValue("${valuePrefix}OnlyDamage", true)
    private val selfDamageValue = BoolValue("${valuePrefix}SelfDamage", true)
    private var ticks = 0
    private var cancelFlag = false
    var coDynamic = "Vulcan LongJump Bypass - by Alan wood 69"
    
    override fun onEnable() {
        longjump.noTimerModify = true
        if(!mc.thePlayer.onGround) {
            onAttemptDisable()
        }
        mc.timer.timerSpeed = 0.5f
        ticks = 0
    }
    
    override fun onDisable() {
        longjump.noTimerModify = false
        MovementUtils.resetMotion(false)
        mc.timer.timerSpeed = 1.0f
    }
    
    override fun onUpdate(event: UpdateEvent) {
        mc.timer.timerSpeed = 0.5f
        if (mc.thePlayer.fallDistance > 0 && ticks % 2 == 0 && mc.thePlayer.fallDistance < 2.2) 
            mc.thePlayer.motionY += 0.14
        
        ticks ++
        
        if (ticks == 0) {
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.0784, mc.thePlayer.posZ, true))
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false))
            MovementUtils.strafe(7.9f)
            mc.thePlayer.motionY = 0.41999998688698
            cancelFlag = true
            
        } else if (ticks == 1) {
            mc.thePlayer.motionY += 0.1
            MovementUtils.strafe(2.79f)
        } else if (ticks == 2) {
            MovementUtils.strafe(2.56f)
        } else if (ticks == 3) {
            MovementUtils.strafe(0.49f)
            mc.thePlayer.onGround = true
        } else if (ticks == 4) {
            MovementUtils.strafe(0.59f)
        } else if (ticks == 5) {
            MovementUtils.strafe(0.3f)
        }
            
    }
    
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S08PacketPlayerPosLook && cancelFlag) {
            cancelFlag = false
            event.cancelEvent()
        }

    }
    
    override fun onAttemptJump() {
        ticks = 0
    }
    

 
}
