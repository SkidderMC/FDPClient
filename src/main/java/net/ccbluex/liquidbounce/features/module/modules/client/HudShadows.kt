/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue

@ModuleInfo(name = "HudShadows", category = ModuleCategory.CLIENT, canEnable = false)
object HudShadows : Module() {

    val buttonShadowValue = BoolValue("ButtonShadow", true)
    val UiShadowValue = ListValue("UiShadowMode", arrayOf("TextureShadows", "TextureGlow", "None"), "TextureShadows")

}
