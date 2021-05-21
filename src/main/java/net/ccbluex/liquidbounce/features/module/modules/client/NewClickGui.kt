package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo

@ModuleInfo(name = "NewClickGui", description = "clickgui.", category = ModuleCategory.CLIENT, canEnable = false)
class NewClickGui : Module() {
    override fun onEnable() {
        mc.displayGuiScreen(LiquidBounce.newClickGui)
    }
}