/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.client.settings.GameSettings

object KeepTabList : Module("KeepTabList", Category.VISUAL, gameDetecting = false, hideModule = false) {
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer == null || mc.theWorld == null) return
        mc.gameSettings.keyBindPlayerList.pressed = true
    }
    override fun onDisable() {
        mc.gameSettings.keyBindPlayerList.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindPlayerList)
    }
}