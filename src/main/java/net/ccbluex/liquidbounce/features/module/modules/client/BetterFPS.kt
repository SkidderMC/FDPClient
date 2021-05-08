/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.ListValue
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo

@ModuleInfo(name = "BetterFPS", description = "Make math calc faster.", category = ModuleCategory.CLIENT, array = false, canEnable = false)
class BetterFPS : Module() {
    val sinMode = ListValue("SinMode", arrayOf("Vanilla", "Taylor", "LibGDX", "RivensFull", "RivensHalf", "Rivens"), "Vanilla")
    val cosMode = ListValue("CosMode", arrayOf("Vanilla", "Taylor", "LibGDX", "RivensFull", "RivensHalf", "Rivens"), "Vanilla")
}