/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.movement

/**
 * A small, version-agnostic abstraction of the four directional movement keys.
 *
 * Build it from raw key state, from the player's `moveForward`/`moveStrafing` values, or
 * from one of the presets, then ask it for [isMoving], the [invert]ed input, or the world
 * [movementYaw] a given base yaw should travel along. Shared by every module that needs to
 * reason about movement input without re-deriving the same key math each time.
 *
 * Note: on 1.8.9 `moveStrafing > 0` means strafing **left**, which is why the
 * `(movementForward, movementSideways)` constructor maps `sideways > 0` to [left].
 */
data class DirectionalInput(
    val forwards: Boolean,
    val backwards: Boolean,
    val left: Boolean,
    val right: Boolean,
) {

    constructor(movementForward: Float, movementSideways: Float) : this(
        forwards = movementForward > 0.0f,
        backwards = movementForward < 0.0f,
        left = movementSideways > 0.0f,
        right = movementSideways < 0.0f,
    )

    fun invert() = DirectionalInput(
        forwards = backwards,
        backwards = forwards,
        left = right,
        right = left,
    )

    val isMoving: Boolean
        get() = forwards != backwards || left != right

    /**
     * The yaw (in degrees) the player would actually travel along given this input and a
     * [baseYaw] facing. Mirrors vanilla strafe geometry: diagonal inputs blend 90° and the
     * facing, backward inputs flip it. Returns [baseYaw] unchanged when no input is held.
     */
    fun movementYaw(baseYaw: Float): Float {
        if (!isMoving) {
            return baseYaw
        }

        var yaw = baseYaw
        var forward = 1.0f

        if (backwards && !forwards) {
            yaw += 180.0f
            forward = -0.5f
        } else if (forwards && !backwards) {
            forward = 0.5f
        }

        if (left && !right) {
            yaw -= 90.0f * forward
        } else if (right && !left) {
            yaw += 90.0f * forward
        }

        return yaw
    }

    companion object {
        @JvmField
        val NONE = DirectionalInput(forwards = false, backwards = false, left = false, right = false)

        @JvmField
        val FORWARDS = DirectionalInput(forwards = true, backwards = false, left = false, right = false)

        @JvmField
        val BACKWARDS = DirectionalInput(forwards = false, backwards = true, left = false, right = false)

        @JvmField
        val LEFT = DirectionalInput(forwards = false, backwards = false, left = true, right = false)

        @JvmField
        val RIGHT = DirectionalInput(forwards = false, backwards = false, left = false, right = true)

        @JvmField
        val FORWARDS_LEFT = DirectionalInput(forwards = true, backwards = false, left = true, right = false)

        @JvmField
        val FORWARDS_RIGHT = DirectionalInput(forwards = true, backwards = false, left = false, right = true)

        @JvmField
        val BACKWARDS_LEFT = DirectionalInput(forwards = false, backwards = true, left = true, right = false)

        @JvmField
        val BACKWARDS_RIGHT = DirectionalInput(forwards = false, backwards = true, left = false, right = true)
    }
}
