/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.flys.verus

import me.zywl.fdpclient.event.*
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.BlockAir
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.settings.GameSettings
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.stats.StatList
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Timer
import kotlin.math.floor

class VerusFlys : FlyMode("Verus") {

    private var flys = ListValue("Verus-Mode", arrayOf("Jump", "Collide", "Basic", "Custom"), "Jump")

    // Jump
    private val boostValue = BoolValue("${valuePrefix}Boost", false).displayable { flys.equals("Jump") }
    private val speedValue = FloatValue("${valuePrefix}Speed", 2f, 0f, 3f).displayable { boostValue.get() && flys.equals("Jump") }
    private val boostLength = IntegerValue("${valuePrefix}BoostTime", 500, 300, 1000).displayable { boostValue.get() && flys.equals("Jump") }
    private val moveBeforeDamage = BoolValue("${valuePrefix}MoveBeforeDamage", true).displayable { boostValue.get() && flys.equals("Jump") }
    private val airStrafeValue = BoolValue("${valuePrefix}AirStrafe", true).displayable { flys.equals("Jump") }

    // Basic
    private val verusMode = ListValue("VerusBasic-Mode", arrayOf("Packet1", "Packet2"), "Packet1").displayable { flys.equals("Basic") }

    // Custom
    private val airSpeedValue = FloatValue("${valuePrefix}AirSpeed", 0.5f, 0f, 1f).displayable { flys.equals("Custom") }
    private val groundSpeedValue = FloatValue("${valuePrefix}GroundSpeed", 0.42f, 0f, 1f).displayable { flys.equals("Custom") }
    private val hopDelayValue = IntegerValue("${valuePrefix}HopDelay", 3, 0, 10).displayable { flys.equals("Custom") }
    private val onlyOnGround = BoolValue("${valuePrefix}OnlyEnableOnGround", true).displayable { flys.equals("Custom") }


    // Variables
    private var times = 0
    private var timer = MSTimer()
    private var ticks = 0
    private var justEnabled = true
    private var jumped = false
    private var waitTicks = 0

    // Optimize code
    val player: EntityPlayerSP
        get() = mc.thePlayer
    val settings: GameSettings
        get() = mc.gameSettings

    
    


    override fun onEnable() {
        times = 0
        timer.reset()
        ticks = 0
        justEnabled = true
        if (flys.equals("Custom")) {
            if (!player.onGround && onlyOnGround.get()) return
            waitTicks = 0
        }

        if (flys.equals("Collide") || fly.equals("Custom")) {
            sendLegacy()
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        when (flys.get()) {
            "Jump" -> {
                if (boostValue.get()) {
                    settings.keyBindJump.pressed = false
                    if (times < 5 && !moveBeforeDamage.get()) {
                        MovementUtils.strafe(0f)
                    }
                    if (player.onGround && times < 5) {
                        times++
                        timer.reset()
                        if (times <5) {
                            player.jump()
                            MovementUtils.strafe(0.48F)
                        }
                    }

                    if (times >= 5) {
                        if (!timer.hasTimePassed(boostLength.get().toLong())) {
                            MovementUtils.strafe(speedValue.get())
                        } else {
                            times = 0
                        }
                    }
                } else {
                    settings.keyBindJump.pressed = GameSettings.isKeyDown(settings.keyBindJump)
                    if (player.onGround && MovementUtils.isMoving()) {
                        settings.keyBindJump.pressed = false
                        player.jump()
                        MovementUtils.strafe(0.48F)
                    } else if(airStrafeValue.get()) {
                        MovementUtils.strafe()
                    }
                }
            }
            "Basic" -> {
                if (verusMode.get() === "Packet1") {
                    if(player.motionY < 0.4) {
                        player.motionY = 0.0
                    }
                    player.onGround = true
                }
            }
        }

    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        when (flys.get()) {
            "Jump" -> {
                if(boostValue.get()) {
                    if (packet is C03PacketPlayer) {
                        packet.onGround = (times >= 5 && !timer.hasTimePassed(boostLength.get().toLong()))
                    }
                }
            }
            "Basic" -> {
                if(packet is C03PacketPlayer) {
                    if(verusMode.get() === "Packet1") {
                        packet.onGround = true
                    }
                }
            }
        }
    }

    override fun onMove(event: MoveEvent) {
        when (flys.get()) {
            "Collide" -> {
                settings.keyBindJump.pressed = false
                settings.keyBindSneak.pressed = false
                if (ticks % 14 == 0 && player.onGround) {
                    justEnabled = false
                    MovementUtils.strafe(0.69f)
                    event.y = 0.42
                    ticks = 0
                    player.motionY = -(player.posY - floor(player.posY))
                } else {
                    if (GameSettings.isKeyDown(settings.keyBindJump) && ticks % 2 == 1) {
                        if (player.ticksExisted % 2 == 0) {
                            player.motionY = 0.42
                            MovementUtils.strafe(0.3f)
                        }
                    }
                    if (player.onGround) {
                        if (!justEnabled) {
                            MovementUtils.strafe(1.01f)
                        }
                    } else {
                        MovementUtils.strafe(0.41f)
                    }
                }
                ticks++
            }
            "Basic" -> {
                if (verusMode.get() === "Packet2") {
                    val pos = player.position.add(0.0, -1.5, 0.0)
                    PacketUtils.sendPacketNoEvent(
                        C08PacketPlayerBlockPlacement(pos, 1,
                            ItemStack(Blocks.stone.getItem(mc.theWorld, pos)), 0.0F, 0.5F + Math.random().toFloat() * 0.44.toFloat(), 0.0F)
                    )
                    if(player.onGround && !jumped) {
                        player.jump()
                        event.y = 0.42
                        jumped = true
                    }else {
                        event.y = 0.0
                        MovementUtils.strafe(0.35f)
                    }
                }
            }
            "Custom" -> {
                if (MovementUtils.isMoving()) {
                    if (player.onGround) {
                        MovementUtils.strafe(groundSpeedValue.get())
                        waitTicks++
                        if (waitTicks >= hopDelayValue.get()) {
                            waitTicks = 0
                            player.triggerAchievement(StatList.jumpStat)
                            player.motionY = 0.0
                            event.y = 0.41999998688698
                        }
                    } else {
                        MovementUtils.strafe(airSpeedValue.get())
                    }
                }
            }
        }
    }

    override fun onBlockBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y <= fly.launchY) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, fly.launchY, event.z + 1.0)
        }
    }

    override fun onJump(event: JumpEvent) {
        if (flys.equals("Collide") || flys.equals("Custom")) {
            event.cancelEvent()
        }
    }
}