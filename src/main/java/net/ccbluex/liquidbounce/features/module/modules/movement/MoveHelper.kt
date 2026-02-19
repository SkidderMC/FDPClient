/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.client.settings.GameSettings
import net.minecraft.entity.EntityLivingBase

/**
 * MoveHelper module - Assists with movement
 *
 * Provides an advanced S-Tap mechanism for customizable
 * movement assistance when attacking entities within a configurable range.
 *
 * @author itsakc-me
 */
object MoveHelper : Module("MoveHelper", Category.MOVEMENT, Category.SubCategory.MOVEMENT_MAIN) {

    // General Settings
    private val range by intRange("Range", 2..3, 1..6)

    // Close Range Settings
    private val closeMode by choices("CloseMode", arrayOf("Interval", "OnAttack"), "Interval")
    private val closeInterval by int("CloseInterval", 10, 1..40) { closeMode == "Interval" }
    private val closeHoldLength by int("CloseHoldLength", 2, 1..10)

    // Far Range Settings
    private val farMode by choices("FarMode", arrayOf("Interval", "OnAttack"), "Interval")
    private val farInterval by int("FarInterval", 20, 1..40) { farMode == "Interval" }
    private val farHoldLength by int("FarHoldLength", 3, 1..10)

    private val onlyWhenHurt by boolean("OnlyWhenHurt", true)

    private var activeTicks = -1
    private var intervalTimer = MSTimer()

    override val tag
        get() = activeTicks.takeIf { it >= 0 }?.toString() ?: "0"

    override fun onDisable() {
        activeTicks = -1
        mc.gameSettings.keyBindBack.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindBack)
        mc.gameSettings.keyBindForward.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindForward)
    }

    val onAttack = handler<AttackEvent> {
        val thePlayer = mc.thePlayer ?: return@handler
        val target = it.targetEntity as? EntityLivingBase ?: return@handler

        if ((onlyWhenHurt && thePlayer.hurtTime <= 0) || !target.isAttackingEntity(thePlayer, 45.0 * (range.last / range.first), range.last.toDouble())) return@handler

        val distance = thePlayer.getDistanceToEntityBox(target)

        when {
            distance < range.first -> handleLogic(closeMode, closeInterval, closeHoldLength, target)
            distance < range.last -> handleLogic(farMode, farInterval, farHoldLength, target)
        }
    }

    val onUpdate = handler<UpdateEvent> {
        if (activeTicks < 0) return@handler
        activeTicks--

        mc.gameSettings.keyBindBack.pressed = if (activeTicks >= 0) true else GameSettings.isKeyDown(mc.gameSettings.keyBindBack)
        mc.gameSettings.keyBindForward.pressed = if (activeTicks >= 0) false else GameSettings.isKeyDown(mc.gameSettings.keyBindForward)
    }

    private fun handleLogic(mode: String, interval: Int, holdLength: Int, target: EntityLivingBase) {
        when (mode.lowercase()) {
            "interval" -> {
                if (intervalTimer.hasTimePassed(interval * 50L)) {
                    activeTicks = holdLength
                    intervalTimer.reset()
                }
            }

            "onhit" -> {
                if (target.hurtTime > 0) {
                    activeTicks = holdLength
                }
            }
        }
    }
}
