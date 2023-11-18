/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.special.DiscordRPC
import net.ccbluex.liquidbounce.features.value.*

@ModuleInfo(name = "DiscordRPC", category = ModuleCategory.CLIENT)
class DiscordRPCModule : Module() {

    val showServerValue = BoolValue("ShowServer", false)
    val showNameValue = BoolValue("ShowName", false)
    val showHealthValue = BoolValue("ShowHealth", true)
    val showOtherValue = BoolValue("ShowOther", false)
    val showModuleValue = BoolValue("ShowModule", false)
    val animated = BoolValue("ShouldAnimate?", true)

    override fun onEnable() {
        DiscordRPC.run()
    }

    override fun onDisable() {
        DiscordRPC.stop()
    }
}
