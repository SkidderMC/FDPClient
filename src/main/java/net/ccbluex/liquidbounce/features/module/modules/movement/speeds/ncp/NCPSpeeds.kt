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
import net.minecraft.potion.Potion
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
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



    override fun onEnable() {
        when (modeValue.get()) {
            "NCPBhop" -> {
                mc.timer.timerSpeed = 1f
                level = if (mc.theWorld.getCollidingBoundingBoxes(
                        mc.thePlayer,
                        mc.thePlayer.entityBoundingBox.offset(0.0, mc.thePlayer.motionY, 0.0)
                    ).size > 0 || mc.thePlayer.isCollidedVertically
                ) 1 else 4
            }
            "NCPFHop" -> {
                mc.timer.timerSpeed = 1.0866f
                super.onEnable()
            }
            "NCPHop" -> {
                mc.timer.timerSpeed = 1.0866f
                super.onEnable()
            }
            "NCPStable" -> {
                mc.timer.timerSpeed = ncpstabletimerValue.get()
            }
            "LNCPHop" -> {
                mc.timer.timerSpeed = 1.0865f
            }
        }
    }

    override fun onDisable() {
        when (modeValue.get()) {
            "NCPBhop" -> {
                mc.timer.timerSpeed = 1f
                moveSpeed = baseMoveSpeed
                level = 0
            }
            "NCPFHop" -> {
                mc.thePlayer.speedInAir = 0.02f
                mc.timer.timerSpeed = 1f
                super.onDisable()
            }
            "NCPHop" -> {
                mc.thePlayer.speedInAir = 0.02f
                mc.timer.timerSpeed = 1f
                super.onDisable()
            }
            "NCPLatest" -> {
                mc.thePlayer.jumpMovementFactor = 0.02f
            }
            "NCPStable" -> {
                mc.timer.timerSpeed = 1f
                mc.thePlayer.jumpMovementFactor = 0.2f
                mc.thePlayer.setVelocity(
                    // reduce the motion a bit to avoid flags but don't stop completely
                    mc.thePlayer.motionX / 3,
                    mc.thePlayer.motionY,
                    mc.thePlayer.motionZ / 3
                )
            }
            "LNCPHop" -> {
                mc.thePlayer.speedInAir = 0.02f
                mc.timer.timerSpeed = 1f
            }
        }

    }

    override fun onUpdate() {
        when (modeValue.get()) {
            "NCPFHop" -> {
                if (MovementUtils.isMoving()) {
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump()
                        mc.thePlayer.motionX *= 1.01
                        mc.thePlayer.motionZ *= 1.01
                        mc.thePlayer.speedInAir = 0.0223f
                    }
                    mc.thePlayer.motionY -= 0.00099999
                    MovementUtils.strafe()
                } else {
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                }
            }
            "NCPHop" -> {
                if (MovementUtils.isMoving()) {
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump()
                        mc.thePlayer.speedInAir = 0.0223f
                    }
                    MovementUtils.strafe()
                } else {
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                }
            }
            "NCPLatest" -> {
                if (mc.thePlayer.ticksExisted % 20 <= 9) {
                    mc.timer.timerSpeed = 1.05f
                } else {
                    mc.timer.timerSpeed = 0.98f
                }

                if (MovementUtils.isMoving()) {
                    if (mc.thePlayer.onGround) {
                        wasSlow = false
                        mc.thePlayer.jump()
                        MovementUtils.strafe(0.47f)
                        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                            MovementUtils.strafe(0.48f * (1.0f + 0.13f * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).amplifier + 1)))
                        }
                    }
                    if (MovementUtils.getSpeed() < 0.277)
                        wasSlow = true
                    if (wasSlow)
                        MovementUtils.strafe(0.277f)


                } else {
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                    wasSlow = true
                }
            }
            "NCPStable" -> {
                if (MovementUtils.isMoving()) {
                    mc.thePlayer.jumpMovementFactor = ncpstablejumpMovementFactorValue.get()
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump()
                    }
                    MovementUtils.strafe(max(MovementUtils.getSpeed(), MovementUtils.getSpeedWithPotionEffects(0.27).toFloat()))
                } else {
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                }
            }
            "LNCPHop" -> {
                if (MovementUtils.isMoving()) {
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump()
                        mspeed = MovementUtils.defaultSpeed().toDouble() * 1.73

                        justJumped = true
                    } else {
                        if (justJumped) {
                            mspeed *= 0.72150289018
                            justJumped = false
                        } else {
                            mspeed -= mspeed / 159
                        }
                    }
                    if (mspeed < MovementUtils.defaultSpeed().toDouble())
                        mspeed = MovementUtils.defaultSpeed().toDouble()

                    MovementUtils.strafe(mspeed.toFloat())
                } else {
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                }
            }
        }
    }

    override fun onPreMotion() {
        when (modeValue.get()) {
            "NCPBhop" -> {
                val xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX
                val zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ
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
                    mc.timer.timerSpeed = 1f
                } else {
                    if (MovementUtils.isMoving()) mc.timer.timerSpeed = 32767f
                    if (MovementUtils.isMoving()) {
                        mc.timer.timerSpeed = 1.3f
                        mc.thePlayer.motionX *= 1.0199999809265137
                        mc.thePlayer.motionZ *= 1.0199999809265137
                    }
                }

                if (mc.thePlayer.onGround && MovementUtils.isMoving()) level = 2

                if (round(mc.thePlayer.posY - mc.thePlayer.posY.toInt().toDouble()) == round(0.138)) {
                    val thePlayer = mc.thePlayer
                    thePlayer.motionY -= 0.08
                    event.y = event.y - 0.09316090325960147
                    thePlayer.posY -= 0.09316090325960147
                }

                when {
                    (level == 1 && (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f)) -> {
                        level = 2
                        moveSpeed = 1.35 * baseMoveSpeed - 0.01
                    }

                    level == 2 -> {
                        level = 3
                        mc.thePlayer.motionY = 0.399399995803833
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
                                mc.thePlayer,
                                mc.thePlayer.entityBoundingBox.offset(0.0, mc.thePlayer.motionY, 0.0)
                            ).size > 0 || mc.thePlayer.isCollidedVertically
                        ) level = 1
                        moveSpeed = lastDist - lastDist / 159.0
                    }
                }

                moveSpeed = moveSpeed.coerceAtLeast(baseMoveSpeed)
                val movementInput = mc.thePlayer.movementInput
                var forward = movementInput.moveForward
                var strafe = movementInput.moveStrafe
                var yaw = mc.thePlayer.rotationYaw

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
                mc.thePlayer.stepHeight = 0.6f
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
            if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) baseSpeed *= 1.0 + 0.2 * (mc.thePlayer.getActivePotionEffect(
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
        return this.getBlock(mc.thePlayer.entityBoundingBox.offset(0.0, offset, 0.0))
    }

    private fun round(value: Double): Double {
        var bigDecimal = BigDecimal(value)
        bigDecimal = bigDecimal.setScale(3, RoundingMode.HALF_UP)
        return bigDecimal.toDouble()
    }
}