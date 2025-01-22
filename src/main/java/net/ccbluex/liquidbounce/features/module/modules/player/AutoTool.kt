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
import net.ccbluex.liquidbounce.utils.inventory.SilentHotbar
import net.ccbluex.liquidbounce.event.handler

object AutoTool : Module("AutoTool", Category.PLAYER, subjective = true, gameDetecting = false) {

    private val switchBack by boolean("SwitchBack", false)
    private val onlySneaking by boolean("OnlySneaking", false)


    val onGameTick = handler<GameTickEvent> {
        if (!switchBack || mc.gameSettings.keyBindAttack.isKeyDown)
            return@handler

        SilentHotbar.resetSlot(this)
    }
    
    val onClick = handler<ClickBlockEvent> { event ->
        val player = mc.thePlayer ?: return@handler

        val block = mc.theWorld.getBlockState(event.clickedBlock ?: return@handler).block

        if (onlySneaking && !player.isSneaking || block.getBlockHardness(mc.theWorld, event.clickedBlock) == 0f)
            return@handler

        var fastest = 1f

        val slot = (0..8).maxByOrNull {
            val item = player.inventory.getStackInSlot(it) ?: return@maxByOrNull 1f

            item.getStrVsBlock(block).also { speed -> fastest = fastest.coerceAtLeast(speed) }
        } ?: return@handler

        if (fastest == (player.currentEquippedItem?.getStrVsBlock(block) ?: 1f))
            return@handler

        SilentHotbar.selectSlotSilently(this, slot, render = false, resetManually = true)
    }

}