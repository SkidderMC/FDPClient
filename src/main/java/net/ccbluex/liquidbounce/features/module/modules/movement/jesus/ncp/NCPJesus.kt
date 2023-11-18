package net.ccbluex.liquidbounce.features.module.modules.movement.jesus.ncp

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.jesus.JesusMode
import net.minecraft.block.BlockLiquid
import net.minecraft.block.material.Material
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos

class NCPJesus : JesusMode("NCP") {
    private var nextTick = false
    override fun onEnable() {
        nextTick = false
    }
    override fun onJesus(event: UpdateEvent, blockPos: BlockPos) {
        if (jesus.isLiquidBlock() && mc.thePlayer.isInsideOfMaterial(Material.air)) {
            mc.thePlayer.motionY = 0.08
        }
    }

    override fun onBlockBB(event: BlockBBEvent) {
        if (mc.thePlayer == null || mc.thePlayer.entityBoundingBox == null) {
            return
        }

        if (event.block is BlockLiquid && !jesus.isLiquidBlock() && !mc.thePlayer.isSneaking) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), (event.x + 1).toDouble(), (event.y + 1).toDouble(), (event.z + 1).toDouble())
        }
    }

    override fun onPacket(event: PacketEvent) {
        if (mc.thePlayer == null) {
            return
        }

        if (event.packet is C03PacketPlayer) {
            if (jesus.isLiquidBlock(
                    AxisAlignedBB(
                        mc.thePlayer.entityBoundingBox.maxX,
                        mc.thePlayer.entityBoundingBox.maxY,
                        mc.thePlayer.entityBoundingBox.maxZ,
                        mc.thePlayer.entityBoundingBox.minX,
                        mc.thePlayer.entityBoundingBox.minY - 0.01,
                        mc.thePlayer.entityBoundingBox.minZ
                    )
                )
            ) {
                nextTick = !nextTick
                if (nextTick) {
                    event.packet.y -= 0.001
                }
            }
        }
    }

}