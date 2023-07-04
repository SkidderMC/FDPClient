package net.ccbluex.liquidbounce.features.module.modules.movement.flys.vulcan

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.minecraft.client.settings.GameSettings
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

class VulcanGhostFly : FlyMode("VulcanGhost") {

    private val timerValue = FloatValue("${valuePrefix}Timer", 2f, 1f, 3f)

    private var ticks = 0
    private var modifyTicks = 0
    private var stage = FlyStage.WAITING
    private var flags = 0
    private var groundX = 0.0
    private var groundY = 0.0
    private var groundZ = 0.0

    override fun onEnable() {
        ticks = 0
        modifyTicks = 0
        flags = 0
        mc.thePlayer.setPosition(mc.thePlayer.posX, (mc.thePlayer.posY * 2).roundToInt().toDouble() / 2, mc.thePlayer.posZ)
        stage = FlyStage.WAITING
        ClientUtils.displayChatMessage("§8[§c§lVulcan-Ghost-Flight§8] §aPlease press Sneak before you land on ground!")
        ClientUtils.displayChatMessage("§8[§c§lVulcan-Ghost-Flight§8] §aYou can go Up/Down by pressing Jump/Sneak")
    }
    
    override fun onDisable() {
        mc.timer.timerSpeed = 1.0f
    }

    override fun onUpdate(event: UpdateEvent) {
        ticks++
        modifyTicks++
        mc.gameSettings.keyBindJump.pressed = false
        mc.gameSettings.keyBindSneak.pressed = false
        when(stage) {
            FlyStage.FLYING,FlyStage.WAITING -> {
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

    override fun onPacket(event: PacketEvent) {

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

    enum class FlyStage {
        WAITING,
        FLYING,
        WAIT_APPLY
    }
}
