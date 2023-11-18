package net.ccbluex.liquidbounce.features.module.modules.player.nofalls.aac

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofalls.NoFallMode
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.BlockPos

class AAC5014Nofall : NoFallMode("AAC5.0.14") {
    private var aac5Check = false
    private var aac5doFlag = false
    private var aac5Timer = 0
    override fun onEnable() {
        aac5Check = false
        aac5Timer = 0
        aac5doFlag = false
    }
    override fun onNoFall(event: UpdateEvent) {
        var offsetYs = 0.0
        aac5Check = false
        while (mc.thePlayer.motionY - 1.5 < offsetYs) {
            val blockPos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + offsetYs, mc.thePlayer.posZ)
            val block = BlockUtils.getBlock(blockPos)
            val axisAlignedBB = block!!.getCollisionBoundingBox(mc.theWorld, blockPos, BlockUtils.getState(blockPos))
            if (axisAlignedBB != null) {
                offsetYs = -999.9
                aac5Check = true
            }
            offsetYs -= 0.5
        }
        if (mc.thePlayer.onGround) {
            mc.thePlayer.fallDistance = -2f
            aac5Check = false
        }
        if (aac5Timer > 0) {
            aac5Timer -= 1
        }
        if (aac5Check && mc.thePlayer.fallDistance > 2.5 && !mc.thePlayer.onGround) {
            aac5doFlag = true
            aac5Timer = 18
        } else {
            if (aac5Timer < 2) aac5doFlag = false
        }
        if (aac5doFlag) {
            if (mc.thePlayer.onGround) {
                mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.5, mc.thePlayer.posZ, true))
            } else {
                mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.42, mc.thePlayer.posZ, true))
            }
        }
    }
}