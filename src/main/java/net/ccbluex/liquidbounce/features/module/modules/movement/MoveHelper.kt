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
import net.ccbluex.liquidbounce.config.Configurable

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
        .describe("Distance range separating close-range from far-range S-tap.")

    // Close Range Settings
    private val closeMode by choices("CloseMode", arrayOf("Interval", "Hit"), "Interval")
        .describe("How to trigger the S-tap at close range.")
    private val closeInterval by int("CloseInterval", 500, 0..1000) { closeMode == "Interval" }
        .describe("Interval timing between close-range S-tap.")
    private val closeHoldLength by int("CloseHold", 100, 0..500)
        .describe("How long to hold back movement at close range.")

    // Far Range Settings
    private val farMode by choices("FarMode", arrayOf("Interval", "Hit"), "Interval")
        .describe("How to trigger the S-tap at far range.")
    private val farInterval by int("FarInterval", 500, 0..1000) { farMode == "Interval" }
        .describe("Interval timing between far-range S-tap.")
    private val farHoldLength by int("FarHold", 150, 0..500)
        .describe("How long to hold back movement at far range.")

    private val onlyWhenHurt by boolean("OnlyWhenHurt", true)
        .describe("Only assist movement while you are hurt.")

    private var activeTicks = -1
    private var intervalTimer = MSTimer()

    override val tag
        get() = activeTicks.coerceAtLeast(0).toString()

    private val generalGroup = Configurable("General")
    private val closeGroup = Configurable("Close")
    private val farGroup = Configurable("Far")

    init {
        moveValues(generalGroup, "Range", "OnlyWhenHurt")
        moveValues(closeGroup, "CloseMode", "CloseInterval", "CloseHold")
        moveValues(farGroup, "FarMode", "FarInterval", "FarHold")
        addValues(listOf(generalGroup, closeGroup, farGroup))
    }

    override fun onDisable() {
        activeTicks = -1
        mc.gameSettings.keyBindBack.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindBack)
        mc.gameSettings.keyBindForward.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindForward)
    }

    val onAttack = handler<AttackEvent> {
        val player = mc.thePlayer ?: return@handler
        val target = it.targetEntity as? EntityLivingBase ?: return@handler

        if (onlyWhenHurt && player.hurtResistantTime <= 0) return@handler

        val distance = player.getDistanceToEntityBox(target)

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
        when (mode) {
            "Interval" -> {
                if (intervalTimer.hasTimePassed(interval * 50L)) {
                    activeTicks = holdLength
                    intervalTimer.reset()
                }
            }

            "Hit" -> {
                if (target.hurtResistantTime > 0) {
                    activeTicks = holdLength
                }
            }
        }
    }
}
