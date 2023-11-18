package net.ccbluex.liquidbounce.features.module.modules.movement.flys.verus

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.minecraft.block.BlockAir
import net.minecraft.stats.StatList
import net.minecraft.util.AxisAlignedBB

class VerusCustomFly : FlyMode("VerusCustom") {

    private val airSpeedValue = FloatValue("${valuePrefix}AirSpeed", 0.5f, 0f, 1f)
    private val groundSpeedValue = FloatValue("${valuePrefix}GroundSpeed", 0.42f, 0f, 1f)
    private val hopDelayValue = IntegerValue("${valuePrefix}HopDelay", 3, 0, 10)
    private val onlyOnGround = BoolValue("${valuePrefix}OnlyEnableOnGround", true)

    private var waitTicks = 0

    override fun onEnable() {
        if (!mc.thePlayer.onGround && onlyOnGround.get()) return
        waitTicks = 0
        sendLegacy()
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
