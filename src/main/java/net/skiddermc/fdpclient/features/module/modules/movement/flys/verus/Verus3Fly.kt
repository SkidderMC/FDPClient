package net.skiddermc.fdpclient.features.module.modules.movement.flys.verus

import net.skiddermc.fdpclient.event.BlockBBEvent
import net.skiddermc.fdpclient.event.UpdateEvent
import net.skiddermc.fdpclient.features.module.modules.movement.flys.FlyMode
import net.skiddermc.fdpclient.utils.MovementUtils
import net.skiddermc.fdpclient.value.BoolValue
import net.minecraft.block.BlockAir
import net.minecraft.util.AxisAlignedBB
import net.minecraft.client.settings.GameSettings

class Verus3Fly : FlyMode("Verus3") {

    private val airStrafeValue = BoolValue("${valuePrefix}AirStrafe", true)

    override fun onUpdate(event: UpdateEvent) {
        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
            mc.gameSettings.keyBindJump.pressed = false
            mc.thePlayer.jump()
            MovementUtils.strafe(0.48F)
        } else if(airStrafeValue.get()) {
            MovementUtils.strafe()
        }
    }

    override fun onBlockBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y <= fly.launchY) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, fly.launchY, event.z + 1.0)
        }
    }
}
