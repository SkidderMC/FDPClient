package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.combat.AutoClicker.canClick
import net.ccbluex.liquidbounce.features.module.modules.combat.AutoClicker.canItemBlock
import net.ccbluex.liquidbounce.utils.extensions.getPing
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.utils.timing.TickedActions.nextTick
import net.minecraft.client.settings.GameSettings
import kotlin.math.max

/**
 * AutoBlock module - Automatically blocks attacks
 *
 * Automatically blocks with your sword when being attacked or when attacking entities.
 * Can be configured to block only when keys are pressed or continuously.
 *
 * @author itsakc-me
 */
object AutoBlock : Module("AutoBlock", Category.COMBAT, Category.SubCategory.COMBAT_LEGIT) {

    private val delay by int("Delay", 0, 0..1000)
        .describe("Delay before blocking.")
    private val onlyWhenHurt by boolean("OnlyWhenHurt", false)
        .describe("Only block when you are hurt.")
    private val hold by int("Hold", 450, 0..1000)
        .describe("Duration to hold the block.")
    private val pingCompensation by boolean("PingCompensation", true)
        .describe("Compensate for ping when blocking.")
    private val compensationThreshold by int("CompensationThreshold", 100, 0..500) { pingCompensation }
        .describe("Minimum hold time when ping compensation is enabled.")

    private val canBlock: Boolean
        get() = canClick && canItemBlock()
    private val physicalBlock: Boolean
        get() = GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem)
    private val ping: Int
        get() = mc.thePlayer?.run { getPing() } ?: 0

    private var timer = MSTimer()
    private var isBlocking = false

    override val tag
        get() = "${effectiveHold()}"

    override fun onDisable() {
        timer.reset()
        isBlocking = false
    }

    val onAttack = handler<AttackEvent> {
        if (mc.thePlayer == null || !timer.hasTimePassed(delay)) return@handler
        if (isBlocking || KillAura.blockStatus) return@handler

        if (onlyWhenHurt && mc.thePlayer.hurtResistantTime <= 0) {
            timer.reset()
            return@handler
        }

        if (canBlock && !physicalBlock) {
            nextTick {
                timer.reset()
                isBlocking = true
            }
        }
    }

    val onUpdate = handler<UpdateEvent> {
        if (!isBlocking) return@handler

        // Check if the timer has expired
        if (timer.hasTimePassed(effectiveHold())) {
            timer.reset()
            isBlocking = false
        }

        // ONLY affect when there is no physical blocking
        if (!physicalBlock) {
            mc.gameSettings.keyBindUseItem.pressed = isBlocking
        }
    }

    private fun effectiveHold(): Int {
        if (!pingCompensation) return hold;
        return max(compensationThreshold, hold - ping)
    }
}
