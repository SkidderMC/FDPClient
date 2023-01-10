/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.injection.forge.mixins.client.IMixinKeyBinding

@ModuleInfo(name = "SprintToggle", category = ModuleCategory.MOVEMENT)
class SprintToggle : Module() {
    var enabled = false
    override fun onEnable() {
        (mc.gameSettings.keyBindSprint as IMixinKeyBinding).setPressed(true)
        enabled = true
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        (mc.gameSettings.keyBindSprint as IMixinKeyBinding).setPressed(enabled)
    }

    override fun onDisable() {
        (mc.gameSettings.keyBindSprint as IMixinKeyBinding).setPressed(false)
        enabled = false
    }
}