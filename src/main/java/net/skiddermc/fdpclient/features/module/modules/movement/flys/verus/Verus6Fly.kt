package net.skiddermc.fdpclient.features.module.modules.movement.flys.verus

import net.skiddermc.fdpclient.event.BlockBBEvent
import net.skiddermc.fdpclient.event.JumpEvent
import net.skiddermc.fdpclient.event.MoveEvent
import net.skiddermc.fdpclient.features.module.modules.movement.flys.FlyMode
import net.skiddermc.fdpclient.utils.MovementUtils
import net.skiddermc.fdpclient.value.FloatValue
import net.skiddermc.fdpclient.value.IntegerValue
import net.minecraft.block.BlockAir
import net.minecraft.stats.StatList
import net.minecraft.util.AxisAlignedBB

class Verus6Fly : FlyMode("Verus6") {

    private val airSpeedValue = FloatValue("${valuePrefix}AirSpeed", 0.5f, 0f, 1f)
    private val groundSpeedValue = FloatValue("${valuePrefix}GroundSpeed", 0.42f, 0f, 1f)
    private val hopDelayValue = IntegerValue("${valuePrefix}HopDelay", 3, 0, 10)

    private var waitTicks = 0

    override fun onEnable() {
        waitTicks = 0
    }

    override fun onMove(event: MoveEvent) {
        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.onGround) {
                MovementUtils.strafe(groundSpeedValue.get())
                waitTicks++
                if (waitTicks >= hopDelayValue.get()) {
                    waitTicks = 0
                    mc.thePlayer.triggerAchievement(StatList.jumpStat)
                    mc.thePlayer.motionY = 0.0
                    event.y = 0.41999998688698
                }
            } else {
                MovementUtils.strafe(airSpeedValue.get())
            }
        }
    }

    override fun onBlockBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y <= fly.launchY) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, fly.launchY, event.z + 1.0)
        }
    }

    override fun onJump(event: JumpEvent) = event.cancelEvent()
}