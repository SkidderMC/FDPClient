package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.aac

import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.ccbluex.liquidbounce.utils.block.block
import net.ccbluex.liquidbounce.utils.block.state
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.util.BlockPos

object AAC5014 : NoFallMode("AAC5.0.14") {
    private var aac5Check = false
    private var aac5DoFlag = false
    private var aac5Timer = 0

    override fun onEnable() {
        aac5Check = false
        aac5DoFlag = false
        aac5Timer = 0
    }

    override fun onUpdate() {
        val player = mc.thePlayer ?: return
        var offsetYs = 0.0
        aac5Check = false

        while (player.motionY - 1.5 < offsetYs) {
            val blockPos = BlockPos(player.posX, player.posY + offsetYs, player.posZ)
            val block = blockPos.block
            val state = blockPos.state
            val axisAlignedBB = if (block != null && state != null) {
                block.getCollisionBoundingBox(mc.theWorld, blockPos, state)
            } else {
                null
            }

            if (axisAlignedBB != null) {
                offsetYs = -999.9
                aac5Check = true
            }

            offsetYs -= 0.5
        }

        if (player.onGround) {
            player.fallDistance = -2f
            aac5Check = false
        }

        if (aac5Timer > 0) {
            aac5Timer--
        }

        if (aac5Check && player.fallDistance > 2.5f && !player.onGround) {
            aac5DoFlag = true
            aac5Timer = 18
        } else if (aac5Timer < 2) {
            aac5DoFlag = false
        }

        if (aac5DoFlag) {
            sendPacket(
                C04PacketPlayerPosition(
                    player.posX,
                    player.posY + if (player.onGround) 0.5 else 0.42,
                    player.posZ,
                    true
                ),
                false
            )
        }
    }
}
