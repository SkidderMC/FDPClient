/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.special.discord.DiscordRPC
import me.zywl.fdpclient.value.impl.BoolValue

@ModuleInfo(name = "DiscordRPC", category = ModuleCategory.CLIENT, defaultOn = true)
class DiscordRPCModule : Module() {

    val showServerValue = BoolValue("ShowServer", true)
    val showNameValue = BoolValue("ShowName", true)
    val showHealthValue = BoolValue("ShowHealth", false)
    val showOtherValue = BoolValue("ShowOther", false)
    val showModuleValue = BoolValue("ShowModule", false)
    val animated = BoolValue("ShouldAnimate?", true)
    val gato = BoolValue("BreadRetard", false)

    override fun onEnable() {
        DiscordRPC.run()
    }

    override fun onDisable() {
        DiscordRPC.stop()
    }
}
