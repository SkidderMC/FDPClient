/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import jdk.nashorn.internal.ir.Block
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.TickEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacketNoEvent
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import scala.collection.script.Update


@ModuleInfo(name = "AutoTool", category = ModuleCategory.PLAYER)
class AutoTool : Module() {

    private val silentSwitch = BoolValue("SilentSwitch", false) // this might cause a ban
    private var backSlot = -1
    private var destroyTime = 0
    private var facing: EnumFacing? = null
    private var breakingBlockPos: BlockPos = BlockPos(0, 0, 0)

    private fun swapBack() {
        if (backSlot == -1) return
        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(backSlot))
        backSlot = -1
        destroyTime = 0
    }

    @EventTarget
    fun onPacket(e: PacketEvent) {
        val packet = e.packet
        if (packet is C07PacketPlayerDigging) {
            if (packet.status == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) switchSlot(packet.position)
            else if (packet.status == C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK || packet.status == C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK) swapBack()
            facing = packet.facing
        }
    }

    @EventTarget
    fun onUpdate(e: UpdateEvent) {
        if (backSlot == -1) return
        if (destroyTime != 0) {
            --destroyTime
            ClientUtils.logInfo("Destroy Time Remain: $destroyTime")
            return
        }
        ClientUtils.logInfo("Break $breakingBlockPos!")
        mc.theWorld.setBlockState(breakingBlockPos, Blocks.air.defaultState, 11)
        sendPacketNoEvent(C07PacketPlayerDigging(
            C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
            breakingBlockPos,
            facing
        ))
        swapBack()
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

        if (bestSlot != -1 && bestSlot != mc.thePlayer.inventory.currentItem) {
            if (silentSwitch.get()) {
                val blockState = mc.theWorld.getBlockState(blockPos)
                val blockHardness = blockState.block.getBlockHardness(mc.theWorld, blockPos)
                val digSpeed = mc.thePlayer.inventory.getStackInSlot(bestSlot).getStrVsBlock(block)
                destroyTime += ((blockHardness / digSpeed) * 30).toInt() + 1
                mc.netHandler.addToSendQueue(C09PacketHeldItemChange(bestSlot))
                backSlot = mc.thePlayer.inventory.currentItem
                breakingBlockPos = blockPos
                ClientUtils.logInfo("Silent Swap! $backSlot")
            } else mc.thePlayer.inventory.currentItem = bestSlot
        }
    }
}