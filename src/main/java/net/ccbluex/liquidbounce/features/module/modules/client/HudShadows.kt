package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue


@ModuleInfo(name = "HudShadows", category = ModuleCategory.CLIENT, canEnable = false)
object Modules : Module() {
// idk, dont qestion it! (hello xigua)
    val buttonShadowValue = BoolValue("ButtonShadow", false)
    val UiShadowValue = ListValue("UiShadowMode", arrayOf("TextureShadows", "TextureGlow", "None"), "TextureGlow")

}