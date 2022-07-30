/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.features.module.modules.client

import net.skiddermc.fdpclient.features.module.Module
import net.skiddermc.fdpclient.features.module.ModuleCategory
import net.skiddermc.fdpclient.features.module.ModuleInfo
import net.skiddermc.fdpclient.ui.client.hud.designer.GuiHudDesigner

@ModuleInfo(name = "HudDesigner", category = ModuleCategory.CLIENT, canEnable = false)
class HudDesigner : Module() {
    override fun onEnable() {
        mc.displayGuiScreen(GuiHudDesigner())
    }
}