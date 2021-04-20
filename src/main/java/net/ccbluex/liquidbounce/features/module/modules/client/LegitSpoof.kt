package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
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
        Display.setTitle(LiquidBounce.CLIENT_NAME + " " + LiquidBounce.CLIENT_VERSION + " | " + LiquidBounce.MINECRAFT_VERSION + if (LiquidBounce.IN_DEV) " | DEVELOPMENT BUILD" else "")
    }
}