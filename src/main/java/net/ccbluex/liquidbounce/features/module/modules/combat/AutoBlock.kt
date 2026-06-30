package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.combat.AutoClicker.canClick
import net.ccbluex.liquidbounce.features.module.modules.combat.AutoClicker.canItemBlock
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.client.EntityLookup
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.extensions.getPing
import net.ccbluex.liquidbounce.utils.extensions.toTicks
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.utils.timing.TickedActions.nextTick
import net.minecraft.client.settings.GameSettings
import net.minecraft.entity.EntityLivingBase
import kotlin.math.min

/**
 * AutoBlock module - Automatically blocks attacks
 *
 * Automatically blocks with your sword when being attacked or when attacking entities.
 * Can be configured to block only when keys are pressed or continuously.
 *
 * Implements post-swing block logic: after attacking an entity, immediately re-block
 * to minimize the unblocked window. Also predicts incoming damage using hurtResistantTime
 * to start blocking early (the "before" window).
 *
 * @author itsakc-me
 */
object AutoBlock : Module("AutoBlock", Category.COMBAT, Category.SubCategory.COMBAT_LEGIT) {

    private val mode                  by choices("Mode", arrayOf("Attack", "Damage"), "Damage")
        .describe("How to block attacks.")
    private val delay                 by int("Delay", 10, 0..20)
        .describe("Delay between blocks in ticks.")
    private val hold                  by int("Hold", 2, 0..20)
        .describe("Duration to hold the block.")
    private val range                 by float("Range", 3f, 1f..5f)             { mode == "Damage" }
        .describe("Range to block attacks from.")
    private val onlyWhenHurt          by boolean("OnlyWhenHurt", true)                      { mode == "Damage" }
        .describe("Only block when you are hurt.")
    private val before                by int("Before", 4, 0..20)                { mode == "Damage" && !pingCompensation }
        .describe("How early to start blocking before you can take damage again.")
    private val pingCompensation      by boolean("PingCompensation", true)                  { mode == "Damage" }
        .describe("Compensate before duration by ping.")
    private val compensationThreshold by int("CompensationThreshold", 5, 0..10) { mode == "Damage" && pingCompensation }
        .describe("Maximum before time when ping compensation is enabled.")
    private val leftClickPressed      by boolean("LeftClickPressed", true)
        .describe("Only block when left mouse button is pressed.")
    private val rightClickPressed     by boolean("RightClickPressed", false)
        .describe("Only block when right mouse button is pressed.")

    init {
        group("General", "Mode", "Delay", "Hold", "Range")
        group("Damage", "OnlyWhenHurt", "Before", "PingCompensation", "CompensationThreshold")
        group("Input", "LeftClickPressed", "RightClickPressed")
    }

    private val canBlock: Boolean
        get() = canClick && canItemBlock()
    private val physicalBlock: Boolean
        get() = GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem)
    private val ping: Int
        get() = mc.thePlayer?.run { getPing().toTicks() } ?: 0

    /**
     * Entities within [range] that are valid attack targets.
     */
    private val entitiesInRange by EntityLookup<EntityLivingBase> {
        isSelected(it, true) && mc.thePlayer.getDistanceToEntityBox(it) <= range
    }

    private var timer = MSTimer()
    private var isBlocking = false

    override val tag
        get() = "${hold * 50L}"

    override fun onDisable() {
        timer.reset()

        // Release the simulated block key when the module is disabled
        if (isBlocking && !physicalBlock) {
            mc.gameSettings.keyBindUseItem.pressed = false
        }

        isBlocking = false
    }

    /**
     * Check whether the configured input conditions are satisfied.
     *
     * - If [leftClickPressed] is on, the left mouse button must be held.
     * - If [rightClickPressed] is on, the right mouse button must be held.
     * - If both are off, the condition is always met (no input required).
     */
    private fun conditionsMet(): Boolean {
        val leftOk  = !leftClickPressed  || GameSettings.isKeyDown(mc.gameSettings.keyBindAttack)
        val rightOk = !rightClickPressed || GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem)
        return leftOk && rightOk
    }

    /**
     * Returns `true` when there is a valid target within [range].
     */
    private fun hasTargetInRange(): Boolean = entitiesInRange.isNotEmpty()

    /**
     * Determines whether the player is about to be hit based on their
     * [EntityLivingBase.hurtResistantTime] countdown. In vanilla MC, `hurtResistantTime`
     * starts at 20 (= `maxHurtResistantTime`) after a hit and counts
     * down to 0. The player can be hit again once it reaches 10 or below.
     *
     * The [effectiveBefore] setting specifies how many ticks *before* the player
     * becomes vulnerable again the module should start blocking.
     * For example, `before = 2` means "start blocking when
     * `hurtResistantTime <= 10 + 2` (i.e., 12)" (assuming `pingCompensation = false`).
     *
     * When `hurtResistantTime == 0` the player has not been hit recently
     * — we allow blocking if a target is in [range] and [onlyWhenHurt] is false (first-hit
     * protection).
     */
    private fun shouldBlockByHurtTime(): Boolean {
        val player = mc.thePlayer ?: return false
        val hrt = player.hurtResistantTime

        // Not been hit recently — check "onlyWhenHurt" (first-hit / approach scenario)
        if (hrt <= 0) return !onlyWhenHurt

        // Block when within the "effectiveBefore" window of becoming vulnerable again.
        // The player becomes vulnerable at hurtResistantTime <= 10 (half of max).
        return hrt <= (player.maxHurtResistantTime / 2) + effectiveBefore()
    }

    // ── Post-swing block (AttackEvent) ──────────────────────────────────

    /**
     * Post-swing block logic: re-block after attacking an entity on next-tick.
     * This minimizes the window where the sword is unblocked around a swing.
     */
    val onAttack = handler<AttackEvent> { event ->
        if (mode != "Attack") return@handler

        if (event.targetEntity == null) return@handler
        if (isBlocking || KillAura.blockStatus) return@handler
        if (!timer.hasTimePassed(delay * 50L)) return@handler
        if (!conditionsMet()) return@handler

        // Start blocking on the next tick after the swing
        if (canBlock && !physicalBlock) {
            nextTick {
                timer.reset()
                isBlocking = true
            }
        }
    }

    // ── Damage-prediction block (GameTickEvent) ─────────────────────────

    /**
     * Periodically checks whether blocking should begin based on:
     * 1. A valid target being within [range].
     * 2. The player's [EntityLivingBase.hurtResistantTime] being within the [effectiveBefore] window.
     * 3. Input conditions ([leftClickPressed] / [rightClickPressed]).
     * 4. The player is not yet blocking (either via this module or KillAura).
     */
    val onTick = handler<GameTickEvent> {
        if (mode != "Damage") return@handler

        if (mc.thePlayer == null) return@handler
        if (isBlocking || KillAura.blockStatus) return@handler
        if (!timer.hasTimePassed(delay * 50L)) return@handler
        if (!conditionsMet()) return@handler
        if (!hasTargetInRange()) return@handler
        if (!shouldBlockByHurtTime()) return@handler

        if (canBlock && !physicalBlock) {
            timer.reset()
            isBlocking = true
        }
    }

    // ── Hold / release management (UpdateEvent) ─────────────────────────

    /**
     * Manages the simulated block key press:
     * - Releases the block once the [hold] duration has elapsed.
     * - Presses/releases `keyBindUseItem` only when the player is not
     *   physically holding right-click (to avoid conflicting with manual input).
     */
    val onUpdate = handler<UpdateEvent> {
        if (!isBlocking) {
            return@handler
        }

        // Release block once the hold duration has expired
        if (timer.hasTimeElapsed(hold * 50L)) {
            isBlocking = false
            timer.reset()
        }

        // Simulate right-click hold while blocking (only when not physically held)
        if (!physicalBlock) {
            mc.gameSettings.keyBindUseItem.pressed = isBlocking
        }
    }

    /**
     * Returns the effective "before" time based on the player's ping.
     */
    private fun effectiveBefore(): Int {
        if (!pingCompensation) return before
        return min(compensationThreshold, ping + 2)
    }
}
