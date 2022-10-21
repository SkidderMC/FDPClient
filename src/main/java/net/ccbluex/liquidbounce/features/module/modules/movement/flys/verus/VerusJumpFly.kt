package net.ccbluex.liquidbounce.features.module.modules.movement.flys.verus

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.block.BlockAir
import net.minecraft.client.settings.GameSettings
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos

class VerusJumpFly : FlyMode("VerusJump") {

    override fun onUpdate(event: UpdateEvent) {
        val pos = mc.thePlayer.position.add(0.0, -1.5, 0.0)
        if(mc.gameSettings.keyBindJump.isKeyDown()) {
        PacketUtils.sendPacketNoEvent(
            C08PacketPlayerBlockPlacement(pos, 1,
                ItemStack(Blocks.stone.getItem(mc.theWorld, pos)), 0.0F, 0.5F + Math.random().toFloat() * 0.44.toFloat(), 0.0F)
        );
            if (mc.thePlayer.ticksExisted % 5 == 0) {
                mc.thePlayer.motionY = 0.42
                MovementUtils.strafe(0.2F)
            }
        }
    }
}
