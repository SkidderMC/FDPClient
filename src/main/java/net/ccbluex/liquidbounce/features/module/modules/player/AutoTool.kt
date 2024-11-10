/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.ClickBlockEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.SilentHotbar
import net.ccbluex.liquidbounce.value.boolean

object AutoTool : Module("AutoTool", Category.PLAYER, subjective = true, gameDetecting = false, hideModule = false) {

    private val switchBack by boolean("SwitchBack", false)
    private val onlySneaking by boolean("OnlySneaking", false)

    @EventTarget
    fun onGameTick(event: GameTickEvent) {
        if (!switchBack || mc.gameSettings.keyBindAttack.isKeyDown)
            return

        SilentHotbar.resetSlot(this)
    }

    @EventTarget
    fun onClick(event: ClickBlockEvent) {
        val player = mc.thePlayer ?: return

        val block = mc.theWorld.getBlockState(event.clickedBlock ?: return).block

        if (onlySneaking && !player.isSneaking || block.getBlockHardness(mc.theWorld, event.clickedBlock) == 0f)
            return

        var fastest = 1f

        val slot = (0..8).maxByOrNull {
            val item = player.inventory.getStackInSlot(it) ?: return@maxByOrNull 1f

            item.getStrVsBlock(block).also { speed -> fastest = fastest.coerceAtLeast(speed) }
        } ?: return

        if (fastest == (player.currentEquippedItem?.getStrVsBlock(block) ?: 1f))
            return

        SilentHotbar.selectSlotSilently(this, slot, render = false, resetManually = true)
    }

}