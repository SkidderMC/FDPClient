package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.ncp

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.minecraft.network.play.client.C03PacketPlayer

class NCPDamage2Longjump : LongJumpMode("NCPDamage2") {
 
    private var canBoost = false
    private var x = 0.0
    private var y = 0.0
    private var z = 0.0
    private var balance = 0
    private var damageStat = false
    private var hasJumped = false
    private var speed = 0.0f
    override fun onEnable() {
        hasJumped = false
        damageStat = false
        balance = 0
        LiquidBounce.hud.addNotification(Notification(longjump.name, "Wait for damage...", NotifyType.SUCCESS, jumpYPosArr.size * 4 * 50))
        x = mc.thePlayer.posX
        y = mc.thePlayer.posY
        z = mc.thePlayer.posZ
        speed = 1.2f
    }
    override fun onUpdate(event: UpdateEvent) {
        if (!damageStat) {
            mc.thePlayer.setPosition(x, y, z)
            if (balance > jumpYPosArr.size * 4) {
                repeat(4) {
                    jumpYPosArr.forEach {
                        PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(x, y + it, z, false))
                    }
                    PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(x, y, z, false))
                }
                PacketUtils.sendPacketNoEvent(C03PacketPlayer(true))
                damageStat = true
            }
        } else if (!hasJumped) {
            MovementUtils.strafe(0.278f * speed)
            speed -= 0.001f
            mc.thePlayer.jump()
            mc.thePlayer.motionY = 0.419999
            hasJumped = true
        }
        if(longjump.autoDisableValue.get() && hasJumped) {
            longjump.state = false
        }
    }

    override fun onJump(event: JumpEvent) {
        canBoost = true
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer) {
            if (!damageStat) {
                balance++
                event.cancelEvent()
            }
        }
    }
}
