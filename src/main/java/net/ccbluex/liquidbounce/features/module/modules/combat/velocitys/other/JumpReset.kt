/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.other

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode

class JumpReset : VelocityMode("JumpReset") {
    private var start = 0
    override fun onPacket(event: PacketEvent) {
    while (mc.thePlayer.hurtTime >= 8) {
        mc.gameSettings.keyBindJump.pressed = true
        break
    }
    while (mc.thePlayer.hurtTime >= 7 && !mc.gameSettings.keyBindForward.pressed) {
        mc.gameSettings.keyBindForward.pressed = true
        start = 1
        break
    }
    if (mc.thePlayer.hurtTime in 1..6) {
        mc.gameSettings.keyBindJump.pressed = false
        if (start == 1) {
             mc.gameSettings.keyBindForward.pressed = false
             start = 0
            }
        }
    }
}