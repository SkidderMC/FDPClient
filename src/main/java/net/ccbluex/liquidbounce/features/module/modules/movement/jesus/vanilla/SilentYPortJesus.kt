package net.ccbluex.liquidbounce.features.module.modules.movement.jesus.vanilla

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.jesus.JesusMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.block.BlockLiquid
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos

class SilentYPortJesus : JesusMode("SilentYPort") {
    private val yPortUpValue = FloatValue("${valuePrefix}Up", 0.1f, 0.0f, 0.5f)
    private val yPortDownValue = FloatValue("${valuePrefix}Down", 0.1f, 0.0f, 0.5f)
    private val yPortSpeedValue = FloatValue("${valuePrefix}SpeedModify", 1.0f, 0.0f, 1.5f)
    private val yPortGroundValue = BoolValue("${valuePrefix}SpoofGround", false)
    private val yPortConvertValue = BoolValue("${valuePrefix}ConvertGround", true)
    private val yPortConvertDelayValue = IntegerValue("${valuePrefix}ConvertDelay", 1000, 0, 2000).displayable { yPortConvertValue.get() }

    private var nextTick = false
    private val timer = MSTimer()

    override fun onEnable() {
        nextTick = false
        timer.reset()
    }
    override fun onJesus(event: UpdateEvent, blockPos: BlockPos) {
        if (mc.thePlayer.onGround && jesus.isLiquidBlock(
                AxisAlignedBB(mc.thePlayer.entityBoundingBox.maxX, mc.thePlayer.entityBoundingBox.maxY,
                mc.thePlayer.entityBoundingBox.maxZ, mc.thePlayer.entityBoundingBox.minX, mc.thePlayer.entityBoundingBox.minY - 0.015626,
                mc.thePlayer.entityBoundingBox.minZ)
            )) {
            mc.thePlayer.motionX *= yPortSpeedValue.get().toDouble()
            mc.thePlayer.motionZ *= yPortSpeedValue.get().toDouble()
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
            if (mc.thePlayer.onGround && jesus.isLiquidBlock(
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
                event.packet.y =
                    mc.thePlayer.posY + if (nextTick) yPortUpValue.get().toDouble() else -yPortDownValue.get()
                        .toDouble()
                if (timer.hasTimePassed(
                        yPortConvertDelayValue.get().toLong()
                    ) && yPortConvertValue.get() && !yPortGroundValue.get()
                ) {
                    event.packet.onGround = true
                    timer.reset()
                } else {
                    event.packet.onGround = yPortGroundValue.get()
                }
            }
        }
    }
}