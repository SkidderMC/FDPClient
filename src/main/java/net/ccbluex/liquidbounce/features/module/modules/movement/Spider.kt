/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.movement.MovementUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils.collideBlockIntersects
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.utils.client.pos
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.minecraft.block.BlockAir
import net.minecraft.block.BlockLadder
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

object Spider : Module("Spider", Category.MOVEMENT) {

    private val modeValue by choices("Mode", arrayOf("Collide", "Motion", "AAC3.3.12", "AAC4", "Checker", "Vulcan", "Polar"), "Collide")
    private val motionValue by float("Motion", 0.42F, 0.1F..1F) { modeValue == "Motion" }
    private val avoidLadderValue by boolean("AvoidLadder", false)

    private var groundHeight = 0.0
    private var glitch = false
    private var wasTimer = false
    private var ticks = 0

    val onUpdate = handler<UpdateEvent> {
        resetTimerIfNeeded()
        if (!canExecuteMovementLogic()) return@handler

        if (avoidLadderValue && isBlockAboveLadder()) return@handler
        updateGroundHeight()

        when (modeValue.lowercase()) {
            "collide" -> handleCollideMode()
            "aac4" -> handleAAC4Mode()
            "aac3.3.12" -> handleAAC3Mode()
            "motion" -> handleMotionMode()
            "checker" -> handleCheckerMode()
            "vulcan" -> handleVulcanMode()
            "polar" -> handlePolar()
        }
    }

    val onMove = handler<MoveEvent> { event ->
        if (shouldHaltMovementForChecker()) {
            event.x = 0.0
            event.z = 0.0
            event.y = motionValue.toDouble()
        }
    }

    val onPacket = handler<PacketEvent> { event ->
        val packet = event.packet

        if (packet is C03PacketPlayer) {
            handlePacketForGlitch(packet)
            handlePacketForVulcan(packet)
        }
    }

    val onBlockBB = handler<BlockBBEvent> { event ->
        if (shouldOverrideBoundingBoxForChecker(event)) {
            event.boundingBox = AxisAlignedBB.fromBounds(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        }

        if (shouldOverrideBoundingBoxForModes(event)) {
            event.boundingBox = AxisAlignedBB.fromBounds(
                event.x.toDouble(),
                event.y.toDouble(),
                event.z.toDouble(),
                event.x + 1.0,
                floor(mc.thePlayer.posY),
                event.z + 1.0
            )
        }

        if (modeValue == "Polar") {
            handlePolar(event)
        }
    }

    override fun onDisable() {
        resetTimer()
        wasTimer = false
    }

    private fun resetTimerIfNeeded() {
        if (wasTimer) {
            mc.timer.timerSpeed = 1.0f
        }
    }

    private fun canExecuteMovementLogic(): Boolean {
        return mc.thePlayer.isCollidedHorizontally && mc.thePlayer.isMoving &&
                collideBlockIntersects(mc.thePlayer.entityBoundingBox) { block -> block !is BlockAir }
    }

    private fun isBlockAboveLadder(): Boolean {
        val blockAbovePlayer = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 1, mc.thePlayer.posZ)
        return mc.theWorld.getBlockState(blockAbovePlayer).block is BlockLadder
    }

    private fun updateGroundHeight() {
        if (mc.thePlayer.onGround) {
            groundHeight = mc.thePlayer.posY
        }
    }

    private fun handleCollideMode() {
        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump()
        }
    }

    private fun handleAAC4Mode() {
        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump()
            wasTimer = true
            mc.timer.timerSpeed = 0.4f
        }
    }

    private fun handleAAC3Mode() {
        if (mc.thePlayer.onGround) ticks = 0
        ticks++

        when (ticks) {
            1, 12, 23 -> mc.thePlayer.motionY = 0.43
            29 -> mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.5, mc.thePlayer.posZ)
            else -> if (ticks >= 30) ticks = 0
        }
    }

    private fun handleMotionMode() {
        mc.thePlayer.motionY = motionValue.toDouble()
    }

    private fun handleCheckerMode() {
        if (mc.thePlayer.isCollidedHorizontally && mc.thePlayer.onGround) {
            mc.thePlayer.jump()
        }
    }

    private fun handleVulcanMode() {
        if (mc.thePlayer.onGround) {
            ticks = 0
            mc.thePlayer.jump()
        }
        ticks++

        if (ticks in 2..3) {
            mc.thePlayer.jump()
            MovementUtils.resetMotion(false)
        }
    }

    private fun handlePolar() {
    }

    private fun handlePolar(event: BlockBBEvent) {
        event.boundingBox?.let { bb ->
            if (event.pos.y >= mc.thePlayer.posY || (mc.thePlayer.isSneaking && mc.thePlayer.onGround)) {
                event.boundingBox = AxisAlignedBB(
                    bb.minX + 0.0001, bb.minY, bb.minZ + 0.0001,
                    bb.maxX - 0.0001, bb.maxY, bb.maxZ - 0.0001
                )
            }
        }
    }

    private fun shouldHaltMovementForChecker(): Boolean {
        return modeValue == "Checker" &&
                collideBlockIntersects(mc.thePlayer.entityBoundingBox) { block -> block !is BlockAir } &&
                mc.thePlayer.movementInput.moveForward > 0.0
    }

    private fun handlePacketForGlitch(packet: C03PacketPlayer) {
        if (glitch) {
            glitch = false
            val yaw = MovementUtils.direction.toFloat()
            packet.x -= sin(yaw) * 0.00000001
            packet.z += cos(yaw) * 0.00000001
        }
    }

    private fun handlePacketForVulcan(packet: C03PacketPlayer) {
        if (modeValue == "Vulcan") {
            when (ticks) {
                3 -> {
                    val yaw = MovementUtils.direction.toFloat()
                    val randomOffset = Math.random() * 0.03 + 0.22
                    packet.y -= 0.1
                    packet.x += sin(yaw) * randomOffset
                    packet.z -= cos(yaw) * randomOffset
                }
                2 -> packet.onGround = true
            }
        }
    }

    private fun shouldOverrideBoundingBoxForChecker(event: BlockBBEvent): Boolean {
        return modeValue == "Checker" &&
                (collideBlockIntersects(mc.thePlayer.entityBoundingBox) { block -> block !is BlockAir } ||
                        mc.thePlayer.isCollidedHorizontally)
    }

    private fun shouldOverrideBoundingBoxForModes(event: BlockBBEvent): Boolean {
        return (modeValue == "Collide" || modeValue == "AAC4") &&
                mc.thePlayer.isCollidedHorizontally &&
                mc.thePlayer.motionY <= 0.0
    }

    private fun resetTimer() {
        mc.timer.timerSpeed = 1.0f
    }

    override val tag: String
        get() = modeValue
}