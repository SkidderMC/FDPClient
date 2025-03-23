/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

object CustomModel : Module("CustomModel", Category.VISUAL) {
    val mode by choices("Mode", arrayOf("Imposter", "Rabbit", "Freddy", "Female", "None"), "Imposter")
    val rotatePlayer by boolean("RotatePlayer", false)

    val breastPhysics by boolean("BreastPhysics", true) { mode == "Female" }
    val breastGravity by float("BreastGravity", 0.25f, 0.1f..0.5f) { mode == "Female" }
    val breastBounce by float("BreastBounce", 0.6f, 0.1f..1.0f) { mode == "Female" }
    val breastRotation by float("BreastRotation", 0f, 0f..10f) { mode == "Female" }
    val breastNoArmor by boolean("NoRenderInArmor", false) { mode == "Female" }

    override val tag: String
        get() = mode
}