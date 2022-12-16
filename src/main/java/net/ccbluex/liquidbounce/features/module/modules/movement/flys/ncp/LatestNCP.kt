package net.ccbluex.liquidbounce.features.module.modules.movement.flys.ncp

import jdk.nashorn.internal.ir.Block
import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos

class LatestNCP : FlyMode("LatestNCP") {
    private val speedValue = FloatValue("Speed", 15f, 10f, 20f)
    private val timerValue = FloatValue("Timer", 0.8F , 0.5f , 1.0f)
    private var blockPos: BlockPos? = null
    private var setBlocks = false
    private var jumped = false

    override fun onEnable() {
        blockPos = null
        setBlocks = false
        jumped = false
    }

    override fun onUpdate(event: UpdateEvent) {
        if(!setBlocks) {
            blockPos = BlockPos(mc.thePlayer.posX , mc.thePlayer.posY + 2, mc.thePlayer.posZ)
            mc.theWorld.setBlockState(blockPos, Blocks.barrier.defaultState)
            setBlocks = true
        } else {
            mc.timer.timerSpeed = timerValue.get()
            if(!jumped) {
                mc.thePlayer.jump()
                jumped = true
            } else {
                MovementUtils.strafe(speedValue.get())
            }
        }
    }

    override fun onDisable() {
        if(blockPos != null) mc.theWorld.destroyBlock(blockPos, false)
    }

}