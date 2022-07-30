package net.skiddermc.fdpclient.features.module.modules.movement.flys.verus

import net.skiddermc.fdpclient.event.BlockBBEvent
import net.skiddermc.fdpclient.event.JumpEvent
import net.skiddermc.fdpclient.event.MoveEvent
import net.skiddermc.fdpclient.features.module.modules.movement.flys.FlyMode
import net.skiddermc.fdpclient.utils.MovementUtils
import net.minecraft.block.BlockAir
import net.minecraft.util.AxisAlignedBB
import net.minecraft.client.settings.GameSettings

class Verus5Fly : FlyMode("Verus5") {
    private var ticks = 0

    override fun onEnable() {
        ticks = 0
    }

    override fun onMove(event: MoveEvent) {
        mc.gameSettings.keyBindJump.pressed = false
        mc.gameSettings.keyBindSneak.pressed = false
        if (ticks % 10 == 0 && mc.thePlayer.onGround) {
            MovementUtils.strafe(1f)
            event.y = 0.42
            ticks = 0
            mc.thePlayer.motionY = 0.0
            mc.timer.timerSpeed = 4f
        } else {
            if (GameSettings.isKeyDown(mc.gameSettings.keyBindJump) && ticks % 2 == 1) {
                event.y = 0.5
                MovementUtils.strafe(0.48f)
                fly.launchY += 0.5
                mc.timer.timerSpeed = 1f
                return
            }
            mc.timer.timerSpeed = 1f
            if (mc.thePlayer.onGround) {
                MovementUtils.strafe(0.8f)
            } else {
                MovementUtils.strafe(0.72f)
            }
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