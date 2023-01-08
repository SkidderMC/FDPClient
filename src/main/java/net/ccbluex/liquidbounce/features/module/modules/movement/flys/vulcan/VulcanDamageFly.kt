package net.ccbluex.liquidbounce.features.module.modules.movement.flys.vulcan

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.minecraft.client.settings.GameSettings
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import kotlin.math.sqrt

class VulcanDamageFly : FlyMode("VulcanDamage") {
    private var flag = false
    
    private var lastSentX = 0.0
    private var lastSentY = 0.0
    private var lastSentZ = 0.0

    override fun onEnable() {
        flag = false
        lastSentX = mc.thePlayer.posX
        lastSentY = mc.thePlayer.posY
        lastSentZ = mc.thePlayer.posZ
        if(mc.thePlayer.onGround && mc.thePlayer.hurtTime > 0) {
            mc.timer.timerSpeed = 0.3f
            PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 2 + Math.random() / 2, mc.thePlayer.posZ, false))
        } else {
            if (mc.thePlayer.hurtTime == 0 && mc.thePlayer.onGround) {
                ClientUtils.displayChatMessage("§8[§c§Vulcan-Dmg-Fly§8] §aGetting damage from other entities is required to bypass.")
            }else {
                ClientUtils.displayChatMessage("§8[§c§Vulcan-Dmg-Fly§8] §aYou need to stand on Ground.")
            }
            fly.state = false
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        if (!flag) {
            mc.timer.timerSpeed = 0.3f
        }else {
            mc.timer.timerSpeed = 1.0f
        }
        mc.gameSettings.keyBindJump.pressed = false
        mc.gameSettings.keyBindSneak.pressed = false
        fly.antiDesync = true
        MovementUtils.strafe((1.2 + Math.random() / 10).toFloat())
        if(GameSettings.isKeyDown(mc.gameSettings.keyBindJump)) {
            mc.thePlayer.motionY = 0.42
        } else if(GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
            mc.thePlayer.motionY = -0.42
        } else {
            mc.thePlayer.motionY = 0.0
        }
        if(!MovementUtils.isMoving()) {
            MovementUtils.resetMotion(false)
        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1.0f
        MovementUtils.resetMotion(true)
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if(packet is C03PacketPlayer && (packet is C04PacketPlayerPosition || packet is C06PacketPlayerPosLook)) {
            val deltaX = packet.x - lastSentX
            val deltaY = packet.y - lastSentY
            val deltaZ = packet.z - lastSentZ
            
            if (sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) > 8) {
                lastSentX = packet.x
                lastSentY = packet.y
                lastSentZ = packet.z
                return
            }
            event.cancelEvent()
        }else if(packet is C03PacketPlayer) {
            event.cancelEvent()
        }
        if(packet is S08PacketPlayerPosLook) {
            if (!flag) {
                flag = true
                val deltaX = packet.x - mc.thePlayer.posX
                val deltaY = packet.y - mc.thePlayer.posY
                val deltaZ = packet.z - mc.thePlayer.posZ

                if (sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) < 10) {
                    event.cancelEvent()
                    PacketUtils.sendPacketNoEvent(
                        C06PacketPlayerPosLook(
                            packet.x,
                            packet.y,
                            packet.z,
                            packet.getYaw(),
                            packet.getPitch(),
                            false
                        )
                    )
                }
            }
        }
    }
}
