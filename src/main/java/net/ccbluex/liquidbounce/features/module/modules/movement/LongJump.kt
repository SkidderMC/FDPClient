/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * http://proxy.liulihaocai.pw/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.EnumAutoDisableType
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.EnumFacing

@ModuleInfo(name = "LongJump", category = ModuleCategory.MOVEMENT, autoDisable = EnumAutoDisableType.FLAG)
class LongJump : Module() {
    private val modeValue = ListValue("Mode", arrayOf("NCP", "NCPDamage", "AACv1", "AACv2", "AACv3", "Mineplex", "Mineplex2", "Mineplex3", "RedeSkyTest", "RedeSky", "RedeSky2", "RedeSky3", "OldBlocksMC", "OldBlocksMC2", "HYT4v4"), "NCP")
    private val ncpBoostValue = FloatValue("NCPBoost", 4.25f, 1f, 10f)

    // redesky
    private val rsJumpMovementValue = FloatValue("RedeSkyJumpMovement", 0.13F, 0.05F, 0.25F).displayable { modeValue.equals("RedeSky") }
    private val rsMotionYValue = FloatValue("RedeSkyMotionY", 0.81F, 0.05F, 1F).displayable { modeValue.equals("RedeSky") }
    private val rsMoveReducerValue = BoolValue("RedeSkyMovementReducer", true).displayable { modeValue.equals("RedeSky") }
    private val rsReduceMovementValue = FloatValue("RedeSkyReduceMovement", 0.08F, 0.05F, 0.25F).displayable { modeValue.equals("RedeSky") }
    private val rsMotYReducerValue = BoolValue("RedeSkyMotionYReducer", true).displayable { modeValue.equals("RedeSky") }
    private val rsReduceYMotionValue = FloatValue("RedeSkyReduceYMotion", 0.15F, 0.01F, 0.20F).displayable { modeValue.equals("RedeSky") }
    private val rsUseTimerValue = BoolValue("RedeSkyTimer", true).displayable { modeValue.equals("RedeSky") }
    private val rsTimerValue = FloatValue("RedeSkyTimer", 0.30F, 0.1F, 1F).displayable { modeValue.equals("RedeSky") }

    // redesky2
    private val rs2AirSpeedValue = FloatValue("RedeSky2AirSpeed", 0.1F, 0.05F, 0.25F).displayable { modeValue.equals("RedeSky2") }
    private val rs2MinAirSpeedValue = FloatValue("RedeSky2MinAirSpeed", 0.08F, 0.05F, 0.25F).displayable { modeValue.equals("RedeSky2") }
    private val rs2ReduceAirSpeedValue = FloatValue("RedeSky2ReduceAirSpeed", 0.16F, 0.05F, 0.25F).displayable { modeValue.equals("RedeSky2") }
    private val rs2AirSpeedReducerValue = BoolValue("RedeSky2AirSpeedReducer", true).displayable { modeValue.equals("RedeSky2") }
    private val rs2YMotionValue = FloatValue("RedeSky2YMotion", 0.08F, 0.01F, 0.20F).displayable { modeValue.equals("RedeSky2") }
    private val rs2MinYMotionValue = FloatValue("RedeSky2MinYMotion", 0.04F, 0.01F, 0.20F).displayable { modeValue.equals("RedeSky2") }
    private val rs2ReduceYMotionValue = FloatValue("RedeSky2ReduceYMotion", 0.15F, 0.01F, 0.20F).displayable { modeValue.equals("RedeSky2") }
    private val rs2YMotionReducerValue = BoolValue("RedeSky2YMotionReducer", true).displayable { modeValue.equals("RedeSky2") }
    private val rs3JumpTimeValue = IntegerValue("RedeSky3JumpTime", 500, 300, 1500).displayable { modeValue.equals("RedeSky3") }
    private val rs3BoostValue = FloatValue("RedeSky3Boost", 1F, 0.3F, 1.5F).displayable { modeValue.equals("RedeSky3") }
    private val rs3HeightValue = FloatValue("RedeSky3Height", 1F, 0.3F, 1.5F).displayable { modeValue.equals("RedeSky3") }
    private val rs3TimerValue = FloatValue("RedeSky3Timer", 1F, 0.1F, 5F).displayable { modeValue.equals("RedeSky3") }
    private val ncpdInstantValue = BoolValue("NCPDamageInstant", false).displayable { modeValue.equals("NCPDamage") }
    // settings
    private val autoJumpValue = BoolValue("AutoJump", true)
    private val autoDisableValue = BoolValue("AutoDisable", true)
    private var jumped = false
    private var hasJumped = false
    private var canBoost = false
    private var teleported = false
    private var canMineplexBoost = false
    private var timer = MSTimer()
    var airTicks = 0
    private var balance = 0
    private var x = 0.0
    private var y = 0.0
    private var z = 0.0
    private var damageStat = false
    private val jumpYPosArr = arrayOf(0.41999998688698, 0.7531999805212, 1.00133597911214, 1.16610926093821, 1.24918707874468, 1.24918707874468, 1.1707870772188, 1.0155550727022, 0.78502770378924, 0.4807108763317, 0.10408037809304, 0.0)

    override fun onEnable() {
        airTicks = 0
        balance = 0
        hasJumped = false
        damageStat = false
        if (modeValue.equals("ncpdamage")) {
            x = mc.thePlayer.posX
            y = mc.thePlayer.posY
            z = mc.thePlayer.posZ
            if(ncpdInstantValue.get()) {
                balance = 114514
            } else {
                LiquidBounce.hud.addNotification(Notification(name, "Wait for damage...", NotifyType.SUCCESS, jumpYPosArr.size * 4 * 50))
            }
        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1F
        when (modeValue.get().lowercase()) {
            "redesky2" -> {
                mc.thePlayer.speedInAir = 0.02F
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        mc.thePlayer ?: return

        if (modeValue.equals("ncpdamage")) {
            if (!damageStat) {
                mc.thePlayer.setPosition(x, y, z)
                if (balance > jumpYPosArr.size * 4) {
                    repeat(4) {
                        jumpYPosArr.forEach {
                            PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(x, y + it, z, false))
                        }
                        PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(x, y, z, false))
                    }
                    PacketUtils.sendPacketNoEvent(C03PacketPlayer(true))
                    damageStat = true
                }
            } else {
                MovementUtils.strafe(0.50f * ncpBoostValue.get())
                mc.thePlayer.jump()
                state = false
            }
            return
        }

        if (jumped) {
            val mode = modeValue.get()

            if (!mc.thePlayer.onGround) {
                airTicks++
            } else {
                airTicks = 0
            }

            if (mc.thePlayer.onGround || mc.thePlayer.capabilities.isFlying) {
                jumped = false
                canMineplexBoost = false

                if (mode.equals("NCP", ignoreCase = true)) {
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                }
                return
            }
            run {
                when (mode.lowercase()) {
                    "ncp" -> {
                        MovementUtils.strafe(MovementUtils.getSpeed() * if (canBoost) ncpBoostValue.get() else 1f)
                        canBoost = false
                    }

                    "aacv1" -> {
                        mc.thePlayer.motionY += 0.05999
                        MovementUtils.strafe(MovementUtils.getSpeed() * 1.08f)
                    }

                    "aacv2", "mineplex3" -> {
                        mc.thePlayer.jumpMovementFactor = 0.09f
                        mc.thePlayer.motionY += 0.0132099999999999999999999999999
                        mc.thePlayer.jumpMovementFactor = 0.08f
                        MovementUtils.strafe()
                    }

                    "aacv3" -> {
                        if (mc.thePlayer.fallDistance > 0.5f && !teleported) {
                            val value = 3.0
                            var x = 0.0
                            var z = 0.0

                            when (mc.thePlayer.horizontalFacing) {
                                EnumFacing.NORTH -> z = -value
                                EnumFacing.EAST -> x = +value
                                EnumFacing.SOUTH -> z = +value
                                EnumFacing.WEST -> x = -value
                            }

                            mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z)
                            teleported = true
                        }
                    }

                    "mineplex" -> {
                        mc.thePlayer.motionY += 0.0132099999999999999999999999999
                        mc.thePlayer.jumpMovementFactor = 0.08f
                        MovementUtils.strafe()
                    }

                    "mineplex2" -> {
                        if (!canMineplexBoost) {
                            return@run
                        }

                        mc.thePlayer.jumpMovementFactor = 0.1f
                        if (mc.thePlayer.fallDistance > 1.5f) {
                            mc.thePlayer.jumpMovementFactor = 0f
                            mc.thePlayer.motionY = (-10f).toDouble()
                        }

                        MovementUtils.strafe()
                    }

                    "redesky" -> {
                        if (!mc.thePlayer.onGround) {
                            if (rsMoveReducerValue.get()) {
                                mc.thePlayer.jumpMovementFactor = rsJumpMovementValue.get() - (airTicks * (rsReduceMovementValue.get() / 100))
                            } else {
                                mc.thePlayer.jumpMovementFactor = rsJumpMovementValue.get()
                            }
                            if (rsMotYReducerValue.get()) {
                                mc.thePlayer.motionY += (rsMotionYValue.get() / 10F) - (airTicks * (rsReduceYMotionValue.get() / 100))
                            } else {
                                mc.thePlayer.motionY += rsMotionYValue.get() / 10F
                            }
                            if (rsUseTimerValue.get()) {
                                mc.timer.timerSpeed = rsTimerValue.get()
                            }
                        }
                    }

                    "redesky2" -> {
                        if (!mc.thePlayer.onGround) {
                            if (rs2YMotionReducerValue.get()) {
                                val motY = rs2YMotionValue.get() - (airTicks * (rs2ReduceYMotionValue.get() / 100))
                                if (motY <rs2MinYMotionValue.get()) {
                                    mc.thePlayer.motionY += rs2MinYMotionValue.get()
                                } else {
                                    mc.thePlayer.motionY += motY
                                }
                            } else {
                                mc.thePlayer.motionY += rs2YMotionValue.get()
                            }
                            // as reduce
                            if (rs2AirSpeedReducerValue.get()) {
                                val airSpeed = rs2AirSpeedValue.get() - (airTicks * (rs2ReduceAirSpeedValue.get() / 100))
                                if (airSpeed <rs2MinAirSpeedValue.get()) {
                                    mc.thePlayer.speedInAir = rs2MinAirSpeedValue.get()
                                } else {
                                    mc.thePlayer.speedInAir = airSpeed
                                }
                            } else {
                                mc.thePlayer.speedInAir = rs2AirSpeedValue.get()
                            }
                        }
                    }

                    "redesky3" -> {
                        if (!timer.hasTimePassed(rs3JumpTimeValue.get().toLong())) {
                            mc.thePlayer.motionY += rs3HeightValue.get() / 10F
                            MovementUtils.move(rs3BoostValue.get() / 10F)
                            mc.timer.timerSpeed = rs3TimerValue.get()
                        } else {
                            mc.timer.timerSpeed = 1F
                        }
                    }

                    "oldblocksmc" -> {
                        mc.thePlayer.jumpMovementFactor = 0.1f
                        mc.thePlayer.motionY += 0.0132
                        mc.thePlayer.jumpMovementFactor = 0.09f
                        mc.timer.timerSpeed = 0.8f
                        MovementUtils.strafe()
                    }

                    "oldblocksmc2" -> {
                        mc.thePlayer.motionY += 0.01554
                        MovementUtils.strafe(MovementUtils.getSpeed() * 1.114514f)
                        mc.timer.timerSpeed = 0.917555f
                    }

                    "redeskytest" -> {
                        mc.thePlayer.motionY = 0.42
                        MovementUtils.strafe(MovementUtils.getSpeed() * 1.12f)
                        mc.timer.timerSpeed = 0.8f
                    }

                    "hyt4v4" -> {
                        mc.thePlayer.motionY += 0.031470000997
                        MovementUtils.strafe(MovementUtils.getSpeed() * 1.0114514f)
                        mc.timer.timerSpeed = 1.0114514f
                    }
                }
            }
        }

        if (autoJumpValue.get() && mc.thePlayer.onGround && MovementUtils.isMoving()) {
            jumped = true
            if (hasJumped && autoDisableValue.get()) {
                state = false
                return
            }
            mc.thePlayer.jump()
            hasJumped = true
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer) {
            if (modeValue.equals("NCPDamage") && !damageStat) {
                balance++
                event.cancelEvent()
            }
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        mc.thePlayer ?: return
        val mode = modeValue.get()

        if (mode.equals("mineplex3", ignoreCase = true)) {
            if (mc.thePlayer.fallDistance != 0.0f) {
                mc.thePlayer.motionY += 0.037
            }
        } else if (mode.equals("ncp", ignoreCase = true) && !MovementUtils.isMoving() && jumped) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
            event.zeroXZ()
        }
    }

    @EventTarget(ignoreCondition = true)
    fun onJump(event: JumpEvent) {
        jumped = true
        canBoost = true
        teleported = false

        timer.reset()

        if (state) {
            when (modeValue.get().lowercase()) {
                "mineplex" -> event.motion = event.motion * 4.08f
                "mineplex2" -> {
                    if (mc.thePlayer!!.isCollidedHorizontally) {
                        event.motion = 2.31f
                        canMineplexBoost = true
                        mc.thePlayer!!.onGround = false
                    }
                }
            }
        }
    }

    override val tag: String
        get() = modeValue.get()
}
