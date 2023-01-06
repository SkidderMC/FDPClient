package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.matrix

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import kotlin.math.cos
import kotlin.math.sin

class VulcanLongjump : LongJumpMode("Vulcan") {
    private val repeatValue = IntegerValue("${valuePrefix}RepeatTimes", 2, 1, 3)
    private val distanceValue = FloatValue("${valuePrefix}Distance", 7.0f, 2.0f, 8.0f)
    private val onlyDamageValue = BoolValue("${valuePrefix}OnlyDamage", true)
    var waitFlag = false
    var isFlagged = false
    
    override fun onEnable() {
        if(!mc.thePlayer.onGround) {
            onAttemptDisable()
		}
    }
    
    override fun onDisable() {
        
    }
    
    override fun onUpdate(event: UpdateEvent) {
        if ((!onlyDamageValue.get() || mc.thePlayer.hurtTime > 0) && !waitFlag && !isFlagged) {
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
    }
    
    override fun onAttemptDisable() {
        if (!waitFlag && !isFlagged) {
            longjump.state = false
        }
    }
    
    override fun onAttemptJump() {
        if (!onlyDamageValue.get() || mc.thePlayer.hurtTime > 0)
            return
        mc.thePlayer.jump()
        MovementUtils.strafe(0.485)
    }
    
    override fun onJump(event: JumpEvent) {
        waitFlag = true
        lastTickOnGround = true
    }
}
