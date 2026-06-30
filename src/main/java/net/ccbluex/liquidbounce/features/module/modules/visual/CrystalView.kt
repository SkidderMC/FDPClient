/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

/** Controls the scale, offset, spin and bounce of end-crystal models. */
object CrystalView : Module(
    "CrystalView",
    Category.VISUAL,
    Category.SubCategory.RENDER_SELF,
    gameDetecting = false,
) {
    val size by float("Size", 1f, 0.1f..1.5f)
        .describe("Scale applied to the entire crystal model.")
    val yTranslate by float("YTranslate", 0f, -2f..2f)
        .describe("Vertical model offset.")
    val spinSpeed by float("SpinSpeed", 3f, 0f..5f)
        .describe("Crystal rotation speed; vanilla is 3.")
    val bounce by float("Bounce", 1f, -1f..1f)
        .describe("Multiplier applied to the vanilla bounce animation.")
}
