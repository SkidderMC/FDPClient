/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.movement

enum class MovementCorrection(val tag: String) {

    OFF("Off"),
    STRICT("Strict"),
    SILENT("Silent"),
    CHANGE_LOOK("ChangeLook");

    val correctsMovement: Boolean
        get() = this != OFF

    val tweaksInput: Boolean
        get() = this == SILENT

    val changesLook: Boolean
        get() = this == CHANGE_LOOK

    fun baseYaw(clientYaw: Float, serverYaw: Float): Float = when (this) {
        OFF, CHANGE_LOOK -> clientYaw
        STRICT, SILENT -> serverYaw
    }

    fun correctedMovementYaw(input: DirectionalInput, clientYaw: Float, serverYaw: Float): Float =
        input.movementYaw(baseYaw(clientYaw, serverYaw))

    companion object {
        @JvmStatic
        fun fromTag(tag: String): MovementCorrection =
            values().firstOrNull { it.tag.equals(tag, ignoreCase = true) } ?: OFF
    }
}
