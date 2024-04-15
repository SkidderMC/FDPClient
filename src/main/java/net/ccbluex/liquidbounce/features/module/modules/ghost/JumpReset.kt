/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.ghost

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.minecraft.client.settings.GameSettings

@ModuleInfo(name = "JumpReset", category = ModuleCategory.GHOST)
class JumpReset : Module() {

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.hurtTime >= 8) {
            mc.gameSettings.keyBindJump.pressed = true
        }
        if (mc.thePlayer.hurtTime >= 7) {
            mc.gameSettings.keyBindForward.pressed = true
        } else if (mc.thePlayer.hurtTime >= 4) {
            mc.gameSettings.keyBindJump.pressed = false
            mc.gameSettings.keyBindForward.pressed = false
        } else {
            mc.gameSettings.keyBindForward.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindForward)
            mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        }
    }
}
