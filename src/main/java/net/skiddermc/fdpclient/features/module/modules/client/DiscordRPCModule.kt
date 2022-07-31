/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.features.module.modules.client

import net.skiddermc.fdpclient.features.module.Module
import net.skiddermc.fdpclient.features.module.ModuleCategory
import net.skiddermc.fdpclient.features.module.ModuleInfo
import net.skiddermc.fdpclient.features.special.DiscordRPC
import net.skiddermc.fdpclient.value.BoolValue

@ModuleInfo(name = "DiscordRPC", category = ModuleCategory.CLIENT)
class DiscordRPCPack : Module() {
    val showserver = BoolValue("ShowServer", false)
    override fun onEnable() {
        DiscordRPC.run()
    }

    override fun onDisable() {
        DiscordRPC.stop()
    }
}
