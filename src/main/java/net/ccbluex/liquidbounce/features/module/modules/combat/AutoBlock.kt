package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.combat.AutoClicker.canClick
import net.ccbluex.liquidbounce.features.module.modules.combat.AutoClicker.canItemBlock
import net.ccbluex.liquidbounce.utils.extensions.isAttackingEntity
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.client.settings.GameSettings
import net.minecraft.entity.EntityLivingBase

/**
 * AutoBlock module - Automatically blocks attacks
 *
 * Automatically blocks with your sword when being attacked or when attacking entities.
 * Can be configured to block only when keys are pressed or continuously.
 *
 * @author itsakc-me
 */
object AutoBlock : Module("AutoBlock", Category.COMBAT, Category.SubCategory.COMBAT_LEGIT) {

    private val delay by int("Delay", 300, 300..1000)
    private val onlyWhenHurt by boolean("OnlyWhenHurt", false)
    private val endOfLastBlock by boolean("EndOfLastBlock", true)
    private val maxHoldTicks by int("MaxHoldTicks", 2, 1..20)

    private var timer = MSTimer()
    private var isBlocking = false

    override fun onDisable() {
        timer.reset()
        isBlocking = false
    }

    val onAttack = handler<AttackEvent> {
        val target = it.targetEntity as? EntityLivingBase ?: return@handler
        val thePlayer = mc.thePlayer ?: return@handler
        if (isBlocking || KillAura.blockStatus) return@handler

        if ((onlyWhenHurt && mc.thePlayer.hurtTime <= 0) || !target.isAttackingEntity(thePlayer, 45.0, 3.5)) return@handler

        if (!mc.gameSettings.keyBindUseItem.isKeyDown && canClick && canItemBlock() && timer.hasTimePassed(delay)) {
            isBlocking = true
            timer.reset()
        }
    }

    val onUpdate = handler<UpdateEvent> {
        if (!isBlocking) return@handler

        // Check if the timer has expired
        if (timer.hasTimePassed(maxHoldTicks * 50L) || mc.objectMouseOver?.entityHit as? EntityLivingBase == null) {
            if (endOfLastBlock) {
                timer.reset()
            }
            isBlocking = false
        }

        // ONLY affect when there is no physical blocking
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem)) {
            mc.gameSettings.keyBindUseItem.pressed = isBlocking
        }
    }
}
