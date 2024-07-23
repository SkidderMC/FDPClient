/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.FDPClient.discordRPC
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.BoolValue

object DiscordRPCModule : Module("DiscordRPC", Category.CLIENT, hideModule = false) {

    val showServerValue = BoolValue("ShowServer", false)
    val showNameValue = BoolValue("ShowName", true)
    val showHealthValue = BoolValue("ShowHealth", false)
    val showOtherValue = BoolValue("ShowOther", false)
    val showModuleValue = BoolValue("ShowModule", false)
    val animated = BoolValue("ShouldAnimate", true)

    override fun onEnable() {
        discordRPC.run()
    }

    override fun onDisable() {
        discordRPC.stop()
    }
}
