/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.config.boolean
import net.ccbluex.liquidbounce.config.choices

object CustomModel : Module("CustomModel", Category.VISUAL, hideModule = false) {
    val mode by choices("Mode", arrayOf("Imposter", "Rabbit", "Freddy", "None"), "Imposter")

    val rotatePlayer by  boolean("RotatePlayer", false)

    override val tag: String
        get() = mode
}