/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.utils.timing.MSTimer

object FastPlace : Module("FastPlace", Category.OTHER, Category.SubCategory.MISCELLANEOUS) {
    private val speedValue by int("Speed", 0, 0..4)
        .describe("Right-click delay timer (lower is faster).")
    private val cooldown by int("Cooldown", 0, 0..1000, " ms")
        .describe("Minimum cooldown between fast placements.")
    private val startDelay by int("StartDelay", 0, 0..1000, " ms")
        .describe("Delay after holding use before fast placing.")
    val onlyBlocks by boolean("OnlyBlocks", true)
        .describe("Only speed up placing actual blocks.")
    val facingBlocks by boolean("OnlyWhenFacingBlocks", true)
        .describe("Only speed up while aiming at a block.")

    // Vanilla place-rate timer value applied after a successful right click
    private const val VANILLA_DELAY = 4

    private val cooldownTimer = MSTimer()
    private var heldSince = 0L

    // Resolved by the place-rate logic as the right-click delay timer (lower = faster, 0 = max speed)
    val speed: Int
        get() {
            val time = System.currentTimeMillis()

            if (mc.gameSettings.keyBindUseItem.isKeyDown) {
                if (heldSince == 0L) heldSince = time
            } else {
                heldSince = 0L
            }

            if (startDelay > 0 && (heldSince == 0L || time - heldSince < startDelay)) {
                return VANILLA_DELAY
            }

            if (cooldown > 0 && !cooldownTimer.hasTimePassed(cooldown)) {
                return VANILLA_DELAY
            }

            cooldownTimer.reset()
            return speedValue
        }
}
