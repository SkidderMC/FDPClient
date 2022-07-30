package net.skiddermc.fdpclient.features.module.modules.movement.flys.other

import net.skiddermc.fdpclient.event.BlockBBEvent
import net.skiddermc.fdpclient.features.module.modules.movement.flys.FlyMode
import net.minecraft.block.BlockAir
import net.minecraft.util.AxisAlignedBB

class FakeGroundFly : FlyMode("FakeGround") {
    override fun onBlockBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y <= fly.launchY) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, fly.launchY, event.z + 1.0)
        }
    }
}