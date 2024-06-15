package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import me.zywl.fdpclient.event.BlockBBEvent
import me.zywl.fdpclient.event.JumpEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import me.zywl.fdpclient.value.impl.BoolValue
import net.minecraft.block.BlockAir
import net.minecraft.util.AxisAlignedBB

class FakeGroundFly : FlyMode("FakeGround") {
    private val noJumpValue = BoolValue("${valuePrefix}-NoJump", false)
    private val jumpUpYValue = BoolValue("${valuePrefix}-JumpUpY", false)
    override fun onBlockBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y <= fly.launchY) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, fly.launchY, event.z + 1.0)
        }
    }
    override fun onJump(event: JumpEvent) {
        if (noJumpValue.get()) {
            event.cancelEvent()
        }else if (jumpUpYValue.get()) {
            fly.launchY += 1.0
        }
    }
}