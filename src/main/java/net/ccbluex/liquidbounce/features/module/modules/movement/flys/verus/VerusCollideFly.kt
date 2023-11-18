package net.ccbluex.liquidbounce.features.module.modules.movement.flys.verus

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.block.BlockAir
import net.minecraft.client.settings.GameSettings
import net.minecraft.util.AxisAlignedBB
import kotlin.math.floor

class VerusCollideFly : FlyMode("VerusCollide") {
    private var ticks = 0
    private var justEnabled = true

    override fun onEnable() {
        ticks = 0
        justEnabled = true
        sendLegacy()
    }

    override fun onMove(event: MoveEvent) {
        mc.gameSettings.keyBindJump.pressed = false
        mc.gameSettings.keyBindSneak.pressed = false
        if (ticks % 14 == 0 && mc.thePlayer.onGround) {
            justEnabled = false
            MovementUtils.strafe(0.69f)
            event.y = 0.42
            ticks = 0
            mc.thePlayer.motionY = -(mc.thePlayer.posY - floor(mc.thePlayer.posY))
        } else {
            if (GameSettings.isKeyDown(mc.gameSettings.keyBindJump) && ticks % 2 == 1) {
                if (mc.thePlayer.ticksExisted % 2 == 0) {
                    mc.thePlayer.motionY = 0.42
                    MovementUtils.strafe(0.3f)
                }
            }
            if (mc.thePlayer.onGround) {
                if (!justEnabled) {
                    MovementUtils.strafe(1.01f)
                }
            } else {
                MovementUtils.strafe(0.41f)
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
