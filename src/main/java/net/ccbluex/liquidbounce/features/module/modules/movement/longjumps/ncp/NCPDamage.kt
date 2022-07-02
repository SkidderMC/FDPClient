package net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.ncp

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.movement.longjumps.LongJumpMode
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.*
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.value.*
import net.minecraft.network.play.client.*

class NCPDamage : LongJumpMode("NCPDamage") {
    private val ncpBoostValue = FloatValue("${valuePrefix}Boost", 4.25f, 1f, 10f)
    private val ncpdInstantValue = BoolValue("${valuePrefix}DamageInstant", false)
    private val jumpYPosArr = arrayOf(0.41999998688698, 0.7531999805212, 1.00133597911214, 1.16610926093821, 1.24918707874468, 1.24918707874468, 1.1707870772188, 1.0155550727022, 0.78502770378924, 0.4807108763317, 0.10408037809304, 0.0)
    private var canBoost = false
    private var x = 0.0
    private var y = 0.0
    private var z = 0.0
    private var balance = 0
    private var damageStat = false
    private var hasJumped = false
    override fun onEnable() {
        hasJumped = false
        damageStat = false
        if(ncpdInstantValue.get()) {
            balance = 114514
        } else {
            balance = 0
            LiquidBounce.hud.addNotification(Notification(longjump.name, "Wait for damage...", NotifyType.SUCCESS, jumpYPosArr.size * 4 * 50))
        }
        x = mc.thePlayer.posX
        y = mc.thePlayer.posY
        z = mc.thePlayer.posZ
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
            MovementUtils.strafe(0.50f * ncpBoostValue.get())
            mc.thePlayer.jump()
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