/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.extensions.tryJump
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils
import net.ccbluex.liquidbounce.utils.timing.MSTimer

object JumpReset : Module("JumpReset", Category.MOVEMENT, Category.SubCategory.MOVEMENT_EXTRAS, gameDetecting = false) {

    private val mode by choices("Mode", arrayOf("Jump", "SprintReset", "Both"), "Jump")
        .describe("React to a hit by hopping, dropping sprint, or both to cut knockback.")
    private val onlyOnGround by boolean("OnlyOnGround", true)
        .describe("Only react while on the ground.")
    private val onlyWhileMoving by boolean("OnlyWhileMoving", true)
        .describe("Only react while moving.")
    private val hurtTimeTrigger by int("HurtTime", 9, 1..10)
        .describe("Hurt-time tick to react on (10 is the moment you are hit).")
    private val chance by int("Chance", 100, 0..100, "%")
        .describe("Chance to react to a hit.")
    private val delay by int("Delay", 0, 0..300, "ms")
        .describe("Minimum delay between reactions.")

    private val timer = MSTimer()

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        if (player.hurtTime != hurtTimeTrigger) return@handler
        if (onlyOnGround && !player.onGround) return@handler
        if (onlyWhileMoving && !player.isMoving) return@handler
        if (!timer.hasTimePassed(delay.toLong())) return@handler
        if (RandomUtils.nextInt(0, 100) >= chance) return@handler

        when (mode) {
            "Jump" -> player.tryJump()
            "SprintReset" -> player.isSprinting = false
            "Both" -> {
                player.isSprinting = false
                player.tryJump()
            }
        }

        timer.reset()
    }
}
