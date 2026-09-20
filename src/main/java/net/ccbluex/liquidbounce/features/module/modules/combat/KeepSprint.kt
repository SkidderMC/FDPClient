/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils
import net.ccbluex.liquidbounce.event.handler

object KeepSprint : Module("KeepSprint", Category.COMBAT, Category.SubCategory.COMBAT_LEGIT) {


    val sprintMode by choices("SprintMode", arrayOf("MotionSprint", "Normal", "Timer"), "MotionSprint")
        .describe("Method of keeping sprint / speed after attacking.")

    private val chance by int("Chance", 100, 0..100, "%")
        .describe("Chance to keep sprint motion after an attack.")
    val motionAfterAttackOnGround by float("MotionAfterAttackOnGround", 0.6f, 0.0f..1f)
        .describe("Motion kept after attacking on the ground.")
    val motionAfterAttackInAir by float("MotionAfterAttackInAir", 0.6f, 0.0f..1f)
        .describe("Motion kept after attacking in the air.")
    private val motionWhenHurt by float("MotionWhenHurt", 0.6f, 0.0f..1f)
        .describe("Motion kept while recently hurt.")
    private val hurtTimeRange by intRange("HurtTime", 1..10, 1..10)
        .describe("Hurt-time window that uses the hurt motion value.")

    private val timer1 by float("Timer1", 2F, 0.1F..10F) { sprintMode == "Timer" }
        .describe("Game timer speed multiplier after attacking or being hurt.")
    private val timerAllowNegative by boolean("TimerAllowNegative", true) { sprintMode == "Timer" }
        .describe("Allow timer speeds below normal.")
    private val timerBoostTicks by int("TimerBoostTicks", 0, 0..200, "ticks") { sprintMode == "Timer" }
        .describe("Ticks to apply the boost speed each cycle.")
    private val timerBoostSpeed by float("TimerBoostSpeed", 2F, 0.1F..10F) { sprintMode == "Timer" && timerBoostTicks > 0 }
        .describe("Timer speed used during the boost phase.")
    private val timerNormalTicks by int("TimerNormalTicks", 20, 1..200, "ticks") { sprintMode == "Timer" && timerBoostTicks > 0 }
        .describe("Ticks to apply the normal speed each cycle (timer1).")
    private val timerTrigger by choices("TimerTrigger", arrayOf("Attack", "Hurt", "Both"), "Both") { sprintMode == "Timer" }
        .describe("Actions that activate the timer modification.")
    private val timerActiveDuration by int("TimerActiveDuration", 40, 5..200, "ticks") { sprintMode == "Timer" }
        .describe("How long the timer stays active after the last trigger.")

    private var lastAttackTick = -999
    private var lastHurtTick = -999
    private var inTimerBoost = false
    private var timerPhaseTicks = 0

    val motionAfterAttack: Float
        get() {
            if (sprintMode == "Timer") return 1.0f
            if (RandomUtils.nextInt(0, 100) >= chance) return 0.6f
            val player = mc.thePlayer ?: return 0.6f
            if (player.hurtTime in hurtTimeRange) return motionWhenHurt
            return if (player.onGround) motionAfterAttackOnGround else motionAfterAttackInAir
        }
    fun handlePostAttack() {
        val player = mc.thePlayer ?: return
        when (sprintMode) {
            "Normal" -> player.isSprinting = true
            "Timer" -> {
                if (timerTrigger in arrayOf("Attack", "Both")) {
                    lastAttackTick = player.ticksExisted
                }
            }
        }
    }

    override fun onEnable() {
        lastAttackTick = -999
        lastHurtTick = -999
        inTimerBoost = false
        timerPhaseTicks = 0
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1F
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        if (sprintMode != "Timer") {
            if (mc.timer.timerSpeed != 1F) mc.timer.timerSpeed = 1F
            return@handler
        }

        if (timerTrigger in arrayOf("Hurt", "Both")) {
            if (player.hurtTime == 10 && player.ticksExisted - lastHurtTick > 1) {
                lastHurtTick = player.ticksExisted
            }
        }
        val lastTrigger = maxOf(lastAttackTick, lastHurtTick)
        val isActive = player.ticksExisted - lastTrigger < timerActiveDuration

        if (!isActive) {
            mc.timer.timerSpeed = 1F
            inTimerBoost = false
            timerPhaseTicks = 0
            return@handler
        }

        if (timerBoostTicks <= 0) {
            mc.timer.timerSpeed = clampTimerSpeed(timer1)
            return@handler
        }

        val limit = if (inTimerBoost) timerBoostTicks else timerNormalTicks
        if (timerPhaseTicks >= limit) {
            inTimerBoost = !inTimerBoost
            timerPhaseTicks = 0
        }

        mc.timer.timerSpeed = clampTimerSpeed(if (inTimerBoost) timerBoostSpeed else timer1)
        timerPhaseTicks++
    }

    private fun clampTimerSpeed(value: Float) = if (timerAllowNegative) value else value.coerceAtLeast(1F)
}
