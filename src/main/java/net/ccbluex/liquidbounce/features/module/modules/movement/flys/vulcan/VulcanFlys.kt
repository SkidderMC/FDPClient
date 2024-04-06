/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.flys.vulcan

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.TransferUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.BlockLadder
import net.minecraft.client.settings.GameSettings
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.client.C0FPacketConfirmTransaction
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import net.minecraft.block.material.Material

class VulcanFlys : FlyMode("Vulcan") {

    /**
     * The fly called GhostNew is imported from LiquidBounce Legacy
     * Credits to EclipseDev for this
     */


    private var flys = ListValue("Vulcan-Mode", arrayOf("High", "Clip", "Damage", "Fast", "Ghost", "GhostNew"), "GhostNew")

    private val vulcanhighheight = IntegerValue("VulcanHigh-ClipHeight", 10, 50, 100).displayable { flys.equals("High") }
    private val vulcanclipcanClipValue = BoolValue("VulcanClip-CanClip", true).displayable { flys.equals("Clip") }

    // Vulcan Damage
    private val bypassMode = ListValue("VulcanDmg-BypassMode", arrayOf("Damage", "SelfDamage", "InstantDamage", "Flag"), "InstantDamage").displayable { flys.equals("Damage") }
    private val flyMode = ListValue("VulcanDmg-FlyMode", arrayOf("Timer", "CancelMove", "Clip"), "CancelMove").displayable { flys.equals("Damage") }
    private val flyHSpeedValue = FloatValue("VulcanDmg-Horizontal", 1.0f, 0.5f, 2.5f).displayable { flyMode.equals("CancelMove") && flys.equals("Damage") }
    private val flyVSpeedValue = FloatValue("VulcanDmg-Vertical", 0.42f, 0.42f, 2.5f).displayable{ flyMode.equals("CancelMove") && flys.equals("Damage") }
    private val flyDistanceValue = FloatValue("VulcanDmg-Distance", 10.0f, 6.0f, 10.0f).displayable { flys.equals("Damage") }
    private val autoDisableValue = BoolValue("VulcanDmg-AutoDisable", true).displayable { flys.equals("Damage") }
    private val flyTimerValue = FloatValue("VulcanDmg-Timer", 0.05f, 0.05f, 0.25f).displayable{ flyMode.equals("Timer") }

    // Fast
    private val speedValue = FloatValue("VulcanFast-Speed", 1f, 0.1f, 6f).displayable { flys.equals("Fast") }
    private val verticalValue = BoolValue("VulcanFast-Vertical", false).displayable { flys.equals("Fast") }

    // Ghost
    private val timerValue = FloatValue("VulcanGhost-Timer", 2f, 1f, 3f).displayable { flys.equals("Ghost") }


    // Variables
    private var ticks = 0
    private var sent = false
    private var waitFlag = false
    private var canGlide = false
    private var isStarted = false
    private var isDamaged = false
    private var dmgJumpCount = 0
    private var flyTicks = 0
    private var lastSentX = 0.0
    private var lastSentY = 0.0
    private var lastSentZ = 0.0
    private var lastTickX = 0.0
    private var lastTickY = 0.0
    private var lastTickZ = 0.0
    private var isSuccess = false
    private var vticks = 0
    private var doCancel = false
    private var stage = FlyStage.FLYING
    private var startX = 0.0
    private var startZ = 0.0
    private var startY = 0.0
    private var modifyTicks = 0
    private var flags = 0
    private var groundX = 0.0
    private var groundY = 0.0
    private var groundZ = 0.0

    override fun onEnable() {
        sent = false


        when (flys.get()) {
            "GhostNew" -> {
                ClientUtils.displayChatMessage("§8[§c§lVulcanFly§8] §fEnsure that you sneak on landing.")
                ClientUtils.displayChatMessage("§8[§c§lVulcanFly§8] §fAfter landing, go backward (Air) and go forward to landing location, then sneak again.")
                ClientUtils.displayChatMessage("§8[§c§lVulcanFly§8] §fAnd then you can turn off fly.")
            }
            "Clip" -> {
                if(mc.thePlayer.onGround && vulcanclipcanClipValue.get()) {
                    clip(0f, -0.1f)
                    waitFlag = true
                    canGlide = false
                    ticks = 0
                    mc.timer.timerSpeed = 0.1f
                } else {
                    waitFlag = false
                    canGlide = true
                }
            }
            "Damage" -> {
                flyTicks = 0
                waitFlag = false
                isStarted = false
                isDamaged = false
                dmgJumpCount = 0
                mc.timer.timerSpeed = 1.0f
                if (bypassMode.equals("InstantDamage")) {
                    dmgJumpCount = 11451
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY,
                            mc.thePlayer.posZ,
                            true
                        )
                    )
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY - 0.0784,
                            mc.thePlayer.posZ,
                            false
                        )
                    )
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY,
                            mc.thePlayer.posZ,
                            true
                        )
                    )
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY + 0.41999998688697815,
                            mc.thePlayer.posZ,
                            false
                        )
                    )
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY + 0.7531999805212,
                            mc.thePlayer.posZ,
                            false
                        )
                    )
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY + 1.0,
                            mc.thePlayer.posZ,
                            true
                        )
                    )
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY + 1.4199999868869781,
                            mc.thePlayer.posZ,
                            false
                        )
                    )
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY + 1.7531999805212,
                            mc.thePlayer.posZ,
                            false
                        )
                    )
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY + 2.0,
                            mc.thePlayer.posZ,
                            true
                        )
                    )
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY + 2.419999986886978,
                            mc.thePlayer.posZ,
                            false
                        )
                    )
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY + 2.7531999805212,
                            mc.thePlayer.posZ,
                            false
                        )
                    )
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY + 3.00133597911214,
                            mc.thePlayer.posZ,
                            false
                        )
                    )
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 3.00133597911214, mc.thePlayer.posZ)
                    waitFlag = true
                } else if (bypassMode.equals("Flag")) {
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY,
                            mc.thePlayer.posZ,
                            true
                        )
                    )
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY - 2,
                            mc.thePlayer.posZ,
                            true
                        )
                    )
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY,
                            mc.thePlayer.posZ,
                            true
                        )
                    )
                } else {
                    runSelfDamageCore()
                }
            }
            "Fast" -> {
                vticks = 0
                doCancel = false
                if(verticalValue.get()) {
                    ClientUtils.displayChatMessage("§8[§c§lVulcan-Fast-Flight§8] §cVertical Flying sometimes flag!")
                }
                if(mc.thePlayer.posY % 1 != 0.0) {
                    fly.state = false
                    ClientUtils.displayChatMessage("§8[§c§lVulcan-Fast-Flight§8] §cPlease stand on a solid block to fly!")
                    isSuccess = true
                    return
                }
                stage = FlyStage.FLYING
                isSuccess = false
                ClientUtils.displayChatMessage("§8[§c§lVulcan-Fast-Flight§8] §aPlease press Sneak before you land on ground!")
                ClientUtils.displayChatMessage("§8[§c§lVulcan-Fast-Flight§8] §7Tips: DO NOT Use killaura when you're flying!")
                startX = mc.thePlayer.posX
                startY = mc.thePlayer.posY
                startZ = mc.thePlayer.posZ
            }
            "Ghost" -> {
                ticks = 0
                modifyTicks = 0
                flags = 0
                mc.thePlayer.setPosition(mc.thePlayer.posX, (mc.thePlayer.posY * 2).roundToInt().toDouble() / 2, mc.thePlayer.posZ)
                stage = FlyStage.WAITING
                ClientUtils.displayChatMessage("§8[§c§lVulcan-Ghost-Flight§8] §aPlease press Sneak before you land on ground!")
                ClientUtils.displayChatMessage("§8[§c§lVulcan-Ghost-Flight§8] §aYou can go Up/Down by pressing Jump/Sneak")
            }
        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1.0f

        if (flys.equals("Fast")) {
            if (!isSuccess) {
                mc.thePlayer.setPosition(startX, startY, startZ)
                ClientUtils.displayChatMessage("§8[§c§lVulcan-Fast-Flight§8] §cFly attempt Failed...")
                ClientUtils.displayChatMessage("§8[§c§lVulcan-Fast-Flight§8] §cIf it keeps happen, Don't use it again in CURRENT gameplay")
            }
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        when (flys.get()) {
            "Damage" -> {
                if (flyTicks > 7 && autoDisableValue.get()) {
                    fly.state = false
                    fly.onDisable()
                    return
                }

                if (!bypassMode.equals("InstantDamage") && runSelfDamageCore()) {
                    return
                }
                if (bypassMode.equals("InstantDamage") && dmgJumpCount == 11451) {
                    if (!isStarted) {
                        return
                    } else {
                        isStarted = false
                        waitFlag = false
                        mc.netHandler.addToSendQueue(
                            C03PacketPlayer.C04PacketPlayerPosition(
                                mc.thePlayer.posX,
                                mc.thePlayer.posY,
                                mc.thePlayer.posZ,
                                true
                            )
                        )
                        dmgJumpCount = 999
                    }
                }
                mc.thePlayer.jumpMovementFactor = 0.00f
                if (!isStarted && !waitFlag) {
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY - 0.0784,
                            mc.thePlayer.posZ,
                            false
                        )
                    )
                    waitFlag = true
                }
                if (isStarted) {
                    when (flyMode.get().lowercase()) {
                        "cancelmove" -> {
                            mc.timer.timerSpeed = 1.0f
                            MovementUtils.resetMotion(false)
                            if (!mc.gameSettings.keyBindSneak.isKeyDown) {
                                MovementUtils.resetMotion(true)
                                if (mc.gameSettings.keyBindJump.isKeyDown) {
                                    mc.thePlayer.motionY = flyVSpeedValue.get().toDouble()
                                }
                            }

                            MovementUtils.strafe(flyHSpeedValue.get())
                        }
                        "timer" -> {
                            flyTicks++
                            mc.timer.timerSpeed = flyTimerValue.get()
                            MovementUtils.resetMotion(true)
                            if (flyTicks > 4) {
                                MovementUtils.strafe(flyDistanceValue.get() - 0.005f)
                            } else {
                                MovementUtils.strafe(flyDistanceValue.get() - 0.205f + flyTicks.toFloat() * 0.05f)
                            }
                        }
                        "clip" -> {
                            MovementUtils.resetMotion(true)
                            if (mc.thePlayer.ticksExisted % 10 == 0) {
                                flyTicks++
                                val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
                                mc.thePlayer.setPosition(mc.thePlayer.posX + (-sin(yaw) * flyDistanceValue.get()), mc.thePlayer.posY + 0.42, mc.thePlayer.posZ + (cos(yaw) * flyDistanceValue.get()))
                                PacketUtils.sendPacketNoEvent(
                                    C03PacketPlayer.C04PacketPlayerPosition(
                                        mc.thePlayer.posX,
                                        mc.thePlayer.posY,
                                        mc.thePlayer.posZ,
                                        false
                                    )
                                )
                            }
                        }
                    }
                }
            }
            "Fast" -> {
                when (stage) {
                    FlyStage.FLYING -> {
                        isSuccess = false

                        MovementUtils.resetMotion(false)

                        MovementUtils.strafe(speedValue.get())
                        doCancel = true

                        if(mc.gameSettings.keyBindSneak.pressed) {
                            MovementUtils.strafe(0.45f)
                            if(verticalValue.get()) {
                                mc.thePlayer.motionY = 0.0 - speedValue.get().toDouble()
                            }
                        }
                        if(verticalValue.get()) {
                            if(mc.gameSettings.keyBindJump.pressed) {
                                mc.thePlayer.motionY = speedValue.get().toDouble()
                            } else if(!mc.gameSettings.keyBindSneak.pressed) {
                                mc.thePlayer.motionY = 0.0
                            }
                        }
                        if(mc.gameSettings.keyBindSneak.pressed && mc.thePlayer.ticksExisted % 2 == 1) {
                            val fixedY = mc.thePlayer.posY - (mc.thePlayer.posY % 1)
                            val underBlock2 = BlockUtils.getBlock(BlockPos(mc.thePlayer.posX, fixedY - 1, mc.thePlayer.posZ)) ?: return
                            if(underBlock2.isFullBlock) {
                                stage = FlyStage.WAIT_APPLY
                                MovementUtils.resetMotion(true)
                                mc.thePlayer.jumpMovementFactor = 0.00f
                                doCancel = false
                                mc.thePlayer.onGround = false
                                var fixedX = mc.thePlayer.posX - (mc.thePlayer.posX % 1)
                                var fixedZ = mc.thePlayer.posZ - (mc.thePlayer.posZ % 1)
                                if(fixedX>0) {
                                    fixedX += 0.5
                                }else{
                                    fixedX -= 0.5
                                }
                                if(fixedZ>0) {
                                    fixedZ += 0.5
                                }else{
                                    fixedZ -= 0.5
                                }
                                mc.thePlayer.setPosition(fixedX, fixedY, fixedZ)
                                mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, fixedY , mc.thePlayer.posZ, true))
                                doCancel = true
                                ClientUtils.displayChatMessage("§8[§c§lVulcan-Flight§8] §aWaiting for landing...")
                            } else {
                                ClientUtils.displayChatMessage("§8[§c§lVulcan-Flight§8] §cYou can only land on a solid block!")
                            }
                        }
                    }
                    FlyStage.WAIT_APPLY -> {
                        vticks++
                        doCancel = false
                        if(vticks == 60) {
                            ClientUtils.displayChatMessage("§8[§c§lVulcan-Flight§8] §cSeems took a long time! Please turn off the Flight manually")
                        }
                        mc.timer.timerSpeed = 1f
                        MovementUtils.resetMotion(true)
                        mc.thePlayer.jumpMovementFactor = 0.00f
                        val fixedY = mc.thePlayer.posY - (mc.thePlayer.posY % 1)
                        if(mc.theWorld.getCollisionBoxes(mc.thePlayer.entityBoundingBox.offset(0.0, -10.0, 0.0)).isEmpty() && mc.theWorld.getCollisionBoxes(mc.thePlayer.entityBoundingBox.offset(0.0, -12.0, 0.0)).isEmpty()) {
                            mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, fixedY - 10, mc.thePlayer.posZ, true))
                        }else {
                            mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, fixedY - 1024, mc.thePlayer.posZ, true))
                        }
                        doCancel = true
                    }
                }
            }
            "Ghost" -> {
                ticks++
                modifyTicks++
                mc.gameSettings.keyBindJump.pressed = false
                mc.gameSettings.keyBindSneak.pressed = false
                when(stage) {
                    FlyStage.FLYING, FlyStage.WAITING -> {
                        if(stage == FlyStage.FLYING) {
                            mc.timer.timerSpeed = timerValue.get()
                        }else{
                            mc.timer.timerSpeed = 1.0f
                        }
                        if(ticks == 2 && GameSettings.isKeyDown(mc.gameSettings.keyBindJump) && modifyTicks>=6 && mc.theWorld.getCollisionBoxes(mc.thePlayer.entityBoundingBox.offset(0.0, 0.5, 0.0)).isEmpty()) {
                            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY+0.5, mc.thePlayer.posZ)
                            modifyTicks = 0
                        }
                        if(!MovementUtils.isMoving() && ticks == 1 && (GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) || GameSettings.isKeyDown(mc.gameSettings.keyBindJump)) && modifyTicks>=5) {
                            val playerYaw = mc.thePlayer.rotationYaw * Math.PI / 180
                            mc.thePlayer.setPosition(mc.thePlayer.posX + 0.05 * -sin(playerYaw)
                                , mc.thePlayer.posY
                                , mc.thePlayer.posZ + 0.05 * cos(playerYaw)
                            )
                        }
                        if(ticks == 2 && GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) && modifyTicks>=6 && mc.theWorld.getCollisionBoxes(mc.thePlayer.entityBoundingBox.offset(0.0, -0.5, 0.0)).isEmpty()) {
                            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY-0.5, mc.thePlayer.posZ)
                            modifyTicks = 0
                        }else if(ticks == 2 && GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) && mc.theWorld.getCollisionBoxes(mc.thePlayer.entityBoundingBox.offset(0.0, -0.5, 0.0))
                                .isNotEmpty()) {
                            PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX+0.05,mc.thePlayer.posY,mc.thePlayer.posZ,true))
                            PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX,mc.thePlayer.posY,mc.thePlayer.posZ,true))
                            PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX,mc.thePlayer.posY+0.42,mc.thePlayer.posZ,true))
                            PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX,mc.thePlayer.posY+0.7532,mc.thePlayer.posZ,true))
                            PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX,mc.thePlayer.posY+1.0,mc.thePlayer.posZ,true))
                            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY+1.0, mc.thePlayer.posZ)
                            stage = FlyStage.WAIT_APPLY
                            modifyTicks = 0
                            groundY = mc.thePlayer.posY - 1.0
                            groundX = mc.thePlayer.posX
                            groundZ = mc.thePlayer.posZ
                            ClientUtils.displayChatMessage("§8[§c§lVulcan-Ghost-Flight§8] §aWaiting to land...")
                        }
                        mc.thePlayer.onGround = true
                        mc.thePlayer.motionY = 0.0
                    }
                    FlyStage.WAIT_APPLY -> {
                        mc.timer.timerSpeed = 1.0f
                        MovementUtils.resetMotion(true)
                        mc.thePlayer.jumpMovementFactor = 0.0f
                        if (modifyTicks >= 10) {
                            val playerYaw = mc.thePlayer.rotationYaw * Math.PI / 180
                            if (modifyTicks % 2 != 0) {
                                mc.thePlayer.setPosition(mc.thePlayer.posX + 0.1 * -sin(playerYaw)
                                    , mc.thePlayer.posY
                                    , mc.thePlayer.posZ + 0.1 * cos(playerYaw)
                                )
                            }else{
                                mc.thePlayer.setPosition(mc.thePlayer.posX - 0.1 * -sin(playerYaw)
                                    , mc.thePlayer.posY
                                    , mc.thePlayer.posZ - 0.1 * cos(playerYaw)
                                )
                                if (modifyTicks >= 16 && ticks == 2) {
                                    modifyTicks = 16
                                    mc.thePlayer.setPosition(mc.thePlayer.posX
                                        , mc.thePlayer.posY + 0.5
                                        , mc.thePlayer.posZ)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onMotion(event: MotionEvent) {
        when (flys.get()) {
            "High" -> {
                if (event.eventState == EventState.PRE && !mc.thePlayer.onGround) {
                    mc.timer.timerSpeed = 1f
                    mc.thePlayer.motionY = -if(ticks % 2 == 0) {
                        0.16
                    } else {
                        0.10
                    }
                    if(ticks == 0) {
                        mc.thePlayer.motionY = -0.07
                    }
                    ticks++
                } else if (mc.thePlayer.onGround && !sent) {
                    sent = true
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY,
                            mc.thePlayer.posZ,
                            true
                        )
                    )
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY - 0.0784,
                            mc.thePlayer.posZ,
                            false
                        )
                    )
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY,
                            mc.thePlayer.posZ,
                            true
                        )
                    )
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY + 0.41999998688697815,
                            mc.thePlayer.posZ,
                            false
                        )
                    )
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY + 0.7531999805212,
                            mc.thePlayer.posZ,
                            false
                        )
                    )
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY + 1.0,
                            mc.thePlayer.posZ,
                            true
                        )
                    )
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY + 1.4199999868869781,
                            mc.thePlayer.posZ,
                            false
                        )
                    )
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY + 1.7531999805212,
                            mc.thePlayer.posZ,
                            false
                        )
                    )
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY + 2.0,
                            mc.thePlayer.posZ,
                            true
                        )
                    )
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY + 2.419999986886978,
                            mc.thePlayer.posZ,
                            false
                        )
                    )
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY + 2.7531999805212,
                            mc.thePlayer.posZ,
                            false
                        )
                    )
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY + 3.00133597911214,
                            mc.thePlayer.posZ,
                            false
                        )
                    )
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 3.00133597911214, mc.thePlayer.posZ)
                }

                if (sent && mc.thePlayer.hurtTime == 9) {
                    mc.thePlayer.posY += vulcanhighheight.get()
                }
            }
            "Clip" -> {
                if (event.eventState == EventState.PRE && canGlide) {
                    mc.timer.timerSpeed = 1f
                    mc.thePlayer.motionY = -if(ticks % 2 == 0) {
                        0.17
                    } else {
                        0.10
                    }
                    if(ticks == 0) {
                        mc.thePlayer.motionY = -0.07
                    }
                    ticks++
                }
            }
            "" -> {}
        }
    }

    override fun onBlockBB(event: BlockBBEvent) {
        if (!flys.equals("GhostNew"))
        if (!mc.gameSettings.keyBindJump.isKeyDown && mc.gameSettings.keyBindSneak.isKeyDown) return
        if (!event.block.material.blocksMovement() && event.block.material != Material.carpet && event.block.material != Material.vine && event.block.material != Material.snow && event.block !is BlockLadder) {
            event.boundingBox = AxisAlignedBB(
                -2.0,
                -1.0,
                -2.0,
                2.0,
                1.0,
                2.0
            ).offset(
                event.x.toDouble(),
                event.y.toDouble(),
                event.z.toDouble()
            )
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        when (flys.get()) {
            "GhostNew" -> {
                if (packet is S08PacketPlayerPosLook) {
                    event.cancelEvent()
                }
            }
            "Clip" -> {
                if(packet is S08PacketPlayerPosLook && waitFlag) {
                    waitFlag = false
                    mc.thePlayer.setPosition(packet.x, packet.y, packet.z)
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C06PacketPlayerPosLook(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY,
                            mc.thePlayer.posZ,
                            mc.thePlayer.rotationYaw,
                            mc.thePlayer.rotationPitch,
                            false
                        )
                    )
                    event.cancelEvent()
                    mc.thePlayer.jump()
                    clip(0.127318f, 0f)
                    clip(3.425559f, 3.7f)
                    clip(3.14285f, 3.54f)
                    clip(2.88522f, 3.4f)
                    canGlide = true
                }
            }
            "Damage" -> {
                if (packet is C03PacketPlayer && waitFlag) {
                    event.cancelEvent()
                }
                if (packet is C03PacketPlayer && (dmgJumpCount < 4 && ( bypassMode.equals("SelfDamage") || bypassMode.equals("InstantDamage") ) )) {
                    packet.onGround = false
                }
                if (isStarted && flyMode.equals("cancelmove")) {
                    if(packet is C03PacketPlayer && (packet is C03PacketPlayer.C04PacketPlayerPosition || packet is C03PacketPlayer.C06PacketPlayerPosLook)) {
                        val deltaX = packet.x - lastSentX
                        val deltaY = packet.y - lastSentY
                        val deltaZ = packet.z - lastSentZ

                        if (sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) > flyDistanceValue.get()) {
                            flyTicks++
                            PacketUtils.sendPacketNoEvent(
                                C03PacketPlayer.C04PacketPlayerPosition(
                                    lastTickX,
                                    lastTickY,
                                    lastTickZ,
                                    false
                                )
                            )
                            lastSentX = lastTickX
                            lastSentY = lastTickY
                            lastSentZ = lastTickZ
                        }
                        lastTickX = packet.x
                        lastTickY = packet.y
                        lastTickZ = packet.z
                        event.cancelEvent()
                    }else if(packet is C03PacketPlayer) {
                        event.cancelEvent()
                    }
                }

                if (packet is C03PacketPlayer && flyMode.equals("clip") && isStarted) {
                    event.cancelEvent()
                }

                if (packet is S08PacketPlayerPosLook) {
                    isStarted = true
                    waitFlag = false
                }

                if (packet is S08PacketPlayerPosLook && waitFlag && !flyMode.equals("cancelmove")) {
                    if (bypassMode.equals("InstantDamage")) PacketUtils.sendPacketNoEvent(
                        C03PacketPlayer.C06PacketPlayerPosLook(
                            packet.x,
                            packet.y,
                            packet.z,
                            packet.yaw,
                            packet.pitch,
                            false
                        )
                    )
                    mc.timer.timerSpeed = 1.0f
                    flyTicks = 0

                } else if (packet is S08PacketPlayerPosLook && flyMode.equals("cancelmove")) {
                    lastSentX = packet.x
                    lastSentY = packet.y
                    lastSentZ = packet.z

                    if (!bypassMode.equals("InstantDamage")) event.cancelEvent()

                    TransferUtils.noMotionSet = true
                    PacketUtils.sendPacketNoEvent(
                        C03PacketPlayer.C06PacketPlayerPosLook(
                            packet.x,
                            packet.y,
                            packet.z,
                            packet.yaw,
                            packet.pitch,
                            false
                        )
                    )
                }

                if (packet is C0FPacketConfirmTransaction) { //Make sure it works with Vulcan Velocity
                    val transUID = (packet.uid).toInt()
                    if (transUID >= -31767 && transUID <= -30769) {
                        event.cancelEvent()
                        PacketUtils.sendPacketNoEvent(packet)
                    }
                }
            }
            "Fast" -> {
                when (val packet = event.packet) {
                    is C03PacketPlayer -> {
                        if(doCancel) {
                            event.cancelEvent()
                            doCancel = false
                        }
                        packet.onGround = true
                    }
                    is S08PacketPlayerPosLook -> {
                        if (stage == FlyStage.WAIT_APPLY) {
                            if(sqrt((packet.x-mc.thePlayer.posX)*(packet.x-mc.thePlayer.posX)
                                        +(packet.y-mc.thePlayer.posY)*(packet.y-mc.thePlayer.posY)
                                        +(packet.z-mc.thePlayer.posZ)*(packet.z-mc.thePlayer.posZ)) < 1.4) {
                                isSuccess = true
                                fly.state = false
                                return
                            }
                        }
                        event.cancelEvent()
                    }
                    is C0BPacketEntityAction -> {
                        event.cancelEvent()
                    }
                }
            }
            "Ghost" -> {
                when (val packet = event.packet) {
                    is C03PacketPlayer -> {
                        if(ticks > 2) {
                            ticks = 0
                            packet.y += 0.5
                        }
                        packet.onGround = true
                    }
                    is S08PacketPlayerPosLook -> {
                        if (stage == FlyStage.WAITING) {
                            flags++
                            if (flags >= 2) {
                                flags = 0
                                stage = FlyStage.FLYING
                            }
                        }
                        if (stage == FlyStage.WAIT_APPLY) {
                            if(sqrt((packet.x - groundX) * (packet.x - groundX)
                                        + (packet.z - groundZ) * (packet.z - groundZ)) < 1.4 && packet.y >= (groundY - 0.5)) {
                                fly.state = false
                                return
                            }
                        }
                        event.cancelEvent()
                    }
                    is C0BPacketEntityAction -> {
                        event.cancelEvent()
                    }
                }
            }
        }
    }



    private fun clip(dist: Float, y: Float) {
        val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
        val x = -sin(yaw) * dist
        val z = cos(yaw) * dist
        mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z)
        mc.netHandler.addToSendQueue(
            C03PacketPlayer.C04PacketPlayerPosition(
                mc.thePlayer.posX,
                mc.thePlayer.posY,
                mc.thePlayer.posZ,
                false
            )
        )
    }

    fun runSelfDamageCore(): Boolean {
        mc.timer.timerSpeed = 1.0f
        if (bypassMode.equals("Damage") || bypassMode.equals("Flag")) {
            if (!bypassMode.equals("Flag")) {
                if (mc.thePlayer.hurtTime > 0 || isDamaged) {
                    isDamaged = true
                    dmgJumpCount = 999
                    return false
                }else {
                    return true
                }
            }
            isDamaged = true
            dmgJumpCount = 999
            return false
        }
        if (isDamaged) {
            dmgJumpCount = 999
            return false
        }
        mc.thePlayer.jumpMovementFactor = 0.00f
        if (mc.thePlayer.onGround) {
            if (dmgJumpCount >= 4) {
                mc.netHandler.addToSendQueue(
                    C03PacketPlayer.C04PacketPlayerPosition(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY,
                        mc.thePlayer.posZ,
                        true
                    )
                )
                isDamaged = true
                dmgJumpCount = 999
                return false
            }
            dmgJumpCount++
            MovementUtils.resetMotion(true)
            mc.thePlayer.jump()
        }
        MovementUtils.resetMotion(false)
        return true
    }

    enum class FlyStage {
        WAITING,
        FLYING,
        WAIT_APPLY
    }

}