package net.ccbluex.liquidbounce.features.module.modules.movement.jesus.vanilla

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.jesus.JesusMode
import net.minecraft.block.BlockLiquid
import net.minecraft.util.AxisAlignedBB

class VanillaJesus : JesusMode("Vanilla") {
    override fun onBlockBB(event: BlockBBEvent) {
        if (mc.thePlayer == null || mc.thePlayer.entityBoundingBox == null) {
            return
        }

        if (event.block is BlockLiquid && !jesus.isLiquidBlock() && !mc.thePlayer.isSneaking) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), (event.x + 1).toDouble(), (event.y + 1).toDouble(), (event.z + 1).toDouble())
        }
    }
}