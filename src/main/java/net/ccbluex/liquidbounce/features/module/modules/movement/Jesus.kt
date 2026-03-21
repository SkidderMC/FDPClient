/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.utils.block.BlockUtils.collideBlock
import net.ccbluex.liquidbounce.utils.block.block
import net.ccbluex.liquidbounce.utils.movement.MovementUtils
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.block.BlockLiquid
import net.minecraft.client.settings.GameSettings
import net.minecraft.block.material.Material
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import org.lwjgl.input.Keyboard

object Jesus : Module("Jesus", Category.MOVEMENT, Category.SubCategory.MOVEMENT_MAIN,Keyboard.KEY_J) {

    val mode by choices(
        "Mode",
        arrayOf(
            "Vanilla", "NCP", "AAC", "AAC3.3.11", "AAC4.2.1", "AACFly", "Spartan",
            "Matrix", "Jump", "Legit", "SilentYPort", "Twillight", "Vulcan", "Dolphin"
        ),
        "NCP"
    )
    private val aacFly by float("AACFlyMotion", 0.5f, 0.1f..1f) { mode == "AACFly" }
    private val jumpMotion by float("Jump-Motion", 0.5f, 0.1f..1f) { mode == "Jump" }
    private val silentYPortUp by float("SilentYPort-Up", 0.1f, 0f..0.5f) { mode == "SilentYPort" }
    private val silentYPortDown by float("SilentYPort-Down", 0.1f, 0f..0.5f) { mode == "SilentYPort" }
    private val silentYPortSpeedModify by float("SilentYPort-SpeedModify", 1.0f, 0f..1.5f) {
        mode == "SilentYPort"
    }
    private val silentYPortSpoofGround by boolean("SilentYPort-SpoofGround", false) { mode == "SilentYPort" }
    private val silentYPortConvertGround by boolean("SilentYPort-ConvertGround", true) { mode == "SilentYPort" }
    private val silentYPortConvertDelay by int("SilentYPort-ConvertDelay", 1000, 0..2000) {
        mode == "SilentYPort" && silentYPortConvertGround
    }

    private val noJump by boolean("NoJump", false)

    private var nextTick = false
    private var silentYPortNextTick = false
    private val silentYPortTimer = MSTimer()
    private var vulcanStep = 0

    private fun isLiquidBlock(bb: AxisAlignedBB = mc.thePlayer.entityBoundingBox): Boolean {
        return collideBlock(bb) { it is BlockLiquid }
    }

    override fun onEnable() {
        nextTick = false
        silentYPortNextTick = false
        vulcanStep = 0
        silentYPortTimer.reset()
    }

    override fun onDisable() {
        mc.thePlayer?.let {
            mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        }
    }

    val onUpdate = handler<UpdateEvent> {
        val thePlayer = mc.thePlayer

        if (thePlayer == null || thePlayer.isSneaking) return@handler

        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)

        when (mode.lowercase()) {
            "ncp" -> if (collideBlock(thePlayer.entityBoundingBox) { it is BlockLiquid } && thePlayer.isInsideOfMaterial(
                    Material.air
                ) && !thePlayer.isSneaking) thePlayer.motionY = 0.08

            "aac" -> {
                val blockPos = thePlayer.position.down()
                if (!thePlayer.onGround && blockPos.block == Blocks.water || thePlayer.isInWater) {
                    if (!thePlayer.isSprinting) {
                        thePlayer.motionX *= 0.99999
                        thePlayer.motionY *= 0.0
                        thePlayer.motionZ *= 0.99999
                        if (thePlayer.isCollidedHorizontally) thePlayer.motionY =
                            ((thePlayer.posY - (thePlayer.posY - 1).toInt()).toInt() / 8f).toDouble()
                    } else {
                        thePlayer.motionX *= 0.99999
                        thePlayer.motionY *= 0.0
                        thePlayer.motionZ *= 0.99999
                        if (thePlayer.isCollidedHorizontally) thePlayer.motionY =
                            ((thePlayer.posY - (thePlayer.posY - 1).toInt()).toInt() / 8f).toDouble()
                    }
                    if (thePlayer.fallDistance >= 4) thePlayer.motionY =
                        -0.004 else if (thePlayer.isInWater) thePlayer.motionY = 0.09
                }
                if (thePlayer.hurtTime != 0) thePlayer.onGround = false
            }

            "aac4.2.1" -> {
                val blockPos = thePlayer.position.down()
                if (!thePlayer.onGround && blockPos.block == Blocks.water || thePlayer.isInWater) {
                    thePlayer.motionY *= 0.0
                    thePlayer.jumpMovementFactor = 0.08f
                    if (thePlayer.fallDistance <= 0 && thePlayer.isInWater) {
                        mc.gameSettings.keyBindJump.pressed = true
                    }
                }
            }

            "spartan" -> if (thePlayer.isInWater) {
                if (thePlayer.isCollidedHorizontally) {
                    thePlayer.motionY += 0.15
                    return@handler
                }
                val block = BlockPos(thePlayer).up().block
                val blockUp = BlockPos(thePlayer.posX, thePlayer.posY + 1.1, thePlayer.posZ).block

                if (blockUp is BlockLiquid) {
                    thePlayer.motionY = 0.1
                } else if (block is BlockLiquid) {
                    thePlayer.motionY = 0.0
                }

                thePlayer.onGround = true
                thePlayer.motionX *= 1.085
                thePlayer.motionZ *= 1.085
            }

            "matrix" -> if (thePlayer.isInWater) {
                mc.gameSettings.keyBindJump.pressed = false
                if (thePlayer.isCollidedHorizontally) {
                    thePlayer.motionY = 0.09
                    return@handler
                }
                val block = BlockPos(thePlayer.posX, thePlayer.posY + 1, thePlayer.posZ).block
                val blockUp = BlockPos(thePlayer.posX, thePlayer.posY + 1.1, thePlayer.posZ).block
                if (blockUp is BlockLiquid) {
                    thePlayer.motionY = 0.1
                } else if (block is BlockLiquid) {
                    thePlayer.motionY = 0.0
                }
                thePlayer.motionX *= 1.15
                thePlayer.motionZ *= 1.15
            }

            "aac3.3.11" -> if (thePlayer.isInWater) {
                thePlayer.motionX *= 1.17
                thePlayer.motionZ *= 1.17
                if (thePlayer.isCollidedHorizontally)
                    thePlayer.motionY = 0.24
                else if (BlockPos(thePlayer).up().block != Blocks.air)
                    thePlayer.motionY += 0.04
            }

            "jump" -> if (thePlayer.position.down().block == Blocks.water && thePlayer.onGround) {
                thePlayer.motionY = jumpMotion.toDouble()
            }

            "silentyport" -> if (thePlayer.onGround && isLiquidBlock(
                    AxisAlignedBB(
                        thePlayer.entityBoundingBox.maxX,
                        thePlayer.entityBoundingBox.maxY,
                        thePlayer.entityBoundingBox.maxZ,
                        thePlayer.entityBoundingBox.minX,
                        thePlayer.entityBoundingBox.minY - 0.015626,
                        thePlayer.entityBoundingBox.minZ
                    )
                )
            ) {
                thePlayer.motionX *= silentYPortSpeedModify.toDouble()
                thePlayer.motionZ *= silentYPortSpeedModify.toDouble()
            }

            "twillight" -> if (thePlayer.isInWater) {
                thePlayer.motionX *= 1.04
                thePlayer.motionZ *= 1.04
                MovementUtils.strafe()
            }

            "vulcan" -> if (isLiquidBlock() && thePlayer.isInsideOfMaterial(Material.air)) {
                thePlayer.motionY = 0.08
            }

            "dolphin" -> if (thePlayer.isInWater) thePlayer.motionY += 0.03999999910593033
        }
    }

    val onMove = handler<MoveEvent> { event ->
        when (mode.lowercase()) {
            "aacfly" -> if (mc.thePlayer.isInWater) {
                event.y = aacFly.toDouble()
                mc.thePlayer.motionY = aacFly.toDouble()
            }

            "twillight" -> if (mc.thePlayer.isInWater) {
                event.y = 0.01
                mc.thePlayer.motionY = 0.01
            }
        }
    }

    val onBlockBB = handler<BlockBBEvent> { event ->
        if (mc.thePlayer == null)
            return@handler

        if (event.block is BlockLiquid && !collideBlock(mc.thePlayer.entityBoundingBox) { it is BlockLiquid } && !mc.thePlayer.isSneaking) {
            when (mode.lowercase()) {
                "vanilla", "ncp", "jump", "silentyport", "vulcan" -> event.boundingBox = AxisAlignedBB.fromBounds(
                    event.x.toDouble(),
                    event.y.toDouble(),
                    event.z.toDouble(),
                    event.x + 1.toDouble(),
                    event.y + 1.toDouble(),
                    event.z + 1.toDouble()
                )
            }
        }
    }

    val onPacket = handler<PacketEvent> { event ->
        val thePlayer = mc.thePlayer

        if (thePlayer == null || event.packet !is C03PacketPlayer)
            return@handler

        val packetPlayer = event.packet

        when (mode.lowercase()) {
            "ncp" -> if (collideBlock(
                    AxisAlignedBB.fromBounds(
                        thePlayer.entityBoundingBox.maxX,
                        thePlayer.entityBoundingBox.maxY,
                        thePlayer.entityBoundingBox.maxZ,
                        thePlayer.entityBoundingBox.minX,
                        thePlayer.entityBoundingBox.minY - 0.01,
                        thePlayer.entityBoundingBox.minZ
                    )
                ) { it is BlockLiquid }
            ) {
                nextTick = !nextTick
                if (nextTick) {
                    packetPlayer.y -= 0.001
                }
            }

            "silentyport" -> if (thePlayer.onGround && isLiquidBlock(
                    AxisAlignedBB(
                        thePlayer.entityBoundingBox.maxX,
                        thePlayer.entityBoundingBox.maxY,
                        thePlayer.entityBoundingBox.maxZ,
                        thePlayer.entityBoundingBox.minX,
                        thePlayer.entityBoundingBox.minY - 0.01,
                        thePlayer.entityBoundingBox.minZ
                    )
                )
            ) {
                silentYPortNextTick = !silentYPortNextTick
                packetPlayer.y = thePlayer.posY + if (silentYPortNextTick) {
                    silentYPortUp.toDouble()
                } else {
                    -silentYPortDown.toDouble()
                }

                if (silentYPortConvertGround && !silentYPortSpoofGround && silentYPortTimer.hasTimePassed(silentYPortConvertDelay.toLong())) {
                    packetPlayer.onGround = true
                    silentYPortTimer.reset()
                } else {
                    packetPlayer.onGround = silentYPortSpoofGround
                }
            }

            "vulcan" -> if (isLiquidBlock(
                    AxisAlignedBB(
                        thePlayer.entityBoundingBox.maxX,
                        thePlayer.entityBoundingBox.maxY,
                        thePlayer.entityBoundingBox.maxZ,
                        thePlayer.entityBoundingBox.minX,
                        thePlayer.entityBoundingBox.minY - 0.01,
                        thePlayer.entityBoundingBox.minZ
                    )
                )
            ) {
                vulcanStep++
                packetPlayer.onGround = false
                when (vulcanStep) {
                    1 -> packetPlayer.y += 0.08232659236482401
                    2 -> packetPlayer.y += 0.13927999979019162
                    3 -> packetPlayer.y += 0.16542399994277950
                    4 -> packetPlayer.y += 0.11427136035293574
                    5 -> packetPlayer.y += 0.04194693730418576
                    6 -> {
                        packetPlayer.y += 0.01236341326161235
                        vulcanStep = 0
                    }
                }
            }
        }
    }

    val onJump = handler<JumpEvent> { event ->
        val thePlayer = mc.thePlayer ?: return@handler

        val block = BlockPos(thePlayer.posX, thePlayer.posY - 0.01, thePlayer.posZ).block

        if (noJump && block is BlockLiquid)
            event.cancelEvent()
    }

    override val tag
        get() = mode
}
