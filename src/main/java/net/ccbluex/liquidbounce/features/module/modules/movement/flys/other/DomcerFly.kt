package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.minecraft.block.BlockAir
import net.minecraft.util.AxisAlignedBB

class DomcerFly : FlyMode("Domcer") {
    private var flyValue = FloatValue("${valuePrefix}Vertical", 0.5f, 0.1f, 3f)
    private var ticks = 0

    override fun onEnable() {
        ticks = 0
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }

    override fun onMove(event: MoveEvent) {
        if (ticks % 10 == 0 && mc.thePlayer.onGround) {
            MovementUtils.strafe(1f)
            event.y = 0.42
            ticks = 0
            mc.thePlayer.motionY = 0.0
            MovementUtils.setMotion(1.1485 + Math.random() / 50)
        } else {
            if (mc.gameSettings.keyBindJump.isKeyDown && ticks % 2 == 1) {
                event.y = flyValue.get().toDouble()
                MovementUtils.strafe(0.425f)
                fly.launchY += flyValue.get().toDouble()
                mc.timer.timerSpeed = 0.95f
                return
            }
            mc.timer.timerSpeed = 1f
            MovementUtils.strafe(0.685f)
        }
        ticks++
    }

    override fun onBlockBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y <= fly.launchY) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, fly.launchY, event.z + 1.0)
        }
    }

    override fun onJump(event: JumpEvent) = event.cancelEvent()
}
