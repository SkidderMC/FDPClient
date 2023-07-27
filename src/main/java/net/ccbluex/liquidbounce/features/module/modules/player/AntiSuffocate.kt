package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.utils.PacketUtils


@ModuleInfo(name = "AntiSuffocate", category = ModuleCategory.PLAYER)
object AntiSuffocate : Module(){

    private val swing = BoolValue("Swing", true)

    fun onPreMotion(){

        if (mc.thePlayer.isEntityInsideOpaqueBlock()){
            PacketUtils.sendPacketNoEvent(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, BlockPos(mc.thePlayer).down(), EnumFacing.UP))

            if (swing.get()){
                mc.thePlayer.swingItem();
            }
        }

    }
}