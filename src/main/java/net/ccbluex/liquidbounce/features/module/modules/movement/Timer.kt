/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.handler.combat.CombatManager

object Timer : Module("Timer", Category.MOVEMENT, Category.SubCategory.MOVEMENT_EXTRAS, gameDetecting = false) {

    private val mode by choices("Mode", arrayOf("OnMove", "NoMove", "Always"), "OnMove")
        .describe("When the timer modification applies.")
    private val speed by float("Speed", 2F, 0.1F..10F)
        .describe("Game timer speed multiplier.")

    private val allowNegative by boolean("AllowNegative", true)
        .describe("Allow timer speeds below normal.")

    private val normalizeDuringCombat by boolean("NormalizeDuringCombat", false)
        .describe("Reset the timer to normal speed while in combat.")

    private val boostTicks by int("BoostTicks", 0, 0..200, "ticks")
        .describe("Ticks to apply the boost speed each cycle.")
    private val boostSpeed by float("BoostSpeed", 2F, 0.1F..10F) { boostTicks > 0 }
        .describe("Timer speed used during the boost phase.")
    private val normalTicks by int("NormalTicks", 20, 1..200, "ticks") { boostTicks > 0 }
        .describe("Ticks to apply the normal speed each cycle.")

    private var inBoost = false
    private var phaseTicks = 0

    private fun resetCycle() {
        inBoost = false
        phaseTicks = 0
    }

    private fun clampSpeed(value: Float) = if (allowNegative) value else value.coerceAtLeast(1F)

    override fun onEnable() {
        resetCycle()
    }

    override fun onDisable() {
        resetCycle()
        mc.timer.timerSpeed = 1F
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        if (normalizeDuringCombat && CombatManager.inCombatState) {
            resetCycle()
            mc.timer.timerSpeed = 1F
            return@handler
        }

        val active = mode == "Always" || mode == "OnMove" && player.isMoving ||
            mode == "NoMove" && !player.isMoving

        if (!active) {
            resetCycle()
            mc.timer.timerSpeed = 1F
            return@handler
        }

        if (boostTicks <= 0) {
            resetCycle()
            mc.timer.timerSpeed = clampSpeed(speed)
            return@handler
        }

        val limit = if (inBoost) boostTicks else normalTicks
        if (phaseTicks >= limit) {
            inBoost = !inBoost
            phaseTicks = 0
        }

        mc.timer.timerSpeed = clampSpeed(if (inBoost) boostSpeed else speed)
        phaseTicks++
    }

       val onWorld = handler<WorldEvent> { event ->
        if (event.worldClient != null)
            return@handler

        state = false
    }
}
