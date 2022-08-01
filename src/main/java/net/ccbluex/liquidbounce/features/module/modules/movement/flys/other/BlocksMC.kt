package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.minecraft.block.Block
import net.minecraft.block.BlockAir
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.AxisAlignedBB
import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils

class BlocksMC : FlyMode("BlocksMC") {
    private var blocksBB = false
    private var ticks = 0
    override fun onEnable() {
        blocksBB = false
        ticks = 0
        if(mc.thePlayer.onGround) {
            mc.thePlayer.motionY = 0.4
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        if(mc.thePlayer.onGround && mc.thePlayer.posY >= fly.launchY + 0.8) {
            blocksBB = true
        }
        if(blocksBB) {
            ticks++
            if(ticks<20) {
                mc.timer.timerSpeed = 2f
            } else {
                mc.timer.timerSpeed = 1f
            }
        }
    }
    override fun onBlockBB(event: BlockBBEvent) {
        if(!blocksBB) return

        if (event.block is BlockAir && event.y <= fly.launchY + 1) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, fly.launchY, event.z + 1.0)
        }
    }
}