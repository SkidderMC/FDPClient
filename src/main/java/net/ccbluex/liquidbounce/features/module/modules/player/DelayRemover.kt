/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.updateControls
import net.ccbluex.liquidbounce.event.handler

object DelayRemover : Module("DelayRemover", Category.PLAYER, Category.SubCategory.PLAYER_COUNTER) {

    val noClickDelay by boolean("NoClickDelay", true)
        .describe("Remove the delay between left clicks.")

    val blockBreakDelay by boolean("NoBlockHitDelay", false)
        .describe("Remove the delay between block hits.")

    val noSlowBreak by boolean("NoSlowBreak", false)
        .describe("Remove mining slowdown effects.")
    val air by boolean("Air", true) { noSlowBreak }
        .describe("Remove the in-air mining slowdown.")
    val water by boolean("Water", true) { noSlowBreak }
        .describe("Remove the underwater mining slowdown.")
    val miningFatigue by boolean("MiningFatigue", true) { noSlowBreak }
        .describe("Remove mining fatigue slowdown.")

    val exitGuiValue by boolean("NoExitGuiDelay", true)
        .describe("Remove the delay after closing a GUI.")

    private var prevGui = false


    val onTick = handler<GameTickEvent> {
        if (noClickDelay) {
            mc.leftClickCounter = 0
        }

        if (blockBreakDelay) {
            mc.playerController.blockHitDelay = 0
        }

        if (mc.currentScreen == null && exitGuiValue) {
            if (prevGui) updateControls()
            prevGui = false
        } else {
            prevGui = true
        }
    }

}
