/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.boolean
import net.ccbluex.liquidbounce.value.float

object CameraView : Module("CameraView", Category.VISUAL, hideModule = false) {

    val fovValue by float("FOV", 1f, 0f.. 30f)
    val clipValue by boolean("CameraClip", false)
}