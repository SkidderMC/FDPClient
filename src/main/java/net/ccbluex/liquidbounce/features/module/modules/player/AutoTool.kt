/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.ClickBlockEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.util.BlockPos

@ModuleInfo(name = "AutoTool", category = ModuleCategory.PLAYER)
object AutoTool : Module() {

    private val noCombat = BoolValue("NoCombat", true)
    private val silent = BoolValue("Silent", false)

    @EventTarget
    fun onClick(event: ClickBlockEvent) {
        if (FDPClient.combatManager.inCombat && noCombat.get()) return
        switchSlot(event.clickedBlock ?: return)
    }

    fun switchSlot(blockPos: BlockPos) {
        var bestSpeed = 1F
        var bestSlot = -1

        val block = mc.theWorld.getBlockState(blockPos).block

        for (i in 0..8) {
            val item = mc.thePlayer.inventory.getStackInSlot(i) ?: continue
            val speed = item.getStrVsBlock(block)

            if (speed > bestSpeed) {
                bestSpeed = speed
                bestSlot = i
            }
        }

            if (bestSlot != -1) {
                if (!silent.get()) {
                    mc.thePlayer.inventory.currentItem = bestSlot
                } else {
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(bestSlot))
                    mc.playerController.updateController()             
        }
    }
  }
}
