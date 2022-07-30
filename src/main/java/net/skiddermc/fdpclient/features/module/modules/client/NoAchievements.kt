/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.features.module.modules.client

import net.skiddermc.fdpclient.event.EventTarget
import net.skiddermc.fdpclient.event.TickEvent
import net.skiddermc.fdpclient.features.module.Module
import net.skiddermc.fdpclient.features.module.ModuleCategory
import net.skiddermc.fdpclient.features.module.ModuleInfo

@ModuleInfo(name = "NoAchievements", category = ModuleCategory.CLIENT, array = false)
class NoAchievements : Module() {
    @EventTarget
    fun onTick(event: TickEvent) {
        mc.guiAchievement.clearAchievements()
    }
}
