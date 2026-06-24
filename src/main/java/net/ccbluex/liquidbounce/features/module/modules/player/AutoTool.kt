/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.ClickBlockEvent

import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.handler.combat.CombatManager
import net.ccbluex.liquidbounce.utils.inventory.SilentHotbar
import net.ccbluex.liquidbounce.utils.inventory.durability
import net.ccbluex.liquidbounce.utils.inventory.ToolSelection
import net.ccbluex.liquidbounce.event.handler

object AutoTool : Module("AutoTool", Category.PLAYER, Category.SubCategory.PLAYER_ASSIST, subjective = true, gameDetecting = false) {

    private val switchBack by boolean("SwitchBack", false)
        .describe("Switch back to the previous slot after mining.")
    private val onlySneaking by boolean("OnlySneaking", false)
        .describe("Only switch tools while sneaking.")
    private val notDuringCombat by boolean("NotDuringCombat", false)
        .describe("Avoid switching tools while in combat.")
    private val ignoreDurability by boolean("IgnoreDurability", true)
        .describe("Use tools even if nearly broken.")
    private val minimumDurability by int("MinimumDurability", 2, 0..100, isSupported = { !ignoreDurability })
        .describe("Keep at least this much durability when selecting a tool.")
    private val distance by float("Distance", 64f, 1f..64f)
        .describe("Max distance to the block to switch tools.")
    private val swapPreviousDelay by int("SwapPreviousDelay", 0, 0..100, "ticks")
        .describe("Ticks before switching back to the old slot.")


    val onGameTick = handler<GameTickEvent> {
        if (!switchBack || swapPreviousDelay > 0 || mc.gameSettings.keyBindAttack.isKeyDown)
            return@handler

        SilentHotbar.resetSlot(this)
    }

    val onClick = handler<ClickBlockEvent> { event ->
        val player = mc.thePlayer ?: return@handler

        val clickedBlock = event.clickedBlock ?: return@handler

        val block = mc.theWorld.getBlockState(clickedBlock).block

        if (onlySneaking && !player.isSneaking || block.getBlockHardness(mc.theWorld, clickedBlock) == 0f)
            return@handler

        if (notDuringCombat && CombatManager.inCombatState)
            return@handler

        if (player.getDistanceSq(clickedBlock) > distance.toDouble() * distance.toDouble())
            return@handler

        val stacks = (0..8).map(player.inventory::getStackInSlot)
        val candidate = ToolSelection.bestHotbarTool(
            stacks,
            block,
            player.inventory.currentItem,
            if (ignoreDurability) 0 else minimumDurability
        ) ?: return@handler

        if (candidate.slot == player.inventory.currentItem)
            return@handler

        val currentSpeed = player.currentEquippedItem?.getStrVsBlock(block) ?: 1f
        if (candidate.effectiveSpeed <= currentSpeed && (ignoreDurability ||
                player.currentEquippedItem?.durability ?: Int.MAX_VALUE > minimumDurability)) return@handler

        if (swapPreviousDelay > 0) {
            SilentHotbar.selectSlotSilently(this, candidate.slot, ticksUntilReset = swapPreviousDelay, render = false)
        } else {
            SilentHotbar.selectSlotSilently(this, candidate.slot, render = false, resetManually = true)
        }
    }

}
