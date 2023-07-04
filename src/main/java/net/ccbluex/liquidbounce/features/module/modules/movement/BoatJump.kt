/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.entity.item.EntityBoat
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C0CPacketInput
import net.minecraft.util.Vec3
import kotlin.math.cos
import kotlin.math.sin

@ModuleInfo(name = "BoatJump", category = ModuleCategory.MOVEMENT)
object BoatJump : Module() {

    private val modeValue = ListValue("Mode", arrayOf("Boost", "Launch", "Matrix"), "Boost")
    private val hBoostValue = FloatValue("HBoost", 2f, 0f, 6f)
    private val vBoostValue = FloatValue("VBoost", 2f, 0f, 6f)
    private val matrixTimerStartValue = FloatValue("MatrixTimerStart", 0.3f, 0.1f, 1f).displayable { modeValue.equals("Matrix") }
    private val matrixTimerAirValue = FloatValue("MatrixTimerAir", 0.5f, 0.1f, 1.5f).displayable { modeValue.equals("Matrix") }
    private val launchRadiusValue = FloatValue("LaunchRadius", 4F, 3F, 10F).displayable { modeValue.equals("Launch") }
    private val delayValue = IntegerValue("Delay", 200, 100, 500)
    private val autoHitValue = BoolValue("AutoHit", true)

    private var jumpState = 1
    private val timer = MSTimer()
    private val hitTimer = MSTimer()
    private var lastRide = false
    private var hasStopped = false

    override fun onEnable() {
        jumpState = 1
        lastRide = false
    }

    override fun onDisable() {
        hasStopped = false
        mc.timer.timerSpeed = 1f
        mc.thePlayer.speedInAir = 0.02f
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        // println(mc.timer.timerSpeed)
        if (mc.thePlayer.onGround && !mc.thePlayer.isRiding) {
            hasStopped = false
            mc.timer.timerSpeed = 1f
            mc.thePlayer.speedInAir = 0.02f
        }

        when (modeValue.get().lowercase()) {
            "matrix" -> {
                if (hasStopped) {
                    mc.timer.timerSpeed = matrixTimerAirValue.get()
                } else {
                    mc.timer.timerSpeed = 1f
                }
            }
        }

        if (mc.thePlayer.isRiding && jumpState == 1) {
            if (!lastRide) {
                timer.reset()
            }

            if (timer.hasTimePassed(delayValue.get().toLong())) {
                jumpState = 2
                when (modeValue.get().lowercase()) {
                    "matrix" -> {
                        mc.timer.timerSpeed = matrixTimerStartValue.get()
                        mc.netHandler.addToSendQueue(
                            C0CPacketInput(
                                mc.thePlayer.moveStrafing,
                                mc.thePlayer.moveForward,
                                false,
                                true
                            )
                        )
                    }
                    else -> {
                        mc.netHandler.addToSendQueue(
                            C0CPacketInput(
                                mc.thePlayer.moveStrafing,
                                mc.thePlayer.moveForward,
                                false,
                                true
                            )
                        )
                    }
                }
            }
        } else if (jumpState == 2 && !mc.thePlayer.isRiding) {
            val radiansYaw = mc.thePlayer.rotationYaw * Math.PI / 180

            when (modeValue.get().lowercase()) {
                "boost" -> {
                    mc.thePlayer.motionX = hBoostValue.get() * -sin(radiansYaw)
                    mc.thePlayer.motionZ = hBoostValue.get() * cos(radiansYaw)
                    mc.thePlayer.motionY = vBoostValue.get().toDouble()
                    jumpState = 1
                }
                "launch" -> {
                    mc.thePlayer.motionX += (hBoostValue.get() * 0.1) * -sin(radiansYaw)
                    mc.thePlayer.motionZ += (hBoostValue.get() * 0.1) * cos(radiansYaw)
                    mc.thePlayer.motionY += vBoostValue.get() * 0.1

                    var hasBoat = false
                    for (entity in mc.theWorld.loadedEntityList) {
                        if (entity is EntityBoat && mc.thePlayer.getDistanceToEntity(entity) < launchRadiusValue.get()) {
                            hasBoat = true
                            break
                        }
                    }
                    if (!hasBoat) {
                        jumpState = 1
                    }
                }
                "matrix" -> {
                    hasStopped = true
                    mc.timer.timerSpeed = matrixTimerAirValue.get()
                    mc.thePlayer.motionX = hBoostValue.get() * -sin(radiansYaw)
                    mc.thePlayer.motionZ = hBoostValue.get() * cos(radiansYaw)
                    mc.thePlayer.motionY = vBoostValue.get().toDouble()
                    jumpState = 1
                }
            }

            timer.reset()
            hitTimer.reset()
        }

        lastRide = mc.thePlayer.isRiding

        if (autoHitValue.get() && !mc.thePlayer.isRiding && hitTimer.hasTimePassed(1500)) {
            for (entity in mc.theWorld.loadedEntityList) {
                if (entity is EntityBoat && mc.thePlayer.getDistanceToEntity(entity) < 3) {
                    mc.netHandler.addToSendQueue(C02PacketUseEntity(entity, Vec3(0.5, 0.5, 0.5)))
                    mc.netHandler.addToSendQueue(C02PacketUseEntity(entity, C02PacketUseEntity.Action.INTERACT))
                    hitTimer.reset()
                }
            }
        }
    }
}