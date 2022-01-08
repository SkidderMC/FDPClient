/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.misc.FallingPlayer
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import kotlin.math.cos
import kotlin.math.sin

@ModuleInfo(name = "Velocity", category = ModuleCategory.COMBAT)
class Velocity : Module() {

    /**
     * OPTIONS
     */
    private val horizontalValue = FloatValue("Horizontal", 0F, -2F, 2F)
    private val verticalValue = FloatValue("Vertical", 0F, -2F, 2F)
    private val velocityTickValue = IntegerValue("VelocityTick", 1, 0, 10).displayable { modeValue.equals("Tick") || modeValue.equals("OldSpartan")}
    private val modeValue = ListValue("Mode", arrayOf("Simple", "Tick", "Vanilla", "AACPush", "AACZero", "AAC4Reduce", "AAC5Reduce",
                                                      "Redesky1", "Redesky2",
                                                      "AAC5.2.0", "AAC5.2.0Combat",
                                                      "MatrixReduce", "MatrixSimple", "MatrixGround",
                                                      "Reverse", "SmoothReverse",
                                                      "Jump",
                                                      "Phase", "PacketPhase", "Glitch", "Spoof",
                                                      "Legit"), "Simple")

    // Reverse
    private val reverseStrengthValue = FloatValue("ReverseStrength", 1F, 0.1F, 1F).displayable { modeValue.equals("Reverse") }
    private val reverse2StrengthValue = FloatValue("SmoothReverseStrength", 0.05F, 0.02F, 0.1F).displayable { modeValue.equals("SmoothReverse") }

    // AAC Push
    private val aacPushXZReducerValue = FloatValue("AACPushXZReducer", 2F, 1F, 3F).displayable { modeValue.equals("AACPush") }
    private val aacPushYReducerValue = BoolValue("AACPushYReducer", true).displayable { modeValue.equals("AACPush") }

    // phase
    private val phaseHeightValue = FloatValue("PhaseHeight", 0.5F, 0F, 1F)
        .displayable { modeValue.contains("Phase") }
    private val phaseOnlyGround = BoolValue("PhaseOnlyGround", true)
        .displayable { modeValue.contains("Phase") }

    // legit
    private val legitStrafeValue = BoolValue("LegitStrafe", false).displayable { modeValue.equals("Legit") }
    private val legitFaceValue = BoolValue("LegitFace", true).displayable { modeValue.equals("Legit") }

    private val rspAlwaysValue = BoolValue("RedeskyAlwaysReduce", true)
        .displayable { modeValue.contains("RedeSky") }
    private val rspDengerValue = BoolValue("RedeskyOnlyDanger", false)
        .displayable { modeValue.contains("RedeSky") }

    private val onlyGroundValue = BoolValue("OnlyGround", false)
    private val onlyCombatValue = BoolValue("OnlyCombat", false)
    // private val onlyHitVelocityValue = BoolValue("OnlyHitVelocity",false)
    private val noFireValue = BoolValue("noFire", false)
    /**
     * VALUES
     */
    private var velocityTimer = MSTimer()
    private var velocityCalcTimer = MSTimer()
    private var velocityInput = false
    private var velocityTick = 0
    private var velocityAirTick = 0

    // SmoothReverse
    private var reverseHurt = false

    // AACPush
    private var jump = false

    // Legit
    private var pos: BlockPos? = null

    private var redeCount = 24

    private var templateX = 0
    private var templateY = 0
    private var templateZ = 0

    private var isMatrixOnGround = false

    override val tag: String
        get() = modeValue.get()

    override fun onDisable() {
        mc.thePlayer?.speedInAir = 0.02F
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if(velocityInput) {
            velocityTick++
        }else velocityTick = 0
        
        if (redeCount <24) redeCount++
        if (mc.thePlayer.isInWater || mc.thePlayer.isInLava || mc.thePlayer.isInWeb) {
            return
        }

        if ((onlyGroundValue.get() && !mc.thePlayer.onGround) || (onlyCombatValue.get() && !LiquidBounce.combatManager.inCombat)) {
            return
        }
            // if(onlyHitVelocityValue.get() && mc.thePlayer.motionY<0.05) return；
        if (noFireValue.get() && mc.thePlayer.isBurning) return

        when (modeValue.get().lowercase()) {
            "tick" -> {
                if(velocityTick > velocityTickValue.get()) {
                    if(mc.thePlayer.motionY > 0) mc.thePlayer.motionY = 0.0
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                    mc.thePlayer.jumpMovementFactor = -0.00001f
                    velocityInput = false
                }
                if(mc.thePlayer.onGround && velocityTick > 1) {
                    velocityInput = false
                }
            }
            
            "jump" -> if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.onGround) {
                mc.thePlayer.motionY = 0.42
            }

            "reverse" -> {
                if (!velocityInput) {
                    return
                }

                if (!mc.thePlayer.onGround) {
                    MovementUtils.strafe(MovementUtils.getSpeed() * reverseStrengthValue.get())
                } else if (velocityTimer.hasTimePassed(80L)) {
                    velocityInput = false
                }
            }

            "smoothreverse" -> {
                if (!velocityInput) {
                    mc.thePlayer.speedInAir = 0.02F
                    return
                }

                if (mc.thePlayer.hurtTime > 0) {
                    reverseHurt = true
                }

                if (!mc.thePlayer.onGround) {
                    if (reverseHurt) {
                        mc.thePlayer.speedInAir = reverse2StrengthValue.get()
                    }
                } else if (velocityTimer.hasTimePassed(80L)) {
                    velocityInput = false
                    reverseHurt = false
                }
            }

            "aac4reduce" -> {
                if (mc.thePlayer.hurtTime> 0 && !mc.thePlayer.onGround && velocityInput && velocityTimer.hasTimePassed(80L)) {
                    mc.thePlayer.motionX *= 0.62
                    mc.thePlayer.motionZ *= 0.62
                }
                if (velocityInput && (mc.thePlayer.hurtTime <4 || mc.thePlayer.onGround) && velocityTimer.hasTimePassed(120L)) {
                    velocityInput = false
                }
            }

            "aac5reduce" -> {
                if (mc.thePlayer.hurtTime> 1 && velocityInput) {
                    mc.thePlayer.motionX *= 0.81
                    mc.thePlayer.motionZ *= 0.81
                }
                if (velocityInput && (mc.thePlayer.hurtTime <5 || mc.thePlayer.onGround) && velocityTimer.hasTimePassed(120L)) {
                    velocityInput = false
                }
            }

            "aac5.2.0combat" -> {
                if (mc.thePlayer.hurtTime> 0 && velocityInput) {
                    velocityInput = false
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                    mc.thePlayer.motionY = 0.0
                    mc.thePlayer.jumpMovementFactor = -0.002f
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, 1.7976931348623157E+308, mc.thePlayer.posZ, true))
                }
                if (velocityTimer.hasTimePassed(80L) && velocityInput) {
                    velocityInput = false
                    mc.thePlayer.motionX = templateX / 8000.0
                    mc.thePlayer.motionZ = templateZ / 8000.0
                    mc.thePlayer.motionY = templateY / 8000.0
                    mc.thePlayer.jumpMovementFactor = -0.002f
                }
            }

            "aacpush" -> {
                if (jump) {
                    if (mc.thePlayer.onGround) {
                        jump = false
                    }
                } else {
                    // Strafe
                    if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.motionX != 0.0 && mc.thePlayer.motionZ != 0.0) {
                        mc.thePlayer.onGround = true
                    }

                    // Reduce Y
                    if (mc.thePlayer.hurtResistantTime > 0 && aacPushYReducerValue.get() &&
                            !LiquidBounce.moduleManager[Speed::class.java]!!.state) {
                        mc.thePlayer.motionY -= 0.014999993
                    }
                }

                // Reduce XZ
                if (mc.thePlayer.hurtResistantTime >= 19) {
                    val reduce = aacPushXZReducerValue.get()

                    mc.thePlayer.motionX /= reduce
                    mc.thePlayer.motionZ /= reduce
                }
            }
           "matrixreduce" -> {
                if (mc.thePlayer.hurtTime > 0) {
                    if (mc.thePlayer.onGround) {
                        if (mc.thePlayer.hurtTime <= 6) {
                            mc.thePlayer.motionX *= 0.70
                            mc.thePlayer.motionZ *= 0.70
                        }
                        if (mc.thePlayer.hurtTime <= 5) {
                            mc.thePlayer.motionX *= 0.80
                            mc.thePlayer.motionZ *= 0.80
                        }
                    } else if (mc.thePlayer.hurtTime <= 10) {
                        mc.thePlayer.motionX *= 0.60
                        mc.thePlayer.motionZ *= 0.60
                    }
                }
           }

           "matrixground" -> {
               isMatrixOnGround = mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown
               if (isMatrixOnGround) mc.thePlayer.onGround = false
           }

            "glitch" -> {
                mc.thePlayer.noClip = velocityInput

                if (mc.thePlayer.hurtTime == 7) {
                    mc.thePlayer.motionY = 0.4
                }

                velocityInput = false
            }

            "aaczero" -> {
                if (mc.thePlayer.hurtTime > 0) {
                    if (!velocityInput || mc.thePlayer.onGround || mc.thePlayer.fallDistance > 2F) {
                        return
                    }

                    mc.thePlayer.addVelocity(0.0, -1.0, 0.0)
                    mc.thePlayer.onGround = true
                } else {
                    velocityInput = false
                }
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if ((onlyGroundValue.get() && !mc.thePlayer.onGround) || (onlyCombatValue.get() && !LiquidBounce.combatManager.inCombat)) {
            return
        }

        val packet = event.packet
        if (packet is S12PacketEntityVelocity) {
            if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer) {
                return
            }
            // if(onlyHitVelocityValue.get() && packet.getMotionY()<400.0) return
            if (noFireValue.get() && mc.thePlayer.isBurning) return
            velocityTimer.reset()
            velocityTick = 0

            when (modeValue.get().lowercase()) {
                "tick" -> {
                    velocityInput = true
                    val horizontal = horizontalValue.get()
                    val vertical = verticalValue.get()

                    if (horizontal == 0F && vertical == 0F) {
                        event.cancelEvent()
                    }

                    packet.motionX = (packet.getMotionX() * horizontal).toInt()
                    packet.motionY = (packet.getMotionY() * vertical).toInt()
                    packet.motionZ = (packet.getMotionZ() * horizontal).toInt()
                }
                "simple" -> {
                    //velocityInput = true
                    val horizontal = horizontalValue.get()
                    val vertical = verticalValue.get()

                    if (horizontal == 0F && vertical == 0F) {
                        event.cancelEvent()
                    }

                    packet.motionX = (packet.getMotionX() * horizontal).toInt()
                    packet.motionY = (packet.getMotionY() * vertical).toInt()
                    packet.motionZ = (packet.getMotionZ() * horizontal).toInt()
                }
                "vanilla" -> {
                    event.cancelEvent()
                }
                "matrixsimple" -> {
                    packet.motionX = (packet.getMotionX() * 0.36).toInt()
                    packet.motionZ = (packet.getMotionZ() * 0.36).toInt()
                    if (mc.thePlayer.onGround) {
                        packet.motionX = (packet.getMotionX() * 0.9).toInt()
                        packet.motionZ = (packet.getMotionZ() * 0.9).toInt()
                    }
                }

                "matrixground" -> {
                    packet.motionX = (packet.getMotionX() * 0.36).toInt()
                    packet.motionZ = (packet.getMotionZ() * 0.36).toInt()
                    if (isMatrixOnGround) {
                        packet.motionY = (-628.7).toInt()
                        packet.motionX = (packet.getMotionX() * 0.6).toInt()
                        packet.motionZ = (packet.getMotionZ() * 0.6).toInt()
                    }
                }

                "aac4reduce" -> {
                    velocityInput = true
                    packet.motionX = (packet.getMotionX() * 0.6).toInt()
                    packet.motionZ = (packet.getMotionZ() * 0.6).toInt()
                }

                "aac5.2.0" -> {
                    event.cancelEvent()
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, 1.7976931348623157E+308, mc.thePlayer.posZ, true))
                }

                "aac5reduce", "reverse", "smoothreverse", "aaczero" -> velocityInput = true

                "phase" -> {
                    if (!mc.thePlayer.onGround && phaseOnlyGround.get()) {
                        return
                    }

                    velocityInput = true
                    mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX, mc.thePlayer.posY - phaseHeightValue.get(), mc.thePlayer.posZ)
                    event.cancelEvent()
                    packet.motionX = 0
                    packet.motionY = 0
                    packet.motionZ = 0
                }

                "aac5.2.0combat" -> {
                    event.cancelEvent()
                    velocityInput = true
                    templateX = packet.motionX
                    templateZ = packet.motionZ
                    templateY = packet.motionY
                }

                "spoof" -> {
                    event.cancelEvent()
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + packet.motionX / 8000.0, mc.thePlayer.posY + packet.motionY / 8000.0, mc.thePlayer.posZ + packet.motionZ / 8000.0, false))
                }

                "packetphase" -> {
                    if (!mc.thePlayer.onGround && phaseOnlyGround.get()) {
                        return
                    }

//                    chat("MOTX=${packet.motionX}, MOTZ=${packet.motionZ}")
                    if (packet.motionX <500 && packet.motionY <500) {
                        return
                    }

                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - phaseHeightValue.get(), mc.thePlayer.posZ, false))
                    event.cancelEvent()
                    packet.motionX = 0
                    packet.motionY = 0
                    packet.motionZ = 0
                }

                "glitch" -> {
                    if (!mc.thePlayer.onGround) {
                        return
                    }

                    velocityInput = true
                    event.cancelEvent()
                }

                "legit" -> {
                    pos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)
                }

                "redesky2" -> {
                    if (packet.getMotionX() == 0 && packet.getMotionZ() == 0) { // ignore horizonal velocity
                        return
                    }

                    val target = LiquidBounce.combatManager.getNearByEntity(LiquidBounce.moduleManager[KillAura::class.java]!!.rangeValue.get() + 1) ?: return
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                    packet.motionX = 0
                    packet.motionZ = 0
                    for (i in 0..redeCount) {
                        mc.thePlayer.sendQueue.addToSendQueue(C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK))
                        mc.thePlayer.sendQueue.addToSendQueue(C0APacketAnimation())
                    }
                    if (redeCount> 12) redeCount -= 5
                }

                "redesky1" -> {
                    if (packet.getMotionX() == 0 && packet.getMotionZ() == 0) { // ignore horizonal velocity
                        return
                    }

                    if (rspDengerValue.get()) {
                        val pos = FallingPlayer(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, packet.motionX / 8000.0, packet.motionY / 8000.0, packet.motionZ / 8000.0, 0f, 0f, 0f, 0f).findCollision(60)
                        if (pos != null && pos.y> (mc.thePlayer.posY - 7)) {
                            return
                        }
                    }

                    val target = LiquidBounce.combatManager.getNearByEntity(LiquidBounce.moduleManager[KillAura::class.java]!!.rangeValue.get()) ?: return
                    if (rspAlwaysValue.get()) {
                        mc.thePlayer.motionX = 0.0
                        mc.thePlayer.motionZ = 0.0
                        // mc.thePlayer.motionY=(packet.motionY/8000f)*1.0
                        packet.motionX = 0
                        packet.motionZ = 0
                        // event.cancelEvent() better stuff
                    }

                    if (velocityCalcTimer.hasTimePassed(500)) {
                        if (!rspAlwaysValue.get()) {
                            mc.thePlayer.motionX = 0.0
                            mc.thePlayer.motionZ = 0.0
                            // mc.thePlayer.motionY=(packet.motionY/8000f)*1.0
                            packet.motionX = 0
                            packet.motionZ = 0
                        }
                        val count = if (!velocityCalcTimer.hasTimePassed(800)) {
                            8
                        } else if (!velocityCalcTimer.hasTimePassed(1200)) {
                            12
                        } else {
                            25
                        }
                        for (i in 0..count) {
                            mc.thePlayer.sendQueue.addToSendQueue(C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK))
                            mc.thePlayer.sendQueue.addToSendQueue(C0APacketAnimation())
                        }
                        velocityCalcTimer.reset()
                    } else {
                        packet.motionX = (packet.motionX * 0.6).toInt()
                        packet.motionZ = (packet.motionZ * 0.6).toInt()
                        for (i in 0..4) {
                            mc.thePlayer.sendQueue.addToSendQueue(C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK))
                            mc.thePlayer.sendQueue.addToSendQueue(C0APacketAnimation())
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if ((onlyGroundValue.get() && !mc.thePlayer.onGround) || (onlyCombatValue.get() && !LiquidBounce.combatManager.inCombat)) {
            return
        }

        when (modeValue.get().lowercase()) {
            "legit" -> {
                if (pos == null || mc.thePlayer.hurtTime <= 0) {
                    return
                }

                val rot = RotationUtils.getRotations(pos!!.x.toDouble(), pos!!.y.toDouble(), pos!!.z.toDouble())
                if (legitFaceValue.get()) {
                    RotationUtils.setTargetRotation(rot)
                }
                val yaw = rot.yaw
                if (legitStrafeValue.get()) {
                    val speed = MovementUtils.getSpeed()
                    val yaw1 = Math.toRadians(yaw.toDouble())
                    mc.thePlayer.motionX = -sin(yaw1) * speed
                    mc.thePlayer.motionZ = cos(yaw1) * speed
                } else {
                    var strafe = event.strafe
                    var forward = event.forward
                    val friction = event.friction

                    var f = strafe * strafe + forward * forward

                    if (f >= 1.0E-4F) {
                        f = MathHelper.sqrt_float(f)

                        if (f < 1.0F) {
                            f = 1.0F
                        }

                        f = friction / f
                        strafe *= f
                        forward *= f

                        val yawSin = MathHelper.sin((yaw * Math.PI / 180F).toFloat())
                        val yawCos = MathHelper.cos((yaw * Math.PI / 180F).toFloat())

                        mc.thePlayer.motionX += strafe * yawCos - forward * yawSin
                        mc.thePlayer.motionZ += forward * yawCos + strafe * yawSin
                    }
                }
            }
        }
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (mc.thePlayer.isInWater || mc.thePlayer.isInLava || mc.thePlayer.isInWeb || (onlyGroundValue.get() && !mc.thePlayer.onGround)) {
            return
        }

        if ((onlyGroundValue.get() && !mc.thePlayer.onGround) || (onlyCombatValue.get() && !LiquidBounce.combatManager.inCombat)) {
            return
        }

        when (modeValue.get().lowercase()) {
            "aacpush" -> {
                jump = true

                if (!mc.thePlayer.isCollidedVertically) {
                    event.cancelEvent()
                }
            }
            "aaczero" -> if (mc.thePlayer.hurtTime > 0) {
                event.cancelEvent()
            }
        }
    }
}
