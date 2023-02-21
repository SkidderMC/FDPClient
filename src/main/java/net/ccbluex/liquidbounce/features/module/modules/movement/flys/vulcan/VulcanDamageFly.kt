package net.ccbluex.liquidbounce.features.module.modules.movement.flys.vulcan

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.minecraft.client.settings.GameSettings
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.client.C0FPacketConfirmTransaction
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import kotlin.math.sqrt

class VulcanDamageFly : FlyMode("VulcanDamage") {
    private val onlyDamageValue = BoolValue("${valuePrefix}OnlyDamage", true)
    private val selfDamageValue = BoolValue("${valuePrefix}SelfDamage", true)
    private val vanillaValue = BoolValue("${valuePrefix}Vanilla", false)
    private val flyTimerValue = FloatValue("${valuePrefix}Timer", 0.05f, 0.02f, 0.15f).displayable{ !vanillaValue.get() }
    private var waitFlag = false
    private var isStarted = false
    var isDamaged = false
    var dmgJumpCount = 0
    var flyTicks = 0
    
    private var lastSentX = 0.0
    private var lastSentY = 0.0
    private var lastSentZ = 0.0
    
    private var lastTickX = 0.0
    private var lastTickY = 0.0
    private var lastTickZ = 0.0
    
    fun runSelfDamageCore(): Boolean {
        mc.timer.timerSpeed = 1.0f
        if (!onlyDamageValue.get() || !selfDamageValue.get()) {
            if (onlyDamageValue.get()) {
                if (mc.thePlayer.hurtTime > 0 || isDamaged) {
                    isDamaged = true
                    dmgJumpCount = 999
                    return false
                }else {
                    return true
                }
            }
            isDamaged = true
            dmgJumpCount = 999
            return false
        }
        if (isDamaged) {
            dmgJumpCount = 999
            return false
        }
        mc.thePlayer.jumpMovementFactor = 0.00f
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

    override fun onEnable() {
        flyTicks = 0
        waitFlag = false
        isStarted = false
        isDamaged = false
        dmgJumpCount = 0
        mc.timer.timerSpeed = 1.0f
        runSelfDamageCore()
    }

    override fun onUpdate(event: UpdateEvent) {
        if (runSelfDamageCore()) {
            return
        }
        mc.thePlayer.jumpMovementFactor = 0.00f
        if (!isStarted && !waitFlag) {
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.0784, mc.thePlayer.posZ, false))
            waitFlag = true
        }
        if (isStarted) {
            if (vanillaValue.get()) {
                mc.timer.timerSpeed = 1.0f
                if (!mc.gameSettings.keyBindSneak.isKeyDown) {
                    MovementUtils.resetMotion(true)
                    if (mc.gameSettings.keyBindJump.isKeyDown) {
                        mc.thePlayer.motionY = 0.42
                    }
                }
            } else {
                mc.timer.timerSpeed = flyTimerValue.get()
            }
            flyTicks++
            if (flyTicks > 8) {
                fly.state = false
            }
            MovementUtils.strafe(if (vanillaValue.get()) { 0.96f } else { 9.8f + flyTicks.toFloat() * 0.05f })
        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1.0f
        MovementUtils.resetMotion(true)
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C03PacketPlayer && waitFlag) {
            event.cancelEvent()
        }
        if (packet is C03PacketPlayer && (dmgJumpCount < 4 && selfDamageValue.get())) {
            packet.onGround = false
        }
        if (isStarted && vanillaValue.get()) {
            if(packet is C03PacketPlayer && (packet is C04PacketPlayerPosition || packet is C06PacketPlayerPosLook)) {
                val deltaX = packet.x - lastSentX
                val deltaY = packet.y - lastSentY
                val deltaZ = packet.z - lastSentZ

                if (sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) > 9.0) {
                    PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(lastTickX, lastTickY, lastTickZ, false))
                    lastSentX = lastTickX
                    lastSentY = lastTickY
                    lastSentZ = lastTickZ
                }
                lastTickX = packet.x
                lastTickY = packet.y
                lastTickZ = packet.z
                event.cancelEvent()
            }else if(packet is C03PacketPlayer) {
                event.cancelEvent()
            }
        }
        if (packet is S08PacketPlayerPosLook && waitFlag && !vanillaValue.get()) {
            isStarted = true
            waitFlag = false
            mc.timer.timerSpeed = 1.0f
            flyTicks = 0
        }else if (packet is S08PacketPlayerPosLook && vanillaValue.get()) {
            lastSentX = packet.x
            lastSentY = packet.y
            lastSentZ = packet.z
            PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(packet.x, packet.y, packet.z, packet.yaw, packet.pitch, false))
            mc.thePlayer.setPosition(packet.x, packet.y, packet.z)
            event.cancelEvent()
            isStarted = true
            waitFlag = false
        }
        if (packet is C0FPacketConfirmTransaction) { //Make sure it works with Vulcan Combat Disabler
            val transUID = (packet.uid).toInt()
            if (transUID >= -31767 && transUID <= -30769) {
                event.cancelEvent()
                PacketUtils.sendPacketNoEvent(packet)
            }
        }
    }
}
