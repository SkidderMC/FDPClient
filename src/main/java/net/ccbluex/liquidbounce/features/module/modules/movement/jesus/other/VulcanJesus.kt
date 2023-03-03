package net.ccbluex.liquidbounce.features.module.modules.movement.jesus.other

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.jesus.JesusMode
import net.minecraft.block.BlockLiquid
import net.minecraft.block.material.Material
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos

class VulcanJesus : JesusMode("Vulcan") {
    private var nextStep = 0

    override fun onEnable() {}

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
                        mc.thePlayer.entityBoundingBox.minY - 0.01
                        mc.thePlayer.entityBoundingBox.minZ
                )
            )) {
                nextStep++
                event.packet.onGround = false //Jesus A
                when (nextStep) {
                    1 -> event.packet.y += 0.082326592364824014f
                    2 -> event.packet.y += 0.139279999790191618f
                    3 -> event.packet.y += 0.165423999942779505f
                    4 -> event.packet.y += 0.114271360352935746f
                    5 -> event.packet.y += 0.04194693730418576f
                    6 -> {
                        event.packet.y += 0.01236341326161235f
                        nextStep = 0
                    }
                }
            }
        }
    }
}
