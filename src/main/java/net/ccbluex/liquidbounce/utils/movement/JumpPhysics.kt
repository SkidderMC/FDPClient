/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.movement

private const val VANILLA_GRAVITY = 0.08
private const val VANILLA_VERTICAL_DRAG = 0.98

fun nextVanillaAirMotionY(motionY: Double): Double =
    (motionY - VANILLA_GRAVITY) * VANILLA_VERTICAL_DRAG

/** Integrates vanilla 1.8 air physics until the upward part of a jump is exhausted. */
fun projectedVanillaJumpHeight(initialMotionY: Double): Double {
    require(initialMotionY.isFinite() && initialMotionY >= 0.0) {
        "Initial vertical motion must be finite and non-negative"
    }

    var verticalMotion = initialMotionY
    var height = 0.0
    while (verticalMotion > 0.0) {
        height += verticalMotion
        verticalMotion = nextVanillaAirMotionY(verticalMotion)
    }
    return height
}
