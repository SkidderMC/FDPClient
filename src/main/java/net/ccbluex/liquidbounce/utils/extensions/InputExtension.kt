/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.extensions

import net.minecraft.util.MovementInput

fun MovementInput.reset() {
    this.moveForward = 0f
    this.moveStrafe = 0f
    this.jump = false
    this.sneak = false
}

val MovementInput.isSideways
    get() = moveForward != 0f && moveStrafe != 0f
val MovementInput.isMoving
    get() = moveForward != 0f || moveStrafe != 0f