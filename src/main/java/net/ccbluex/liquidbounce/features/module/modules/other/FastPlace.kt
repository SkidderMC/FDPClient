/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category

object FastPlace : Module("FastPlace", Category.OTHER) {
    val speed by int("Speed", 0, 0..4)
    val onlyBlocks by boolean("OnlyBlocks", true)
    val facingBlocks by boolean("OnlyWhenFacingBlocks", true)
}
