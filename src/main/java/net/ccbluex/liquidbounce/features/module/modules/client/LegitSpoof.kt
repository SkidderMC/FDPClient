package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.value.BoolValue
import org.lwjgl.opengl.Display

@ModuleInfo(name = "LegitSpoof", description = "Hack? No im legit.", category = ModuleCategory.CLIENT)
class LegitSpoof : Module() {
    val render=BoolValue("Render",true)
    private val title=BoolValue("Title",true)

    override fun onEnable() {
        if(title.get()){
            Display.setTitle("Minecraft 1.8.9")
        }
    }

    override fun onDisable() {
        ClientUtils.setTitle()
    }
}