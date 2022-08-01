/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import org.lwjgl.input.Keyboard

@ModuleInfo(name = "KeyBindManager", category = ModuleCategory.CLIENT, keyBind = Keyboard.KEY_RMENU, canEnable = false)
class KeyBindManager : Module() {
    override fun onEnable() {
        mc.displayGuiScreen(LiquidBounce.keyBindManager)
    }
}