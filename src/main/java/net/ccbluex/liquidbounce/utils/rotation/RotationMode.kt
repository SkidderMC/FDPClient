/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.rotation

enum class RotationActionTiming {
    INSTANT,
    POST_MOVE
}

/** Defines packet-safe action ordering without coupling a module to the rotation executor. */
data class RotationMode(
    val timing: RotationActionTiming = RotationActionTiming.POST_MOVE,
    val aimAfterAction: Boolean = false
) {
    init {
        require(!aimAfterAction || timing == RotationActionTiming.INSTANT) {
            "Aim-after-action is only valid for instant actions"
        }
    }

    /** Returns whether the aim request was accepted. */
    fun execute(aim: () -> Boolean, action: () -> Unit): Boolean = when (timing) {
        RotationActionTiming.INSTANT -> {
            if (aimAfterAction) {
                action()
                aim()
            } else {
                val accepted = aim()
                if (accepted) action()
                accepted
            }
        }

        RotationActionTiming.POST_MOVE -> {
            val accepted = aim()
            if (accepted) PostRotationExecutor.runPostMove(action)
            accepted
        }
    }
}
