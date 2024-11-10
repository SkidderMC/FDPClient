/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.FDPClient.discordRPC
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.boolean

object DiscordRPCModule : Module("DiscordRPC", Category.CLIENT, hideModule = false) {

    val showServerValue = boolean("ShowServer", false)
    val showNameValue = boolean("ShowName", true)
    val showHealthValue = boolean("ShowHealth", false)
    val showOtherValue = boolean("ShowOther", false)
    val showModuleValue = boolean("ShowModule", false)
    val animated = boolean("ShouldAnimate", true)

    override fun onEnable() {
        discordRPC.run()
    }

    override fun onDisable() {
        discordRPC.stop()
    }
}
