package net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.other

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.fakeGroundJumpUpY
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.fakeGroundNoJump
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.jumpY
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.FlyMode
import net.minecraft.block.BlockAir
import net.minecraft.util.AxisAlignedBB

object FakeGround : FlyMode("FakeGround") {
    override fun onBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y <= jumpY) {
            event.boundingBox = AxisAlignedBB.fromBounds(
                event.x.toDouble(),
                event.y.toDouble(),
                event.z.toDouble(),
                event.x + 1.0,
                jumpY,
                event.z + 1.0
            )
        }
    }

    override fun onJump(event: JumpEvent) {
        if (fakeGroundNoJump) {
            event.cancelEvent()
        } else if (fakeGroundJumpUpY) {
            jumpY += 1.0
        }
    }
}
