/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.features.module.modules.render

import net.skiddermc.fdpclient.features.module.Module
import net.skiddermc.fdpclient.features.module.ModuleCategory
import net.skiddermc.fdpclient.features.module.ModuleInfo
import net.skiddermc.fdpclient.value.FloatValue

@ModuleInfo(name = "NoFOV", category = ModuleCategory.RENDER)
class NoFOV : Module() {
    val fovValue = FloatValue("FOV", 1f, 0f, 1.5f)
}
