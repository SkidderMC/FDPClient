/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.animations

enum class Direction {
    FORWARDS,
    BACKWARDS;

    fun opposite(): Direction = if (this == FORWARDS) BACKWARDS else FORWARDS
}
