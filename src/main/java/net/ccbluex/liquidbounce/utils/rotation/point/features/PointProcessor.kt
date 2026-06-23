/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.rotation.point.features

import net.ccbluex.liquidbounce.config.ToggleableValueGroup
import net.ccbluex.liquidbounce.utils.rotation.point.PointInsideBox

abstract class PointProcessor(name: String, enabled: Boolean) : ToggleableValueGroup(name, enabled) {
    abstract fun process(point: PointInsideBox): PointInsideBox
}
