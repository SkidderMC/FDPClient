package net.ccbluex.liquidbounce.features.module.modules.movement.flys.vulcan

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.BlockPos
import kotlin.math.sqrt

class VulcanFastFly : FlyMode("VulcanFast") {

    private val speedValue = FloatValue("${valuePrefix}Speed", 1f, 0.1f, 6f)
    private val verticalValue = BoolValue("${valuePrefix}Vertical", false)

    private var isSuccess = false
    private var vticks = 0
    private var doCancel = false
    private var stage = FlyStage.FLYING
    private var startX = 0.0
    private var startZ = 0.0
    private var startY = 0.0

    override fun onEnable() {
        vticks = 0
        doCancel = false
        if(verticalValue.get()) {
            ClientUtils.displayChatMessage("§8[§c§lVulcan-Fast-Fly§8] §cVertical Flying sometimes flag!")
        }
        if(mc.thePlayer.posY % 1 != 0.0) {
            fly.state = false
            ClientUtils.displayChatMessage("§8[§c§lVulcan-Fast-Fly§8] §cPlease stand on a solid block to fly!")
            isSuccess = true
            return
        }
        stage = FlyStage.FLYING
        isSuccess = false
        ClientUtils.displayChatMessage("§8[§c§lVulcan-Fast-Fly§8] §aPlease press Sneak before you land on ground!")
        ClientUtils.displayChatMessage("§8[§c§lVulcan-Fast-Fly§8] §7Tips: DO NOT Use killaura when you're flying!")
        startX = mc.thePlayer.posX
        startY = mc.thePlayer.posY
        startZ = mc.thePlayer.posZ
    }
    
    override fun onDisable() {
        mc.timer.timerSpeed = 1.0f
        if (!isSuccess) {
            mc.thePlayer.setPosition(startX, startY, startZ)
            ClientUtils.displayChatMessage("§8[§c§lVulcan-Fast-Fly§8] §cFly attempt Failed...")
            ClientUtils.displayChatMessage("§8[§c§lVulcan-Fast-Fly§8] §cIf it keeps happen, Don't use it again in CURRENT gameplay")
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        when(stage) {
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
                        ClientUtils.displayChatMessage("§8[§c§lVulcan-Fly§8] §aWaiting for landing...")
                    } else {
                        ClientUtils.displayChatMessage("§8[§c§lVulcan-Fly§8] §cYou can only land on a solid block!")
                    }
                }
            }
            FlyStage.WAIT_APPLY -> {
                vticks++
                doCancel = false
                if(vticks == 60) {
                    ClientUtils.displayChatMessage("§8[§c§lVulcan-Fly§8] §cSeems took a long time! Please turn off the Fly manually")
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

    override fun onPacket(event: PacketEvent) {

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

    enum class FlyStage {
        FLYING,
        WAIT_APPLY
    }
}
