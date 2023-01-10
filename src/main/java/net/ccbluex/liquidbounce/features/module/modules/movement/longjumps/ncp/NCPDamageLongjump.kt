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
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.network.play.client.C03PacketPlayer

class NCPDamageLongjump : LongJumpMode("NCPDamage") {
    private val modeValue = ListValue("${valuePrefix}Mode", arrayOf("Normal", "OldHypixel"), "Normal")
    private val hypBoostValue = FloatValue("${valuePrefix}BoostSpeed", 1.2f, 1f, 2f).displayable { modeValue.equals("OldHypixel") }
    private val ncpBoostValue = FloatValue("${valuePrefix}Boost", 4.25f, 1f, 10f).displayable { modeValue.equals("Normal") }
    private val ncpdInstantValue = BoolValue("${valuePrefix}DamageInstant", false)
    private val jumpYPosArr = arrayOf(0.0, 0.41999998688698, 0.7531999805212, 1.00133597911214, 1.16610926093821, 1.24918707874468, 1.24918707874468, 1.1707870772188, 1.0155550727022, 0.78502770378924, 0.4807108763317, 0.10408037809304)
    private var canBoost = false
    private var x = 0.0
    private var y = 0.0
    private var z = 0.0
    private var boostSpeed = 1.2f
    private var balance = 0
    private var damageStat = false
    private var hasJumped = false
    override fun onEnable() {
        sendLegacy()
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
        boostSpeed = hypBoostValue.get()
    }
    override fun onUpdate(event: UpdateEvent) {
        if (!damageStat) {
            mc.thePlayer.setPosition(x, y, z)
            mc.thePlayer.onGround = false
            mc.thePlayer.motionY = 0.0
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
            mc.thePlayer.jumpMovementFactor = 0.0f
            if (balance >= jumpYPosArr.size * 3) {
                repeat(3) {
                    jumpYPosArr.forEach {
                        PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(x, y + it, z, false))
                    }
                }
                PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(x, y, z, true))
                damageStat = true
                mc.thePlayer.onGround = true
                longjump.airTick = 0
            }
        } else if (modeValue.equals("OldHypixel")) {
            mc.thePlayer.motionY += 0.0049
            if (longjump.airTick <= 10) {
                MovementUtils.strafe(0.278f * boostSpeed)
                boostSpeed -= 0.0008f + hypBoostValue.get() * 0.000167f
            }
        }
    }

    override fun onJump(event: JumpEvent) {
        if (modeValue.equals("Normal")) {
            MovementUtils.strafe(0.50f * ncpBoostValue.get())
        }
        longjump.airTick = 0
        hasJumped = true
    }
    
    override fun onAttemptJump() {
        if (damageStat && !hasJumped) {
            mc.thePlayer.jump()
        }
        if (modeValue.equals("OldHypixel")) {
            MovementUtils.strafe(0.472f + 0.08f * boostSpeed)
            mc.thePlayer.motionY = 0.419999
        }
    }
    
    override fun onAttemptDisable() {
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionZ = 0.0
        longjump.state = false
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
