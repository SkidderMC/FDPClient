/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.RotationUpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getCenterDistance
import net.ccbluex.liquidbounce.utils.block.block
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBlockBox
import net.ccbluex.liquidbounce.utils.rotation.RotationPriority
import net.ccbluex.liquidbounce.utils.rotation.RotationSettings
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.faceBlock
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.setTargetRotation
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.START_DESTROY_BLOCK
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MovingObjectPosition
import java.awt.Color

object PacketMine : Module("PacketMine", Category.OTHER, Category.SubCategory.MISCELLANEOUS, gameDetecting = false) {

    private val range by float("Range", 5F, 1F..6F)
    private val instant by boolean("Instant", true)
    private val visualSwing by boolean("VisualSwing", true).subjective()
    private val esp by boolean("ESP", true).subjective()

    private val options = RotationSettings(this).withoutKeepRotation().withRequestPriority(RotationPriority.NORMAL)

    private var blockPos: BlockPos? = null
    private var enumFacing: EnumFacing? = null

    private fun isMineable(pos: BlockPos): Boolean {
        val block = pos.block ?: return false
        return block != Blocks.air && block != Blocks.bedrock && getCenterDistance(pos) <= range
    }

    val onRotationUpdate = handler<RotationUpdateEvent> {
        mc.theWorld ?: return@handler

        val target = mc.objectMouseOver?.takeIf { it.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK }?.blockPos
            ?.takeIf { isMineable(it) }

        if (target == null) {
            blockPos = null
            enumFacing = null
            return@handler
        }

        blockPos = target
        enumFacing = mc.objectMouseOver?.sideHit ?: enumFacing ?: EnumFacing.UP

        if (options.rotationsActive) {
            val spot = faceBlock(target) ?: return@handler
            setTargetRotation(spot.rotation, options = options)
        }
    }

    val onTick = handler<GameTickEvent> {
        val pos = blockPos ?: return@handler
        val facing = enumFacing ?: return@handler

        if (!isMineable(pos)) {
            blockPos = null
            enumFacing = null
            return@handler
        }

        if (visualSwing) {
            mc.thePlayer.swingItem()
        } else {
            sendPacket(C0APacketAnimation())
        }

        if (instant) {
            sendPackets(
                C07PacketPlayerDigging(START_DESTROY_BLOCK, pos, facing),
                C07PacketPlayerDigging(STOP_DESTROY_BLOCK, pos, facing)
            )
        } else {
            sendPacket(C07PacketPlayerDigging(START_DESTROY_BLOCK, pos, facing))
            mc.playerController.clickBlock(pos, facing)
        }
    }

    val onRender3D = handler<Render3DEvent> {
        if (!esp) return@handler
        drawBlockBox(blockPos ?: return@handler, Color.RED, true)
    }

    override fun onDisable() {
        blockPos?.let { pos ->
            enumFacing?.let { facing ->
                sendPacket(C07PacketPlayerDigging(STOP_DESTROY_BLOCK, pos, facing))
            }
        }
        blockPos = null
        enumFacing = null
    }
}
