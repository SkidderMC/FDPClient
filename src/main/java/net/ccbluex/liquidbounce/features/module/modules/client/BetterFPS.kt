/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.client.mathalgo.*
import net.ccbluex.liquidbounce.value.ListValue

@ModuleInfo(name = "BetterFPS", category = ModuleCategory.CLIENT, array = false, canEnable = false)
class BetterFPS : Module() {
    // Math
    val libGDX = LibGDXMath()
    val rivensFull = RivensFullMath()
    val rivensHalf = RivensHalfMath()
    val rivens = RivensMath()
    val taylor = TaylorMath()
    val newMC = NewMCMath()

    val sinMode = ListValue("SinMode", arrayOf("Vanilla", "Taylor", "LibGDX", "RivensFull", "RivensHalf", "Rivens", "Java", "1.16"), "Vanilla")
    val cosMode = ListValue("CosMode", arrayOf("Vanilla", "Taylor", "LibGDX", "RivensFull", "RivensHalf", "Rivens", "Java", "1.16"), "Vanilla")

    fun sin(value: Float) = when (sinMode.get().lowercase()) {
        "taylor" -> taylor.sin(value)
        "libgdx" -> libGDX.sin(value)
        "rivensfull" -> rivensFull.sin(value)
        "rivenshalf" -> rivensHalf.sin(value)
        "rivens" -> rivens.sin(value)
        "java" -> kotlin.math.sin(value.toDouble()).toFloat()
        "1.16" -> newMC.sin(value)
        else -> null
    }

    fun cos(value: Float) = when (cosMode.get().lowercase()) {
        "taylor" -> taylor.cos(value)
        "libgdx" -> libGDX.cos(value)
        "rivensfull" -> rivensFull.cos(value)
        "rivenshalf" -> rivensHalf.cos(value)
        "rivens" -> rivens.cos(value)
        "java" -> kotlin.math.cos(value.toDouble()).toFloat()
        "1.16" -> newMC.cos(value)
        else -> null
    }
}
