package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.vulcan

import me.zywl.fdpclient.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import me.zywl.fdpclient.value.impl.BoolValue
import me.zywl.fdpclient.value.impl.IntegerValue
import me.zywl.fdpclient.value.impl.ListValue
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.settings.GameSettings
import net.minecraft.util.Timer
import kotlin.math.abs

class VulcanSpeeds : SpeedMode("Vulcan") {

    private val modeValue = ListValue("Vulcan-Mode", arrayOf("LowHop","Hop", "OldGround", "YPort", "YPort2", "LowHop2"), "LowHop")

    private val boostDelayValue = IntegerValue("Boost-Delay", 8, 2, 15).displayable { modeValue.equals("OldGround") }
    private val boostSpeedValue = BoolValue("Ground-Boost", true).displayable { modeValue.equals("OldGround") }


    // Variable
    private var portSwitcher = 0
    private var wasTimer = false
    private var ticks = 0
    private var jumped = false
    private var jumpCount = 0
    private var yMotion = 0.0
    private var launchY = 0.0

    // Optimize code
    val player: EntityPlayerSP
        get() = mc.thePlayer
    val timer: Timer
        get() = mc.timer
    val settings: GameSettings
        get() = mc.gameSettings


    override fun onEnable() {
        ticks = 0
        wasTimer = true
        timer.timerSpeed = 1.0f
        portSwitcher = 0
        launchY = player.posY
    }

    override fun onDisable() {
        wasTimer = true
        timer.timerSpeed = 1.0f
        portSwitcher = 0
        ticks = 0
    }

    override fun onMove(event: MoveEvent) {
        if (modeValue.equals("OldGround")) {
            if (jumpCount >= boostDelayValue.get() && boostSpeedValue.get()) {
                event.x *= 1.7181145141919810
                event.z *= 1.7181145141919810
                jumpCount = 0
            } else if (!boostSpeedValue.get()) {
                jumpCount = 4
            }
        }
    }

    override fun onUpdate() {

        when (modeValue.get()) {
            "LowHop" -> {
                ticks++
                player.jumpMovementFactor = 0.0245f
                if (player.onGround && MovementUtils.isMoving()) {
                    player.jump()
                    ticks = 0
                    MovementUtils.strafe()
                    if (MovementUtils.getSpeed() < 0.5f) {
                        MovementUtils.strafe(0.484f)
                    }
                    launchY = player.posY
                }else if (player.posY > launchY && ticks <= 1) {
                    player.setPosition(player.posX, launchY, player.posZ)
                }else if (ticks == 5) {
                    player.motionY = -0.17
                }
                if (MovementUtils.getSpeed() < 0.215) {
                    MovementUtils.strafe(0.215f)
                }
            }
            "Hop" -> {
                if (wasTimer) {
                    timer.timerSpeed = 1.00f
                    wasTimer = false
                }
                if (abs(player.movementInput.moveStrafe) < 0.1f) {
                    player.jumpMovementFactor = 0.026499f
                }else {
                    player.jumpMovementFactor = 0.0244f
                }
                settings.keyBindJump.pressed = GameSettings.isKeyDown(settings.keyBindJump)

                if (MovementUtils.getSpeed() < 0.215f && !player.onGround) {
                    MovementUtils.strafe(0.215f)
                }
                if (player.onGround && MovementUtils.isMoving()) {
                    settings.keyBindJump.pressed = false
                    player.jump()
                    if (!player.isAirBorne) {
                        return //Prevent flag with Fly
                    }
                    timer.timerSpeed = 1.25f
                    wasTimer = true
                    MovementUtils.strafe()
                    if(MovementUtils.getSpeed() < 0.5f) {
                        MovementUtils.strafe(0.4849f)
                    }
                }else if (!MovementUtils.isMoving()) {
                    timer.timerSpeed = 1.00f
                    player.motionX = 0.0
                    player.motionZ = 0.0
                }

            }
            "OldGround" -> {
                if (jumped) {
                    player.motionY = -0.1
                    player.onGround = false
                    jumped = false
                    yMotion = 0.0
                }
                player.jumpMovementFactor = 0.025f
                if (player.onGround && MovementUtils.isMoving()) {
                    if (player.isCollidedHorizontally || settings.keyBindJump.pressed) {
                        if (!settings.keyBindJump.pressed) {
                            player.jump()
                        }
                        return
                    }
                    player.jump()
                    player.motionY = 0.0
                    yMotion = 0.1 + Math.random() * 0.03
                    MovementUtils.strafe(0.48f + jumpCount * 0.001f)
                    jumpCount++
                    jumped = true
                } else if (MovementUtils.isMoving()) {
                    MovementUtils.strafe(0.27f + jumpCount * 0.0018f)
                }
            }
            "YPort" -> {
                ticks++
                if (wasTimer) {
                    timer.timerSpeed = 1.00f
                    wasTimer = false
                }
                player.jumpMovementFactor = 0.0245f
                if (!player.onGround && ticks > 3 && player.motionY > 0) {
                    player.motionY = -0.27
                }

                settings.keyBindJump.pressed = GameSettings.isKeyDown(settings.keyBindJump)
                if (MovementUtils.getSpeed() < 0.215f && !player.onGround) {
                    MovementUtils.strafe(0.215f)
                }
                if (player.onGround && MovementUtils.isMoving()) {
                    ticks = 0
                    settings.keyBindJump.pressed = false
                    player.jump()
                    if (!player.isAirBorne) {
                        return //Prevent flag with Fly
                    }
                    timer.timerSpeed = 1.2f
                    wasTimer = true
                    if(MovementUtils.getSpeed() < 0.48f) {
                        MovementUtils.strafe(0.48f)
                    }else{
                        MovementUtils.strafe((MovementUtils.getSpeed() *0.985).toFloat())
                    }
                }else if (!MovementUtils.isMoving()) {
                    timer.timerSpeed = 1.00f
                    player.motionX = 0.0
                    player.motionZ = 0.0
                }

            }
            "LowHop2" -> {
                if (MovementUtils.isMoving()) {
                    if (!player.onGround && player.fallDistance > 1.1) {
                        timer.timerSpeed = 1f
                        player.motionY = -0.25
                        return
                    }

                    if (player.onGround) {
                        player.jump()
                        MovementUtils.strafe(0.4815f)
                        timer.timerSpeed = 1.263f
                    } else if (player.ticksExisted % 4 == 0) {
                        if (player.ticksExisted % 3 == 0) {
                            player.motionY = -0.01 / player.motionY
                        } else {
                            player.motionY = -player.motionY / player.posY
                        }
                        timer.timerSpeed = 0.8985f
                    }
                }
            }
        }
    }
}

