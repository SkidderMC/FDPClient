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

class VulcanLongjump : LongJumpMode("Vulcan") {
    private val repeatValue = IntegerValue("${valuePrefix}RepeatTimes", 2, 1, 6)
    private val distanceValue = FloatValue("${valuePrefix}Distance", 7.0f, 2.0f, 8.0f)
    private val onlyDamageValue = BoolValue("${valuePrefix}OnlyDamage", true)
    private val selfDamageValue = BoolValue("${valuePrefix}SelfDamage", true)
    var waitFlag = false
    var isFlagged = false
    var lastTickOnGround = false
    var isDamaged = false
    var dmgJumpCount = 0
    var coDynamic = "Vulcan LongJump Bypass - by Co Dynamic 2023 01 05"
    
    override fun onEnable() {
        longjump.noTimerModify = true
        if(!mc.thePlayer.onGround) {
            onAttemptDisable()
        }
        mc.timer.timerSpeed = 1.0f
        waitFlag = false
        isFlagged = false
        isDamaged = false
        dmgJumpCount = 0
        lastTickOnGround = mc.thePlayer.onGround
        runSelfDamageCore()
    }
    
    override fun onDisable() {
        longjump.noTimerModify = false
        MovementUtils.resetMotion(false)
        mc.timer.timerSpeed = 1.0f
    }
    
    override fun onUpdate(event: UpdateEvent) {
        if (runSelfDamageCore()) {
            return
        }
        if ((onlyDamageValue.get() && mc.thePlayer.hurtTime == 0) && !waitFlag && !isFlagged && longjump.airTick < 888) {
            mc.thePlayer.onGround = false
            MovementUtils.resetMotion(true)
            mc.thePlayer.jumpMovementFactor = 0.0f
            longjump.airTick = -1
            return
        }
        if (waitFlag && isFlagged) {
            mc.timer.timerSpeed = 0.5f
            MovementUtils.strafe(distanceValue.get())
            repeat(repeatValue.get()) {
                mc.thePlayer.setPosition(mc.thePlayer.posX + mc.thePlayer.motionX, mc.thePlayer.posY, mc.thePlayer.posZ + mc.thePlayer.motionZ)
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.0784, mc.thePlayer.posZ, true))
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
            }
            MovementUtils.resetMotion(false)
            waitFlag = false
            isFlagged = false
            longjump.airTick = 999
        }
        if (waitFlag && !isFlagged && mc.thePlayer.onGround) {
            if (!lastTickOnGround) {
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.0784, mc.thePlayer.posZ, true))
            }
            mc.timer.timerSpeed = 0.25f
            mc.thePlayer.onGround = false
            MovementUtils.resetMotion(true)
            mc.thePlayer.jumpMovementFactor = 0.0f
            lastTickOnGround = true
            return
        }
        lastTickOnGround = mc.thePlayer.onGround
    }
    
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S08PacketPlayerPosLook && waitFlag) {
            isFlagged = true
        }
        if (packet is S12PacketEntityVelocity) {
            if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer) return
            event.cancelEvent()
        }
        if (packet is C03PacketPlayer && dmgJumpCount < 4) {
            packet.onGround = false
        }
        if (packet is C0FPacketConfirmTransaction) { //Make sure it works with Vulcan Combat Disabler
            val transUID = (packet.uid).toInt()
            if (transUID >= -31767 && transUID <= -30769) {
                event.cancelEvent()
                PacketUtils.sendPacketNoEvent(packet)
            }
        }
    }
    
    override fun onAttemptDisable() {
        if (!waitFlag && !isFlagged) {
            longjump.state = false
        }
    }
    
    override fun onAttemptJump() {
        if (onlyDamageValue.get() && mc.thePlayer.hurtTime == 0)
            return
        mc.thePlayer.jump()
        MovementUtils.strafe(0.485f)
    }
    
    override fun onJump(event: JumpEvent) {
        waitFlag = true
        mc.timer.timerSpeed = 1.0f
        lastTickOnGround = true
    }
    
    fun runSelfDamageCore(): Boolean {
        if (!onlyDamageValue.get() || !selfDamageValue.get()) {
            isDamaged = true
            dmgJumpCount = 999
            return false
        }
        if (isDamaged) {
            dmgJumpCount = 999
            return false
        }
        longjump.airTick = -1
        mc.thePlayer.jumpMovementFactor = 0.0f
        if (mc.thePlayer.onGround) {
            if (dmgJumpCount >= 4) {
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
                isDamaged = true
                dmgJumpCount = 999
                return false
            }
            dmgJumpCount++
            MovementUtils.resetMotion(true)
            mc.thePlayer.jump()
        }
        MovementUtils.resetMotion(false)
        return true
    }
}
