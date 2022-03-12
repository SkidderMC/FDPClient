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

    private val timerValue = FloatValue("${valuePrefix}Timer", 1f, 0.1f, 3f)

    private var stage = FlyStage.WAIT_FLAG
    private var lastX = 0.0
    private var lastZ = 0.0

    override fun onEnable() {
        if(mc.thePlayer.posY % 1 != 0.0) {
            fly.state = false
            ClientUtils.displayChatMessage("§8[§c§lVulcan-Fly§8] §cPlease stand on a solid block to fly!")
        }
        stage = FlyStage.WAIT_FLAG
    }

    override fun onUpdate(event: UpdateEvent) {
        when(stage) {
            FlyStage.WAIT_FLAG -> {
                jitterY(0.5, 3)
            }
            FlyStage.FLYING -> {
                jitterY(0.5, 3)
                MovementUtils.strafe()
                mc.timer.timerSpeed = timerValue.get()

                if(mc.gameSettings.keyBindSneak.pressed && mc.thePlayer.ticksExisted % 3 == 0) {
                    val fixedY = mc.thePlayer.posY - (mc.thePlayer.posY % 1)
                    val underBlock = BlockUtils.getBlock(BlockPos(mc.thePlayer.posX, fixedY - 1, mc.thePlayer.posZ)) ?: return
                    if(underBlock.isFullBlock) {
                        stage = FlyStage.WAIT_APPLY
                        mc.thePlayer.isAirBorne = true
                        mc.thePlayer.triggerAchievement(StatList.jumpStat)
//                        arrayOf(0.41999998688698, 0.7531999805212, 1.00133597911214, 1.16610926093821, 1.24918707874468, 1.24918707874468, 1.1707870772188).forEach {
//                            mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, fixedY + it, mc.thePlayer.posZ, true))
//                        }
                        mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, fixedY, mc.thePlayer.posZ, true))
                        mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, fixedY + 0.41999998688698, mc.thePlayer.posZ, true))
                        mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, fixedY + 0.7531999805212, mc.thePlayer.posZ, true))
                    } else {
                        ClientUtils.displayAlert("§8[§c§lVulcan-Fly§8] §cYou can only land on a solid block!")
                    }
                }
            }
            FlyStage.WAIT_APPLY -> {
                mc.timer.timerSpeed = 1f
                mc.thePlayer.motionX = 0.0
                mc.thePlayer.motionY = 0.0
                mc.thePlayer.motionZ = 0.0
                val fixedY = mc.thePlayer.posY - (mc.thePlayer.posY % 1)
                mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, fixedY + 1, mc.thePlayer.posZ, true))
            }
        }
    }

    private fun jitterY(offset: Double, tick: Int) {
        if(mc.thePlayer.ticksExisted % tick == 0) {
            mc.thePlayer.setPosition(mc.thePlayer.posX, fly.launchY + offset, mc.thePlayer.posZ)
        } else {
            mc.thePlayer.setPosition(mc.thePlayer.posX, fly.launchY, mc.thePlayer.posZ)
        }
        mc.thePlayer.motionY = 0.0
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if(packet is C03PacketPlayer) {
            packet.onGround = true
        } else if(packet is S08PacketPlayerPosLook) {
            if(stage == FlyStage.WAIT_FLAG) {
                mc.thePlayer.setPosition(mc.thePlayer.posX, fly.launchY, mc.thePlayer.posZ)
                stage = FlyStage.FLYING
            } else if (stage == FlyStage.WAIT_APPLY) {
                if(packet.x != lastX && packet.z != lastZ) {
                    fly.state = false
                    return
                }
            } else {
                lastX = packet.x
                lastZ = packet.z
            }
            event.cancelEvent()
        }
    }

    enum class FlyStage {
        WAIT_FLAG,
        FLYING,
        WAIT_APPLY
    }
}