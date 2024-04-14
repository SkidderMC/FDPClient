/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.ncp

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.Block
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.potion.Potion
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import net.minecraft.util.Timer
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

class NCPSpeeds : SpeedMode("NCP") {

    private val modeValue = ListValue("NCP-Mode", arrayOf("NCPBhop", "LNCPHop", "NCPFHop", "NCPHop", "NCPLatest", "NCPStable"), "NCPHop")

    private val ncpstabletimerValue = FloatValue("${valuePrefix}Timer", 1.088f, 1f, 2f).displayable { modeValue.equals("NCPStable") }
    private val ncpstablejumpMovementFactorValue = FloatValue("${valuePrefix}Speed", 0.029f, 0f, 0.1f).displayable { modeValue.equals("NCPStable") }

    // Variables
    private var level = 1
    private var moveSpeed = 0.2873
    private var lastDist = 0.0
    private var timerDelay = 0
    private var wasSlow = false
    private var mspeed = 0.0
    private var justJumped = false

    // Optimize code
    val player: EntityPlayerSP
        get() = mc.thePlayer
    val timer: Timer
        get() = mc.timer



    override fun onEnable() {
        when (modeValue.get()) {
            "NCPBhop" -> {
                timer.timerSpeed = 1f
                level = if (mc.theWorld.getCollidingBoundingBoxes(
                        player,
                        player.entityBoundingBox.offset(0.0, player.motionY, 0.0)
                    ).size > 0 || player.isCollidedVertically
                ) 1 else 4
            }
            "NCPFHop" -> {
                timer.timerSpeed = 1.0866f
                super.onEnable()
            }
            "NCPHop" -> {
                timer.timerSpeed = 1.0866f
                super.onEnable()
            }
            "NCPStable" -> {
                timer.timerSpeed = ncpstabletimerValue.get()
            }
            "LNCPHop" -> {
                timer.timerSpeed = 1.0865f
            }
        }
    }

    override fun onDisable() {
        when (modeValue.get()) {
            "NCPBhop" -> {
                timer.timerSpeed = 1f
                moveSpeed = baseMoveSpeed
                level = 0
            }
            "NCPFHop" -> {
                player.speedInAir = 0.02f
                timer.timerSpeed = 1f
                super.onDisable()
            }
            "NCPHop" -> {
                player.speedInAir = 0.02f
                timer.timerSpeed = 1f
                super.onDisable()
            }
            "NCPLatest" -> {
                player.jumpMovementFactor = 0.02f
            }
            "NCPStable" -> {
                timer.timerSpeed = 1f
                player.jumpMovementFactor = 0.2f
                player.setVelocity(
                    // reduce the motion a bit to avoid flags but don't stop completely
                    player.motionX / 3,
                    player.motionY,
                    player.motionZ / 3
                )
            }
            "LNCPHop" -> {
                player.speedInAir = 0.02f
                timer.timerSpeed = 1f
            }
        }

    }

    override fun onUpdate() {
        when (modeValue.get()) {
            "NCPFHop" -> {
                if (MovementUtils.isMoving()) {
                    if (player.onGround) {
                        player.jump()
                        player.motionX *= 1.01
                        player.motionZ *= 1.01
                        player.speedInAir = 0.0223f
                    }
                    player.motionY -= 0.00099999
                    MovementUtils.strafe()
                } else {
                    player.motionX = 0.0
                    player.motionZ = 0.0
                }
            }
            "NCPHop" -> {
                if (MovementUtils.isMoving()) {
                    if (player.onGround) {
                        player.jump()
                        player.speedInAir = 0.0223f
                    }
                    MovementUtils.strafe()
                } else {
                    player.motionX = 0.0
                    player.motionZ = 0.0
                }
            }
            "NCPLatest" -> {
                if (player.ticksExisted % 20 <= 9) {
                    timer.timerSpeed = 1.05f
                } else {
                    timer.timerSpeed = 0.98f
                }

                if (MovementUtils.isMoving()) {
                    if (player.onGround) {
                        wasSlow = false
                        player.jump()
                        MovementUtils.strafe(0.47f)
                        if (player.isPotionActive(Potion.moveSpeed)) {
                            MovementUtils.strafe(0.48f * (1.0f + 0.13f * (player.getActivePotionEffect(Potion.moveSpeed).amplifier + 1)))
                        }
                    }
                    if (MovementUtils.getSpeed() < 0.277)
                        wasSlow = true
                    if (wasSlow)
                        MovementUtils.strafe(0.277f)


                } else {
                    player.motionX = 0.0
                    player.motionZ = 0.0
                    wasSlow = true
                }
            }
            "NCPStable" -> {
                if (MovementUtils.isMoving()) {
                    player.jumpMovementFactor = ncpstablejumpMovementFactorValue.get()
                    if (player.onGround) {
                        player.jump()
                    }
                    MovementUtils.strafe(max(MovementUtils.getSpeed(), MovementUtils.getSpeedWithPotionEffects(0.27).toFloat()))
                } else {
                    player.motionX = 0.0
                    player.motionZ = 0.0
                }
            }
            "LNCPHop" -> {
                if (MovementUtils.isMoving()) {
                    if (player.onGround) {
                        player.jump()
                        mspeed = MovementUtils.defaultSpeed() * 1.73

                        justJumped = true
                    } else {
                        if (justJumped) {
                            mspeed *= 0.72150289018
                            justJumped = false
                        } else {
                            mspeed -= mspeed / 159
                        }
                    }
                    if (mspeed < MovementUtils.defaultSpeed())
                        mspeed = MovementUtils.defaultSpeed()

                    MovementUtils.strafe(mspeed.toFloat())
                } else {
                    player.motionX = 0.0
                    player.motionZ = 0.0
                }
            }
        }
    }

    override fun onPreMotion() {
        when (modeValue.get()) {
            "NCPBhop" -> {
                val xDist = player.posX - player.prevPosX
                val zDist = player.posZ - player.prevPosZ
                lastDist = sqrt(xDist * xDist + zDist * zDist)
            }
        }
    }

    override fun onMove(event: MoveEvent) {
        when (modeValue.get()) {
            "NCPBhop" -> {
                ++timerDelay
                timerDelay %= 5

                if (timerDelay != 0) {
                    timer.timerSpeed = 1f
                } else {
                    if (MovementUtils.isMoving()) timer.timerSpeed = 32767f
                    if (MovementUtils.isMoving()) {
                        timer.timerSpeed = 1.3f
                        player.motionX *= 1.0199999809265137
                        player.motionZ *= 1.0199999809265137
                    }
                }

                if (player.onGround && MovementUtils.isMoving()) level = 2

                if (round(player.posY - player.posY.toInt().toDouble()) == round(0.138)) {
                    val thePlayer = player
                    thePlayer.motionY -= 0.08
                    event.y = event.y - 0.09316090325960147
                    thePlayer.posY -= 0.09316090325960147
                }

                when {
                    (level == 1 && (player.moveForward != 0.0f || player.moveStrafing != 0.0f)) -> {
                        level = 2
                        moveSpeed = 1.35 * baseMoveSpeed - 0.01
                    }

                    level == 2 -> {
                        level = 3
                        player.motionY = 0.399399995803833
                        event.y = 0.399399995803833
                        moveSpeed *= 2.149
                    }

                    level == 3 -> {
                        level = 4
                        val difference = 0.66 * (lastDist - baseMoveSpeed)
                        moveSpeed = lastDist - difference
                    }

                    else -> {
                        if (mc.theWorld.getCollidingBoundingBoxes(
                                player,
                                player.entityBoundingBox.offset(0.0, player.motionY, 0.0)
                            ).size > 0 || player.isCollidedVertically
                        ) level = 1
                        moveSpeed = lastDist - lastDist / 159.0
                    }
                }

                moveSpeed = moveSpeed.coerceAtLeast(baseMoveSpeed)
                val movementInput = player.movementInput
                var forward = movementInput.moveForward
                var strafe = movementInput.moveStrafe
                var yaw = player.rotationYaw

                if (forward == 0.0f && strafe == 0.0f) {
                    event.x = 0.0
                    event.z = 0.0
                } else if (forward != 0.0f) {
                    if (strafe >= 1.0f) {
                        yaw += (if (forward > 0.0f) -45 else 45).toFloat()
                        strafe = 0.0f
                    } else if (strafe <= -1.0f) {
                        yaw += (if (forward > 0.0f) 45 else -45).toFloat()
                        strafe = 0.0f
                    }
                    if (forward > 0.0f) {
                        forward = 1.0f
                    } else if (forward < 0.0f) {
                        forward = -1.0f
                    }
                }

                val mx2 = cos(Math.toRadians((yaw + 90.0f).toDouble()))
                val mz2 = sin(Math.toRadians((yaw + 90.0f).toDouble()))
                event.x = forward.toDouble() * moveSpeed * mx2 + strafe.toDouble() * moveSpeed * mz2
                event.z = forward.toDouble() * moveSpeed * mz2 - strafe.toDouble() * moveSpeed * mx2
                player.stepHeight = 0.6f
                if (forward == 0.0f && strafe == 0.0f) {
                    event.x = 0.0
                    event.z = 0.0
                }
            }
        }
    }

    private val baseMoveSpeed: Double
        get() {
            var baseSpeed = 0.2873
            if (player.isPotionActive(Potion.moveSpeed)) baseSpeed *= 1.0 + 0.2 * (player.getActivePotionEffect(
                Potion.moveSpeed
            ).amplifier + 1)
            return baseSpeed
        }

    private fun getBlock(axisAlignedBB: AxisAlignedBB): Block? {
        for (x in MathHelper.floor_double(axisAlignedBB.minX) until MathHelper.floor_double(axisAlignedBB.maxX) + 1) {
            for (z in MathHelper.floor_double(axisAlignedBB.minZ) until MathHelper.floor_double(axisAlignedBB.maxZ) + 1) {
                val block = mc.theWorld.getBlockState(BlockPos(x, axisAlignedBB.minY.toInt(), z)).block
                if (block != null) return block
            }
        }
        return null
    }

    private fun getBlock(offset: Double): Block? {
        return this.getBlock(player.entityBoundingBox.offset(0.0, offset, 0.0))
    }

    private fun round(value: Double): Double {
        var bigDecimal = BigDecimal(value)
        bigDecimal = bigDecimal.setScale(3, RoundingMode.HALF_UP)
        return bigDecimal.toDouble()
    }
}