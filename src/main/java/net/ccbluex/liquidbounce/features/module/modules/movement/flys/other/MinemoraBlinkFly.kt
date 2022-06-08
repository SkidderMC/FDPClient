package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode

class MinemoraFly : FlyMode("Minemora") {
    private var tick = 0
    override fun onEnable() {
        tick = 0
        if(!LiquidBounce.moduleManager.getModule("Blink")!!.state) {
            LiquidBounce.moduleManager.getModule("Blink")!!.state = true
        }
    }
    override fun onDisable() {
        tick = 0
        LiquidBounce.moduleManager.getModule("Blink")!!.state = false
    }
    override fun onPreMotion() {
        tick++
        mc.timer.timerSpeed = 1.0f
        if (tick == 1) {
                mc.timer.timerSpeed = 0.25f
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 3.42f, mc.thePlayer.posZ, false))
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false))
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
                mc.thePlayer.jump()
            } else {
                if (mc.gameSettings.keyBindForward.pressed || mc.gameSettings.keyBindLeft.pressed || mc.gameSettings.keyBindRight.pressed || mc.gameSettings.keyBindBack.pressed) {
                    MovementUtils.strafe(1.7f)
                } else {
                    mc.thePlayer.motionZ = 0.0
                    mc.thePlayer.motionX = 0.0
                }
            if (mc.gameSettings.keyBindJump.pressed) {
                mc.thePlayer.motionY = 1.7
            } else if (mc.gameSettings.keyBindSneak.pressed) {
                mc.thePlayer.motionY = -1.7
            } else {
                mc.thePlayer.motionY = 0.0
            }
        }
    }
}
