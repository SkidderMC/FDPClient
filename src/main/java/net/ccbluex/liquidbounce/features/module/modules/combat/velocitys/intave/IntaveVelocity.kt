/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.intave

import me.zywl.fdpclient.event.PacketEvent
import me.zywl.fdpclient.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.minecraft.client.settings.GameSettings

class IntaveVelocity : VelocityMode("Intave") {

    private var jumped = 0

    override fun onUpdate(event: UpdateEvent) {
        if (mc.currentScreen != null) return
    }
    
    override fun onPacket(event: PacketEvent) {
        if (mc.thePlayer.hurtTime == 9) {
            if (++jumped % 2 == 0 && mc.thePlayer.onGround && mc.thePlayer.isSprinting && mc.currentScreen == null) {
                mc.gameSettings.keyBindJump.pressed = true
                jumped = 0 // reset
            }
        } else {
            mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        }
    }

}
