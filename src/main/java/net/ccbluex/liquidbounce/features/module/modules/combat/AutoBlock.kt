package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.combat.AutoClicker.canClick
import net.ccbluex.liquidbounce.features.module.modules.combat.AutoClicker.canItemBlock
import net.ccbluex.liquidbounce.utils.timing.MSTimer
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
    private val onlyWhenHurt by boolean("OnlyWhenHurt", false)
    private val hold by int("Hold", 450, 0..1000)
    private val pingCompensation by boolean("PingCompensation", true)
    private val compensationThreshold by int("CompensationThreshold", 100, 0..hold) { pingCompensation }

    private val canBlock: Boolean
        get() = canClick && canItemBlock()
    private val physicalBlock: Boolean
        get() = GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem)

    private var timer = MSTimer()
    private var isBlocking = false

    override fun onDisable() {
        timer.reset()
        isBlocking = false
    }

    val onAttack = handler<AttackEvent> {
        if (mc.thePlayer == null) return@handler
        if (isBlocking || KillAura.blockStatus) return@handler

        if (onlyWhenHurt && mc.thePlayer.hurtTime <= 0) {
            timer.reset()
            return@handler
        }

        if (canBlock && !physicalBlock && timer.hasTimePassed(delay)) {
            timer.reset()
            isBlocking = true
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
        val pingMs = mc.netHandler?.getPlayerInfo(mc.thePlayer?.uniqueID ?: return hold)?.responseTime ?: 50
        return max(compensationThreshold, pingMs - hold)
    }
}
