package net.ccbluex.liquidbounce.features.module.modules.movement.flys.verus

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.stats.StatList
import net.minecraft.util.BlockPos

class Vulcan2Fly : FlyMode("Vulcan2") {

    private val timerValue = FloatValue("${valuePrefix}Speed", 1f, 0.5f, 3f)

    private var stage = FlyStage.WAIT_FLAG
    private var lastX = 0.0
    private var lastZ = 0.0
    private var isSuccess = false
    private var vticks = 0
    private var flagTimes = 0
    private var doCancel = false

    override fun onEnable() {
        vticks = 0
        flagTimes = 0
        doCancel = false
        if(mc.thePlayer.posY % 1 != 0.0) {
            fly.state = false
            ClientUtils.displayChatMessage("§8[§c§lVulcan-Fly§8] §cPlease stand on a solid block to fly!")
            isSuccess = true
        }
        stage = FlyStage.WAIT_FLAG
        isSuccess = false
        ClientUtils.displayAlert("§8[§c§lVulcan-Fly§8] §aPlease press Sneak before you land on ground!")
    }
    
    override fun onDisable() {
        mc.timer.timerSpeed = 1.0f
        if (!isSuccess) {
            ClientUtils.displayAlert("§8[§c§lVulcan-Fly§8] §cFly attempt Failed...")
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        
        mc.thePlayer.jumpMovementFactor = 0.0265f
        when(stage) {
            FlyStage.WAIT_FLAG -> {
                stage = FlyStage.FLYING
                doCancel = true
            }
            FlyStage.FLYING -> {
                isSuccess = false
                
                mc.thePlayer.motionX = 0.0
                mc.thePlayer.motionY = 0.0
                mc.thePlayer.motionZ = 0.0
                
                MovementUtils.strafe(timerValue.get())
                doCancel = true
                if(mc.gameSettings.keyBindJump.pressed) {
                    mc.thePlayer.motionY = 0.5
                }

                if(mc.gameSettings.keyBindSneak.pressed && mc.thePlayer.ticksExisted % 3 == 0) {
                    mc.thePlayer.motionY = -0.5
                    val fixedY = mc.thePlayer.posY - (mc.thePlayer.posY % 1)
                    val underBlock2 = BlockUtils.getBlock(BlockPos(mc.thePlayer.posX, fixedY - 1, mc.thePlayer.posZ)) ?: return
                    if(underBlock2.isFullBlock) {
                        stage = FlyStage.WAIT_UPDATE
                        doCancel = false
                        mc.thePlayer.motionX = 0.0
                        mc.thePlayer.motionY = 0.0
                        mc.thePlayer.motionZ = 0.0
                        mc.thePlayer.onGround = false
                        mc.thePlayer.jumpMovementFactor = 0.00f
                    } else {
                        ClientUtils.displayAlert("§8[§c§lVulcan-Fly§8] §cYou can only land on a solid block!")
                    }
                }
            }
            FlyStage.WAIT_UPDATE -> {
                mc.thePlayer.motionY = 0.0
                mc.thePlayer.onGround = false
                doCancel = false
                jitterY(0.5, 3)
                mc.thePlayer.jumpMovementFactor = 0.02f
                if(flagTimes>1 && mc.thePlayer.ticksExisted % 3 == 0) {
                    val fixedY = mc.thePlayer.posY - (mc.thePlayer.posY % 1)
                    mc.thePlayer.setPosition(mc.thePlayer.posX, fixedY, mc.thePlayer.posZ)
                    stage = FlyStage.WAIT_APPLY
                }
            }
            FlyStage.WAIT_APPLY -> {
                vticks++
                if(vticks == 80) {
                    ClientUtils.displayAlert("§8[§c§lVulcan-Fly§8] §cSeems took a long time! Please turn off the Fly manually")
                }
                mc.thePlayer.motionX = 0.0
                mc.thePlayer.motionY = 0.0
                mc.thePlayer.motionZ = 0.0
                mc.thePlayer.onGround = false
                mc.thePlayer.jumpMovementFactor = 0.00f
                val fixedY = mc.thePlayer.posY - (mc.thePlayer.posY % 1)
                mc.thePlayer.setPosition(mc.thePlayer.posX, fixedY , mc.thePlayer.posZ)
                if(mc.thePlayer.ticksExisted % 10 == 0) {
                    mc.timer.timerSpeed = 0.3f
                    mc.thePlayer.setPosition(mc.thePlayer.posX, fixedY+1 , mc.thePlayer.posZ)
                    mc.thePlayer.isAirBorne = true
                    mc.thePlayer.triggerAchievement(StatList.jumpStat)
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, fixedY, mc.thePlayer.posZ, true))
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, fixedY + 0.41999998688698, mc.thePlayer.posZ, true))
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, fixedY + 0.7531999805212, mc.thePlayer.posZ, true))
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, fixedY + 1, mc.thePlayer.posZ, true))
                }else{
                    mc.timer.timerSpeed = 1.0f
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, fixedY + 1, mc.thePlayer.posZ, true))
                }
                doCancel = true
            }
        }
    }

    private fun jitterY(offset: Double, tick: Int) {
        if(mc.thePlayer.ticksExisted % tick == 0) {
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - (mc.thePlayer.posY % 1) + offset, mc.thePlayer.posZ)
        } else {
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - (mc.thePlayer.posY % 1) , mc.thePlayer.posZ)
        }
        mc.thePlayer.motionY = 0.0
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if(packet is C03PacketPlayer) {
            if(doCancel) {
                event.cancelEvent()
                doCancel = false
            }
            packet.onGround = true
        } else if(packet is S08PacketPlayerPosLook) {
            if(stage == FlyStage.WAIT_FLAG) {
                mc.thePlayer.setPosition(mc.thePlayer.posX, fly.launchY, mc.thePlayer.posZ)
                stage = FlyStage.FLYING
            } else if (stage == FlyStage.WAIT_APPLY) {
                if(packet.x != lastX && packet.z != lastZ) {
                    isSuccess = true
                    fly.state = false
                    return
                }
            } else {
                lastX = packet.x
                lastZ = packet.z
                if (stage == FlyStage.WAIT_UPDATE) {
                    flagTimes++
                }
            }
            event.cancelEvent()
        }
    }

    enum class FlyStage {
        WAIT_FLAG,
        FLYING,
        WAIT_UPDATE,
        WAIT_APPLY
    }
}
