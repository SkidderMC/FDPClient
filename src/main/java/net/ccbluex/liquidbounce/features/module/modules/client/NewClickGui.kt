package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.ListValue

@ModuleInfo(name = "NewClickGui", description = "clickgui test.", category = ModuleCategory.CLIENT, canEnable = false)
class NewClickGui : Module() {
    var modeValue= ListValue("Mode", LiquidBounce.clickGuiManager.getNameList(),LiquidBounce.clickGuiManager.getNameList()[0])

    override fun onEnable() {
        LiquidBounce.clickGuiManager.displayGui()
    }
}