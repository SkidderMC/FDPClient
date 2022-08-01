package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3

class MineplexFly : FlyMode("Mineplex") {
    private val speedValue = FloatValue("${valuePrefix}Speed", 1f, 0.5f, 10f)

    private val timer = MSTimer()

    override fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.inventory.getCurrentItem() == null) {
            if (mc.gameSettings.keyBindJump.isKeyDown && timer.hasTimePassed(100)) {
                mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.6, mc.thePlayer.posZ)
                timer.reset()
            }
            if (mc.thePlayer.isSneaking && timer.hasTimePassed(100)) {
                mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.6, mc.thePlayer.posZ)
                timer.reset()
            }
            val blockPos = BlockPos(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY - 1, mc.thePlayer.posZ)
            val vec = Vec3(blockPos).addVector(0.4, 0.4, 0.4).add(Vec3(EnumFacing.UP.directionVec))
            mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem(), blockPos, EnumFacing.UP, Vec3(vec.xCoord * 0.4f, vec.yCoord * 0.4f, vec.zCoord * 0.4f))
            MovementUtils.strafe(0.27f)
            mc.timer.timerSpeed = 1 + speedValue.get()
        } else {
            mc.timer.timerSpeed = 1f
            fly.state = false
            ClientUtils.displayChatMessage("§8[§c§lMineplex-§a§lFly§8] §aSelect an empty slot to fly.")
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer) {
            packet.onGround = true
        }
    }

    override fun onBlockBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y <= mc.thePlayer.posY) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, mc.thePlayer.posY, event.z + 1.0)
        }
    }

    override fun onJump(event: JumpEvent) {
        event.cancelEvent()
    }

    override fun onStep(event: StepEvent) {
        event.stepHeight = 0f
    }
}