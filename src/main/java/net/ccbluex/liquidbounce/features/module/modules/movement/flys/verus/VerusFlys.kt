package net.ccbluex.liquidbounce.features.module.modules.movement.flys.verus

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
import net.minecraft.client.settings.GameSettings
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.stats.StatList
import net.minecraft.util.AxisAlignedBB
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



    override fun onEnable() {
        times = 0
        timer.reset()
        ticks = 0
        justEnabled = true
        if (flys.equals("Custom")) {
            if (!mc.thePlayer.onGround && onlyOnGround.get()) return
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
                    mc.gameSettings.keyBindJump.pressed = false
                    if (times < 5 && !moveBeforeDamage.get()) {
                        MovementUtils.strafe(0f)
                    }
                    if (mc.thePlayer.onGround && times < 5) {
                        times++
                        timer.reset()
                        if (times <5) {
                            mc.thePlayer.jump()
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
                    mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
                    if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
                        mc.gameSettings.keyBindJump.pressed = false
                        mc.thePlayer.jump()
                        MovementUtils.strafe(0.48F)
                    } else if(airStrafeValue.get()) {
                        MovementUtils.strafe()
                    }
                }
            }
            "Basic" -> {
                if (verusMode.get() === "Packet1") {
                    if(mc.thePlayer.motionY < 0.4) {
                        mc.thePlayer.motionY = 0.0
                    }
                    mc.thePlayer.onGround = true
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
                mc.gameSettings.keyBindJump.pressed = false
                mc.gameSettings.keyBindSneak.pressed = false
                if (ticks % 14 == 0 && mc.thePlayer.onGround) {
                    justEnabled = false
                    MovementUtils.strafe(0.69f)
                    event.y = 0.42
                    ticks = 0
                    mc.thePlayer.motionY = -(mc.thePlayer.posY - floor(mc.thePlayer.posY))
                } else {
                    if (GameSettings.isKeyDown(mc.gameSettings.keyBindJump) && ticks % 2 == 1) {
                        if (mc.thePlayer.ticksExisted % 2 == 0) {
                            mc.thePlayer.motionY = 0.42
                            MovementUtils.strafe(0.3f)
                        }
                    }
                    if (mc.thePlayer.onGround) {
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
                    val pos = mc.thePlayer.position.add(0.0, -1.5, 0.0)
                    PacketUtils.sendPacketNoEvent(
                        C08PacketPlayerBlockPlacement(pos, 1,
                            ItemStack(Blocks.stone.getItem(mc.theWorld, pos)), 0.0F, 0.5F + Math.random().toFloat() * 0.44.toFloat(), 0.0F)
                    )
                    if(mc.thePlayer.onGround && !jumped) {
                        mc.thePlayer.jump()
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
                    if (mc.thePlayer.onGround) {
                        MovementUtils.strafe(groundSpeedValue.get())
                        waitTicks++
                        if (waitTicks >= hopDelayValue.get()) {
                            waitTicks = 0
                            mc.thePlayer.triggerAchievement(StatList.jumpStat)
                            mc.thePlayer.motionY = 0.0
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