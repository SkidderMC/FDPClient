/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.block.Block
import net.minecraft.block.BlockPane
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

@ModuleInfo(name = "HighJump", category = ModuleCategory.MOVEMENT)
object HighJump : Module() {

    private val heightValue = FloatValue("Height", 2f, 1.1f, 7f)
    private val modeValue = ListValue("Mode", arrayOf("Vanilla", "StableMotion", "Damage", "AACv3", "DAC", "Mineplex", "Matrix", "MatrixWater"), "Vanilla")
    private val glassValue = BoolValue("OnlyGlassPane", false)
    private val stableMotionValue = FloatValue("StableMotion", 0.42f, 0.1f, 1f).displayable { modeValue.equals("StableMotion") }
    private var jumpY = 114514.0

    private var martrixStatus = 0
    private var martrixWasTimer = false

    private val timer = MSTimer()

    override fun onEnable() {
        jumpY = 114514.0
        martrixStatus = 0
        martrixWasTimer = false
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (glassValue.get() && getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)) !is BlockPane) return

        when (modeValue.get().lowercase()) {
            "damage" -> {
                if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.onGround) mc.thePlayer.motionY += (0.42f * heightValue.get()).toDouble()
            }

            "aacv3" -> {
                if (!mc.thePlayer.onGround) mc.thePlayer.motionY += 0.059
            }

            "dac" -> {
                if (!mc.thePlayer.onGround) mc.thePlayer.motionY += 0.049999
            }

            "mineplex" -> {
                if (!mc.thePlayer.onGround) MovementUtils.strafe(0.35f)
            }

            "stablemotion" -> {
                if (jumpY != 114514.0) {
                    if (jumpY + heightValue.get() - 1 > mc.thePlayer.posY) {
                        mc.thePlayer.motionY = stableMotionValue.get().toDouble()
                    } else {
                        jumpY = 114514.0
                    }
                }
            }
            "matrixWater" -> {
                if (mc.thePlayer.isInWater) {
                    if (mc.theWorld.getBlockState(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 1, mc.thePlayer.posZ)).block == Block.getBlockById(9)) {
                        mc.thePlayer.motionY = 0.18
                    } else if (mc.theWorld.getBlockState(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)).block == Block.getBlockById(9)) {
                        mc.thePlayer.motionY = heightValue.get().toDouble()
                        mc.thePlayer.onGround = true
                    }
                }
            }
            "matrix" -> {
                if (martrixWasTimer) {
                    mc.timer.timerSpeed = 1.00f
                    martrixWasTimer = false
                }
                if ((mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(0.0, mc.thePlayer.motionY, 0.0).expand(0.0, 0.0, 0.0)).isNotEmpty() ||
                            mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(0.0, -4.0, 0.0).expand(0.0, 0.0, 0.0)).isNotEmpty()) &&
                    mc.thePlayer.fallDistance > 10) {
                    if (!mc.thePlayer.onGround) {
                        mc.timer.timerSpeed = 0.1f
                        martrixWasTimer = true
                    }
                }
                if (timer.hasTimePassed(1000) && martrixStatus == 1) {
                    mc.timer.timerSpeed = 1.0f
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                    martrixStatus = 0
                    return
                }
                if (martrixStatus == 1 && mc.thePlayer.hurtTime > 0) {
                    mc.timer.timerSpeed = 1.0f
                    mc.thePlayer.motionY = 3.0
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                    mc.thePlayer.jumpMovementFactor = 0.00f
                    martrixStatus = 0
                    return
                }
                if (martrixStatus == 2) {
                    mc.thePlayer.sendQueue.addToSendQueue(C0APacketAnimation())
                    mc.thePlayer.sendQueue.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false))
                    repeat(8) {
                        mc.thePlayer.sendQueue.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.3990, mc.thePlayer.posZ, false))
                        mc.thePlayer.sendQueue.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false))
                    }
                    mc.thePlayer.sendQueue.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
                    mc.thePlayer.sendQueue.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
                    mc.timer.timerSpeed = 0.6f
                    martrixStatus = 1
                    timer.reset()
                    mc.thePlayer.sendQueue.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ), EnumFacing.UP))
                    mc.thePlayer.sendQueue.addToSendQueue(C0APacketAnimation())
                    return
                }
                if (mc.thePlayer.isCollidedHorizontally && martrixStatus == 0 && mc.thePlayer.onGround) {
                    mc.thePlayer.sendQueue.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ), EnumFacing.UP))
                    mc.thePlayer.sendQueue.addToSendQueue(C0APacketAnimation())
                    martrixStatus = 2
                    mc.timer.timerSpeed = 0.05f
                }
                if (mc.thePlayer.isCollidedHorizontally && mc.thePlayer.onGround) {
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                    mc.thePlayer.onGround = false
                }
            }
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if (glassValue.get() && getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)) !is BlockPane) return

        if (!mc.thePlayer.onGround) {
            if ("mineplex" == modeValue.get().lowercase()) {
                mc.thePlayer.motionY += if (mc.thePlayer.fallDistance == 0f) 0.0499 else 0.05
            }
        }
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (glassValue.get() && getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)) !is BlockPane) return

        when (modeValue.get().lowercase()) {
            "vanilla" -> {
                event.motion = event.motion * heightValue.get()
            }

            "mineplex" -> {
                event.motion = 0.47f
            }

            "stablemotion" -> {
                jumpY = mc.thePlayer.posY
            }
        }
    }

    override val tag: String
        get() = modeValue.get()
}
