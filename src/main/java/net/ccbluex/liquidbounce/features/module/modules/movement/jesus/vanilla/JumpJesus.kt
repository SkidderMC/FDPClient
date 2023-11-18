package net.ccbluex.liquidbounce.features.module.modules.movement.jesus.vanilla

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.jesus.JesusMode
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.minecraft.block.BlockLiquid
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos

class JumpJesus : JesusMode("Jump") {
    private val jumpMotionValue = FloatValue("${valuePrefix}Motion", 0.5f, 0.1f, 1f)

    override fun onJesus(event: UpdateEvent, blockPos: BlockPos) {
        if (BlockUtils.getBlock(blockPos) === Blocks.water && mc.thePlayer.onGround) {
            mc.thePlayer.motionY = jumpMotionValue.get().toDouble()
        }
    }

    override fun onBlockBB(event: BlockBBEvent) {
        if (mc.thePlayer == null || mc.thePlayer.entityBoundingBox == null) {
            return
        }

        if (event.block is BlockLiquid && !jesus.isLiquidBlock() && !mc.thePlayer.isSneaking) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), (event.x + 1).toDouble(), (event.y + 1).toDouble(), (event.z + 1).toDouble())
        }
    }
}