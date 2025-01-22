/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category

object ItemPhysics: Module("ItemPhysics", Category.VISUAL) {

    val realistic by boolean("Realistic", false)
    val weight by float("Weight", 0.5F, 0.1F..3F)
    val rotationSpeed by float("RotationSpeed", 1.0F, 0.01F..3F)

}