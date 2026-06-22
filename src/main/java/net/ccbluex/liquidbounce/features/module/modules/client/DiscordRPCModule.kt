/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.FDPClient.discordRPC
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
object DiscordRPCModule : Module("DiscordRPC", Category.CLIENT, Category.SubCategory.CLIENT_GENERAL) {

    val showServerValue by boolean("ShowServer", false)
        .describe("Show the current server in Discord status.")
    val showNameValue by boolean("ShowName", true)
        .describe("Show your username in Discord status.")
    val showHealthValue by boolean("ShowHealth", false)
        .describe("Show your health in Discord status.")
    val showOtherValue by boolean("ShowOther", false)
        .describe("Show extra info in Discord status.")
    val showModuleValue by boolean("ShowModule", false)
        .describe("Show enabled modules in Discord status.")
    val animated by boolean("ShouldAnimate", true)
        .describe("Animate the Discord status presence.")

    override fun onEnable() {
        discordRPC.run()
    }

    override fun onDisable() {
        discordRPC.stop()
    }
}
