/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.*

@ModuleInfo(name = "DiscordRPC", category = ModuleCategory.CLIENT)
class DiscordRPCModule : Module() {
    val drpcValue = ListValue("Mode", arrayOf("ShowServer", "ShowName", "ShowHealth"), "ShowServer")
    val animated = BoolValue("ShouldAnimate?", true)
}
