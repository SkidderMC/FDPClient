package net.ccbluex.liquidbounce.features.module.modules.movement.flys.verus

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement

class VerusBasicFly : FlyMode("VerusBasic") {

    override fun onMove(event: MoveEvent) {
        val pos = mc.thePlayer.position.add(0.0, -1.5, 0.0)
        PacketUtils.sendPacketNoEvent(
            C08PacketPlayerBlockPlacement(pos, 1,
                ItemStack(Blocks.stone.getItem(mc.theWorld, pos)), 0.0F, 0.5F + Math.random().toFloat() * 0.44.toFloat(), 0.0F)
        );
        if(mc.thePlayer.onGround) {
            mc.thePlayer.jump()
            event.y = 0.42
        }else {
            event.y = 0.0
            MovementUtils.strafe(0.35f)
        }
    }
}
