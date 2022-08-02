package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.minecraft.block.BlockAir
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos

class BlocksMCFly : FlyMode("BlocksMC") {
    private var blocksBB = false
    private var ticks = 0
    override fun onEnable() {
        blocksBB = false
        ticks = 0
        mc.gameSettings.keyBindUseItem.pressed = false
        if(mc.thePlayer.onGround) {
            mc.thePlayer.motionY = 0.42
        }
    }

    override fun onDisable() {
        mc.gameSettings.keyBindUseItem.pressed = false
    }

    override fun onUpdate(event: UpdateEvent) {
        if(mc.thePlayer.posY >= fly.launchY + 0.8 && !blocksBB) {
            if(mc.thePlayer.onGround) {
                blocksBB = true
            }
        }
        if(blocksBB) {
            ticks++
            when(ticks) {
                in 1..10 -> mc.timer.timerSpeed = 2f

                in 10..15 -> mc.timer.timerSpeed = 0.8f
            }
            if(ticks>=15) {
                ticks = 0
                mc.timer.timerSpeed = 0.6f
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