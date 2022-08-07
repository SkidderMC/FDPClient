/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.Block
import net.minecraft.block.BlockLiquid
import net.minecraft.block.material.Material
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.MovementInput
import kotlin.math.*

@ModuleInfo(name = "Jesus", category = ModuleCategory.MOVEMENT)
class Jesus : Module() {
    val modeValue = ListValue("Mode", arrayOf("Vanilla", "NCP", "Jump", "AAC", "AACFly", "AAC3.3.11", "AAC4.2.1", "Horizon1.4.6", "Spartan", "Twilight", "Matrix", "Medusa","Vulcan", "Dolphin", "Legit","MatrixXD"), "Vanilla")
    private val noJumpValue = BoolValue("NoJump", false)
    private val jumpMotionValue = FloatValue("JumpMotion", 0.5f, 0.1f, 1f)
        .displayable { modeValue.equals("Jump") || modeValue.equals("AACFly") }

    private var nextTick = false
    private val msTimer = MSTimer()
    private fun isLiquidBlock(bb: AxisAlignedBB = mc.thePlayer.entityBoundingBox): Boolean {
        return BlockUtils.collideBlock(bb) { it is BlockLiquid }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (mc.thePlayer == null || mc.thePlayer.isSneaking) {
            return
        }

        val blockPos = mc.thePlayer.position.down()

        when (modeValue.get().lowercase()) {
            "matrixxd" -> {
                setSpeed(9.9)

                if(BlockUtils.getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 0.0000001, mc.thePlayer.posZ)) == Blocks.water) {
                    MovementUtils.resetMotion(false)
                    mc.thePlayer.fallDistance = 0.0f
                    mc.thePlayer.motionY = 0.06
                    mc.thePlayer.jumpMovementFactor = 0.01f
                }
            }

            "ncp","medusa","vulcan" -> {
                if (isLiquidBlock() && mc.thePlayer.isInsideOfMaterial(Material.air)) {
                    mc.thePlayer.motionY = 0.08
                }
            }
            "jump" -> {
                if (BlockUtils.getBlock(blockPos) === Blocks.water && mc.thePlayer.onGround) {
                    mc.thePlayer.motionY = jumpMotionValue.get().toDouble()
                }
            }
            "aac" -> {
                if (!mc.thePlayer.onGround && BlockUtils.getBlock(blockPos) === Blocks.water || mc.thePlayer.isInWater) {
                    if (!mc.thePlayer.isSprinting) {
                        mc.thePlayer.motionX *= 0.99999
                        mc.thePlayer.motionY *= 0.0
                        mc.thePlayer.motionZ *= 0.99999
                        if (mc.thePlayer.isCollidedHorizontally) {
                            mc.thePlayer.motionY = ((mc.thePlayer.posY - (mc.thePlayer.posY - 1).toInt()).toInt() / 8f).toDouble()
                        }
                    } else {
                        mc.thePlayer.motionX *= 0.99999
                        mc.thePlayer.motionY *= 0.0
                        mc.thePlayer.motionZ *= 0.99999
                        if (mc.thePlayer.isCollidedHorizontally) {
                            mc.thePlayer.motionY = ((mc.thePlayer.posY - (mc.thePlayer.posY - 1).toInt()).toInt() / 8f).toDouble()
                        }
                    }
                    if (mc.thePlayer.fallDistance >= 4) {
                        mc.thePlayer.motionY = -0.004
                    } else if (mc.thePlayer.isInWater) mc.thePlayer.motionY = 0.09
                }
                if (mc.thePlayer.hurtTime != 0) {
                    mc.thePlayer.onGround = false
                }
            }
            "matrix" -> {
                if (mc.thePlayer.isInWater) {
                    mc.gameSettings.keyBindJump.pressed = false
                    if (mc.thePlayer.isCollidedHorizontally) {
                        mc.thePlayer.motionY = +0.09
                        return
                    }
                    val block = BlockUtils.getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 1, mc.thePlayer.posZ))
                    val blockUp = BlockUtils.getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 1.1, mc.thePlayer.posZ))
                    if (blockUp is BlockLiquid) {
                        mc.thePlayer.motionY = 0.1
                    } else if (block is BlockLiquid) {
                        mc.thePlayer.motionY = 0.0
                    }
                    mc.thePlayer.motionX *= 1.15
                    mc.thePlayer.motionZ *= 1.15
                }
             }
            "spartan" -> if (mc.thePlayer.isInWater) {
                if (mc.thePlayer.isCollidedHorizontally) {
                    mc.thePlayer.motionY += 0.15
                    return
                }
                val block = getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 1, mc.thePlayer.posZ))
                val blockUp = getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 1.1, mc.thePlayer.posZ))
                if (blockUp is BlockLiquid) {
                    mc.thePlayer.motionY = 0.1
                } else if (block is BlockLiquid) {
                    mc.thePlayer.motionY = 0.0
                }
                mc.thePlayer.onGround = true
                mc.thePlayer.motionX *= 1.085
                mc.thePlayer.motionZ *= 1.085
            }
            "aac3.3.11" -> {
                if (mc.thePlayer.isInWater) {
                    mc.thePlayer.motionX *= 1.17
                    mc.thePlayer.motionZ *= 1.17
                    if (mc.thePlayer.isCollidedHorizontally) {
                        mc.thePlayer.motionY = 0.24
                    } else if (mc.theWorld.getBlockState(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 1.0, mc.thePlayer.posZ)).block !== Blocks.air) {
                        mc.thePlayer.motionY += 0.04
                    }
                }
            }
            "dolphin" -> {
                if (mc.thePlayer.isInWater) {
                    mc.thePlayer.motionY += 0.03999999910593033
                }
            }
            "aac4.2.1" -> {
                if (!mc.thePlayer.onGround && BlockUtils.getBlock(blockPos) === Blocks.water || mc.thePlayer.isInWater) {
                    mc.thePlayer.motionY *= 0.0
                    mc.thePlayer.jumpMovementFactor = 0.08f
                    if (mc.thePlayer.fallDistance > 0) {
                        return
                    } else if (mc.thePlayer.isInWater) {
                        mc.gameSettings.keyBindJump.pressed = true
                    }
                }
            }
            "horizon1.4.6" -> {
                mc.gameSettings.keyBindJump.pressed = mc.thePlayer.isInWater
                if (mc.thePlayer.isInWater) {
                    MovementUtils.strafe()
                    if (MovementUtils.isMoving() && !mc.thePlayer.onGround) {
                        mc.thePlayer.motionY += 0.13
                    }
                }
            }
            "twilight" -> {
                if (mc.thePlayer.isInWater) {
                    mc.thePlayer.motionX *= 1.04
                    mc.thePlayer.motionZ *= 1.04
                    MovementUtils.strafe()
                }
            }
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if (!mc.thePlayer.isInWater) {
            return
        }

        when (modeValue.get().lowercase()) {
            "aacfly" -> {
                event.y = jumpMotionValue.get().toDouble()
                mc.thePlayer.motionY = jumpMotionValue.get().toDouble()
            }
            "twilight" -> {
                event.y = 0.01
                mc.thePlayer.motionY = 0.01
            }
        }
    }

    @EventTarget
    fun onBlockBB(event: BlockBBEvent) {
        if (mc.thePlayer == null || mc.thePlayer.entityBoundingBox == null) {
            return
        }

        if (event.block is BlockLiquid && !isLiquidBlock() && !mc.thePlayer.isSneaking) {
            when (modeValue.get().lowercase()) {
                "ncp", "vanilla", "jump", "medusa","vulcan" -> {
                    event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), (event.x + 1).toDouble(), (event.y + 1).toDouble(), (event.z + 1).toDouble())
                    if (modeValue.get() == "Vulcan") 
                        MovementUtils.strafe(MovementUtils.getSpeed() * 0.39f)
                }
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.thePlayer == null) {
            return
        }

        if (event.packet is C03PacketPlayer) {
            when (modeValue.get()) {
                "NCP" -> {
                    if (isLiquidBlock(AxisAlignedBB(mc.thePlayer.entityBoundingBox.maxX, mc.thePlayer.entityBoundingBox.maxY,
                        mc.thePlayer.entityBoundingBox.maxZ, mc.thePlayer.entityBoundingBox.minX, mc.thePlayer.entityBoundingBox.minY - 0.01,
                        mc.thePlayer.entityBoundingBox.minZ))) {
                            nextTick = !nextTick
                            if (nextTick) {
                                event.packet.y -= 0.001
                            }
                        }
                }
                "Medusa" -> {
                    nextTick = !nextTick
                    event.packet.y = mc.thePlayer.posY + if (nextTick) 0.1 else -0.1
                    if (msTimer.hasTimePassed(1000)) {
                        event.packet.onGround = true
                        msTimer.reset()
                    } else {
                        event.packet.onGround = false
                    }
                }
                "Vulcan" -> {
                    nextTick = !nextTick
                    event.packet.y = mc.thePlayer.posY + if (nextTick) 0.1 else -0.1
                    if (msTimer.hasTimePassed(1500)) {
                        event.cancelEvent()
                        PacketUtils.sendPacketNoEvent(C03PacketPlayer(true))
                        msTimer.reset()
                    } else {
                        event.packet.onGround = false
                    }
                }
            }

        }
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (mc.thePlayer == null) {
            return
        }

        val block = BlockUtils.getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.01, mc.thePlayer.posZ))
        if ((noJumpValue.get() || modeValue.get().equals("Vulcan"))&& block is BlockLiquid) {
            event.cancelEvent()
        }
    }

    override val tag: String
        get() = modeValue.get()

    fun setSpeed(speed: Double) {
        var forward = mc.thePlayer.movementInput.moveForward.toDouble()
        var strafe = mc.thePlayer.movementInput.moveStrafe.toDouble()
        var yaw = mc.thePlayer.rotationYaw
        if (forward == 0.0 && strafe == 0.0) {
            MovementUtils.resetMotion(false)
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += if(forward > 0.0) -45 else 45
                } else if (strafe < 0.0) {
                    yaw += if(forward > 0.0) 45 else -45
                }
                strafe = 0.0
                if (forward > 0.0) {
                    forward = 1.0
                } else if (forward < 0.0) {
                    forward = -1.0
                }
            }
            mc.thePlayer.motionX = forward * speed * cos(Math.toRadians(yaw + 90.0)) + strafe * speed * sin(Math.toRadians(yaw + 90.0))
            mc.thePlayer.motionZ = forward * speed * sin(Math.toRadians(yaw + 90.0)) - strafe * speed * cos(Math.toRadians(yaw + 90.0))
        }
    }
}
