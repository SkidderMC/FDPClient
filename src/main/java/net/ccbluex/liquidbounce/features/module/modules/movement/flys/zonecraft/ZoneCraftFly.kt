package net.ccbluex.liquidbounce.features.module.modules.movement.flys.zonecraft

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.minecraft.block.BlockAir
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos

class ZoneCraftFly : FlyMode("ZoneCraft") {
    private val timerBoostValue = BoolValue("${valuePrefix}TimerBoost", false)
    
    override fun onMove(event: MoveEvent) {
        mc.timer.timerSpeed = 1f

        if (timerBoostValue.get()) {
            if(mc.thePlayer.ticksExisted % 20 < 10) {
                mc.timer.timerSpeed = 1.25f
            } else {
                mc.timer.timerSpeed = 0.8f
            }
        }
        
        RotationUtils.setTargetRotation(Rotation(mc.thePlayer.rotationYaw, 90f))
        mc.netHandler.networkManager.sendPacket(C08PacketPlayerBlockPlacement(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ), 1, null, 0f, 1f, 0f))

    }
    
    override fun onBlockBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y <= fly.launchY) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, fly.launchY, event.z + 1.0)
        }
    }
}
